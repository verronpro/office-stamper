package pro.verron.officestamper.preset.processors.repeat;

import pro.verron.officestamper.spi.ProcessorContext;

/// Deprecated compatibility shim: this type moved to
/// [pro.verron.officestamper.docx4j11.preset.repeat.RepeatParagraphProcessor] in
/// the 3.4 two-module split. Update the import to the new location.
///
/// @deprecated use [pro.verron.officestamper.docx4j11.preset.repeat.RepeatParagraphProcessor] instead.
@Deprecated(forRemoval = true)
public class RepeatParagraphProcessor
        extends pro.verron.officestamper.docx4j11.preset.repeat.RepeatParagraphProcessor {

    public RepeatParagraphProcessor(ProcessorContext processorContext) {
        super(processorContext);
    }
}
