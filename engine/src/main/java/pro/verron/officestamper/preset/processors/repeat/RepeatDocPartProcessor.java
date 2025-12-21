package pro.verron.officestamper.preset.processors.repeat;

import org.jspecify.annotations.Nullable;
import pro.verron.officestamper.api.OfficeStamper;
import pro.verron.officestamper.api.ProcessorContext;
import pro.verron.officestamper.preset.CommentProcessorFactory;


/// Processes the `<repeatDocPart>` comment. Uses the [OfficeStamper] to stamp sub-documents and copies the resulting
/// content to the correct position in the main document.
///
/// @author Joseph Verron
/// @author Youssouf Naciri
/// @version ${version}
/// @since 1.3.0
public class RepeatDocPartProcessor
        extends RepeatProcessor
        implements CommentProcessorFactory.IRepeatDocPartProcessor {


    /// Creates a new [RepeatDocPartProcessor] instance.
    ///
    /// @param processorContext the processor context
    public RepeatDocPartProcessor(ProcessorContext processorContext) {
        super(processorContext);
    }

    @Override
    public void repeatDocPart(@Nullable Iterable<Object> items) {
        repeat(items);
    }

}
