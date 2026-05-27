package pro.verron.officestamper.api;

import java.util.List;

/// Interface for reporting placeholder resolution events for traceability.
///
/// @since 4.0
public interface TraceabilityReporter {
    /// A reporter that does nothing.
    ///
    /// @return a no-op reporter.
    static TraceabilityReporter noop() {
        return (_, _, _) -> {};
    }

    /// Called when a placeholder expression is resolved.
    ///
    /// @param expression the SpEL expression that was resolved.
    /// @param resolution the result of the resolution.
    /// @param contextStack the current context stack (nesting context).
    void onResolution(String expression, Object resolution, List<Object> contextStack);
}
