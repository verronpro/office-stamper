package pro.verron.officestamper.asciidoc;

import java.util.List;
import java.util.stream.Collectors;

import static pro.verron.officestamper.asciidoc.AsciiDocModel.*;

public final class AsciiDocToHtml {
    private AsciiDocToHtml() {}

    public static String compileToHtml(AsciiDocModel model) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n<html>\n<head>\n<meta charset=\"UTF-8\">\n</head>\n<body>\n");
        for (Block block : model.getBlocks()) {
            html.append(renderBlock(block));
        }
        html.append("</body>\n</html>");
        return html.toString();
    }

    private static String renderBlock(Block block) {
        if (block instanceof Heading(int level, List<Inline> inlines2)) {
            return String.format("<h%d>%s</h%d>\n", level, renderInlines(inlines2), level);
        }
        else if (block instanceof Paragraph(List<Inline> inlines1)) {
            return String.format("<p>%s</p>\n", renderInlines(inlines1));
        }
        else if (block instanceof UnorderedList(List<ListItem> items1)) {
            return "<ul>\n" + items1
                    .stream()
                    .map(item -> "  <li>" + renderInlines(item.inlines()) + "</li>\n")
                    .collect(Collectors.joining()) + "</ul>\n";
        }
        else if (block instanceof OrderedList(List<ListItem> items)) {
            return "<ol>\n" + items
                    .stream()
                    .map(item -> "  <li>" + renderInlines(item.inlines()) + "</li>\n")
                    .collect(Collectors.joining()) + "</ol>\n";
        }
        else if (block instanceof Table(List<Row> rows)) {
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
        else if (block instanceof Blockquote(List<Inline> inlines)) {
            return "<blockquote>" + renderInlines(inlines) + "</blockquote>\n";
        }
        else if (block instanceof CodeBlock(String language, String content)) {
            return String.format("<pre><code class=\"language-%s\">%s</code></pre>\n", language, content);
        }
        else if (block instanceof ImageBlock(String url, String altText)) {
            return String.format("<img src=\"%s\" alt=\"%s\">\n", url, altText);
        }
        return "";
    }

    private static String renderInlines(List<Inline> inlines) {
        StringBuilder sb = new StringBuilder();
        for (Inline inline : inlines) {
            if (inline instanceof Text(String text1)) {
                sb.append(text1);
            }
            else if (inline instanceof Bold(List<Inline> children1)) {
                sb.append("<b>")
                  .append(renderInlines(children1))
                  .append("</b>");
            }
            else if (inline instanceof Italic(List<Inline> children)) {
                sb.append("<i>")
                  .append(renderInlines(children))
                  .append("</i>");
            }
            else if (inline instanceof Tab) {
                sb.append("&nbsp;&nbsp;&nbsp;&nbsp;");
            }
            else if (inline instanceof Link(String url1, String text)) {
                sb.append(String.format("<a href=\"%s\">%s</a>", url1, text));
            }
            else if (inline instanceof InlineImage(String url, String altText)) {
                sb.append(String.format("<img src=\"%s\" alt=\"%s\">", url, altText));
            }
        }
        return sb.toString();
    }
}
