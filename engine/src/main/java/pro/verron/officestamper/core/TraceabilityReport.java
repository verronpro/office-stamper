package pro.verron.officestamper.core;

import pro.verron.officestamper.api.TraceabilityReporter;

import java.util.ArrayList;
import java.util.List;

/// A concrete implementation of TraceabilityReporter that collects all resolution events.
///
/// @since 4.0
public final class TraceabilityReport implements TraceabilityReporter {
    private final List<Resolution> resolutions = new ArrayList<>();

    /// Represents a single placeholder resolution event.
    ///
    /// @param expression the SpEL expression.
    /// @param value the resolved value.
    /// @param contextStack the nesting context at the time of resolution.
    public record Resolution(String expression, Object value, List<Object> contextStack) {}

    @Override
    public void onResolution(String expression, Object value, List<Object> contextStack) {
        resolutions.add(new Resolution(expression, value, contextStack));
    }

    /// Returns the list of all collected resolutions.
    ///
    /// @return list of resolutions.
    public List<Resolution> getResolutions() {
        return List.copyOf(resolutions);
    }
}
