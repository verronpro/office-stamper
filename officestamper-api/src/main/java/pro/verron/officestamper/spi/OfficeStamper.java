package pro.verron.officestamper.spi;

/// Contract for stamping a template document with a context and returning the
/// resulting document. The document type {@code T} is intentionally decoupled
/// from any specific OOXML library (e.g. docx4j); a concrete provider supplies
/// the implementation for a given library/version.
///
/// @param <T> the native document type this stamper operates on (the docx provider uses
///         [OfficeDocument]; other format providers may use their own native types)
public interface OfficeStamper<T> {

    /// Stamps the given template with the given context and returns the
    /// resulting document.
    ///
    /// @param template the template to stamp
    /// @param context  the context to use for stamping
    /// @return the resulting document after stamping
    T stamp(T template, Object context);
}
