package pro.verron.officestamper.utils.openpackaging;

/// Deprecated compatibility shim: this type moved to [pro.verron.officestamper.docx.openpackaging.OpenpackagingFactory]
/// in the 4.0 two-module split. Update the import to the new location.
///
/// @deprecated use [pro.verron.officestamper.docx.openpackaging.OpenpackagingFactory] instead.
@Deprecated(forRemoval = true)
@SuppressWarnings("squid:S2176") // intentional binary-compatibility shim: keeps the legacy simple name
public class OpenpackagingFactory extends pro.verron.officestamper.docx.openpackaging.OpenpackagingFactory {}
