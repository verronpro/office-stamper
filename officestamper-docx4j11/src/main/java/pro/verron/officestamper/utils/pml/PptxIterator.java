package pro.verron.officestamper.utils.pml;

import org.docx4j.openpackaging.packages.PresentationMLPackage;

/// Deprecated compatibility shim: this type moved to
/// [pro.verron.officestamper.docx.pml.PptxIterator] in the 4.0 two-module
/// split. Update the import to the new location.
///
/// @deprecated use [pro.verron.officestamper.docx.pml.PptxIterator] instead.
@Deprecated(forRemoval = true)
public class PptxIterator extends pro.verron.officestamper.docx.pml.PptxIterator {

    public PptxIterator(PresentationMLPackage presentation) {
        super(presentation);
    }
}
