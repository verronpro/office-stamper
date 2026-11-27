package pro.verron.officestamper.api;

import java.util.ServiceLoader;

/// Factory for obtaining a configured [OfficeStamper]. The concrete
/// implementation is discovered at runtime via [ServiceLoader], so the
/// docx4j (or any other OOXML) dependency lives entirely in a separate provider
/// module and can be swapped or removed without touching caller code.
///
/// Example: a user adds `officestamper-docx4j11` to their dependencies
/// and calls [#docxStamper()]; to drop docx4j entirely they remove that
/// module and supply a different provider.
public final class OfficeStampers {

    private OfficeStampers() {
    }

    /// Returns the first [OfficeStamper] implementation available on the
    /// module / class path.
    ///
    /// @return a docx4j-free office stamper
    /// @throws IllegalStateException if no provider is registered
    public static OfficeStamper docxStamper() {
        return ServiceLoader.load(OfficeStamper.class)
                .stream()
                .map(ServiceLoader.Provider::get)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "No OfficeStamper provider found on the classpath. " +
                        "Add a provider module such as officestamper-docx4j11."));
    }
}
