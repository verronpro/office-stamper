package pro.verron.officestamper.utils.openpackaging;

import org.docx4j.openpackaging.packages.OpcPackage;
import org.docx4j.openpackaging.parts.Part;

/// Deprecated compatibility shim: this type moved to
/// [pro.verron.officestamper.docx4j11.openpackaging.OpenPackage] in the 3.4
/// two-module split. Update the import to the new location.
///
/// @deprecated use [pro.verron.officestamper.docx4j11.openpackaging.OpenPackage] instead.
@Deprecated(forRemoval = true)
public class OpenPackage<T extends OpcPackage>
        extends pro.verron.officestamper.docx4j11.openpackaging.OpenPackage<T> {

    public OpenPackage(T document, Part part) {
        super(document, part);
    }
}
