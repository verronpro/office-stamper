package pro.verron.officestamper.api;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

/// A document abstraction that is independent of any specific OOXML library.
/// A provider (e.g. for docx4j 11 or 17) supplies the concrete implementation.
public interface OfficeDocument {

    /// @return the main part of this document (e.g. the word/document.xml part)
    OfficePart mainPart();

    /// @return all parts of this document
    List<OfficePart> parts();

    /// Serializes this document to the given stream.
    ///
    /// @param out the target stream
    /// @throws IOException if writing fails
    void save(OutputStream out) throws IOException;

    /// Returns the native representation of this document, for processors that implement
    /// format-specific actions against the provider's types (e.g. the docx4j package object).
    ///
    /// @param type the expected native type
    /// @param <T>  the native type
    /// @return the native representation, cast to the given type
    /// @throws IllegalArgumentException if the provider does not support the requested type
    <T> T unwrap(Class<T> type);
}
