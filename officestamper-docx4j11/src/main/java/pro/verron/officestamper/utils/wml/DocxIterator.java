package pro.verron.officestamper.utils.wml;

import org.docx4j.wml.ContentAccessor;

/// Deprecated compatibility shim: this type moved to
/// [pro.verron.officestamper.docx4j11.wml.DocxIterator] in the 4.0 two-module
/// split. Update the import to the new location.
///
/// @deprecated use [pro.verron.officestamper.docx4j11.wml.DocxIterator] instead.
@Deprecated(forRemoval = true)
public class DocxIterator extends pro.verron.officestamper.docx4j11.wml.DocxIterator {

    public DocxIterator(ContentAccessor contentAccessor) {
        super(contentAccessor);
    }
}
