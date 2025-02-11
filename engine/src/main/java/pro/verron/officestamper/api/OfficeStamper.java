package pro.verron.officestamper.api;

import org.docx4j.openpackaging.packages.OpcPackage;

import java.io.OutputStream;

/// This is an interface that defines the contract for stamping
/// templates with context and writing the result to an [OutputStream].
///
/// @param <T> The type of the template that can be stamped.
///
/// @author Joseph Verron
/// @version ${version}
/// @since 1.6.4
public interface OfficeStamper<T extends OpcPackage> {

    /// Stamps the template with the context and writes the result to the outputStream.
    ///
    /// @param template     template to stamp
    /// @param context      context to use for stamping
    /// @param outputStream output stream to write the result to
    ///
    /// @throws OfficeStamperException if the stamping fails
    void stamp(T template, Object context, OutputStream outputStream)
            throws OfficeStamperException;
}
