package pro.verron.officestamper.asciidoc.converters;

import java.util.Locale;
import java.util.Map;

/// Provides SVG paths for icons used in simulated editor interfaces.
/// Icons are sourced from Bootstrap Icons (MIT License).
final class AsciiDocIcon {

    private static final Map<String, String> PATHS = Map.of("save",
            "M11 2H9v3h2zM1.5 0h11.586a1.5 1.5 0 0 1 1.06.44l1.415 1.414A1.5 1.5 0 0 1 16 2.914V14.5a1.5 1.5 0 0 1-1"
            + ".5 1.5h-13A1.5 1.5 0 0 1 0 14.5v-13A1.5 1.5 0 0 1 1.5 0M1 1.5v13a.5.5 0 0 0 .5.5H2v-4.5A1.5 1.5 0 0 1 "
            + "3.5 9h9a1.5 1.5 0 0 1 1.5 1.5V15h.5a.5.5 0 0 0 .5-.5V2.914a.5.5 0 0 0-.146-.353l-1.415-1.415A.5.5 0 0 "
            + "0 13.086 1H13v4.5A1.5 1.5 0 0 1 11.5 7h-7A1.5 1.5 0 0 1 3 5.5V1H1.5a.5.5 0 0 0-.5.5m3-.5v4.5a.5.5 0 0 "
            + "0 .5.5h7a.5.5 0 0 0 .5-.5V1zM10 15h2v-4.5a.5.5 0 0 0-.5-.5h-9a.5.5 0 0 0-.5.5V15h2v-4h5z",
            "print",
            "M2.5 8a.5.5 0 1 0 0-1 .5.5 0 0 0 0 1M5 1a2 2 0 0 0-2 2v2H2a2 2 0 0 0-2 2v3a2 2 0 0 0 2 2h1v1a2 2 0 0 0 2"
            + " 2h6a2 2 0 0 0 2-2v-1h1a2 2 0 0 0 2-2V7a2 2 0 0 0-2-2h-1V3a2 2 0 0 0-2-2zM4 3a1 1 0 0 1 1-1h6a1 1 0 0 "
            + "1 1 1v2H4zm1 5a2 2 0 0 0-2 2v1H2a1 1 0 0 1-1-1V7a1 1 0 0 1 1-1h12a1 1 0 0 1 1 1v3a1 1 0 0 1-1 "
            + "1h-1v-1a2 2 0 0 0-2-2zm7 2v3a1 1 0 0 1-1 1H5a1 1 0 0 1-1-1v-3a1 1 0 0 1 1-1h6a1 1 0 0 1 1 1",
            "bold",
            "M8.21 13c2.106 0 3.412-1.087 3.412-2.823 0-1.306-.984-2.283-2.324-2.469v-.055c1.144-.18 2.059-1.036 2"
            + ".059-2.23 0-1.467-1.253-2.434-3.14-2.434H3.5v10zm-.848-8.973c1.13 0 1.75.527 1.75 1.335 0 .96-.626 1"
            + ".474-1.727 1.474h-2.35V4.027zM4.737 11.852V8.4h2.522c1.242 0 1.942.54 1.942 1.436 0 1.071-.845 1.503-1"
            + ".887 1.503z",
            "italic",
            "M7.991 11.674 9.53 4.455c.123-.595.246-.71 1.347-.807l.11-.52H5.309l-.11.52c1.148.105 1.274.203 1.149"
            + ".808L4.812 11.674c-.123.595-.246.71-1.347.807l-.11.52h5.132l.11-.52c-1.148-.105-1.274-.203-1.149-.808",
            "image",
            "M6.002 5.5a1.5 1.5 0 1 1-3 0 1.5 1.5 0 0 1 3 0M2.002 1a2 2 0 0 0-2 2v10a2 2 0 0 0 2 2h12a2 2 0 0 0 "
            + "2-2V3a2 2 0 0 0-2-2h-12zm12 1a1 1 0 0 1 1 1v6.5l-3.777-1.947a.5.5 0 0 0-.577.093l-3.71 3.71-2.66-1"
            + ".772a.5.5 0 0 0-.63.062L1.002 12V3a1 1 0 0 1 1-1h12z",
            "table",
            "M0 2a2 2 0 0 1 2-2h12a2 2 0 0 1 2 2v12a2 2 0 0 1-2 2H2a2 2 0 0 1-2-2V2zm15 2h-4v3h4V4zm0 4h-4v3h4V8zm0 "
            + "4h-4v3h4v-3zM5 4H1v3h4V4zm10-3H2a1 1 0 0 0-1 1v1h14V2a1 1 0 0 0-1-1zm-9 3H6v3h4V4zm0 4H6v3h4V8zm0 "
            + "4H6v3h4v-3zM5 8H1v3h4V8zm0 4H1v3h4v-3z",
            "link",
            "M4.715 6.542 3.343 7.914a3 3 0 1 0 4.242 4.242l1.834-1.834a3 3 0 0 0 1.238-2.459 3.07 3.07 0 0 0-.108-1"
            + ".075 1.335 1.335 0 1 1 2.568-.707 5.07 5.07 0 0 1 .18 1.777 5 5 0 0 1-2.064 4.098l-1.834 1.834a5 5 0 1"
            + " 1-7.07-7.07l1.372-1.372a1.335 1.335 0 0 1 2.568.707zm5.996 2.916 1.372-1.372a3 3 0 1 0-4.242-4.242L8"
            + ".007 5.678a3 3 0 0 0-1.238 2.459c.046.368.135.727.268 1.069a1.335 1.335 0 1 1-2.502.917 5.07 5.07 0 0 "
            + "1-.412-1.609 5 5 0 0 1 2.064-4.098l1.834-1.834a5 5 0 0 1 7.07 7.07l-1.372 1.372a1.335 1.335 0 0 1-2"
            + ".568-.707z",
            "undo",
            "M8.48 10.97a.75.75 0 0 1 0 1.06L7.56 13h1.44a4 4 0 0 0 4-4V3.5a.75.75 0 1 0-1.5 0V9a2.5 2.5 0 0 1-2.5 2"
            + ".5H7.56l.92.97a.75.75 0 0 1-1.06 1.06l-2.25-2.25a.75.75 0 0 1 0-1.06l2.25-2.25a.75.75 0 0 1 1.06 0z",
            "redo",
            "M7.52 10.97a.75.75 0 0 0 0 1.06l.92.97H7a4 4 0 0 1-4-4V3.5a.75.75 0 0 1 1.5 0V9A2.5 2.5 0 0 0 7 11.5h1"
            + ".44l-.92.97a.75.75 0 0 0 1.06 1.06l2.25-2.25a.75.75 0 0 0 0-1.06l-2.25-2.25a.75.75 0 0 0-1.06 0z");

    private AsciiDocIcon() {
        throw new UnsupportedOperationException("Utility class");
    }

    /// Appends an icon to the SVG builder.
    ///
    /// @param svg SVG builder
    /// @param name icon name
    /// @param x x coordinate
    /// @param y y coordinate
    /// @param size icon size (width and height)
    /// @param color icon color
    static void appendIcon(StringBuilder svg, String name, int x, int y, int size, String color) {
        String path = getPath(name);
        if (path == null) return;

        svg.append(String.format(Locale.ROOT, "<g transform=\"translate(%d, %d) scale(%f)\">\n", x, y, size / 16.0));
        svg.append(String.format(Locale.ROOT, "<path d=\"%s\" fill=\"%s\"/>\n", path, color));
        svg.append("</g>\n");
    }

    /// Returns the SVG path for the given icon name.
    ///
    /// @param name icon name
    ///
    /// @return SVG path
    static String getPath(String name) {
        return PATHS.get(name);
    }
}
