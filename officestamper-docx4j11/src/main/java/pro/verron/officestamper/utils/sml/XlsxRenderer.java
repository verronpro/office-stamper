package pro.verron.officestamper.utils.sml;

/// Deprecated compatibility shim: this type moved to [pro.verron.officestamper.docx.sml.XlsxRenderer]
/// in the 4.0 two-module split. Update the import to the new location.
///
/// @deprecated use [pro.verron.officestamper.docx.sml.XlsxRenderer] instead.
@Deprecated(forRemoval = true)
@SuppressWarnings("squid:S2176") // intentional binary-compatibility shim: keeps the legacy simple name
public class XlsxRenderer extends pro.verron.officestamper.docx.sml.XlsxRenderer {}
