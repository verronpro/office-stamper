package pro.verron.officestamper.core;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.*;
import org.jvnet.jaxb2_commons.ppp.Child;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.expression.spel.SpelEvaluationException;
import org.springframework.expression.spel.SpelParseException;
import pro.verron.officestamper.api.*;
import pro.verron.officestamper.utils.WmlFactory;
import pro.verron.officestamper.utils.WmlUtils;

import java.math.BigInteger;
import java.util.*;

import static pro.verron.officestamper.core.Placeholders.findProcessors;

/// Allows registration of [Processor] objects. Each registered
/// [Processor] must implement an interface, which has to be specified at
/// registration time. Provides several getter methods to access the registered
/// [Processor].
///
/// @author Joseph Verron
/// @author Tom Hombergs
/// @version ${version}
/// @since 1.0.0
public class ProcessorRegistry {

    private static final Logger logger = LoggerFactory.getLogger(ProcessorRegistry.class);
    private final DocxPart source;
    private final Processors processors;
    private final ExpressionResolver expressionResolver;
    private final ExceptionResolver exceptionResolver;
    private final ObjectResolverRegistry objectResolverRegistry;

    /// Constructs a new ProcessorRegistry.
    ///
    /// @param source             the source part of the Word document.
    /// @param expressionResolver the resolver for evaluating expressions.
    /// @param processors         map of comment processor instances keyed by their respective class types.
    /// @param exceptionResolver  the resolver for handling exceptions during processing.
    public ProcessorRegistry(
            DocxPart source,
            ExpressionResolver expressionResolver,
            Processors processors,
            ExceptionResolver exceptionResolver,
            ObjectResolverRegistry objectResolverRegistry
    ) {
        this.source = source;
        this.expressionResolver = expressionResolver;
        this.processors = processors;
        this.exceptionResolver = exceptionResolver;
        this.objectResolverRegistry = objectResolverRegistry;
    }

    public <T> void runProcessors(T expressionContext) {
        runProcessors(expressionContext, source);
    }

    private <T> void runProcessors(T expressionContext, DocxPart part) {
        var proceedComments = new ArrayList<Comment>();
        part.streamRun()
            .forEach(run -> {
                var comments = collectComments();
                var runParent = StandardParagraph.from(part, (P) run.getParent());
                var optional = runProcessorsOnRunComment(comments, expressionContext, run, runParent);
                optional.ifPresent(proceedComments::add);
            });
        processors.commitChanges(part);

        // we run the paragraph afterward so that the comments inside work before the whole paragraph comments
        part.streamParagraphs()
            .forEach(p -> {
                var comments = collectComments();
                var paragraphComment = p.getComment();
                paragraphComment.forEach((pc -> {
                    var optional = runProcessorsOnParagraphComment(comments, expressionContext, p, pc.getId());
                    processors.commitChanges(part);
                    optional.ifPresent(proceedComments::add);
                }));
            });

        part.streamParagraphs()
            .forEach(paragraph -> runProcessorsOnInlineContent(expressionContext, paragraph));

        var scanner = part.scanner();
        while (scanner.hasNext()) {
            scanner.next();
            scanner.process(processors, expressionResolver, expressionContext);
        }

        proceedComments.forEach(CommentUtil::deleteComment);
    }

    private Map<BigInteger, Comment> collectComments() {
        var rootComments = new HashMap<BigInteger, Comment>();
        var allComments = new HashMap<BigInteger, Comment>();
        var stack = Collections.asLifoQueue(new ArrayDeque<Comment>());

        var list = WmlUtils.extractCommentElements(document());
        for (Child commentElement : list) {
            if (commentElement instanceof CommentRangeStart crs) onRangeStart(crs, allComments, stack, rootComments);
            else if (commentElement instanceof CommentRangeEnd cre) onRangeEnd(cre, allComments, stack);
            else if (commentElement instanceof R.CommentReference cr) onReference(cr, allComments);
        }
        CommentUtil.getCommentsPart(document().getParts())
                   .map(CommentUtil::extractContent)
                   .map(Comments::getComment)
                   .stream()
                   .flatMap(Collection::stream)
                   .filter(comment -> allComments.containsKey(comment.getId()))
                   .forEach(comment -> allComments.get(comment.getId())
                                                  .setComment(comment));
        return new HashMap<>(rootComments);
    }

