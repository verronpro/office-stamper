package pro.verron.officestamper.utils.pml;

import org.docx4j.openpackaging.packages.PresentationMLPackage;

/// Deprecated compatibility shim: this type moved to
/// [pro.verron.officestamper.docx4j11.pml.PptxIterator] in the 3.4 two-module
/// split. Update the import to the new location.
///
/// @deprecated use [pro.verron.officestamper.docx4j11.pml.PptxIterator] instead.
@Deprecated(forRemoval = true)
public class PptxIterator extends pro.verron.officestamper.docx4j11.pml.PptxIterator {

    public PptxIterator(PresentationMLPackage presentation) {
        super(presentation);
    }
}
