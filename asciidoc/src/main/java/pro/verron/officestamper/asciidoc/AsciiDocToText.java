package pro.verron.officestamper.asciidoc;

import java.util.List;
import java.util.stream.Collectors;

import static pro.verron.officestamper.asciidoc.AsciiDocModel.*;

public final class AsciiDocToText {
    private AsciiDocToText() {}

    public static String compileToText(AsciiDocModel model) {
        StringBuilder adoc = new StringBuilder();
        for (Block block : model.getBlocks()) {
            adoc.append(renderBlock(block));
            adoc.append("\n");
        }
        return adoc.toString()
                   .trim();
    }

    private static String renderBlock(Block block) {
        if (block instanceof Heading(int level, List<Inline> inlines2)) {
            return "=".repeat(level) + " " + renderInlines(inlines2) + "\n";
        }
        else if (block instanceof Paragraph(List<Inline> inlines1)) {
            return renderInlines(inlines1) + "\n";
        }
        else if (block instanceof UnorderedList(List<ListItem> items1)) {
            return items1
                    .stream()
                    .map(item -> "* " + renderInlines(item.inlines()) + "\n")
                    .collect(Collectors.joining());
        }
        else if (block instanceof OrderedList(List<ListItem> items)) {
            return items
                    .stream()
                    .map(item -> ". " + renderInlines(item.inlines()) + "\n")
                    .collect(Collectors.joining());
        }
        else if (block instanceof Table(List<Row> rows)) {
            StringBuilder sb = new StringBuilder("|===\n");
            for (Row row : rows) {
                sb.append("|")
                  .append(row.cells()
                             .stream()
                             .map(cell -> renderInlines(cell.inlines()))
                             .collect(Collectors.joining(" |")))
                  .append("\n");
            }
            sb.append("|===\n");
            return sb.toString();
        }
        else if (block instanceof Blockquote(List<Inline> inlines)) {
            return "____\n" + renderInlines(inlines) + "\n____\n";
        }
        else if (block instanceof CodeBlock(String language, String content)) {
            String header = language
                    .isEmpty() ? "" : "[source," + language + "]\n";
            return header + "----\n" + content + "\n----\n";
        }
        else if (block instanceof ImageBlock(String url, String altText)) {
            return "image::" + url + "[" + altText + "]\n";
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
}
