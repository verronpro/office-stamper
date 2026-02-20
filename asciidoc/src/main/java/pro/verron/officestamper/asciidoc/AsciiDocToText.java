package pro.verron.officestamper.asciidoc;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static pro.verron.officestamper.asciidoc.AsciiDocModel.*;

public final class AsciiDocToText
        implements Function<AsciiDocModel, String> {
    private static String renderInlines(List<Inline> inlines) {
        StringBuilder sb = new StringBuilder();
        for (Inline inline : inlines) {
            if (inline instanceof Text(String text1)) {
                sb.append(text1);
            }
            else if (inline instanceof Bold(List<Inline> children1)) {
                sb.append("*")
                  .append(renderInlines(children1))
                  .append("*");
            }
            else if (inline instanceof Italic(List<Inline> children)) {
                sb.append("_")
                  .append(renderInlines(children))
                  .append("_");
            }
            else if (inline instanceof Tab) {
                sb.append("|TAB|");
            }
            else if (inline instanceof Link(String url1, String text)) {
                sb.append(url1)
                  .append("[")
                  .append(text)
                  .append("]");
            }
            else if (inline instanceof InlineImage(String url, String altText)) {
                sb.append("image:")
                  .append(url)
                  .append("[")
                  .append(altText)
                  .append("]");
            }
        }
        return sb.toString();
    }

    private static String renderBlock(Block block) {
        return switch (block) {
            case Heading(int level, List<Inline> inlines2) ->
                    "=".repeat(level) + " " + renderInlines(inlines2) + "\n\n";
            case Paragraph(List<Inline> inlines1) -> renderInlines(inlines1) + "\n\n";
            case UnorderedList(List<ListItem> items1) -> items1.stream()
                                                               .map(item -> "* " + renderInlines(item.inlines()) + "\n")
                                                               .collect(Collectors.joining()) + "\n";
            case OrderedList(List<ListItem> items) -> items.stream()
                                                           .map(item -> ". " + renderInlines(item.inlines()) + "\n")
                                                           .collect(Collectors.joining()) + "\n";
            case Table(List<Row> rows) -> {
                StringBuilder sb = new StringBuilder("|===\n");
                for (Row row : rows) {
                    var style = row.style();
                    style.ifPresent(s -> sb.append("[%s]\n".formatted(s)));
                    for (Cell cell : row.cells()) {
                        sb.append("|")
                          .append(renderInlines(cell.inlines()))
                          .append("\n");
                    }
                }
                sb.append("|===\n\n");
                yield sb.toString();
            }
            case Blockquote(List<Inline> inlines) -> "____\n" + renderInlines(inlines) + "\n____\n\n";
            case CodeBlock(String language, String content) ->
                    (language.isEmpty() ? "" : "[source," + language + "]\n") + "----\n" + content + "\n----\n\n";
            case ImageBlock(String url, String altText) -> "image::" + url + "[" + altText + "]\n\n";
        };
    }

    public String apply(AsciiDocModel model) {
        return model.getBlocks()
                    .stream()
                    .map(AsciiDocToText::renderBlock)
                    .collect(Collectors.joining());
    }
}
