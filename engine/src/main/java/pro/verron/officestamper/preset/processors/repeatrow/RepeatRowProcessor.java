package pro.verron.officestamper.preset.processors.repeatrow;

import org.jspecify.annotations.Nullable;
import pro.verron.officestamper.api.CommentProcessor;
import pro.verron.officestamper.api.OfficeStamperException;
import pro.verron.officestamper.api.ProcessorContext;
import pro.verron.officestamper.preset.CommentProcessorFactory;

/// Repeats a table row for each element in a list.
///
/// @author Joseph Verron
/// @author Tom Hombergs
/// @since 1.0.0
public class RepeatRowProcessor
        extends CommentProcessor
        implements CommentProcessorFactory.IRepeatRowProcessor {


    /// Constructs a new RepeatRowProcessor with the given processor context.
    ///
    /// @param processorContext the context in which this processor operates, containing information about the
    ///         document and processing environment
    public RepeatRowProcessor(ProcessorContext processorContext) {
        super(processorContext);
    }

    @Override
    public void repeatTableRow(@Nullable Iterable<Object> items) {
        if (items == null) return;

        var contextHolder = context().contextHolder();
        var row = context().paragraph()
                           .parentTableRow()
                           .orElseThrow(OfficeStamperException.throwing("This paragraph is not in a table row."));
        // The copied rows must not carry stale AnchoredComment markers
        comment().deleteFrom(row);

        var table = row.table();
        var index = table.indexOf(row);
        for (Object item : items) {
            var copy = row.copy();
            var contextKey = contextHolder.addBranch(item);
            // Re-discover the hook tags within the copy and bind them to the new context branch
            context().part()
                     .hooks()
                     .ofHooks(copy)
                     .forEachRemaining(hook -> hook.setAttribute("context", contextKey));
            table.add(index++, copy);
        }
        row.remove();
    }
}


