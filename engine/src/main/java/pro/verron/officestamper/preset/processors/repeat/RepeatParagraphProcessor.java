package pro.verron.officestamper.preset.processors.repeat;

import org.jspecify.annotations.Nullable;
import pro.verron.officestamper.api.ProcessorContext;
import pro.verron.officestamper.preset.CommentProcessorFactory;

/// Class used internally to repeat document elements. Used by the lib, should not be instantiated by clients.
///
/// @author Joseph Verron
/// @author Youssouf Naciri
/// @version ${version}
/// @since 1.2.2
public class RepeatParagraphProcessor
        extends RepeatProcessor
        implements CommentProcessorFactory.IParagraphRepeatProcessor {

    /// Constructs a new [RepeatParagraphProcessor] with the given processor context.
    ///
    /// @param processorContext the context in which this processor operates
    public RepeatParagraphProcessor(ProcessorContext processorContext) {
        super(processorContext);
    }

    @Override
    public void repeatParagraph(@Nullable Iterable<Object> items) {
        repeat(items);
    }
}
