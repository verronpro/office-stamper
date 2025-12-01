package pro.verron.officestamper.asciidoc;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static pro.verron.officestamper.asciidoc.AsciiDocModel.*;

/**
 * Minimal AsciiDoc parser producing an {@link AsciiDocModel}.
 * <p>
 * Supported subset: - Headings: lines starting with 1..6 '=' followed by a space - Paragraphs: blocks of non-empty
 * lines separated by blank lines - Inline emphasis: <code>*bold*</code> and <code>_italic_</code>
 */
public final class AsciiDocParser {

    private static final Pattern HEADING = Pattern.compile("^(={1,6})\\s+(.*)$");

    private AsciiDocParser() {
        // utility
    }

    /**
     * Parses the given AsciiDoc string into a model.
     *
     * @param asciidoc source text
     *
     * @return parsed model
     */
    public static AsciiDocModel parse(String asciidoc) {
        var blocks = new ArrayList<Block>();
        if (asciidoc == null || asciidoc.isBlank()) {
            return AsciiDocModel.of(blocks);
        }

        var lines = asciidoc.replace("\r\n", "\n")
                            .replace('\r', '\n')
                            .split("\n");
        var paragraphBuffer = new ArrayList<String>();

        for (String raw : lines) {
            String line = raw.stripTrailing();
            Matcher h = HEADING.matcher(line);
            if (h.matches()) {
                flushParagraph(blocks, paragraphBuffer);
                int level = h.group(1)
                             .length();
                String title = h.group(2)
                                .trim();
                blocks.add(new Heading(level, parseInlines(title)));
                continue;
            }

            if (line.isBlank()) {
                flushParagraph(blocks, paragraphBuffer);
            }
            else {
                paragraphBuffer.add(line);
            }
        }

        flushParagraph(blocks, paragraphBuffer);
        return AsciiDocModel.of(blocks);
    }

    private static void flushParagraph(List<Block> blocks, List<String> buffer) {
        if (buffer.isEmpty()) return;
        String joined = String.join(" ", buffer)
                              .trim();
        if (!joined.isEmpty()) {
            blocks.add(new Paragraph(parseInlines(joined)));
        }
        buffer.clear();
    }

    private static List<Inline> parseInlines(String text) {
        var out = new ArrayList<Inline>();

        if (text == null || text.isEmpty()) {
            return out;
        }

        // Tokenization + single-delimiter stack (foundation for nesting in future work).
        // Current model supports only flat Bold/Italic(String), so we prohibit nesting for now.
        StringBuilder plain = new StringBuilder();
        char openDelim = 0; // '*' or '_' when an inline run is open, otherwise 0
        StringBuilder runBuffer = null; // collects characters inside the current inline run

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);

            // Handle escapes for *, _, and \
            if (c == '\\') {
                if (i + 1 < text.length()) {
                    char next = text.charAt(i + 1);
                    if (next == '*' || next == '_' || next == '\\') {
                        appendChar(openDelim, plain, runBuffer, next);
                        i++; // consume next
                        continue;
                    }
                }
                // Lone backslash, treat literally
                appendChar(openDelim, plain, runBuffer, c);
                continue;
            }

            if (c == '*' || c == '_') {
                if (openDelim == 0) {
                    // Opening a new run. Flush plain text first.
                    flushPlain(out, plain);
                    openDelim = c;
                    runBuffer = new StringBuilder();
                }
                else if (c == openDelim) {
                    // Closing the current run
                    String content = runBuffer.toString();
                    if (openDelim == '*') {
                        out.add(new Bold(content));
                    }
                    else {
                        out.add(new Italic(content));
                    }
                    openDelim = 0;
                    runBuffer = null;
                }
                else {
                    // Different marker inside a run: treat as literal for now
                    runBuffer.append(c);
                }
                continue;
            }

            // Regular character
            appendChar(openDelim, plain, runBuffer, c);
        }

        // If an inline run is still open, treat it as literal text (error tolerance)
        if (openDelim != 0) {
            // Emit the opening marker and its content as plain text
            plain.append(openDelim)
                 .append(runBuffer);
        }

        flushPlain(out, plain);
        return out;
    }

    private static void appendChar(char openDelim, StringBuilder plain, StringBuilder runBuffer, char c) {
        if (openDelim == 0) {
            plain.append(c);
        }
        else {
            runBuffer.append(c);
        }
    }

    private static void flushPlain(List<Inline> out, StringBuilder plain) {
        if (plain.length() > 0) {
            out.add(new Text(plain.toString()));
            plain.setLength(0);
        }
    }
}
