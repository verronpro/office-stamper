package pro.verron.officestamper.utils.sml;

import org.docx4j.openpackaging.packages.SpreadsheetMLPackage;

/// Deprecated compatibility shim: this type moved to
/// [pro.verron.officestamper.docx4j11.sml.XlsxIterator] in the 3.9 two-module
/// split. Update the import to the new location.
///
/// @deprecated use [pro.verron.officestamper.docx4j11.sml.XlsxIterator] instead.
@Deprecated(forRemoval = true)
public class XlsxIterator extends pro.verron.officestamper.docx4j11.sml.XlsxIterator {

    public XlsxIterator(SpreadsheetMLPackage spreadsheet) {
        super(spreadsheet);
    }
}
