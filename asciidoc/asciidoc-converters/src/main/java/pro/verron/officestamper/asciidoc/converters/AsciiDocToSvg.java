package pro.verron.officestamper.asciidoc.converters;

import pro.verron.officestamper.asciidoc.core.AsciiDocModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static pro.verron.officestamper.asciidoc.core.AsciiDocModel.*;

/// Renderer converting an [AsciiDocModel] into an SVG document simulating various editor interfaces.
public final class AsciiDocToSvg
        implements Function<AsciiDocModel, String> {

    private static final int VIEWPORT_WIDTH = 1200;
    private static final int BANNER_HEIGHT = 100;
    private static final int PAGE_WIDTH = 800;
    private static final int PAGE_LEFT = 50;
    private static final int PAGE_MARGIN_TOP = 40;
    private static final int PAGE_PADDING = 72;
    private static final int COMMENTS_LEFT = 900;
    private static final int COMMENT_WIDTH = 250;
    private static final int BODY_FONT_SIZE = 14;
    private static final int LINE_HEIGHT = 20;

    @Override
    public String apply(AsciiDocModel model) {
        Theme theme = Theme.valueOf(model.getAttributes()
                                         .getOrDefault("theme", "word")
                                         .toUpperCase());
        List<CommentInfo> comments = extractComments(model);
        Map<Integer, List<CommentInfo>> blockToComments = mapCommentsToBlocks(comments);

        StringBuilder svgContent = new StringBuilder();
        int pageY = BANNER_HEIGHT + PAGE_MARGIN_TOP;
        int currentY = pageY + PAGE_PADDING;

        List<BlockPosition> blockPositions = new ArrayList<>();

        for (int i = 0;
             i < model.getBlocks()
                      .size();
             i++) {
            Block block = model.getBlocks()
                               .get(i);
            if (block instanceof MacroBlock m && m.name()
                                                  .equals("comment")) continue;

            int startY = currentY;
            boolean isCommented = blockToComments.containsKey(i);

            currentY = renderBlock(svgContent, block, PAGE_LEFT + PAGE_PADDING, currentY, isCommented, theme);
            blockPositions.add(new BlockPosition(i, startY, currentY - 8));
        }

        int pageHeight = Math.max(800, currentY - pageY + PAGE_PADDING);

        StringBuilder fullSvg = new StringBuilder();
        fullSvg.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        fullSvg.append(String.format(
                "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"%d\" height=\"%d\" viewBox=\"0 0 %d %d\">\n",
                VIEWPORT_WIDTH,
                pageY + pageHeight + 50,
                VIEWPORT_WIDTH,
                pageY + pageHeight + 50));

        // Background
        String bgColor = switch (theme) {
            case WORD -> "#e6e6e6";
            case GDOCS -> "#f8f9fa";
            case LIBRE -> "#dfdfdf";
        };
        fullSvg.append(String.format("<rect x=\"0\" y=\"0\" width=\"100%%\" height=\"100%%\" fill=\"%s\"/>\n",
                bgColor));

        // Editor Banner
        String title = model.getAttributes()
                            .getOrDefault("title", "Document.docx");
        switch (theme) {
            case WORD -> renderWordBanner(fullSvg, title);
            case GDOCS -> renderGoogleDocsBanner(fullSvg, title);
            case LIBRE -> renderLibreOfficeBanner(fullSvg, title);
        }

        // Page Shadow
        fullSvg.append(String.format(
                "<rect x=\"%d\" y=\"%d\" width=\"%d\" height=\"%d\" fill=\"#000\" fill-opacity=\"0.1\" rx=\"2\"/>\n",
                PAGE_LEFT + 4,
                pageY + 4,
                PAGE_WIDTH,
                pageHeight));
        // Page
        fullSvg.append(String.format(
                "<rect x=\"%d\" y=\"%d\" width=\"%d\" height=\"%d\" fill=\"white\" stroke=\"#ccc\" "
                + "stroke-width=\"1\"/>\n",
                PAGE_LEFT,
                pageY,
                PAGE_WIDTH,
                pageHeight));

        // Add rendered content
        fullSvg.append(svgContent);

        // Render comments and connectors
        renderComments(fullSvg, blockToComments, blockPositions, theme);

        fullSvg.append("</svg>");
        return fullSvg.toString();
    }

    private List<CommentInfo> extractComments(AsciiDocModel model) {
        List<CommentInfo> comments = new ArrayList<>();
        for (Block block : model.getBlocks()) {
            if (block instanceof MacroBlock(String name, String id, List<String> list) && name.equals("comment")) {
                String start = "";
                String author = "Author";
                String value = "";
                for (String attr : list) {
                    String trimmed = attr.trim();
                    if (trimmed.startsWith("start=")) start = trimmed.substring(6)
                                                                     .replace("\"", "");
                    else if (trimmed.startsWith("author=")) author = trimmed.substring(7)
                                                                            .replace("\"", "");
                    else if (trimmed.startsWith("value=")) value = trimmed.substring(6)
                                                                          .replace("\"", "");
                }
                comments.add(new CommentInfo(id, start, author, value));
            }
        }
        return comments;
    }

    private int renderBlock(StringBuilder body, Block block, int x, int y, boolean highlight, Theme theme) {
        int contentStartIdx = body.length();
        int nextY = y;
        switch (block) {
            case Heading(_, int level, List<Inline> inlines) -> {
                int fontSize = Math.max(18, 34 - (level * 3));
                nextY = appendTextLine(body, renderInlines(inlines), x, y + fontSize, fontSize, 700, theme);
                nextY += 10;
            }
            case Paragraph(_, List<Inline> inlines) -> {
                nextY = appendTextLine(body, renderInlines(inlines), x, y + BODY_FONT_SIZE, BODY_FONT_SIZE, 400, theme);
                nextY += 8;
            }
            case UnorderedList(List<ListItem> items) -> {
                for (ListItem item : items) {
                    nextY = appendTextLine(body,
                            "• " + renderInlines(item.inlines()),
                            x,
                            nextY + BODY_FONT_SIZE,
                            BODY_FONT_SIZE,
                            400,
                            theme);
                }
                nextY += 6;
            }
            case OrderedList(List<ListItem> items) -> {
                int index = 1;
                for (ListItem item : items) {
                    nextY = appendTextLine(body,
                            index + ". " + renderInlines(item.inlines()),
                            x,
                            nextY + BODY_FONT_SIZE,
                            BODY_FONT_SIZE,
                            400,
                            theme);
                    index++;
                }
                nextY += 6;
            }
            case Blockquote(List<Inline> inlines) -> {
                nextY += 8;
                body.append("<line x1=\"")
                    .append(x)
                    .append("\" y1=\"")
                    .append(nextY)
                    .append("\" x2=\"")
                    .append(x)
                    .append("\" y2=\"")
                    .append(nextY + LINE_HEIGHT)
                    .append("\" stroke=\"#888\" stroke-width=\"3\"/>\n");
                nextY = appendTextLine(body,
                        renderInlines(inlines),
                        x + 10,
                        nextY + BODY_FONT_SIZE,
                        BODY_FONT_SIZE,
                        400,
                        theme);
            }
            case CodeBlock(String language, String content) -> {
                nextY += 8;
                int blockHeight = Math.max(LINE_HEIGHT * 2,
                        (content.lines()
                                .toList()
                                .size() + 1) * LINE_HEIGHT);
                body.append("<rect x=\"")
                    .append(x)
                    .append("\" y=\"")
                    .append(nextY)
                    .append("\" width=\"")
                    .append(PAGE_WIDTH - (PAGE_PADDING * 2))
                    .append("\" height=\"")
                    .append(blockHeight)
                    .append("\" fill=\"#f6f8fa\" stroke=\"#d0d7de\" rx=\"6\"/>\n");
                nextY = appendTextLine(body, "[" + language + "]", x + 10, nextY + BODY_FONT_SIZE, 12, 600, theme);
                for (String line : content.lines()
                                          .toList()) {
                    nextY = appendTextLine(body, line, x + 10, nextY + 12, 13, 400, theme);
                }
                nextY += 8;
            }
            case ImageBlock(String url, String altText) -> {
                nextY += 8;
                body.append("<rect x=\"")
                    .append(x)
                    .append("\" y=\"")
                    .append(nextY)
                    .append("\" width=\"320\" height=\"120\" fill=\"#f0f0f0\" stroke=\"#c0c0c0\" rx=\"4\"/>\n");
                nextY = appendTextLine(body,
                        "[image] " + (altText == null ? "" : altText) + " (" + url + ")",
                        x + 8,
                        nextY + 30,
                        12,
                        400,
                        theme);
                nextY += 84;
            }
            default -> nextY += LINE_HEIGHT;
        }

        if (highlight) {
            String highlightColor = switch (theme) {
                case WORD -> "#fff2cc";
                case GDOCS -> "#c2e7ff";
                case LIBRE -> "#ffff00";
            };
            String highlightRect = String.format("<rect x=\"%d\" y=\"%d\" width=\"%d\" height=\"%d\" fill=\"%s\"/>\n",
                    x - 5,
                    y - 2,
                    PAGE_WIDTH - 2 * PAGE_PADDING + 10,
                    nextY - y,
                    highlightColor);
            body.insert(contentStartIdx, highlightRect);
        }

        return nextY;
    }

    private void renderWordBanner(StringBuilder svg, String title) {
        // Blue top bar
        svg.append("<rect x=\"0\" y=\"0\" width=\"100%\" height=\"30\" fill=\"#2b579a\"/>\n");
        svg.append(String.format(
                "<text x=\"50%%\" y=\"20\" font-family=\"Segoe UI, Arial\" font-size=\"12\" fill=\"white\" "
                + "text-anchor=\"middle\">%s - Word</text>\n",
                title));

        // Ribbon area
        svg.append(String.format("<rect x=\"0\" y=\"30\" width=\"100%%\" height=\"%d\" fill=\"#f3f2f1\"/>\n",
                BANNER_HEIGHT - 30));
        svg.append("<line x1=\"0\" y1=\"30\" x2=\"100%\" y2=\"30\" stroke=\"#ccc\"/>\n");
        svg.append(String.format("<line x1=\"0\" y1=\"%d\" x2=\"100%%\" y2=\"%d\" stroke=\"#ccc\"/>\n",
                BANNER_HEIGHT,
                BANNER_HEIGHT));

        // Simple ribbon icons simulation
        svg.append("<text x=\"20\" y=\"60\" font-family=\"Segoe UI, Arial\" font-size=\"11\" fill=\"#333\">File  Home  "
                   + "Insert  Layout  References  Review  View</text>\n");
        svg.append("<rect x=\"20\" y=\"70\" width=\"40\" height=\"20\" fill=\"#2b579a\" rx=\"2\"/>\n");
        svg.append("<text x=\"25\" y=\"84\" font-family=\"Segoe UI, Arial\" font-size=\"10\" "
                   + "fill=\"white\">Paste</text>\n");
    }

    private void renderGoogleDocsBanner(StringBuilder svg, String title) {
        // Top bar
        svg.append("<rect x=\"0\" y=\"0\" width=\"100%\" height=\"60\" fill=\"white\"/>\n");
        svg.append("<circle cx=\"30\" cy=\"30\" r=\"15\" fill=\"#4285f4\"/>\n");
        svg.append("<rect x=\"22\" y=\"22\" width=\"16\" height=\"16\" fill=\"white\" rx=\"2\"/>\n");

        svg.append(String.format("<text x=\"60\" y=\"25\" font-family=\"Product Sans, Arial\" font-size=\"18\" "
                                 + "fill=\"#3c4043\">%s</text>\n", title));
        svg.append("<text x=\"60\" y=\"45\" font-family=\"Arial\" font-size=\"12\" fill=\"#5f6368\">File  Edit  View  "
                   + "Insert  Format  Tools  Extensions  Help</text>\n");

        // Toolbar
        svg.append(String.format("<rect x=\"0\" y=\"60\" width=\"100%%\" height=\"%d\" fill=\"#edf2fa\" rx=\"20\"/>\n",
                BANNER_HEIGHT - 60));
        svg.append("<line x1=\"0\" y1=\"100\" x2=\"100%\" y2=\"100\" stroke=\"#ccc\"/>\n");
    }

    private void renderLibreOfficeBanner(StringBuilder svg, String title) {
        // Top bar
        svg.append("<rect x=\"0\" y=\"0\" width=\"100%\" height=\"30\" fill=\"#dfdfdf\"/>\n");
        svg.append(String.format(
                "<text x=\"10\" y=\"20\" font-family=\"Arial\" font-size=\"12\" fill=\"black\">%s - LibreOffice "
                + "Writer</text>\n",
                title));

        // Menu bar
        svg.append("<rect x=\"0\" y=\"30\" width=\"100%\" height=\"25\" fill=\"#eeeeee\"/>\n");
        svg.append("<text x=\"10\" y=\"47\" font-family=\"Arial\" font-size=\"11\" fill=\"black\">File  Edit  View  "
                   + "Insert  Format  Styles  Table  Form  Tools  Window  Help</text>\n");

        // Toolbars
        svg.append(String.format("<rect x=\"0\" y=\"55\" width=\"100%%\" height=\"%d\" fill=\"#eeeeee\"/>\n",
                BANNER_HEIGHT - 55));
        svg.append("<line x1=\"0\" y1=\"55\" x2=\"100%\" y2=\"55\" stroke=\"#ccc\"/>\n");
        svg.append("<line x1=\"0\" y1=\"100\" x2=\"100%\" y2=\"100\" stroke=\"#ccc\"/>\n");
    }

    private void renderComments(
            StringBuilder svg,
            Map<Integer, List<CommentInfo>> blockToComments,
            List<BlockPosition> blockPositions,
            Theme theme
    ) {
        int commentY = BANNER_HEIGHT + PAGE_MARGIN_TOP + PAGE_PADDING;
        String strokeColor = switch (theme) {
            case WORD -> "#ffc000";
            case GDOCS -> "#0b57d0";
            case LIBRE -> "#808080";
        };
        for (BlockPosition pos : blockPositions) {
            List<CommentInfo> comments = blockToComments.get(pos.index);
            if (comments != null) {
                for (CommentInfo c : comments) {
                    svg.append(String.format(
                            "<rect x=\"%d\" y=\"%d\" width=\"%d\" height=\"60\" fill=\"#f9f9f9\" stroke=\"%s\" "
                            + "stroke-width=\"1\" rx=\"4\"/>\n",
                            COMMENTS_LEFT,
                            commentY,
                            COMMENT_WIDTH,
                            strokeColor));
                    if (theme == Theme.GDOCS) {
                        svg.append(String.format("<circle cx=\"%d\" cy=\"%d\" r=\"5\" fill=\"#4285f4\"/>\n",
                                COMMENTS_LEFT + 15,
                                commentY + 20));
                    }
                    svg.append(String.format("<text x=\"%d\" y=\"%d\" font-family=\"Segoe UI, Arial\" font-size=\"11\" "
                                             + "font-weight=\"bold\" fill=\"#333\">%s</text>\n",
                            COMMENTS_LEFT + (theme == Theme.GDOCS ? 25 : 10),
                            commentY + 20,
                            escape(c.author)));
                    svg.append(String.format("<text x=\"%d\" y=\"%d\" font-family=\"Segoe UI, Arial\" font-size=\"11\" "
                                             + "fill=\"#666\">%s</text>\n",
                            COMMENTS_LEFT + 10,
                            commentY + 40,
                            escape(c.value)));

                    int startX = PAGE_LEFT + PAGE_WIDTH;
                    int startY = (pos.startY + pos.endY) / 2;
                    int endX = COMMENTS_LEFT;
                    int endY = commentY + 30;
                    svg.append(String.format(
                            "<line x1=\"%d\" y1=\"%d\" x2=\"%d\" y2=\"%d\" stroke=\"%s\" stroke-width=\"1\" "
                            + "stroke-dasharray=\"4\"/>\n",
                            startX,
                            startY,
                            endX,
                            endY,
                            strokeColor));

                    commentY += 80;
                }
            }
        }
    }

    private Map<Integer, List<CommentInfo>> mapCommentsToBlocks(List<CommentInfo> comments) {
        Map<Integer, List<CommentInfo>> map = new HashMap<>();
        for (CommentInfo c : comments) {
            try {
                int blockIndex = Integer.parseInt(c.start.split(",")[0]);
                map.computeIfAbsent(blockIndex, k -> new ArrayList<>())
                   .add(c);
            } catch (Exception ignored) {}
        }
        return map;
    }

    private int appendTextLine(StringBuilder body, String line, int x, int y, int fontSize, int weight, Theme theme) {
        String fontFamily = switch (theme) {
            case WORD -> "Calibri, Arial, sans-serif";
            case GDOCS -> "Arial, sans-serif";
            case LIBRE -> "Liberation Serif, Times New Roman, serif";
        };
        body.append("<text x=\"")
            .append(x)
            .append("\" y=\"")
            .append(y)
            .append("\" font-family=\"")
            .append(fontFamily)
            .append("\" font-size=\"")
            .append(fontSize)
            .append("\" font-weight=\"")
            .append(weight)
            .append("\" fill=\"#111\">")
            .append(escape(line))
            .append("</text>\n");
        return y + LINE_HEIGHT;
    }

    private String renderInlines(List<Inline> inlines) {
        StringBuilder text = new StringBuilder();
        for (Inline inline : inlines) {
            switch (inline) {
                case Text(String value) -> text.append(value);
                case Bold(List<Inline> children) -> text.append(renderInlines(children));
                case Italic(List<Inline> children) -> text.append(renderInlines(children));
                case Link(String url, String label) -> text.append(label == null || label.isBlank() ? url : label);
                case InlineImage(String url, Map<String, String> attributes) -> {
                    String title = attributes.getOrDefault("title", "image");
                    text.append('[')
                        .append(title)
                        .append(": ")
                        .append(url)
                        .append(']');
                }
                case Tab _ -> text.append("    ");
                default -> text.append(inline.text());
            }
        }
        return text.toString();
    }

    private String escape(String value) {
        if (value == null) return "";
        return value.replace("&", "&amp;")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;")
                    .replace("\"", "&quot;")
                    .replace("'", "&apos;")
                    .replaceAll("\\p{Cntrl}", "")
                    .stripTrailing();
    }

    public enum Theme {
        WORD,
        GDOCS,
        LIBRE
    }

    private record CommentInfo(String id, String start, String author, String value) {}

    private record BlockPosition(int index, int startY, int endY) {}
}
