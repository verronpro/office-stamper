package pro.verron.officestamper.asciidoc.converters;

import java.awt.*;
import java.util.Map;

/// Utility class for font management in AsciiDoc previews.
final class AsciiDocFont {

    private static final Map<AsciiDocToSvg.Theme, String> THEME_FONTS = Map.of(
            AsciiDocToSvg.Theme.WORD, "Calibri, 'Carlito', 'Arial', sans-serif",
            AsciiDocToSvg.Theme.GDOCS, "Arial, 'Arimo', sans-serif",
            AsciiDocToSvg.Theme.LIBRE, "'Liberation Serif', 'Tinos', 'Times New Roman', serif"
    );

    private static final Map<AsciiDocToSvg.Theme, String> AWT_FALLBACKS = Map.of(
            AsciiDocToSvg.Theme.WORD, Font.SANS_SERIF,
            AsciiDocToSvg.Theme.GDOCS, Font.SANS_SERIF,
            AsciiDocToSvg.Theme.LIBRE, Font.SERIF
    );

    private AsciiDocFont() {
        throw new UnsupportedOperationException("Utility class");
    }

    /// Returns the SVG font-family string for a given theme.
    ///
    /// @param theme target editor theme
    ///
    /// @return font-family string
    static String getFontFamily(AsciiDocToSvg.Theme theme) {
        return THEME_FONTS.get(theme);
    }

    /// Returns an AWT Font for metrics calculations.
    ///
    /// @param theme target editor theme
    /// @param fontSize font size
    /// @param weight font weight (e.g. 400, 700)
    ///
    /// @return AWT Font
    static Font getAwtFont(AsciiDocToSvg.Theme theme, int fontSize, int weight) {
        String primaryFont = switch (theme) {
            case WORD -> "Calibri";
            case GDOCS -> "Arial";
            case LIBRE -> "Liberation Serif";
        };
        int style = (weight >= 700) ? Font.BOLD : Font.PLAIN;
        Font font = new Font(primaryFont, style, fontSize);

        // Check if font is actually loaded, if not use fallback
        if (font.getFamily()
                .equals(Font.DIALOG) && !primaryFont.equalsIgnoreCase("Dialog")) {
            return new Font(AWT_FALLBACKS.get(theme), style, fontSize);
        }
        return font;
    }
}
