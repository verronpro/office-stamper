package pro.verron.officestamper;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jspecify.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;

import static java.time.OffsetDateTime.now;

class JsonEmitter implements Emitter {
    // Minimal structured logging when --log-format=json
    @Override
    public void emit(String level, String message, @Nullable Map<String, ?> fields) {
        try {
            var map = new LinkedHashMap<String, Object>();
            map.put("ts", now().toString());
            map.put("level", level.toLowerCase());
            map.put("msg", message);
            if (fields != null && !fields.isEmpty()) map.put("fields", fields);
            var json = new ObjectMapper().writeValueAsString(map);
            System.out.println(json);
        } catch (Exception ignored) {
            System.out.println("{\"level\":\"error\",\"msg\":\"failed to emit json log\"}");
        }
    }
}
