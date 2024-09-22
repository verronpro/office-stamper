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
import pro.verron.officestamper.api.DocxPart;
import pro.verron.officestamper.api.ExceptionResolver;

import java.math.BigInteger;
import java.util.*;

import static pro.verron.officestamper.core.CommentCollectorWalker.collectComments;
import static pro.verron.officestamper.core.CommentUtil.getCommentString;

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
    private final Map<Class<?>, Object> commentProcessors;
    private final ExpressionResolver expressionResolver;
    private final ExceptionResolver exceptionResolver;

    /**
     * Constructs a new CommentProcessorRegistry.
     *
     * @param source the source part of the Word document.
     * @param expressionResolver the resolver for evaluating expressions.
     * @param commentProcessors map of comment processor instances keyed by their respective class types.
     * @param exceptionResolver the resolver for handling exceptions during processing.
     */
    public CommentProcessorRegistry(
            DocxPart source,
            ExpressionResolver expressionResolver,
            Map<Class<?>, Object> commentProcessors,
            ExceptionResolver exceptionResolver
    ) {
        this.source = source;
        this.expressionResolver = expressionResolver;
        this.commentProcessors = commentProcessors;
        this.exceptionResolver = exceptionResolver;
    }

    public <T> void runProcessors(T expressionContext) {
        var proceedComments = new ArrayList<Comment>();

        source.streamParagraphs()
              .map(P::getContent)
              .flatMap(Collection::stream)
              .map(XmlUtils::unwrap)
              .filter(R.class::isInstance)
              .map(R.class::cast)
              .forEach(run -> {
                  var comments = collectComments(source);
                  var runParent = (P) run.getParent();
                  var optional = runProcessorsOnRunComment(comments, expressionContext, run, runParent);
                  if (optional.isPresent()) {
                      var comment = optional.get();
                      for (Object processor : commentProcessors.values()) {
                          var commentProcessor = (CommentProcessor) processor;
                          commentProcessor.commitChanges(source);
                          commentProcessor.reset();
                      }
                      proceedComments.add(comment);
                  }
              });
        // we run the paragraph afterward so that the comments inside work before the whole paragraph comments
        source.streamParagraphs()
              .forEach(p -> {
                  var document = source.document();
                  var comments = collectComments(source);
                  var optional = runProcessorsOnParagraphComment(document, comments, expressionContext, p);
                  if (optional.isPresent()) {
                      for (Object processor : commentProcessors.values()) {
                          var commentProcessor = (CommentProcessor) processor;
                          commentProcessor.commitChanges(source);
                          commentProcessor.reset();
                      }
                      proceedComments.add(optional.get());
                  }
              });
        source.streamParagraphs()
              .forEach(paragraph -> runProcessorsOnInlineContent(expressionContext, paragraph));
        for (Comment comment : proceedComments) {
            CommentUtil.deleteComment(comment);
        }
    }

    private <T> Optional<Comment> runProcessorsOnRunComment(
            Map<BigInteger, Comment> comments,
            T expressionContext,
            R run,
            P paragraph
    ) {
        return CommentUtil
                .getCommentAround(run, source.document())
                .flatMap(c -> runCommentProcessors(
                        comments,
                        expressionContext,
                        c,
                        paragraph, run
                ));
    }

    /**
     * Takes the first comment on the specified paragraph and tries to evaluate
     * the string within the comment against all registered
     * {@link CommentProcessor}s.
     *
     * @param document          the Word document.
     * @param comments          the comments within the document.
     * @param expressionContext the context root object
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
                .flatMap(c -> runCommentProcessors(
                        comments,
                        expressionContext,
                        c,
                        paragraph,
                        null
                ));
    }

    /**
     * Finds all processor expressions within the specified paragraph and tries
     * to evaluate it against all registered {@link CommentProcessor}s.
     *
     * @param context   the context root object against which evaluation is done
     * @param paragraph the paragraph to process.
     * @param <T>       type of the context root object
     */
    private <T> void runProcessorsOnInlineContent(
            T context,
            P paragraph
    ) {
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
                var message = "Placeholder '%s' failed to process.".formatted(placeholder);
                exceptionResolver.resolve(placeholder, message, e);
            }
            for (var processor : commentProcessors.values()) {
                ((CommentProcessor) processor).commitChanges(source);
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
            logger.debug("Comment '{}' successfully processed by a comment processor.", placeholder.expression());
            return Optional.of(commentWrapper);
        } catch (SpelEvaluationException | SpelParseException e) {
            var message = "Comment '%s' failed to process.".formatted(placeholder.expression());
            exceptionResolver.resolve(placeholder, message, e);
            return Optional.empty();
        }
    }
}
