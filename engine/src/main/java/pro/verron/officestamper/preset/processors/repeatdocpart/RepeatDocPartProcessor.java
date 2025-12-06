package pro.verron.officestamper.preset.processors.repeatdocpart;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.ContentAccessor;
import org.docx4j.wml.P;
import org.docx4j.wml.R;
import org.docx4j.wml.SectPr;
import org.jvnet.jaxb2_commons.ppp.Child;
import org.springframework.lang.Nullable;
import pro.verron.officestamper.api.CommentProcessor;
import pro.verron.officestamper.api.OfficeStamper;
import pro.verron.officestamper.api.OfficeStamperException;
import pro.verron.officestamper.api.ProcessorContext;
import pro.verron.officestamper.core.CommentUtil;
import pro.verron.officestamper.core.DocumentUtil;
import pro.verron.officestamper.core.SectionUtil;
import pro.verron.officestamper.preset.CommentProcessorFactory;
import pro.verron.officestamper.utils.WmlFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import static java.util.stream.Collectors.toMap;
import static pro.verron.officestamper.core.DocumentUtil.walkObjectsAndImportImages;
import static pro.verron.officestamper.core.SectionUtil.getPreviousSectionBreakIfPresent;

/// This class processes the &lt;ds: repeat&gt; tag. It uses the [OfficeStamper] to stamp the sub document and then
/// copies the resulting sub document to the correct position in the main document.
///
/// @author Joseph Verron
/// @author Youssouf Naciri
/// @version ${version}
/// @since 1.3.0
public class RepeatDocPartProcessor
        extends CommentProcessor
        implements CommentProcessorFactory.IRepeatDocPartProcessor {
    private static final ThreadFactory threadFactory = Executors.defaultThreadFactory();

    private final OfficeStamper<WordprocessingMLPackage> stamper;

    private RepeatDocPartProcessor(ProcessorContext processorContext, OfficeStamper<WordprocessingMLPackage> stamper) {
        super(processorContext);
        this.stamper = stamper;
    }

    /// newInstance.
    ///
    /// @param stamper the stamper
    ///
    /// @return a new instance of this processor
    public static CommentProcessor newInstance(
            ProcessorContext processorContext,
            OfficeStamper<WordprocessingMLPackage> stamper
    ) {
        return new RepeatDocPartProcessor(processorContext, stamper);
    }

    /// {@inheritDoc}
    @Override
    public void repeatDocPart(@Nullable Iterable<Object> expressionContexts) {
        if (expressionContexts == null) return;
        var comment = comment();
        var elements = comment.getElements();
        if (elements.isEmpty()) return;
        var parent = comment.getParent();
        var siblings = parent.getContent();
        var context = context();
        var part = context.part();
        var document = part.document();
        var firstElement = elements.getFirst();
        var subTemplate = CommentUtil.createSubWordDocument(comment, document);
        var oddNumberOfBreaks = SectionUtil.hasOddNumberOfSectionBreaks(elements);
        var optionalPreviousSectionBreak = getPreviousSectionBreakIfPresent(firstElement, parent);
        Function<SectPr, UnaryOperator<List<Object>>> function =
                sectionBreak -> (UnaryOperator<List<Object>>) objs -> insertSectionBreak(
                        objs,
                        sectionBreak,
                        oddNumberOfBreaks);
        var sectionBreakInserter = optionalPreviousSectionBreak.map(function)
                                                               .orElse(t -> t);
        var changes = stampSubDocuments(document, expressionContexts, parent, subTemplate, sectionBreakInserter);
        var index = siblings.indexOf(firstElement);
        siblings.addAll(index, changes);
        siblings.removeAll(elements);
    }

    private static List<Object> insertSectionBreak(
            List<Object> elements,
            SectPr previousSectionBreak,
            boolean oddNumberOfBreaks
    ) {
        var inserts = new ArrayList<>(elements);
        if (oddNumberOfBreaks) {
            if (inserts.getLast() instanceof P p) {
                SectionUtil.applySectionBreakToParagraph(previousSectionBreak, p);
            }
            else {
                // when the last repeated element is not a paragraph,
                // it is necessary to add one carrying the section break.
                P p = WmlFactory.newParagraph(List.of());
                SectionUtil.applySectionBreakToParagraph(previousSectionBreak, p);
                inserts.add(p);
            }
        }
        return inserts;
    }

    private List<Object> stampSubDocuments(
            WordprocessingMLPackage document,
            Iterable<Object> expressionContexts,
            ContentAccessor gcp,
            WordprocessingMLPackage subTemplate,
            UnaryOperator<List<Object>> sectionBreakInserter
    ) {
        var subDocuments = stampSubDocuments(expressionContexts, subTemplate);
        var replacements = subDocuments.stream()
                                       //TODO: move side effect somewhere else
                                       .map(p -> walkObjectsAndImportImages(p, document))
                                       .map(Map::entrySet)
                                       .flatMap(Set::stream)
                                       .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));

        var changes = new ArrayList<>();
        for (WordprocessingMLPackage subDocument : subDocuments) {
            var os = sectionBreakInserter.apply(DocumentUtil.allElements(subDocument));
            os.stream()
              .filter(ContentAccessor.class::isInstance)
              .map(ContentAccessor.class::cast)
              .forEach(o -> recursivelyReplaceImages(o, replacements));
            os.forEach(c -> setParentIfPossible(c, gcp));
            changes.addAll(os);
        }
        return changes;
    }

    private List<WordprocessingMLPackage> stampSubDocuments(
            Iterable<Object> subContexts,
            WordprocessingMLPackage subTemplate
    ) {
        var subDocuments = new ArrayList<WordprocessingMLPackage>();
        for (Object subContext : subContexts) {
            var templateCopy = outputWord(os -> copy(subTemplate, os));
            var subDocument = outputWord(os -> stamp(subContext, templateCopy, os));
            subDocuments.add(subDocument);
        }
        return subDocuments;
    }

    private static void recursivelyReplaceImages(ContentAccessor r, Map<R, R> replacements) {
        Queue<ContentAccessor> q = new ArrayDeque<>();
        q.add(r);
        while (!q.isEmpty()) {
            ContentAccessor run = q.remove();
            if (replacements.containsKey(run) && run instanceof Child child
                && child.getParent() instanceof ContentAccessor parent) {
                List<Object> parentContent = parent.getContent();
                parentContent.add(parentContent.indexOf(run), replacements.get(run));
                parentContent.remove(run);
            }
            else {
                q.addAll(run.getContent()
                            .stream()
                            .filter(ContentAccessor.class::isInstance)
                            .map(ContentAccessor.class::cast)
                            .toList());
            }
        }
    }

    private static void setParentIfPossible(Object object, ContentAccessor parent) {
        if (object instanceof Child child) child.setParent(parent);
    }

    private WordprocessingMLPackage outputWord(Consumer<OutputStream> outputter) {
        var exceptionHandler = new ProcessorExceptionHandler();
        try (var os = new PipedOutputStream(); var is = new PipedInputStream(os)) {
            // closing on exception to not block the pipe infinitely
            // TODO: model both PipedxxxStream as 1 class for only 1 close()
            exceptionHandler.onException(is::close); // I know it's redundant,
            exceptionHandler.onException(os::close); // but symmetry

            var thread = threadFactory.newThread(() -> outputter.accept(os));
            thread.setUncaughtExceptionHandler(exceptionHandler);
            thread.start();
            var wordprocessingMLPackage = WordprocessingMLPackage.load(is);
            thread.join();
            return wordprocessingMLPackage;
        } catch (Docx4JException | IOException e) {
            OfficeStamperException exception = new OfficeStamperException(e);
            exceptionHandler.exception()
                            .ifPresent(exception::addSuppressed);
            throw exception;
        } catch (InterruptedException e) {
            OfficeStamperException exception = new OfficeStamperException(e);
            exceptionHandler.exception()
                            .ifPresent(e::addSuppressed);
            Thread.currentThread()
                  .interrupt();
            throw exception;
        }
    }

    private void copy(WordprocessingMLPackage aPackage, OutputStream outputStream) {
        try {
            aPackage.save(outputStream);
        } catch (Docx4JException e) {
            throw new OfficeStamperException(e);
        }
    }

    private void stamp(Object context, WordprocessingMLPackage template, OutputStream outputStream) {
        stamper.stamp(template, context, outputStream);
    }

    /// A functional interface representing runnable task able to throw an exception. It extends the [Runnable]
    /// interface and provides default implementation of the [Runnable#run()] method handling the exception by
    /// rethrowing it wrapped inside a [OfficeStamperException].
    ///
    /// @author Joseph Verron
    /// @version ${version}
    /// @since 1.6.6
    interface ThrowingRunnable
            extends Runnable {

        /// Executes the runnable task, handling any exception by throwing it wrapped inside a
        /// [OfficeStamperException].
        default void run() {
            try {
                throwingRun();
            } catch (Exception e) {
                throw new OfficeStamperException(e);
            }
        }

        /// Executes the runnable task
        ///
        /// @throws Exception if an exception occurs executing the task
        void throwingRun()
                throws Exception;
    }

    /// This class is responsible for capturing and handling uncaught exceptions that occur in a thread. It implements
    /// the [Thread.UncaughtExceptionHandler] interface and can be assigned to a thread using the
    /// [Thread#setUncaughtExceptionHandler(Thread.UncaughtExceptionHandler)] method. When an exception occurs in the
    /// thread, the [ProcessorExceptionHandler#uncaughtException(Thread, Throwable)] method will be called. This class
    /// provides the following features:
    /// 1. Capturing and storing the uncaught exception.
    /// 2. Executing a list of routines when an exception occurs.
    /// 3. Providing access to the captured exception, if any. Example usage:
    /// <code>
    /// ProcessorExceptionHandler exceptionHandler = new ProcessorExceptionHandler(){};
    /// thread.setUncaughtExceptionHandler(exceptionHandler);
    /// </code>
    ///
    /// @author Joseph Verron
    /// @version ${version}
    /// @see Thread.UncaughtExceptionHandler
    /// @since 1.6.6
    static class ProcessorExceptionHandler
            implements Thread.UncaughtExceptionHandler {
        private final AtomicReference<Throwable> exception;
        private final List<Runnable> onException;

        /// Constructs a new instance for managing thread's uncaught exceptions. Once set to a thread, it retains the
        /// exception information and performs specified routines.
        public ProcessorExceptionHandler() {
            this.exception = new AtomicReference<>();
            this.onException = new CopyOnWriteArrayList<>();
        }

        /// {@inheritDoc}
        ///
        /// Captures and stores an uncaught exception from a thread run and executes all defined routines on occurrence
        /// of the exception.
        @Override
        public void uncaughtException(Thread t, Throwable e) {
            exception.set(e);
            onException.forEach(Runnable::run);
        }

        /// Adds a routine to the list of routines that should be run when an exception occurs.
        ///
        /// @param runnable The runnable routine to be added
        public void onException(ThrowingRunnable runnable) {
            onException.add(runnable);
        }

        /// Returns the captured exception if present.
        ///
        /// @return an [Optional] containing the captured exception, or an [Optional#empty()] if no exception was
        ///         captured
        public Optional<Throwable> exception() {
            return Optional.ofNullable(exception.get());
        }
    }
}
