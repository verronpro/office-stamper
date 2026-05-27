package pro.verron.officestamper;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import pro.verron.officestamper.api.TraceabilityReporter;

import java.util.ArrayList;
import java.util.List;

/// A concrete implementation of TraceabilityReporter that collects all resolution events.
///
/// @since 4.0
public final class TraceabilityReport
        implements TraceabilityReporter {
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
    public record Resolution(String expression, Object value, List<Object> contextStack) {}
}
