package pro.verron.officestamper.asciidoc;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static pro.verron.officestamper.asciidoc.AsciiDocModel.*;

/// Minimal AsciiDoc parser producing an [AsciiDocModel].
///
/// Supported subset:
///  - Headings: lines starting with 1..6 '=' followed by a space
///  - Paragraphs: blocks of non-empty lines separated by blank lines
///  - Inline emphasis: <code>*bold*</code> and <code>_italic_</code>
public final class AsciiDocParser {

    private static final Pattern HEADING = Pattern.compile("^(={1,6})\\s+(.*)$");

    private AsciiDocParser() {
        // utility
    }

    /// Parses the given AsciiDoc string into a model.
    ///
    /// @param asciidoc source text
    ///
    /// @return parsed model
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
        // Stack-based inline parser with simple tokens for '*', '_', text, and escapes.
        // Non-overlapping nesting is allowed; crossing markers are treated as plain text.
        var root = new Frame(FrameType.ROOT);
        var stack = new ArrayList<Frame>();
        stack.add(root);

        if (text == null || text.isEmpty()) {
            return root.children;
        }

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);

            // Escapes for '*', '_', and '\\'
            if (c == '\\') {
                if (i + 1 < text.length()) {
                    char next = text.charAt(i + 1);
                    if (next == '*' || next == '_' || next == '\\') {
                        stack.getLast().text.append(next);
                        i++;
                        continue;
                    }
                }
                // Lone backslash
                stack.getLast().text.append(c);
                continue;
            }

            if (c == '*' || c == '_') {
                FrameType type = (c == '*') ? FrameType.BOLD : FrameType.ITALIC;
                Frame top = stack.getLast();
                if (top.type == type) {
                    // Close current frame
                    top.flushTextToChildren();
                    Inline node = (type == FrameType.BOLD) ? new Bold(top.children) : new Italic(top.children);
                    stack.removeLast();
                    Frame parent = stack.getLast();
                    parent.children.add(node);
                }
                else if (top.type == FrameType.BOLD || top.type == FrameType.ITALIC || top.type == FrameType.ROOT) {
                    // Open new frame
                    Frame f = new Frame(type);
                    stack.add(f);
                }
                else {
                    // Should not happen
                    stack.getLast().text.append(c);
                }
                continue;
            }

            // Regular char
            stack.getLast().text.append(c);
        }

        // Unwind: any unclosed frames become literal markers + content as plain text in parent
        while (stack.size() > 1) {
            Frame unfinished = stack.removeLast();
            char marker = unfinished.type == FrameType.BOLD ? '*' : '_';
            unfinished.flushTextToChildren();
            // Build literal: marker + children as text + (no closing marker since it is missing)
            StringBuilder literal = new StringBuilder();
            literal.append(marker);
            for (Inline in : unfinished.children) {
                literal.append(in.text());
            }
            stack.getLast().text.append(literal);
        }

        // Flush remainder text on root
        root.flushTextToChildren();
        return root.children;
    }

    private enum FrameType {
        ROOT,
        BOLD,
        ITALIC
    }

    private static final class Frame {
        final FrameType type;
        final List<Inline> children = new ArrayList<>();
        final StringBuilder text = new StringBuilder();

        Frame(FrameType type) {this.type = type;}

        void flushTextToChildren() {
            if (!text.isEmpty()) {
                children.add(new Text(text.toString()));
                text.setLength(0);
            }
        }
    }
}
