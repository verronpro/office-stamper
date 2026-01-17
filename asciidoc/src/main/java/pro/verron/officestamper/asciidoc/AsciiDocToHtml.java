package pro.verron.officestamper.asciidoc;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static pro.verron.officestamper.asciidoc.AsciiDocModel.*;

public final class AsciiDocToHtml
        implements Function<AsciiDocModel, String> {

    private static String renderBlock(Block block) {
        switch (block) {
            case Heading(int level, List<Inline> inlines) -> {
                return String.format("<h%d>%s</h%d>\n", level, renderInlines(inlines), level);
            }
            case Paragraph(List<Inline> inlines) -> {
                return String.format("<p>%s</p>\n", renderInlines(inlines));
            }
            case UnorderedList(List<ListItem> items) -> {
                return "<ul>\n" + items.stream()
                                       .map(item -> "  <li>" + renderInlines(item.inlines()) + "</li>\n")
                                       .collect(Collectors.joining()) + "</ul>\n";
            }
            case OrderedList(List<ListItem> items) -> {
                return "<ol>\n" + items.stream()
                                       .map(item -> "  <li>" + renderInlines(item.inlines()) + "</li>\n")
                                       .collect(Collectors.joining()) + "</ol>\n";
            }
            case Table(List<Row> rows) -> {
                StringBuilder sb = new StringBuilder("<table>\n");
                for (Row row : rows) {
                    sb.append("  <tr>\n");
                    for (Cell cell : row.cells()) {
                        sb.append("    <td>")
                          .append(renderInlines(cell.inlines()))
                          .append("</td>\n");
                    }
                    sb.append("  </tr>\n");
                }
                sb.append("</table>\n");
                return sb.toString();
            }
            case Blockquote(List<Inline> inlines) -> {
                return "<blockquote>" + renderInlines(inlines) + "</blockquote>\n";
            }
            case CodeBlock(String language, String content) -> {
                return String.format("<pre><code class=\"language-%s\">%s</code></pre>\n", language, content);
            }
            case ImageBlock(String url, String altText) -> {
                return String.format("<img src=\"%s\" alt=\"%s\">\n", url, altText);
            }
            default -> { /* DO NOTHING */ }
        }
        return "";
    }

    private static String renderInlines(List<Inline> inlines) {
        StringBuilder sb = new StringBuilder();
        for (Inline inline : inlines) {
            switch (inline) {
                case Text(String text1) -> sb.append(text1);
                case Bold(List<Inline> children1) -> sb.append("<b>")
                                                       .append(renderInlines(children1))
                                                       .append("</b>");
                case Italic(List<Inline> children) -> sb.append("<i>")
                                                        .append(renderInlines(children))
                                                        .append("</i>");
                case Tab _ -> sb.append("&nbsp;&nbsp;&nbsp;&nbsp;");
                case Link(String url1, String text) -> sb.append(String.format("<a href=\"%s\">%s</a>", url1, text));
                case InlineImage(String url, String altText) ->
                        sb.append(String.format("<img src=\"%s\" alt=\"%s\">", url, altText));
                default -> { /* DO NOTHING */ }
            }
        }
        return sb.toString();
    }

    public String apply(AsciiDocModel model) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n<html>\n<head>\n<meta charset=\"UTF-8\">\n</head>\n<body>\n");
        for (Block block : model.getBlocks()) {
            html.append(renderBlock(block));
        }
        html.append("</body>\n</html>");
        return html.toString();
    }
}
