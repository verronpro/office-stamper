package pro.verron.officestamper.asciidoc;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static pro.verron.officestamper.asciidoc.AsciiDocModel.*;

/// The [AsciiDocParser] class is a utility for parsing AsciiDoc-formatted text
/// and transforming it into structured models. It provides both static and instance-based
/// parsing capabilities and is designed to work with inline structures within the text.
/// ## Superclasses
/// This class extends the following superclasses:
/// - [Object]
/// - [Function]
///
/// ## Methods
///   - [#parse(String)]: A static method to parse an AsciiDoc string into an [AsciiDocModel].
///   - [#apply(String)]: An instance method implementing the [Function] interface for parsing an AsciiDoc string
/// into an [AsciiDocModel].
///
/// ## Internal Behavior
///   - [#parseInlines(String)]: A private static helper method to parse inline elements from a given text input.
public final class AsciiDocParser
        implements Function<String, AsciiDocModel> {

    /// Parses the given AsciiDoc string and produces an [AsciiDocModel] representation.
    ///
    /// @param asciidoc the AsciiDoc content to be parsed
    /// @return an [AsciiDocModel] representing the parsed structure of the input AsciiDoc
    public static AsciiDocModel parse(String asciidoc) {
        return new AsciiDocParser().apply(asciidoc);
    }

    /// Processes an AsciiDoc-formatted string and converts it into an [AsciiDocModel]
    /// representation containing structured blocks such as paragraphs, headings,
    /// lists, tables, images, code blocks, and blockquotes.
    ///
    /// @param asciidoc the AsciiDoc-formatted input string. It may include various types of
    ///                 blocks (e.g., paragraphs, headings, lists, tables, etc.) and
    ///                 formatting constructs.
    /// @return an [AsciiDocModel] object representing the parsed blocks and their
    ///         structure in the input string. Returns an empty model if the input is blank.
    public AsciiDocModel apply(String asciidoc) {
        if (asciidoc.isBlank()) return AsciiDocModel.of(new ArrayList<>());

        String[] lines = asciidoc.split("\r?\n");
        StringBuilder currentParagraph = new StringBuilder();
        boolean inTable = false;
        boolean inBlockquote = false;
        boolean inCodeBlock = false;
        String currentLanguage = "";
        List<Row> currentTableRows = new ArrayList<>();
        StringBuilder currentBlockContent = new StringBuilder();

        var blocks = new ArrayList<Block>();
        for (String line : lines) {
            String trimmed = line.trim();

            if (trimmed.equals("____")) {
                if (inBlockquote) {
                    blocks.add(new Blockquote(parseInlines(currentBlockContent.toString()
                                                                              .trim())));
                    currentBlockContent.setLength(0);
                    inBlockquote = false;
                }
                else {
                    if (!currentParagraph.isEmpty()) {
                        blocks.add(new Paragraph(parseInlines(currentParagraph.toString()
                                                                              .trim())));
                        currentParagraph.setLength(0);
                    }
                    inBlockquote = true;
                }
                continue;
            }

            if (inBlockquote) {
                if (!currentBlockContent.isEmpty()) {
                    currentBlockContent.append(" ");
                }
                currentBlockContent.append(trimmed);
                continue;
            }

            if (trimmed.startsWith("[source")) {
                int commaIndex = trimmed.indexOf(',');
                if (commaIndex != -1) {
                    int bracketIndex = trimmed.indexOf(']');
                    currentLanguage = trimmed.substring(commaIndex + 1, bracketIndex)
                                             .trim();
                }
                continue;
            }

            if (trimmed.equals("----")) {
                if (inCodeBlock) {
                    blocks.add(new CodeBlock(currentLanguage,
                            currentBlockContent.toString()
                                               .trim()));
                    currentBlockContent.setLength(0);
                    currentLanguage = "";
                    inCodeBlock = false;
                }
                else {
                    if (!currentParagraph.isEmpty()) {
                        blocks.add(new Paragraph(parseInlines(currentParagraph.toString()
                                                                              .trim())));
                        currentParagraph.setLength(0);
                    }
                    inCodeBlock = true;
                }
                continue;
            }

            if (inCodeBlock) {
                if (!currentBlockContent.isEmpty()) {
                    currentBlockContent.append("\n");
                }
                currentBlockContent.append(line); // Preserve indentation in code blocks
                continue;
            }

            if (trimmed.startsWith("image::")) {
                if (!currentParagraph.isEmpty()) {
                    blocks.add(new Paragraph(parseInlines(currentParagraph.toString()
                                                                          .trim())));
                    currentParagraph.setLength(0);
                }
                int endUrl = trimmed.indexOf('[', 7);
                if (endUrl != -1) {
                    int endText = trimmed.indexOf(']', endUrl);
                    if (endText != -1) {
                        String url = trimmed.substring(7, endUrl);
                        String altText = trimmed.substring(endUrl + 1, endText);
                        blocks.add(new ImageBlock(url, altText));
                        continue;
                    }
                }
            }

            if (trimmed.equals("|===")) {
                if (inTable) {
                    blocks.add(new Table(currentTableRows));
                    currentTableRows = new ArrayList<>();
                    inTable = false;
                }
                else {
                    if (!currentParagraph.isEmpty()) {
                        blocks.add(new Paragraph(parseInlines(currentParagraph.toString()
                                                                              .trim())));
                        currentParagraph.setLength(0);
                    }
                    inTable = true;
                }
                continue;
            }

            if (inTable) {
                if (trimmed.startsWith("|")) {
                    String[] cellTexts = trimmed.substring(1)
                                                .split("\\|");
                    List<Cell> cells = new ArrayList<>();
                    for (String cellText : cellTexts) {
                        cells.add(Cell.ofInlines(parseInlines(cellText.trim())));
                    }
                    currentTableRows.add(new Row(cells));
                }
                continue;
            }

            if (trimmed.isBlank()) {
                if (!currentParagraph.isEmpty()) {
                    blocks.add(new Paragraph(parseInlines(currentParagraph.toString()
                                                                          .trim())));
                    currentParagraph.setLength(0);
                }
                continue;
            }

            // Check for Headings
            if (trimmed.startsWith("=")) {
                int level = 0;
                while (level < trimmed.length() && trimmed.charAt(level) == '=') {
                    level++;
                }
                if (level > 0 && level <= 6 && level < trimmed.length()
                    && Character.isWhitespace(trimmed.charAt(level))) {
                    if (!currentParagraph.isEmpty()) {
                        blocks.add(new Paragraph(parseInlines(currentParagraph.toString()
                                                                              .trim())));
                        currentParagraph.setLength(0);
                    }
                    String title = trimmed.substring(level)
                                          .trim();
                    blocks.add(new Heading(level, parseInlines(title)));
                    continue;
                }
            }

            // Check for Unordered List Item
            if (trimmed.startsWith("* ")) {
                if (!currentParagraph.isEmpty()) {
                    blocks.add(new Paragraph(parseInlines(currentParagraph.toString()
                                                                          .trim())));
                    currentParagraph.setLength(0);
                }
                String itemText = trimmed.substring(2)
                                         .trim();
                var item = new ListItem(parseInlines(itemText));
                if (!blocks.isEmpty() && blocks.getLast() instanceof UnorderedList(List<ListItem> items1)) {
                    List<ListItem> items = new ArrayList<>(items1);
                    items.add(item);
                    blocks.set(blocks.size() - 1, new UnorderedList(items));
                }
                else {
                    blocks.add(new UnorderedList(List.of(item)));
                }
                continue;
            }

            // Check for Ordered List Item
            if (trimmed.startsWith(". ")) {
                if (!currentParagraph.isEmpty()) {
                    blocks.add(new Paragraph(parseInlines(currentParagraph.toString()
                                                                          .trim())));
                    currentParagraph.setLength(0);
                }
                String itemText = trimmed.substring(2)
                                         .trim();
                var item = new ListItem(parseInlines(itemText));
                if (!blocks.isEmpty() && blocks.getLast() instanceof OrderedList(List<ListItem> items1)) {
                    List<ListItem> items = new ArrayList<>(items1);
                    items.add(item);
                    blocks.set(blocks.size() - 1, new OrderedList(items));
                }
                else {
                    blocks.add(new OrderedList(List.of(item)));
                }
                continue;
            }

            // Otherwise, it's a paragraph part
            if (!currentParagraph.isEmpty()) {
                currentParagraph.append("\n");
            }
            currentParagraph.append(trimmed);
        }

        if (!currentParagraph.isEmpty()) {
            blocks.add(new Paragraph(parseInlines(currentParagraph.toString()
                                                                  .trim())));
        }

        return AsciiDocModel.of(blocks);
    }

    private static List<Inline> parseInlines(String text) {
        // Stack-based inline parser with simple tokens for '*', '_', text, and escapes.
        // Non-overlapping nesting is allowed; crossing markers are treated as plain text.
        var root = new Frame(FrameType.ROOT);
        var stack = new ArrayList<Frame>();
        stack.add(root);

        if (text.isEmpty()) return root.children;

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
                    top.flushTextToChildren();
                    Frame f = new Frame(type);
                    stack.add(f);
                }
                else {
                    // Should not happen
                    stack.getLast().text.append(c);
                }
                continue;
            }

            // Detect literal |TAB| token -> emit a Tab inline
            if (c == '|' && i + 4 < text.length() && text.charAt(i + 1) == 'T' && text.charAt(i + 2) == 'A'
                && text.charAt(i + 3) == 'B' && text.charAt(i + 4) == '|') {
                // Flush any pending text
                stack.getLast()
                     .flushTextToChildren();
                stack.getLast().children.add(new Tab());
                i += 4;
                continue;
            }

            // Simple Link detection: https://example.com[Text]
            if (c == 'h' && text.startsWith("http", i)) {
                int endUrl = text.indexOf('[', i);
                if (endUrl != -1) {
                    int endText = text.indexOf(']', endUrl);
                    if (endText != -1) {
                        stack.getLast()
                             .flushTextToChildren();
                        String url = text.substring(i, endUrl);
                        String linkText = text.substring(endUrl + 1, endText);
                        stack.getLast().children.add(new Link(url, linkText));
                        i = endText;
                        continue;
                    }
                }
            }

            // Simple Image detection: image:url[AltText]
            if (c == 'i' && text.startsWith("image:", i)) {
                int endUrl = text.indexOf('[', i + 6);
                if (endUrl != -1) {
                    int endText = text.indexOf(']', endUrl);
                    if (endText != -1) {
                        stack.getLast()
                             .flushTextToChildren();
                        String url = text.substring(i + 6, endUrl);
                        String title = text.substring(endUrl + 1, endText);
                        stack.getLast().children.add(new InlineImage(url, Map.of("title", title)));
                        i = endText;
                        continue;
                    }
                }
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
