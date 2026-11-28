package pro.verron.officestamper.api;

/// Formatting properties attached to a text run, as an opaque value.
///
/// This is deliberately opaque: the engine's core use is *copy-through* — the properties of
/// a run being replaced are captured via [Element#runProperties()] and applied to the runs of an
/// [Insert] via [Insert#withRunProperties(RunProperties)], preserving the original formatting without
/// this library needing to model every OOXML property.
///
/// Library users who need the raw, provider-specific representation can call [unwrap] — for
/// instance to read or mutate the underlying `RPr` when using the docx4j provider.
public interface RunProperties {

    /// Returns the native representation of these run properties, for library users that need direct
    /// access to the underlying format-specific type.
    ///
    /// @param type the expected native type
    /// @param <T>  the native type
    /// @return the native representation, cast to the given type
    /// @throws IllegalArgumentException if the provider does not support the requested type
    <T> T unwrap(Class<T> type);
}
