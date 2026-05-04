package pro.verron.officestamper.imageio.svg.utils;

import pro.verron.officestamper.preset.Image;

import java.util.HashMap;
import java.util.Map;

/// Factory for creating map-based contexts.
public final class MapContextFactory
        implements ContextFactory {

    /// Default constructor.
    public MapContextFactory() {
    }

    /// Represents the context for an insertable image.
    @Override
    public Object image(Image image) {
        return Map.of("monalisa", image);
    }

    @Override
    public Object name(String name) {
        Map<String, String> map = new HashMap<>();
        map.put("name", name);
        return map;
    }
}
