package pro.verron.officestamper.preset.processors.repeat;

import pro.verron.officestamper.api.CommentProcessorContext;

/// Deprecated compatibility shim: this type moved to
/// [pro.verron.officestamper.docx.preset.repeat.RepeatProcessor] in the 4.0
/// two-module split. Update the import to the new location.
///
/// @deprecated use [pro.verron.officestamper.docx.preset.repeat.RepeatProcessor] instead.
@Deprecated(forRemoval = true)
@SuppressWarnings("squid:S2176") // intentional binary-compatibility shim: keeps the legacy simple name
public class RepeatProcessor
        extends pro.verron.officestamper.docx.preset.repeat.RepeatProcessor {

    public RepeatProcessor(CommentProcessorContext context) {
        super(context);
    }
}
