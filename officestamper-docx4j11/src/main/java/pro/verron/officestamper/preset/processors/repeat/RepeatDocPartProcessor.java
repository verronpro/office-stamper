package pro.verron.officestamper.preset.processors.repeat;

import pro.verron.officestamper.api.ProcessorContext;

/// Deprecated compatibility shim: this type moved to
/// [pro.verron.officestamper.docx.preset.repeat.RepeatDocPartProcessor] in
/// the 4.0 two-module split. Update the import to the new location.
///
/// @deprecated use [pro.verron.officestamper.docx.preset.repeat.RepeatDocPartProcessor] instead.
@Deprecated(forRemoval = true)
@SuppressWarnings("squid:S2176") // intentional binary-compatibility shim: keeps the legacy simple name
public class RepeatDocPartProcessor
        extends pro.verron.officestamper.docx.preset.repeat.RepeatDocPartProcessor {

    public RepeatDocPartProcessor(ProcessorContext processorContext) {
        super(processorContext);
    }
}
