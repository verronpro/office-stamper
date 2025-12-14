package pro.verron.officestamper.api;

import org.docx4j.openpackaging.packages.OpcPackage;

/// This is an interface that defines the contract for stamping templates with context and returning the result as a
/// document.
///
/// @param <T> The type of the template that can be stamped.
///
/// @author Joseph Verron
/// @version ${version}
/// @since 1.6.4
public interface OfficeStamper<T extends OpcPackage> {

    /// Stamps the given template with the given context and returns the resulting document.
    ///
    /// @param template The template to stamp.
    /// @param context The context to use for stamping.
    ///
    /// @return The resulting document after stamping.
    T stamp(T template, Object context);
}
