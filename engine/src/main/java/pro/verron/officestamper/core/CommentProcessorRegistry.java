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

/**
 * Allows registration of {@link CommentProcessor} objects. Each registered
 * ICommentProcessor must implement an interface which has to be specified at
 * registration time. Provides several getter methods to access the registered
 * {@link CommentProcessor}.
 *
 * @author Joseph Verron
 * @author Tom Hombergs
 * @version ${version}
 * @since 1.0.0
 */
public class CommentProcessorRegistry {

    private static final Logger logger = LoggerFactory.getLogger(CommentProcessorRegistry.class);
    private final DocxPart source;
    private final CommentProcessors commentProcessors;
    private final ExpressionResolver expressionResolver;
    private final ExceptionResolver exceptionResolver;

    /**
     * Constructs a new CommentProcessorRegistry.
     *
     * @param source             the source part of the Word document.
     * @param expressionResolver the resolver for evaluating expressions.
     * @param commentProcessors  map of comment processor instances keyed by their respective class types.
     * @param exceptionResolver  the resolver for handling exceptions during processing.
     */
    public CommentProcessorRegistry(
            DocxPart source,
            ExpressionResolver expressionResolver,
            CommentProcessors commentProcessors,
            ExceptionResolver exceptionResolver
    ) {
        this.source = source;
        this.expressionResolver = expressionResolver;
        this.commentProcessors = commentProcessors;
        this.exceptionResolver = exceptionResolver;
    }

    public <T> void runProcessors(T expressionContext) {
        var proceedComments = new ArrayList<Comment>();

        source.streamRun()
              .forEach(run -> {
                  var comments = collectComments();
                  var runParent = StandardParagraph.from(source, (P) run.getParent());
                  var optional = runProcessorsOnRunComment(comments, expressionContext, run, runParent);
                  optional.ifPresent(proceedComments::add);
              });
        commentProcessors.commitChanges(source);

        // we run the paragraph afterward so that the comments inside work before the whole paragraph comments
        source.streamParagraphs()
              .forEach(p -> {
                  var comments = collectComments();
                  var paragraphComment = p.getComment();
                  paragraphComment.forEach((pc -> {
                      var optional = runProcessorsOnParagraphComment(comments, expressionContext, p, pc.getId());
                      commentProcessors.commitChanges(source);
                      optional.ifPresent(proceedComments::add);
                  }));
              });

        source.streamParagraphs()
              .forEach(paragraph -> runProcessorsOnInlineContent(expressionContext, paragraph));

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
            Map<BigInteger, Comment> comments, T expressionContext, R run, Paragraph paragraph
    ) {
        return CommentUtil.getCommentAround(run, document())
                          .flatMap(c -> Optional.ofNullable(comments.get(c.getId())))
                          .flatMap(c -> {
                              var cPlaceholder = c.asPlaceholder();
                              var cComment = c.getComment();
                              comments.remove(cComment.getId());
                              commentProcessors.setContext(new ProcessorContext(paragraph, run, c, cPlaceholder));
                              return runCommentProcessors(expressionContext, cPlaceholder)
                                      ? Optional.of(c)
                                      : Optional.empty();
                          });
    }

    /**
     * Takes the first comment on the specified paragraph and tries to evaluate
     * the string within the comment against all registered
     * {@link CommentProcessor}s.
     *
     * @param comments          the comments within the document.
     * @param expressionContext the context root object
     * @param <T>               the type of the context root object.
     */
    private <T> Optional<Comment> runProcessorsOnParagraphComment(
            Map<BigInteger, Comment> comments, T expressionContext, Paragraph paragraph, BigInteger paragraphCommentId
    ) {
        if (!comments.containsKey(paragraphCommentId)) return Optional.empty();

        var c = comments.get(paragraphCommentId);
        var cPlaceholder = c.asPlaceholder();
        var cComment = c.getComment();
        comments.remove(cComment.getId());
        commentProcessors.setContext(new ProcessorContext(paragraph, null, c, cPlaceholder));
        return runCommentProcessors(expressionContext, c.asPlaceholder()) ? Optional.of(c) : Optional.empty();
    }

    /**
     * Finds all processor expressions within the specified paragraph and tries
     * to evaluate it against all registered {@link CommentProcessor}s.
     *
     * @param context   the context root object against which evaluation is done
     * @param paragraph the paragraph to process.
     * @param <T>       type of the context root object
     */
    private <T> void runProcessorsOnInlineContent(T context, Paragraph paragraph) {
        var processorContexts = findProcessors(paragraph.asString()).stream()
                                                                    .map(paragraph::processorContext)
                                                                    .toList();
        for (var processorContext : processorContexts) {
            commentProcessors.setContext(processorContext);
            var placeholder = processorContext.placeholder();
            try {
                expressionResolver.setContext(context);
                expressionResolver.resolve(placeholder);
                paragraph.replace(placeholder, WmlFactory.newRun(""));
                logger.debug("Placeholder '{}' successfully processed by a comment processor.", placeholder);
            } catch (SpelEvaluationException | SpelParseException e) {
                var message = "Placeholder '%s' failed to process.".formatted(placeholder);
                exceptionResolver.resolve(placeholder, message, e);
            }
            commentProcessors.commitChanges(source);
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

    private void onRangeEnd(
            CommentRangeEnd cre, HashMap<BigInteger, Comment> allComments, Queue<Comment> stack
    ) {
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

    private <T> boolean runCommentProcessors(T context, Placeholder commentPlaceholder) {
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
