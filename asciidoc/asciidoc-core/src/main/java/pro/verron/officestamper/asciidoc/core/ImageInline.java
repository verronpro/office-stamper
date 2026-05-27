package pro.verron.officestamper.asciidoc.core;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

/// Inline image.
///
/// @param path image path
/// @param map alternative text
public record ImageInline(String path, Map<String, String> map)
        implements Inline {

    /// Constructs an instance of the ImageInline class with the specified image path and alternative text mappings.
    ///
    /// @param path the path to the image
    /// @param map a mapping of alternative text attributes associated with the image;
    ///            keys and values represent descriptive labels for different use cases or locales
    public ImageInline(String path, Map<String, String> map) {
        this.path = path;
        this.map = Collections.unmodifiableMap(new TreeMap<>(map));
    }

    @Override
    public String text() {
        return path;
    }
}
