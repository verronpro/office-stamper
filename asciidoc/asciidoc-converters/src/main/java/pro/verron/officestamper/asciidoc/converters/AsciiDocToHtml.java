package pro.verron.officestamper.asciidoc.converters;

import pro.verron.officestamper.asciidoc.core.AsciiDocModel;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static java.util.stream.Collectors.joining;
import static pro.verron.officestamper.asciidoc.core.AsciiDocModel.*;

/// The AsciiDocToHtml class is responsible for rendering an AsciiDoc representation
/// into its corresponding HTML output. It implements the [Function] interface,
/// converting an [AsciiDocModel] to a String containing the HTML representation.
///
/// This class provides static utility methods for rendering various AsciiDoc block
/// types and inline elements into their HTML counterparts. The following block types
/// are supported for conversion:
///
/// - Headings
/// - Paragraphs
/// - Unordered and Ordered Lists
/// - Tables
/// - Blockquotes
/// - Code Blocks
/// - Image Blocks
///
/// Additionally, inline elements such as bold, italic, links, images, and text are
/// rendered with appropriate HTML tags.
///
/// The class adheres to the functional programming paradigm by implementing the
/// [#apply(AsciiDocModel)] method to facilitate the mapping of AsciiDoc models to HTML.
///
/// This class is immutable and cannot be instantiated.
public final class AsciiDocToHtml
        implements Function<AsciiDocModel, String> {

    private static String renderBlock(Block block) {
        switch (block) {
            case Heading(_, int level, List<Inline> inlines) -> {
                return String.format("<h%d>%s</h%d>\n", level, renderInlines(inlines), level);
            }
            case Paragraph(List<String> header, List<Inline> inlines) -> {
                if (header.isEmpty()) return String.format("<p>%s</p>\n", renderInlines(inlines));
                else return String.format("<p class=\"%s\">%s</p>\n", header.getFirst(), renderInlines(inlines));
            }
            case UnorderedList(List<ListItem> items) -> {
                return "<ul>\n" + items.stream()
                                       .map(item -> "  <li>" + renderInlines(item.inlines()) + "</li>\n")
                                       .collect(joining()) + "</ul>\n";
            }
            case OrderedList(List<ListItem> items) -> {
                return "<ol>\n" + items.stream()
                                       .map(item -> "  <li>" + renderInlines(item.inlines()) + "</li>\n")
                                       .collect(joining()) + "</ol>\n";
            }
            case Table(List<Row> rows) -> {
                var sb = new StringBuilder("<table>\n");
                for (var row : rows) {
                    sb.append("  <tr>\n");
                    for (var cell : row.cells()) {
                        sb.append("    <td>")
                          .append(cell.blocks()
                                      .stream()
                                      .map(AsciiDocToHtml::renderBlock)
                                      .collect(joining()))
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
        var sb = new StringBuilder();
        for (var inline : inlines) {
            switch (inline) {
                case Text(String str) -> sb.append(str);
                case Bold(List<Inline> children) -> sb.append("<b>")
                                                      .append(renderInlines(children))
                                                      .append("</b>");
                case Italic(List<Inline> children) -> sb.append("<i>")
                                                        .append(renderInlines(children))
                                                        .append("</i>");
                case Tab _ -> sb.append("&nbsp;&nbsp;&nbsp;&nbsp;");
                case Link(String url, String str) -> sb.append(String.format("<a href=\"%s\">%s</a>", url, str));
                case InlineImage(String url, Map<String, String> map) ->
                        sb.append(String.format("<img src=\"%s\" alt=\"%s\">", url, map.get("title")));
                default -> { /* DO NOTHING */ }
            }
        }
        return sb.toString();
    }

    /// Converts the given AsciiDoc model into an HTML document string.
    ///
    /// @param model the AsciiDocModel containing the blocks to be processed.
    /// @return the resulting HTML document as a string.
    public String apply(AsciiDocModel model) {
        var html = new StringBuilder("""
                <!DOCTYPE html>
                <html>
                  <head>
                    <meta charset="UTF-8">
                  </head>
                  <body>
                """);
        for (var block : model.getBlocks()) html.append(renderBlock(block));
        html.append("""
                  </body>
                </html>""");
        return html.toString();
    }
}
