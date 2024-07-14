package pro.verron.officestamper.core;

import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.Comments;
import org.docx4j.wml.P;
import org.docx4j.wml.R;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.expression.spel.SpelEvaluationException;
import org.springframework.expression.spel.SpelParseException;
import org.springframework.lang.Nullable;
import pro.verron.officestamper.api.Comment;
import pro.verron.officestamper.api.CommentProcessor;

import java.math.BigInteger;
import java.util.*;

import static pro.verron.officestamper.core.CommentUtil.getCommentString;
import static pro.verron.officestamper.core.CommentUtil.getComments;
import static pro.verron.officestamper.core.ExceptionUtil.treatException;

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
    private final Map<Class<?>, Object> commentProcessors;
    private final boolean failOnUnresolvedExpression;
    private final ExpressionResolver expressionResolver;

    /**
     * Creates a new CommentProcessorRegistry.
     *
     * @param expressionResolver         the expression resolver
     * @param commentProcessors          the comment processors
     * @param failOnUnresolvedExpression whether to fail on unresolved expressions
     */
    public CommentProcessorRegistry(
            ExpressionResolver expressionResolver,
            Map<Class<?>, Object> commentProcessors,
            boolean failOnUnresolvedExpression
    ) {
        this.expressionResolver = expressionResolver;
        this.commentProcessors = commentProcessors;
        this.failOnUnresolvedExpression = failOnUnresolvedExpression;
    }

    /**
     * Lets each registered ICommentProcessor run on the specified docx
     * document. At the end of the document, the commit method is called for each
     * ICommentProcessor. The ICommentProcessors are run in the order they were
     * registered.
     *
     * @param document          the docx document over which to run the registered ICommentProcessors.
     * @param expressionContext the context root object
     * @param <T>               a T class
     */
    public <T> void runProcessors(
            WordprocessingMLPackage document, T expressionContext
    ) {
        var comments = getComments(document);
        var proceedComments = new ArrayList<Comment>();

        DocumentUtil.streamParagraphs(document)
                    .map(P::getContent)
                    .flatMap(List::stream)
                    .map(XmlUtils::unwrap)
                    .filter(R.class::isInstance)
                    .map(R.class::cast)
                    .forEach(run -> runProcessorsOnRunComment(document, comments, expressionContext, run)
                            .ifPresent(proceedComments::add));
        // we run the paragraph afterward so that the comments inside work before the whole paragraph comments
        DocumentUtil.streamParagraphs(document)
                    .forEach(paragraph -> runProcessorsOnParagraphComment(document,
                            comments,
                            expressionContext,
                            paragraph)
                            .ifPresent(proceedComments::add));

        DocumentUtil.streamParagraphs(document)
                    .forEach(paragraph -> runProcessorsOnInlineContent(expressionContext, paragraph));

        for (Object processor : commentProcessors.values()) {
            ((CommentProcessor) processor).commitChanges(document);
        }
        for (Comment comment : proceedComments) {
            CommentUtil.deleteComment(comment);
        }
    }

    private <T> Optional<Comment> runProcessorsOnRunComment(
            WordprocessingMLPackage document,
            Map<BigInteger, Comment> comments,
            T expressionContext,
            R run
    ) {
        return CommentUtil
                .getCommentAround(run, document)
                .flatMap(c -> runCommentProcessors(comments, expressionContext, c, (P) run.getParent(), run));
    }

    /**
     * Takes the first comment on the specified paragraph and tries to evaluate
     * the string within the comment against all registered
     * {@link CommentProcessor}s.
     *
     * @param document          the Word document.
     * @param comments          the comments within the document.
     * @param expressionContext the context root object
     * @param paragraph         the paragraph whose comments to evaluate.
     * @param <T>               the type of the context root object.
     */
    private <T> Optional<Comment> runProcessorsOnParagraphComment(
            WordprocessingMLPackage document,
            Map<BigInteger, Comment> comments,
            T expressionContext,
            P paragraph
    ) {
        return CommentUtil
                .getCommentFor(paragraph, document)
                .flatMap(c -> runCommentProcessors(comments, expressionContext, c, paragraph, null));
    }

    /**
     * Finds all processor expressions within the specified paragraph and tries
     * to evaluate it against all registered {@link CommentProcessor}s.
     *
     * @param context   the context root object against which evaluation is done
     * @param paragraph the paragraph to process.
     * @param <T>       type of the context root object
     */
    private <T> void runProcessorsOnInlineContent(T context, P paragraph) {
        var paragraphWrapper = new StandardParagraph(paragraph);
        var text = paragraphWrapper.asString();
        var placeholders = Placeholders.findProcessors(text);

        for (var placeholder : placeholders) {
            for (var processor : commentProcessors.values()) {
                ((CommentProcessor) processor).setParagraph(paragraph);
            }

            try {
                expressionResolver.setContext(context);
                expressionResolver.resolve(placeholder);
                paragraphWrapper.replace(placeholder, RunUtil.create(""));
                logger.debug("Placeholder '{}' successfully processed by a comment processor.", placeholder);
            } catch (SpelEvaluationException | SpelParseException e) {
                var msg = "Placeholder '%s' failed to process.".formatted(placeholder);
                treatException(e, failOnUnresolvedExpression, msg);
            }
        }
    }

    private <T> Optional<Comment> runCommentProcessors(
            Map<BigInteger, Comment> comments,
            T context,
            Comments.Comment comment,
            P paragraph,
            @Nullable R run
    ) {
        Comment commentWrapper = comments.get(comment.getId());

        if (Objects.isNull(commentWrapper)) {
            // no comment to process
            return Optional.empty();
        }

        var placeholder = getCommentString(comment);

        for (final Object processor : commentProcessors.values()) {
            ((CommentProcessor) processor).setParagraph(paragraph);
            ((CommentProcessor) processor).setCurrentRun(run);
            ((CommentProcessor) processor).setCurrentCommentWrapper(commentWrapper);
        }

        try {
            expressionResolver.setContext(context);
            expressionResolver.resolve(placeholder);
            comments.remove(comment.getId());
            logger.debug("Comment '{}' successfully processed by a comment processor.", placeholder);
            return Optional.of(commentWrapper);
        } catch (SpelEvaluationException | SpelParseException e) {
            var msg = "Comment '%s' failed to process.".formatted(placeholder);
            treatException(e, failOnUnresolvedExpression, msg);
            return Optional.empty();
        }
    }

    /**
     * Resets all registered ICommentProcessors.
     */
    public void reset() {
        for (Object processor : commentProcessors.values()) {
            ((CommentProcessor) processor).reset();
        }
    }
}
