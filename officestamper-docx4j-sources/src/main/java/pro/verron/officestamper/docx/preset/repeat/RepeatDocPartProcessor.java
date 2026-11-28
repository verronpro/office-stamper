package pro.verron.officestamper.docx.preset.repeat;

import org.jspecify.annotations.Nullable;
import pro.verron.officestamper.api.OfficeStamper;
import pro.verron.officestamper.api.CommentProcessorContext;
import pro.verron.officestamper.preset.CommentProcessorFactory;


/// Processes the `<repeatDocPart>` AnchoredComment. Uses the [OfficeStamper] to stamp sub-documents and copies the resulting
/// content to the correct position in the main document.
///
/// @author Joseph Verron
/// @author Youssouf Naciri
/// @since 1.3.0
public class RepeatDocPartProcessor
        extends RepeatProcessor
        implements CommentProcessorFactory.IRepeatDocPartProcessor {


    /// Creates a new [RepeatDocPartProcessor] instance.
    ///
    /// @param processorContext the processor context
    public RepeatDocPartProcessor(CommentProcessorContext processorContext) {
        super(processorContext);
    }

    @Override
    public void repeatDocPart(@Nullable Iterable<Object> items) {
        repeat(items);
    }

}
