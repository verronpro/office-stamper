package pro.verron.officestamper.api;

/// Contract for stamping a template document with a context and returning the
/// resulting document. The exchanged documents are docx-free [OfficeDocument]
/// adapters supplied by a provider module (e.g. docx4j 11 or 17); native access
/// remains available through [OfficeDocument#unwrap(Class)].
public interface OfficeStamper {

    /// Stamps the given template with the given context and returns the
    /// resulting document. The template is mutated in place and returned.
    ///
    /// @param template the template to stamp
    /// @param context  the context to use for stamping
    /// @return the resulting document after stamping
    OfficeDocument stamp(OfficeDocument template, Object context);
}
