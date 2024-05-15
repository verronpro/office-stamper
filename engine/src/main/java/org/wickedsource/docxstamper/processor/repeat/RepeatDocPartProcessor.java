package org.wickedsource.docxstamper.processor.repeat;

import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.*;
import org.jvnet.jaxb2_commons.ppp.Child;
import org.wickedsource.docxstamper.processor.BaseCommentProcessor;
import org.wickedsource.docxstamper.util.DocumentUtil;
import org.wickedsource.docxstamper.util.SectionUtil;
import pro.verron.officestamper.api.*;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toMap;
import static org.wickedsource.docxstamper.util.DocumentUtil.walkObjectsAndImportImages;

/**
 * This class is responsible for processing the &lt;ds: repeat&gt; tag.
 * It uses the {@link OfficeStamper} to stamp the sub document and then
 * copies the resulting sub document to the correct position in the
 * main document.
 *
 * @author Joseph Verron
 * @author Youssouf Naciri
 * @version ${version}
 * @since 1.3.0
 */
public class RepeatDocPartProcessor
        extends BaseCommentProcessor
        implements IRepeatDocPartProcessor {
    private static final ThreadFactory threadFactory = Executors.defaultThreadFactory();
    private static final ObjectFactory objectFactory = Context.getWmlObjectFactory();

    private final OfficeStamper<WordprocessingMLPackage> stamper;
    private final Map<Comment, List<Object>> contexts = new HashMap<>();
    private final Supplier<? extends List<?>> nullSupplier;

    private RepeatDocPartProcessor(
            ParagraphPlaceholderReplacer placeholderReplacer,
            OfficeStamper<WordprocessingMLPackage> stamper,
            Supplier<? extends List<?>> nullSupplier
    ) {
        super(placeholderReplacer);
        this.stamper = stamper;
        this.nullSupplier = nullSupplier;
    }

    /**
     * <p>newInstance.</p>
     *
     * @param pr      the placeholderReplacer
     * @param stamper the stamper
     *
     * @return a new instance of this processor
     */
    public static CommentProcessor newInstance(
            ParagraphPlaceholderReplacer pr,
            OfficeStamper<WordprocessingMLPackage> stamper
    ) {
        return new RepeatDocPartProcessor(pr, stamper, Collections::emptyList);
    }

    private static List<Object> documentAsInsertableElements(
            WordprocessingMLPackage subDocument,
            boolean oddNumberOfBreaks,
            SectPr previousSectionBreak
    ) {
        List<Object> inserts = new ArrayList<>(
                DocumentUtil.allElements(subDocument));
        // make sure we replicate the previous section break before each repeated doc part
        if (oddNumberOfBreaks && previousSectionBreak != null) {
            if (DocumentUtil.lastElement(subDocument) instanceof P p) {
                SectionUtil.applySectionBreakToParagraph(previousSectionBreak,
                        p);
            }
            else {
                // when the last element to be repeated is not a paragraph, we need to add a new
                // one right after to carry the section break to have a valid xml
                P p = objectFactory.createP();
                SectionUtil.applySectionBreakToParagraph(previousSectionBreak,
                        p);
                inserts.add(p);
            }
        }
        return inserts;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void repeatDocPart(List<Object> contexts) {
        if (contexts == null)
            contexts = Collections.emptyList();

        Comment currentComment = getCurrentCommentWrapper();
        List<Object> elements = currentComment.getElements();

        if (!elements.isEmpty()) {
            this.contexts.put(currentComment, contexts);
        }
    }

    private List<Object> stampSubDocuments(
            WordprocessingMLPackage document,
            List<Object> expressionContexts,
            ContentAccessor gcp,
            WordprocessingMLPackage subTemplate,
            SectPr previousSectionBreak,
            boolean oddNumberOfBreaks
    ) {
        var subDocuments = stampSubDocuments(expressionContexts, subTemplate);
        var replacements = subDocuments
                .stream()
                .map(p -> walkObjectsAndImportImages(p, document)) // TODO_LATER: move the side effect somewhere else
                .map(Map::entrySet)
                .flatMap(Set::stream)
                .collect(toMap(Entry::getKey, Entry::getValue));

        var changes = new ArrayList<>();
        for (WordprocessingMLPackage subDocument : subDocuments) {
            var os = documentAsInsertableElements(subDocument,
                    oddNumberOfBreaks,
                    previousSectionBreak);
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
            List<Object> subContexts,
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

    /**
     * {@inheritDoc}
     */
    @Override
    public void commitChanges(WordprocessingMLPackage document) {
        for (Entry<Comment, List<Object>> entry : this.contexts.entrySet()) {
            var comment = entry.getKey();
            var expressionContexts = entry.getValue();
            var gcp = requireNonNull(comment.getParent());
            var repeatElements = comment.getElements();
            var subTemplate = comment.tryBuildingSubtemplate(document);
            SectPr previousSectionBreak = SectionUtil.getPreviousSectionBreakIfPresent(
                    repeatElements.get(0), gcp);
            boolean oddNumberOfBreaks = SectionUtil.isOddNumberOfSectionBreaks(
                    repeatElements);

            List<?> changes = expressionContexts == null
                    ? nullSupplier.get()
                    : stampSubDocuments(document,
                            expressionContexts,
                            gcp,
                            subTemplate,
                            previousSectionBreak,
                            oddNumberOfBreaks);

            List<Object> gcpContent = gcp.getContent();
            int index = gcpContent.indexOf(repeatElements.get(0));
            gcpContent.addAll(index, changes);
            gcpContent.removeAll(repeatElements);
        }

    }

    private static void recursivelyReplaceImages(
            ContentAccessor r,
            Map<R, R> replacements
    ) {
        Queue<ContentAccessor> q = new ArrayDeque<>();
        q.add(r);
        while (!q.isEmpty()) {
            ContentAccessor run = q.remove();
            if (replacements.containsKey(run)
                    && run instanceof Child child
                    && child.getParent() instanceof ContentAccessor parent) {
                List<Object> parentContent = parent.getContent();
                parentContent.add(parentContent.indexOf(run),
                        replacements.get(run));
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

    private static void setParentIfPossible(
            Object object,
            ContentAccessor parent
    ) {
        if (object instanceof Child child)
            child.setParent(parent);
    }

    private WordprocessingMLPackage outputWord(Consumer<OutputStream> outputter) {
        var exceptionHandler = new ProcessorExceptionHandler();
        try (
                PipedOutputStream os = new PipedOutputStream();
                PipedInputStream is = new PipedInputStream(os)
        ) {
            // closing on exception to not block the pipe infinitely
            // TODO_LATER: model both PipedxxxStream as 1 class for only 1 close()
            exceptionHandler.onException(is::close); // I know it's redundant,
            exceptionHandler.onException(os::close); // but symmetry

            var thread = threadFactory.newThread(() -> outputter.accept(os));
            thread.setUncaughtExceptionHandler(exceptionHandler);
            thread.start();
            var wordprocessingMLPackage = WordprocessingMLPackage.load(is);
            thread.join();
            return wordprocessingMLPackage;
        } catch (Docx4JException | IOException | InterruptedException e) {
            OfficeStamperException exception = new OfficeStamperException(e);
            exceptionHandler.exception()
                            .ifPresent(exception::addSuppressed);
            throw exception;
        }
    }

    private void copy(
            WordprocessingMLPackage aPackage,
            OutputStream outputStream
    ) {
        try {
            aPackage.save(outputStream);
        } catch (Docx4JException e) {
            throw new OfficeStamperException(e);
        }
    }

    private void stamp(
            Object context,
            WordprocessingMLPackage template,
            OutputStream outputStream
    ) {
        stamper.stamp(template, context, outputStream);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void reset() {
        contexts.clear();
    }

    /**
     * A functional interface representing runnable task able to throw an exception.
     * It extends the {@link Runnable} interface and provides default implementation
     * of the {@link Runnable#run()} method handling the exception by rethrowing it
     * wrapped inside a {@link OfficeStamperException}.
     *
     * @author Joseph Verron
     * @version ${version}
     * @since 1.6.6
     */
    interface ThrowingRunnable
            extends Runnable {

        /**
         * Executes the runnable task, handling any exception by throwing it wrapped
         * inside a {@link OfficeStamperException}.
         */
        default void run() {
            try {
                throwingRun();
            } catch (Exception e) {
                throw new OfficeStamperException(e);
            }
        }

        /**
         * Executes the runnable task
         *
         * @throws Exception if an exception occurs executing the task
         */
        void throwingRun()
                throws Exception;
    }

    /**
     * This class is responsible for capturing and handling uncaught exceptions
     * that occur in a thread.
     * It implements the {@link Thread.UncaughtExceptionHandler} interface and can
     * be assigned to a thread using the
     * {@link Thread#setUncaughtExceptionHandler(Thread.UncaughtExceptionHandler)} method.
     * When an exception occurs in the thread,
     * the {@link ProcessorExceptionHandler#uncaughtException(Thread, Throwable)}
     * method will be called.
     * This class provides the following features:
     * 1. Capturing and storing the uncaught exception.
     * 2. Executing a list of routines when an exception occurs.
     * 3. Providing access to the captured exception, if any.
     * Example usage:
     * <code>
     * ProcessorExceptionHandler exceptionHandler = new
     * ProcessorExceptionHandler(){};
     * thread.setUncaughtExceptionHandler(exceptionHandler);
     * </code>
     *
     * @author Joseph Verron
     * @version ${version}
     * @see Thread.UncaughtExceptionHandler
     * @since 1.6.6
     */
    static class ProcessorExceptionHandler
            implements Thread.UncaughtExceptionHandler {
        private final AtomicReference<Throwable> exception;
        private final List<Runnable> onException;

        /**
         * Constructs a new instance for managing thread's uncaught exceptions.
         * Once set to a thread, it retains the exception information and performs specified routines.
         */
        public ProcessorExceptionHandler() {
            this.exception = new AtomicReference<>();
            this.onException = new CopyOnWriteArrayList<>();
        }

        /**
         * {@inheritDoc}
         * <p>
         * Captures and stores an uncaught exception from a thread run
         * and executes all defined routines on occurrence of the exception.
         */
        @Override
        public void uncaughtException(Thread t, Throwable e) {
            exception.set(e);
            onException.forEach(Runnable::run);
        }

        /**
         * Adds a routine to the list of routines that should be run
         * when an exception occurs.
         *
         * @param runnable The runnable routine to be added
         */
        public void onException(ThrowingRunnable runnable) {
            onException.add(runnable);
        }

        /**
         * Returns the captured exception if present.
         *
         * @return an {@link Optional} containing the captured exception,
         * or an {@link Optional#empty()} if no exception was captured
         */
        public Optional<Throwable> exception() {
            return Optional.ofNullable(exception.get());
        }
    }
}
