package pro.verron.officestamper.asciidoc.converters;

import java.awt.*;

public enum Theme {
    WORD,
    GDOCS,
    LIBRE;

    String getHighlightColor() {
        return switch (this) {
            case WORD -> "#fff2cc";
            case GDOCS -> "#c2e7ff";
            case LIBRE -> "#ffff00";
        };
    }

    // Background
    String getBgColor() {
        return switch (this) {
            case WORD -> "#e6e6e6";
            case GDOCS -> "#f8f9fa";
            case LIBRE -> "#dfdfdf";
        };
    }

    String getAwtFallbacks() {
        return switch (this) {
            case WORD, GDOCS -> Font.SANS_SERIF;
            case LIBRE -> Font.SERIF;
        };
    }

    /// Returns the SVG font-family string for a given theme.
    ///
    /// @return font-family string
    String getFontFamily() {
        return switch (this) {
            case WORD -> "Calibri, 'Carlito', 'Arial', sans-serif";
            case GDOCS -> "Arial, 'Arimo', sans-serif";
            case LIBRE -> "'Liberation Serif', 'Tinos', 'Times New Roman', serif";
        };
    }

    String getPrimaryFont() {
        return switch (this) {
            case WORD -> "Calibri";
            case GDOCS -> "Arial";
            case LIBRE -> "Liberation Serif";
        };
    }
}
