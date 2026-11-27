package pro.verron.officestamper.utils.sml;

import org.docx4j.openpackaging.packages.SpreadsheetMLPackage;

/// Deprecated compatibility shim: this type moved to
/// [pro.verron.officestamper.docx.sml.XlsxIterator] in the 4.0 two-module
/// split. Update the import to the new location.
///
/// @deprecated use [pro.verron.officestamper.docx.sml.XlsxIterator] instead.
@Deprecated(forRemoval = true)
@SuppressWarnings("squid:S2176") // intentional binary-compatibility shim: keeps the legacy simple name
public class XlsxIterator extends pro.verron.officestamper.docx.sml.XlsxIterator {

    public XlsxIterator(SpreadsheetMLPackage spreadsheet) {
        super(spreadsheet);
    }
}
