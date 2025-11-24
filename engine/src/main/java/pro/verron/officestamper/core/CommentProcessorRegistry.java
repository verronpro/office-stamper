package pro.verron.officestamper.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.expression.spel.SpelEvaluationException;
import org.springframework.expression.spel.SpelParseException;
import pro.verron.officestamper.api.*;

import java.math.BigInteger;
import java.util.Map;

/// Allows registration of [CommentProcessor] objects.
/// Each registered [CommentProcessor] must implement an interface specified at registration time.
/// Provides several getter methods to access the registered [CommentProcessor].
///
/// @author Joseph Verron
/// @author Tom Hombergs
/// @version ${version}
/// @since 1.0.0
public class CommentProcessorRegistry {

    private static final Logger logger = LoggerFactory.getLogger(CommentProcessorRegistry.class);
    private final CommentProcessors commentProcessors;
    private final ExpressionResolver expressionResolver;
    private final ExceptionResolver exceptionResolver;

    /// Constructs a new CommentProcessorRegistry.
    ///
    /// @param expressionResolver the resolver for evaluating expressions.
    /// @param commentProcessors  map of comment processor instances keyed by their respective class types.
    /// @param exceptionResolver  the resolver for handling exceptions during processing.
    public CommentProcessorRegistry(
            ExpressionResolver expressionResolver,
            CommentProcessors commentProcessors,
            ExceptionResolver exceptionResolver
    ) {
        this.expressionResolver = expressionResolver;
        this.commentProcessors = commentProcessors;
        this.exceptionResolver = exceptionResolver;
    }

    <T> int runProcessorsOnParagraphComment(
            DocxPart source,
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
        commentProcessors.setContext(new ProcessorContext(paragraph, c, cPlaceholder));

        if (!runCommentProcessors(expressionContext, c.asPlaceholder())) return 0;

        commentProcessors.commitChanges(source);
        CommentUtil.deleteComment(c);
        return 1;
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

    <T> void runProcessorsOnInlineContent(DocxPart source, T context, Tag tag) {
        Placeholder placeholder = tag.asPlaceholder();
        commentProcessors.setContext(new ProcessorContext(tag.getParagraph(), tag.asComment(), placeholder));
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
}
