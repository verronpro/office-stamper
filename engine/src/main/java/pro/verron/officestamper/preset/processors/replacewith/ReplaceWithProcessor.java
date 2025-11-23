package pro.verron.officestamper.preset.processors.replacewith;

import org.docx4j.wml.R;
import org.springframework.lang.Nullable;
import pro.verron.officestamper.api.AbstractCommentProcessor;
import pro.verron.officestamper.api.CommentProcessor;
import pro.verron.officestamper.api.DocxPart;
import pro.verron.officestamper.api.ParagraphPlaceholderReplacer;
import pro.verron.officestamper.preset.CommentProcessorFactory;
import pro.verron.officestamper.utils.Inserts;
import pro.verron.officestamper.utils.WmlFactory;

import java.util.List;
import java.util.function.Function;

/// Processor that replaces the current run with the provided expression.
/// This is useful for replacing an expression in a comment with the result of the expression.
///
/// @author Joseph Verron
/// @author Tom Hombergs
/// @version ${version}
/// @since 1.0.7
public class ReplaceWithProcessor
        extends AbstractCommentProcessor
        implements CommentProcessorFactory.IReplaceWithProcessor {

    private final Function<R, List<Object>> nullSupplier;

    private ReplaceWithProcessor(
            ParagraphPlaceholderReplacer placeholderReplacer,
            Function<R, List<Object>> nullSupplier
    ) {
        super(placeholderReplacer);
        this.nullSupplier = nullSupplier;
    }

    /// Creates a new processor that replaces the current run with the result of the expression.
    ///
    /// @param pr the placeholder replacer to use
    ///
    /// @return the processor
    public static CommentProcessor newInstance(ParagraphPlaceholderReplacer pr) {
        return new ReplaceWithProcessor(pr, R::getContent);
    }

    /// {@inheritDoc}
    @Override
    public void commitChanges(DocxPart document) {
        // nothing to commit
    }

    /// {@inheritDoc}
    @Override
    public void reset() {
        // nothing to reset
    }

    /// {@inheritDoc}
    @Override
    public void replaceWordWith(@Nullable String expression) {
        replaceWith(expression);
    }

    @Override
    public void replaceWith(@Nullable String expression) {
        var comment = this.getCurrentCommentWrapper();
        var from = comment.getCommentRangeStart();
        var to = comment.getCommentRangeEnd();
        getParagraph().replace(from, to, Inserts.of(WmlFactory.newRun(expression)));
    }
}
