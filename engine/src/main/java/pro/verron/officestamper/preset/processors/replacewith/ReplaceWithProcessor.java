package pro.verron.officestamper.preset.processors.replacewith;

import org.jspecify.annotations.Nullable;
import pro.verron.officestamper.api.CommentProcessor;
import pro.verron.officestamper.api.Insert;
import pro.verron.officestamper.api.OfficeStamperException;
import pro.verron.officestamper.api.ProcessorContext;
import pro.verron.officestamper.preset.CommentProcessorFactory;

/// Processor that replaces the current run with the provided expression. This is useful for replacing an expression in
/// a AnchoredComment with the result of the expression.
///
/// @author Joseph Verron
/// @author Tom Hombergs
/// @since 1.0.7
public class ReplaceWithProcessor
        extends CommentProcessor
        implements CommentProcessorFactory.IReplaceWithProcessor {

    /// Constructs a new [ReplaceWithProcessor] instance.
    ///
    /// @param processorContext the context in which this processor operates, providing access to document
    ///         manipulation and expression evaluation utilities.
    public ReplaceWithProcessor(ProcessorContext processorContext) {
        super(processorContext);
    }

    /// Replaces the content between the start and end of the AnchoredComment with the given expression.
    ///
    /// @param expression The expression to replace the content with. Must not be null.
    /// @throws OfficeStamperException if the expression is null, or if the AnchoredComment range start or end is null.
    @Override
    public void replaceWith(@Nullable String expression) {
        if (expression == null) throw new OfficeStamperException("Cannot replace with null expression");
        var from = comment().startAnchor();
        var to = comment().endAnchor();
        paragraph().replace(from, to, new Insert(context().part().factory().run(expression)));
    }
}


