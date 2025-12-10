package pro.verron.officestamper.asciidoc;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.Options;
import org.asciidoctor.ast.*;
import org.asciidoctor.ast.Block;
import org.asciidoctor.ast.Cell;
import org.asciidoctor.ast.Row;
import org.asciidoctor.ast.Table;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import static pro.verron.officestamper.asciidoc.AsciiDocModel.*;

/// Parser based on AsciidoctorJ producing an [AsciiDocModel].
///
/// Supported subset mapped into our model:
///  - Headings: document title (if present) and sections (levels 1..6)
///  - Paragraphs: paragraph blocks
///  - Inline emphasis: <code>*bold*</code> and <code>_italic_</code> via a lightweight inline parser
public final class AsciiDocParser {

    private AsciiDocParser() {
        // utility
    }

    /// Parses the given AsciiDoc string into a model using AsciidoctorJ AST traversal.
    ///
    /// Notes:
    ///  - If the document has a header/title (e.g. a leading "= Title"), it is emitted as a level-1 Heading.
    ///  - Section levels are offset by +1 when a document title is present to preserve the perceived hierarchy of the
    /// previous homemade parser where "= Title" was treated as a heading, not a special header.
    ///
    /// @param asciidoc source text
    ///
    /// @return parsed model
    public static AsciiDocModel parse(String asciidoc) {
        var blocks = new ArrayList<AsciiDocModel.Block>();
        if (asciidoc == null || asciidoc.isBlank()) {
            return AsciiDocModel.of(blocks);
        }

        try (Asciidoctor engine = Asciidoctor.Factory.create()) {
            Options options = Options.builder()
                                     .sourcemap(true)
                                     .build();
            Document doc = engine.load(asciidoc, options);

            for (StructuralNode child : doc.getBlocks()) {
                traverse(child, blocks);
            }
        }

        return AsciiDocModel.of(blocks);
    }

    private static void traverse(StructuralNode node, List<AsciiDocModel.Block> out) {
        switch (node) {
            case Section section -> {
                int lvl = section.getLevel();
                if (lvl >= 1 && lvl <= 6) {
                    out.add(new Heading(lvl, parseInlines(section.getTitle())));
                }
                for (StructuralNode b : section.getBlocks()) {
                    traverse(b, out);
                }
            }
            case Table table -> {
                List<AsciiDocModel.Row> rows = extractTableRowsViaReflection(table);
                if (!rows.isEmpty()) {
                    out.add(new AsciiDocModel.Table(rows));
                }
                // If extraction failed, continue traversal into children to salvage paragraphs
            }
            case PhraseNode phraseNode -> out.add(new Paragraph(parseInlines(phraseNode.getText())));
            case Block block when "simple".equals(block.getContentModel()) ->
                    out.add(new Paragraph(parseInlines(String.join("\n", block.getLines()))));
            default -> {
                // Recurse into other container nodes to keep paragraphs found within
                List<StructuralNode> children = node.getBlocks();
                if (children != null) {
                    for (StructuralNode c : children) traverse(c, out);
                }
            }
        }
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

    private static List<AsciiDocModel.Row> extractTableRowsViaReflection(Table table) {
        var header = table.getHeader()
                          .stream()
                          .map(AsciiDocParser::convertRowReflective)
                          .toList();
        var body = table.getBody()
                        .stream()
                        .map(AsciiDocParser::convertRowReflective)
                        .toList();
        var footer = table.getFooter()
                          .stream()
                          .map(AsciiDocParser::convertRowReflective)
                          .toList();
        return Stream.of(header, body, footer)
                     .flatMap(Collection::stream)
                     .toList();
    }

    private static AsciiDocModel.Row convertRowReflective(Row row) {

        return new AsciiDocModel.Row(row.getCells()
                                        .stream()
                                        .map(AsciiDocParser::convertCell)
                                        .toList());

    }

    private static AsciiDocModel.Cell convertCell(Cell cell) {
        return new AsciiDocModel.Cell(parseInlines(cell.getText()));
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
