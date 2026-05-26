package pro.verron.officestamper.asciidoc.converters;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/// Utility class for text measurement and wrapping in AsciiDoc previews.
final class AsciiDocMetrics {

    private static final Graphics2D GRAPHICS;

    static {
        BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        GRAPHICS = image.createGraphics();
    }

    private AsciiDocMetrics() {
        throw new UnsupportedOperationException("Utility class");
    }

    /// Wraps text to fit within a maximum width.
    ///
    /// @param text text to wrap
    /// @param font font used for measurement
    /// @param maxWidth maximum width in pixels
    ///
    /// @return list of wrapped lines
    static List<String> wrapText(String text, Font font, int maxWidth) {
        if (text == null || text.isEmpty()) return List.of("");

        FontMetrics metrics = GRAPHICS.getFontMetrics(font);
        List<String> lines = new ArrayList<>();
        String[] words = text.split("\\s+");
        StringBuilder currentLine = new StringBuilder();

        for (String word : words) {
            String testLine = currentLine.isEmpty() ? word : currentLine + " " + word;
            if (metrics.stringWidth(testLine) <= maxWidth) {
                if (!currentLine.isEmpty()) currentLine.append(" ");
                currentLine.append(word);
            }
            else {
                if (!currentLine.isEmpty()) {
                    lines.add(currentLine.toString());
                    currentLine = new StringBuilder(word);
                }
                else {
                    // Word itself is longer than maxWidth, force it
                    lines.add(word);
                    currentLine = new StringBuilder();
                }
            }
        }
        if (!currentLine.isEmpty()) {
            lines.add(currentLine.toString());
        }

        return lines.isEmpty() ? List.of("") : lines;
    }
}
