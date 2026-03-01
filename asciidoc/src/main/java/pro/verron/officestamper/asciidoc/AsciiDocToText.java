package pro.verron.officestamper.asciidoc;

import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static pro.verron.officestamper.asciidoc.AsciiDocModel.*;

public final class AsciiDocToText
        implements Function<AsciiDocModel, String> {

    private static String renderInlines(List<@NonNull Inline> inlines) {
        var sb = new StringBuilder();
        for (Inline inline : inlines) {
            sb.append(switch (inline) {
                case Text(String text) -> text;
                case Bold(List<Inline> children) -> "*%s*".formatted(renderInlines(children));
                case Italic(List<Inline> children) -> "_%s_".formatted(renderInlines(children));
                case Sup(List<Inline> children) -> "^%s^".formatted(renderInlines(children));
                case Sub(List<Inline> children) -> "~%s~".formatted(renderInlines(children));
                case Tab _ -> sb.append("\t");
                case Link(String url, String text) -> "%s[%s]".formatted(url, text);
                case InlineImage(String path, Map<String, String> map) -> "image:%s[%s]".formatted(path,
                        map.entrySet()
                           .stream()
                           .map(e -> e.getKey() + "=" + e.getValue())
                           .collect(Collectors.joining(", ")));
                case Styled(String role, List<Inline> children) -> "[%s]#%s#".formatted(role, renderInlines(children));
                case InlineMacro(String name, String id, List<String> list) ->
                        "%s:%s[%s]".formatted(name, id, String.join(", ", list));
            });
        }
        return sb.toString();
    }

    private static String renderCellContent(Cell cell, boolean isAsciidoc, int level) {
        var blockList = cell.blocks();
        if (!isAsciidoc) {
            if (blockList.isEmpty()) return "";
            Paragraph p = (Paragraph) blockList.getFirst();
            return renderInlines(p.inlines());
        }
        else {
            return blockList.stream()
                            .map(block -> renderBlock(block, level))
                            .collect(Collectors.joining())
                            .trim();
        }
    }

    private static String renderBlock(Block block, int tableLevel) {
        return switch (block) {
            case Heading(_, int level, List<Inline> inlines) -> renderHeading(level, inlines);
            case Paragraph(List<String> header, List<Inline> inlines) -> renderHeader(header) + renderInlines(inlines);
            case UnorderedList(List<ListItem> items1) -> renderList(items1, "* ");
            case OrderedList(List<ListItem> items) -> renderList(items, ". ");
            case Table(List<Row> rows) -> renderTable(rows, tableLevel);
            case Blockquote(List<Inline> inlines) -> renderBlockquote(inlines);
            case CodeBlock(String language, String content) -> renderCodeBlock(language, content);
            case ImageBlock(String url, String altText) -> renderImageBlock(url, altText);
            case OpenBlock openBlock -> render(openBlock);
            case MacroBlock(String name, String id, List<String> list) ->
                    "%s::%s[%s]".formatted(name, id, String.join(", ", list));
            case Break _ -> "<<<";
            case CommentLine(String comment) -> ("// %s").formatted(comment);
        } + "\n\n";
    }

    private static String render(OpenBlock openBlock) {
        var sb = new StringBuilder();
        sb.append("[%s]\n".formatted(String.join(", ", openBlock.header())));
        sb.append("--\n");
        openBlock.content()
                 .stream()
                 .map(p -> renderBlock(p, 0))
                 .forEach(sb::append);
        sb.append("--\n");
        return sb.toString();
    }

    private static String renderTable(List<Row> rows, int level) {
        var cellDelimiter = switch (level) {
            case 0 -> "|";
            case 1 -> "!";
            default -> throw new IllegalArgumentException("Table nesting level must be between 0 and 1");
        };
        var tableDelimiter = cellDelimiter + "===";
        var sb = new StringBuilder();
        sb.append(tableDelimiter);
        sb.append("\n");
        for (Row row : rows) {
            var style = row.style();
            style.ifPresent(s -> sb.append("[%s]\n".formatted(s)));
            for (Cell cell : row.cells()) {
                var blockList = cell.blocks();
                var size = blockList.size();
                boolean isAsciidoc = size > 1 || (size == 1 && !(blockList.getFirst() instanceof Paragraph));
                cell.style()
                    .ifPresent(s -> sb.append("[%s]\n".formatted(s)));
                sb.append(isAsciidoc ? "a" + cellDelimiter : cellDelimiter)
                  .append(renderCellContent(cell, isAsciidoc, level + 1))
                  .append("\n");
            }
        }
        sb.append(tableDelimiter);
        return sb.toString();
    }

    private static String renderImageBlock(String url, String altText) {
        return "image::" + url + "[" + altText + "]";
    }

    private static String renderCodeBlock(String language, String content) {
        return (language.isEmpty() ? "" : "[source," + language + "]\n") + "----\n" + content + "\n----";
    }

    private static String renderBlockquote(List<Inline> inlines) {
        return "____\n" + renderInlines(inlines) + "\n____";
    }

    private static String renderList(List<ListItem> items1, String x) {
        return items1.stream()
                     .map(item -> x + renderInlines(item.inlines()) + "\n")
                     .collect(Collectors.joining("\n"));
    }

    private static String renderHeading(int level, List<Inline> inlines) {
        return "=".repeat(level) + " " + renderInlines(inlines);
    }

    private static String renderHeader(List<String> header) {
        if (header.isEmpty()) return "";
        return "[%s]\n".formatted(String.join(", ", header));
    }

    public String apply(AsciiDocModel model) {
        return model.getBlocks()
                    .stream()
                    .map((Block block) -> renderBlock(block, 0))
                    .collect(Collectors.joining());
    }
}