    private <T> Optional<Comment> runProcessorsOnRunComment(
            Map<BigInteger, Comment> comments,
            T expressionContext,
            R run,
            Paragraph paragraph
    ) {
        return CommentUtil.getCommentAround(run, document())
                          .flatMap(c -> Optional.ofNullable(comments.get(c.getId())))
                          .flatMap(c -> {
                              var cPlaceholder = c.asPlaceholder();
                              var cComment = c.getComment();
                              comments.remove(cComment.getId());
                              processors.setContext(new ProcessorContext(paragraph, run, c, cPlaceholder));
                              return runProcessors(expressionContext, cPlaceholder)
                                      ? Optional.of(c)
                                      : Optional.empty();
                          });
    }

    /// Takes the first comment on the specified paragraph and tries to evaluate
    /// the string within the comment against all registered
    /// [Processor]s.
    ///
    /// @param comments          the comments within the document.
    /// @param expressionContext the context root object
    /// @param <T>               the type of the context root object.
    private <T> Optional<Comment> runProcessorsOnParagraphComment(
            Map<BigInteger, Comment> comments,
            T expressionContext,
            Paragraph paragraph,
            BigInteger paragraphCommentId
    ) {
        if (!comments.containsKey(paragraphCommentId)) return Optional.empty();

        var c = comments.get(paragraphCommentId);
        var cPlaceholder = c.asPlaceholder();
        var cComment = c.getComment();
        comments.remove(cComment.getId());
        processors.setContext(new ProcessorContext(paragraph, null, c, cPlaceholder));
        return runProcessors(expressionContext, c.asPlaceholder()) ? Optional.of(c) : Optional.empty();
    }

    /// Finds all processor expressions within the specified paragraph and tries
    /// to evaluate it against all registered [Processor]s.
    ///
    /// @param context   the context root object against which evaluation is done
    /// @param paragraph the paragraph to process.
    /// @param <T>       type of the context root object
    private <T> void runProcessorsOnInlineContent(T context, Paragraph paragraph) {
        var processorContexts = findProcessors(paragraph.asString()).stream()
                                                                    .map(paragraph::processorContext)
                                                                    .toList();
        for (var processorContext : processorContexts) {
            processors.setContext(processorContext);
            var placeholder = processorContext.placeholder();
            try {
                expressionResolver.setContext(context);
                var resolution = expressionResolver.resolve(placeholder);
                var resolve = objectResolverRegistry.resolve(source, placeholder, resolution, "error");
                paragraph.replace(placeholder, resolve);
                logger.debug("Placeholder '{}' successfully processed by a comment processor.", placeholder);
            } catch (SpelEvaluationException | SpelParseException e) {
                var message = "Placeholder '%s' failed to process.".formatted(placeholder);
                var resolution = exceptionResolver.resolve(placeholder, message, e);
                paragraph.replace(placeholder, WmlFactory.newRun(resolution));
            }
            processors.commitChanges(source);
        }
    }

    private WordprocessingMLPackage document() {
        return source.document();
    }

    private void onRangeStart(
            CommentRangeStart crs,
            HashMap<BigInteger, Comment> allComments,
            Queue<Comment> stack,
            HashMap<BigInteger, Comment> rootComments
    ) {
        Comment comment = allComments.get(crs.getId());
        if (comment == null) {
            comment = new StandardComment(document());
            allComments.put(crs.getId(), comment);
            if (stack.isEmpty()) {
                rootComments.put(crs.getId(), comment);
            }
            else {
                stack.peek()
                     .getChildren()
                     .add(comment);
            }
        }
        comment.setCommentRangeStart(crs);
        stack.add(comment);
    }

    private void onRangeEnd(CommentRangeEnd cre, HashMap<BigInteger, Comment> allComments, Queue<Comment> stack) {
        Comment comment = allComments.get(cre.getId());
        if (comment == null)
            throw new OfficeStamperException("Found a comment range end before the comment range start !");

        comment.setCommentRangeEnd(cre);

        if (!stack.isEmpty()) {
            var peek = stack.peek();
            if (peek.equals(comment)) stack.remove();
            else throw new OfficeStamperException("Cannot figure which comment contains the other !");
        }
    }

    private void onReference(R.CommentReference cr, HashMap<BigInteger, Comment> allComments) {
        Comment comment = allComments.get(cr.getId());
        if (comment == null) {
            comment = new StandardComment(document());
            allComments.put(cr.getId(), comment);
        }
        comment.setCommentReference(cr);
    }

    private <T> boolean runProcessors(T context, Placeholder commentPlaceholder) {
        try {
            expressionResolver.setContext(context);
            expressionResolver.resolve(commentPlaceholder);
            logger.debug("Comment '{}' successfully processed by a comment processor.", commentPlaceholder);
            return true;
        } catch (SpelEvaluationException | SpelParseException e) {
            var message = "Comment '%s' failed to process.".formatted(commentPlaceholder.expression());
            exceptionResolver.resolve(commentPlaceholder, message, e);
            return false;
        }
    }

}
