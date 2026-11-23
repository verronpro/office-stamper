package pro.verron.officestamper.api;

import java.util.ServiceLoader;

/// Factory for obtaining a configured {@link OfficeStamper}. The concrete
/// implementation is discovered at runtime via {@link ServiceLoader}, so the
/// docx4j (or any other OOXML) dependency lives entirely in a separate provider
/// module and can be swapped or removed without touching caller code.
///
/// <p>Example: a user adds {@code officestamper-docx4j11} to their dependencies
/// and calls {@link #docxStamper()}; to drop docx4j entirely they remove that
/// module and supply a different provider.
public final class OfficeStampers {

    private OfficeStampers() {
    }

    /// Returns the first {@link OfficeStamper} implementation available on the
    /// module / class path.
    ///
    /// @return a docx4j-free office stamper
    /// @throws IllegalStateException if no provider is registered
    @SuppressWarnings("unchecked")
    public static OfficeStamper<OfficeDocument> docxStamper() {
        return ServiceLoader.load(OfficeStamper.class)
                .stream()
                .map(ServiceLoader.Provider::get)
                .map(s -> (OfficeStamper<OfficeDocument>) s)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "No OfficeStamper provider found on the classpath. " +
                        "Add a provider module such as officestamper-docx4j11."));
    }
}
