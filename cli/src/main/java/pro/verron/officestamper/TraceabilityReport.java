package pro.verron.officestamper;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import pro.verron.officestamper.api.TraceabilityReporter;

import java.util.ArrayList;
import java.util.List;

/// A concrete implementation of TraceabilityReporter that collects all resolution events.
public final class TraceabilityReport
        implements TraceabilityReporter {

    /// Default constructor.
    public TraceabilityReport() {}
    private final List<Resolution> resolutions = new ArrayList<>();

    @Override
    public void onResolution(@NonNull String expression, @Nullable Object value, @NonNull List<Object> contextStack) {
        resolutions.add(new Resolution(expression, value, contextStack));
    }

    /// Returns the list of all collected resolutions.
    ///
    /// @return list of resolutions.
    public List<Resolution> getResolutions() {
        return List.copyOf(resolutions);
    }

    /// Represents a single placeholder resolution event.
    ///
    /// @param expression   the placeholder expression that was resolved
    /// @param value        the resolved value of the expression
    /// @param contextStack the nesting context stack at the time of resolution
    public record Resolution(String expression, Object value, List<Object> contextStack) {}
}
