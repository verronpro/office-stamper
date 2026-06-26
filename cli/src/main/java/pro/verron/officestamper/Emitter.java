package pro.verron.officestamper;

import org.jspecify.annotations.Nullable;

import java.util.Map;

public interface Emitter {
    // Minimal structured logging when --log-format=json
    void emit(String level, String message, @Nullable Map<String, ?> fields);
}
