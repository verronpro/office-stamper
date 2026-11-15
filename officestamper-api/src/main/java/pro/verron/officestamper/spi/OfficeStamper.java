package pro.verron.officestamper.spi;

import java.io.IOException;

/// Contract for stamping a template document with a context and returning the
/// resulting document. The document type {@code T} is intentionally decoupled
/// from any specific OOXML library (e.g. docx4j); a concrete provider supplies
/// the implementation for a given library/version.
///
/// @param <T> the document type, which must extend {@link OfficeDocument}
public interface OfficeStamper<T extends OfficeDocument> {

    /// Stamps the given template with the given context and returns the
    /// resulting document.
    ///
    /// @param template the template to stamp
    /// @param context  the context to use for stamping
    /// @return the resulting document after stamping
    /// @throws IOException if the document cannot be processed
    T stamp(T template, Object context) throws IOException;
}
