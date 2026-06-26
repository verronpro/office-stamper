package pro.verron.officestamper;

import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

import java.util.Map;

class LogEmitter implements Emitter {

    private static final Logger logger = LoggerFactory.getLogger(LogEmitter.class);

    // Minimal structured logging when --log-format=json
    @Override
    public void emit(String level, String message, @Nullable Map<String, ?> fields) {
        // Human logs via java.util.logging
        var lvl = switch (level) {
            case "ERROR" -> Level.ERROR;
            case "WARN" -> Level.WARN;
            default -> Level.INFO;
        };
        logger.atLevel(lvl).log(fields == null || fields.isEmpty() ? message : message + " | " + fields);
    }
}
