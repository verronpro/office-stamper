package pro.verron.officestamper.core;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.CommentRangeEnd;
import org.docx4j.wml.CommentRangeStart;
import org.docx4j.wml.Comments;
import org.docx4j.wml.R;
import org.jvnet.jaxb2_commons.ppp.Child;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.expression.spel.SpelEvaluationException;
import org.springframework.expression.spel.SpelParseException;
import pro.verron.officestamper.api.*;
import pro.verron.officestamper.utils.WmlUtils;

import java.math.BigInteger;
import java.util.*;

/// Allows registration of [CommentProcessor] objects. Each registered
/// ICommentProcessor must implement an interface which has to be specified at
/// registration time. Provides several getter methods to access the registered
/// [CommentProcessor].
///
/// @author Joseph Verron
/// @author Tom Hombergs
/// @version ${version}
/// @since 1.0.0
public class CommentProcessorRegistry {

    private static final Logger logger = LoggerFactory.getLogger(CommentProcessorRegistry.class);
    private final DocxPart source;
    private final CommentProcessors commentProcessors;
    private final ExpressionResolver expressionResolver;
    private final ExceptionResolver exceptionResolver;

    /// Constructs a new CommentProcessorRegistry.
    ///
    /// @param source             the source part of the Word document.
    /// @param expressionResolver the resolver for evaluating expressions.
    /// @param commentProcessors  map of comment processor instances keyed by their respective class types.
    /// @param exceptionResolver  the resolver for handling exceptions during processing.
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

    /// Processes comments and inline content in the document by evaluating them against all registered
    /// [CommentProcessor]s. This method processes runs, paragraphs, and inline content, and applies
    /// changes based on the evaluation results.
    ///
    /// @param <T>               the type of the context root object
    /// @param expressionContext the context root object against which expressions within comments are evaluated
    public <T> void runProcessors(T expressionContext) {
        var paragraphIterator = DocxIterator.ofParagraphs(source);

        while (paragraphIterator.hasNext()) {
            var p = paragraphIterator.next();
            var comments = collectComments();
            var paragraphComment = p.getComment();
            var updates = 0;
            for (Comments.Comment pc : paragraphComment) {
                updates += runProcessorsOnParagraphComment(comments, expressionContext, p, pc.getId());
            }
            if (updates > 0) paragraphIterator.reset();
        }

        var tagIterator = DocxIterator.ofTags(source);
        while (tagIterator.hasNext()) {
            var tag = tagIterator.next();
            runProcessorsOnInlineContent(expressionContext, tag);
            paragraphIterator.reset();
        }
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

    private <T> int runProcessorsOnParagraphComment(
            Map<BigInteger, Comment> comments,
            T expressionContext,
            Paragraph paragraph,
            BigInteger paragraphCommentId
    ) {
        if (!comments.containsKey(paragraphCommentId)) return 0;

        var c = comments.get(paragraphCommentId);
        var cPlaceholder = c.asPlaceholder();
        var cComment = c.getComment();
        comments.remove(cComment.getId());
        commentProcessors.setContext(new ProcessorContext(paragraph, null, c, cPlaceholder));

        if (!runCommentProcessors(expressionContext, c.asPlaceholder())) return 0;

        commentProcessors.commitChanges(source);
        CommentUtil.deleteComment(c);
        return 1;
    }

    private <T> void runProcessorsOnInlineContent(T context, Tag tag) {
        Placeholder placeholder = tag.asPlaceholder();
        commentProcessors.setContext(new ProcessorContext(tag.getParagraph(), null, tag.asComment(), placeholder));
        try {
            expressionResolver.setContext(context);
            expressionResolver.resolve(placeholder);
            tag.remove();
            logger.debug("Placeholder '{}' successfully processed by a comment processor.", placeholder);
        } catch (SpelEvaluationException | SpelParseException e) {
            var message = "Placeholder '%s' failed to process.".formatted(placeholder);
            exceptionResolver.resolve(placeholder, message, e);
        }
        commentProcessors.commitChanges(source);
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
            comment = new StandardComment(source);
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
            comment = new StandardComment(source);
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
