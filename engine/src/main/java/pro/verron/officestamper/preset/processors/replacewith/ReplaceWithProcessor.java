package pro.verron.officestamper.preset.processors.replacewith;

import org.jspecify.annotations.Nullable;
import pro.verron.officestamper.api.CommentProcessor;
import pro.verron.officestamper.api.Insert;
import pro.verron.officestamper.api.OfficeStamperException;
import pro.verron.officestamper.api.ProcessorContext;
import pro.verron.officestamper.preset.CommentProcessorFactory;

import static pro.verron.officestamper.utils.WmlFactory.newRun;

/// Processor that replaces the current run with the provided expression. This is useful for replacing an expression in
/// a comment with the result of the expression.
///
/// @author Joseph Verron
/// @author Tom Hombergs
/// @version ${version}
/// @since 1.0.7
public class ReplaceWithProcessor
        extends CommentProcessor
        implements CommentProcessorFactory.IReplaceWithProcessor {

    private ReplaceWithProcessor(ProcessorContext processorContext) {
        super(processorContext);
    }

    /// Creates a new processor that replaces the current run with the result of the expression.
    ///
    /// @return the processor
    public static CommentProcessor newInstance(ProcessorContext processorContext) {
        return new ReplaceWithProcessor(processorContext);
    }

    @Override
    public void replaceWith(@Nullable String expression) {
        if (expression == null) throw new OfficeStamperException("Cannot replace with null expression");
        var from = comment().getCommentRangeStart();
        if (from == null) throw new OfficeStamperException("Cannot replace with no comment range start");
        var to = comment().getCommentRangeEnd();
        if (to == null) throw new OfficeStamperException("Cannot replace with no comment range end");
        paragraph().replace(from, to, new Insert(newRun(expression)));
    }
}
