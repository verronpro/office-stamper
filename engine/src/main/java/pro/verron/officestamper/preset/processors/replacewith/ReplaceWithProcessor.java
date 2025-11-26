package pro.verron.officestamper.preset.processors.replacewith;

import org.springframework.lang.Nullable;
import pro.verron.officestamper.api.CommentProcessor;
import pro.verron.officestamper.api.PlaceholderReplacer;
import pro.verron.officestamper.api.ProcessorContext;
import pro.verron.officestamper.preset.CommentProcessorFactory;
import pro.verron.officestamper.utils.Inserts;
import pro.verron.officestamper.utils.WmlFactory;

/// Processor that replaces the current run with the provided expression.
/// This is useful for replacing an expression in a comment with the result of the expression.
///
/// @author Joseph Verron
/// @author Tom Hombergs
/// @version ${version}
/// @since 1.0.7
public class ReplaceWithProcessor
        extends CommentProcessor
        implements CommentProcessorFactory.IReplaceWithProcessor {

    private ReplaceWithProcessor(ProcessorContext processorContext, PlaceholderReplacer placeholderReplacer) {
        super(processorContext, placeholderReplacer);
    }

    /// Creates a new processor that replaces the current run with the result of the expression.
    ///
    /// @param pr the placeholder replacer to use
    ///
    /// @return the processor
    public static CommentProcessor newInstance(ProcessorContext processorContext, PlaceholderReplacer pr) {
        return new ReplaceWithProcessor(processorContext, pr);
    }

    @Override
    public void replaceWith(@Nullable String expression) {
        var from = comment().getCommentRangeStart();
        var to = comment().getCommentRangeEnd();
        paragraph().replace(from, to, Inserts.of(WmlFactory.newRun(expression)));
    }
}
