package pro.verron.officestamper.asciidoc.converters;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/// Utility class for font management in AsciiDoc previews.
final class AsciiDocFont {
    private static final Map<String, Font> FONT_CACHE = new HashMap<>();

    private AsciiDocFont() {
        throw new UnsupportedOperationException("Utility class");
    }

    /// Returns an AWT Font for metrics calculations.
    ///
    /// @param theme target editor theme
    /// @param fontSize font size
    /// @param weight font weight (e.g. 400, 700)
    /// @return AWT Font
    static Font getAwtFont(Theme theme, int fontSize, int weight) {
        var key = String.format("%s-%d-%d", theme, fontSize, weight);
        return FONT_CACHE.computeIfAbsent(key, _ -> createFont(theme, fontSize, weight));
    }

    private static Font createFont(Theme theme, int fontSize, int weight) {
        var primaryFont = theme.getPrimaryFont();
        var style = (weight >= 700) ? Font.BOLD : Font.PLAIN;
        var font = new Font(primaryFont, style, fontSize);

        // Check if font is actually loaded, if not use fallback
        return Font.DIALOG.equals(font.getFamily()) ? new Font(theme.getAwtFallbacks(), style, fontSize) : font;
    }

}
