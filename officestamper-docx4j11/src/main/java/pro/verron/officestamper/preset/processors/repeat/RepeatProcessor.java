package pro.verron.officestamper.preset.processors.repeat;

import pro.verron.officestamper.spi.ProcessorContext;

/// Deprecated compatibility shim: this type moved to
/// [pro.verron.officestamper.docx4j11.preset.repeat.RepeatProcessor] in the 3.9
/// two-module split. Update the import to the new location.
///
/// @deprecated use [pro.verron.officestamper.docx4j11.preset.repeat.RepeatProcessor] instead.
@Deprecated(forRemoval = true)
public class RepeatProcessor
        extends pro.verron.officestamper.docx4j11.preset.repeat.RepeatProcessor {

    public RepeatProcessor(ProcessorContext context) {
        super(context);
    }
}
