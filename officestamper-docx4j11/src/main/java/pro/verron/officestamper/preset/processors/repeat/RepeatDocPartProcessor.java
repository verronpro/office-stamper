package pro.verron.officestamper.preset.processors.repeat;

import pro.verron.officestamper.api.ProcessorContext;

/// Deprecated compatibility shim: this type moved to
/// [pro.verron.officestamper.docx4j11.preset.repeat.RepeatDocPartProcessor] in
/// the 4.0 two-module split. Update the import to the new location.
///
/// @deprecated use [pro.verron.officestamper.docx4j11.preset.repeat.RepeatDocPartProcessor] instead.
@Deprecated(forRemoval = true)
public class RepeatDocPartProcessor
        extends pro.verron.officestamper.docx4j11.preset.repeat.RepeatDocPartProcessor {

    public RepeatDocPartProcessor(ProcessorContext processorContext) {
        super(processorContext);
    }
}
