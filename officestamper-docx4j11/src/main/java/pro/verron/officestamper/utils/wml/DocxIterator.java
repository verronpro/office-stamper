package pro.verron.officestamper.utils.wml;

import org.docx4j.wml.ContentAccessor;

/// Deprecated compatibility shim: this type moved to
/// [pro.verron.officestamper.docx.wml.DocxIterator] in the 4.0 two-module
/// split. Update the import to the new location.
///
/// @deprecated use [pro.verron.officestamper.docx.wml.DocxIterator] instead.
@Deprecated(forRemoval = true)
@SuppressWarnings("squid:S2176") // intentional binary-compatibility shim: keeps the legacy simple name
public class DocxIterator extends pro.verron.officestamper.docx.wml.DocxIterator {

    public DocxIterator(ContentAccessor contentAccessor) {
        super(contentAccessor);
    }
}
