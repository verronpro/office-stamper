package pro.verron.officestamper.asciidoc;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.Options;
import org.asciidoctor.ast.Document;
import org.asciidoctor.ast.Section;
import org.asciidoctor.ast.StructuralNode;

import java.util.ArrayList;
import java.util.List;

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
    ///  - Section levels are offset by +1 when a document title is present to preserve the perceived hierarchy
    ///    of the previous homemade parser where "= Title" was treated as a heading, not a special header.
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

            boolean hasHeader = doc.getDoctitle() != null && !doc.getDoctitle()
                                                                 .isBlank();
            if (hasHeader) {
                blocks.add(new AsciiDocModel.Heading(1, parseInlines(doc.getDoctitle())));
            }

            for (StructuralNode child : doc.getBlocks()) {
                traverse(child, blocks, hasHeader ? 1 : 0);
            }
        }

        return AsciiDocModel.of(blocks);
    }

    private static void traverse(StructuralNode node, List<AsciiDocModel.Block> out, int levelOffset) {
        String context = node.getContext();
        if (node instanceof Section section) {
            int lvl = section.getLevel() + levelOffset;
            if (lvl >= 1 && lvl <= 6) {
                out.add(new AsciiDocModel.Heading(lvl, parseInlines(section.getTitle())));
            }
            for (StructuralNode b : section.getBlocks()) {
                traverse(b, out, levelOffset);
            }
            return;
        }

        if ("table".equals(context)) {
            List<AsciiDocModel.Row> rows = extractTableRowsViaReflection(node);
            if (!rows.isEmpty()) {
                out.add(new AsciiDocModel.Table(rows));
                return;
            }
            // If extraction failed, continue traversal into children to salvage paragraphs
        }

        if ("paragraph".equals(context)) {
            // Prefer the AST-provided paragraph text
            Object content = node.getContent();
            String text;
            if (content == null) {
                text = "";
            }
            else if (content instanceof String s) {
                text = s.trim();
            }
            else if (content instanceof List<?> list) {
                text = list.stream()
                           .map(Object::toString)
                           .reduce("", (a, b) -> a.isEmpty() ? b : a + " " + b)
                           .trim();
            }
            else {
                text = content.toString()
                              .trim();
            }
            if (!text.isEmpty()) {
                out.add(new AsciiDocModel.Paragraph(parseInlines(text)));
            }
        }
        else {
            // Recurse into other container nodes to keep paragraphs found within
            List<StructuralNode> children = node.getBlocks();
            if (children != null) {
                for (StructuralNode c : children) traverse(c, out, levelOffset);
            }
        }
    }

    private static AsciiDocModel.Row convertRowFromCells(List<String> cellTexts) {
        List<AsciiDocModel.Cell> cells = new ArrayList<>();
        for (String raw : cellTexts) {
            String txt = raw == null ? "" : raw;
            cells.add(new AsciiDocModel.Cell(parseInlines(txt)));
        }
        return new AsciiDocModel.Row(cells);
    }

    @SuppressWarnings("unchecked")
    private static List<AsciiDocModel.Row> extractTableRowsViaReflection(StructuralNode tableNode) {
        List<AsciiDocModel.Row> out = new ArrayList<>();
        try {
            Object content = tableNode.getContent();
            if (content != null) {
                Object rowsObj = invokeNoArg(content, "getRows");
                if (rowsObj != null) {
                    List<?> head = (List<?>) invokeNoArg(rowsObj, "getHead");
                    if (head != null) {
                        for (Object r : head) out.add(convertRowReflective(r));
                    }
                    List<?> body = (List<?>) invokeNoArg(rowsObj, "getBody");
                    if (body != null) {
                        for (Object r : body) out.add(convertRowReflective(r));
                    }
                }
            }
        } catch (Throwable ignore) {
            // fallback below
        }

        if (!out.isEmpty()) return out;

        // Fallback: best-effort traversal of children nodes for rows/cells
        List<StructuralNode> children = tableNode.getBlocks();
        if (children != null) {
            for (StructuralNode rowNode : children) {
                String ctx = rowNode.getContext();
                if (ctx != null && ctx.contains("row")) {
                    List<String> cells = new ArrayList<>();
                    List<StructuralNode> cellNodes = rowNode.getBlocks();
                    if (cellNodes != null) {
                        for (StructuralNode cellNode : cellNodes) {
                            Object cContent = cellNode.getContent();
                            cells.add(cContent == null ? "" : cContent.toString());
                        }
                    }
                    if (!cells.isEmpty()) out.add(convertRowFromCells(cells));
                }
            }
        }
        return out;
    }

    private static AsciiDocModel.Row convertRowReflective(Object rowObj) {
        try {
            List<?> cells = (List<?>) invokeNoArg(rowObj, "getCells");
            List<String> texts = new ArrayList<>();
            if (cells != null) {
                for (Object c : cells) {
                    String txt = (String) invokeNoArg(c, "getText");
                    if (txt == null || txt.isEmpty()) txt = (String) invokeNoArg(c, "getSource");
                    texts.add(txt == null ? "" : txt);
                }
            }
            return convertRowFromCells(texts);
        } catch (Throwable e) {
            return convertRowFromCells(List.of());
        }
    }

    private static Object invokeNoArg(Object target, String method)
            throws Exception {
        var m = target.getClass()
                      .getMethod(method);
        m.setAccessible(true);
        return m.invoke(target);
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
