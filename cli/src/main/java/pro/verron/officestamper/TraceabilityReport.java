package pro.verron.officestamper;

import org.jspecify.annotations.Nullable;
import pro.verron.officestamper.api.TraceabilityReporter;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

/// A concrete implementation of TraceabilityReporter that collects all
/// resolution events.
public final class TraceabilityReport
        implements TraceabilityReporter {

    private List<Resolution> resolutions;
    private OffsetDateTime timestamp;
    private String data;
    private String template;

    public TraceabilityReport() {
    }

    public TraceabilityReport(
            OffsetDateTime timestamp,
            String template,
            String data
    ) {
        this(timestamp, template, data, new ArrayList<>());
    }

    /// Default constructor.
    public TraceabilityReport(
            OffsetDateTime timestamp,
            String template,
            String data,
            List<Resolution> resolutions
    ) {
        this.data = data;
        this.timestamp = timestamp;
        this.template = template;
        this.resolutions = resolutions;
    }

    @Override
    public void onResolution(
            String expression,
            @Nullable Object value,
            List<Object> contextStack
    ) {
        resolutions.add(new Resolution(expression, value, contextStack));
    }

    public void getTimestamp(OffsetDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public void getData(String data) {
        this.data = data;
    }

    public void getTemplate(String template) {
        this.template = template;
    }

    public List<Resolution> getResolutions() {
        return List.copyOf(resolutions);
    }

    public void setResolutions(List<Resolution> resolutions) {
        this.resolutions = List.copyOf(resolutions);
    }

    public OffsetDateTime getTimestamp() {
        return timestamp;
    }

    public String getData() {
        return data;
    }

    public String getTemplate() {
        return template;
    }

    /// Represents a single placeholder resolution event.
    ///
    /// @param expression   the placeholder expression that was resolved
    /// @param value        the resolved value of the expression
    /// @param contextStack the nesting context stack at the time of resolution
    public record Resolution(
            String expression, @Nullable Object value, List<Object> contextStack
    ) {}
}
