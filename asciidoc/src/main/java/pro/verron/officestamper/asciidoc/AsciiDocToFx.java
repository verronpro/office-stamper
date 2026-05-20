package pro.verron.officestamper.asciidoc;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import javafx.scene.text.Text;

import java.util.List;
import java.util.Map;

import static pro.verron.officestamper.asciidoc.AsciiDocModel.*;

/// Renders [AsciiDocModel] into a JavaFX [Scene].
public final class AsciiDocToFx {

    private static final Color WORD_BLUE = Color.web("#2b579a");
    private static final Color WORD_PAGE_BG = Color.WHITE;
    private static final Color WORD_WORKSPACE_BG = Color.web("#e6e6e6");
    private static final Color COMMENT_BG = Color.web("#fef4f4");
    private static final Color COMMENT_BORDER = Color.web("#f9dada");

    private static void emitInline(TextFlow flow, Inline inline, Font base, FontWeight weight, FontPosture posture) {
        switch (inline) {
            case AsciiDocModel.Text(String text) -> {
                flow.getChildren()
                    .add(styledText(text, base, weight, posture));
                return;
            }
            case Bold(List<Inline> children) -> {
                for (Inline child : children) {
                    emitInline(flow, child, base, FontWeight.BOLD, posture);
                }
                return;
            }
            case Italic(List<Inline> children) -> {
                for (Inline child : children) {
                    emitInline(flow, child, base, weight, FontPosture.ITALIC);
                }
                return;
            }
            case Link link -> {
                Text t = styledText(link.text(), base, weight, posture);
                t.setUnderline(true);
                t.setFill(Color.BLUE);
                flow.getChildren()
                    .add(t);
            }
            case Styled styled -> {
                // Handle placeholder highlighting if possible, or just recurse
                for (Inline child : styled.children()) {
                    emitInline(flow, child, base, weight, posture);
                }
            }
            default -> { /* DO NOTHING */ }
        }
        if (inline instanceof InlineImage ii) {
            flow.getChildren()
                .add(new Text("[Image: " + ii.path() + "]"));
        }
    }

    private static Font fontForHeading(int level) {
        double size = switch (level) {
            case 1 -> 20; // Word Heading 1 is usually around 16pt-20pt
            case 2 -> 16;
            case 3 -> 14;
            default -> 12;
        };
        return Font.font("Calibri", FontWeight.BOLD, size);
    }

    private static Text styledText(String value, Font base, FontWeight weight, FontPosture posture) {
        Text t = new Text(value);
        t.setFont(Font.font(base.getFamily(), weight, posture, base.getSize()));
        // Word uses Calibri by default
        if (t.getFont()
             .getFamily()
             .equals("System")) {
            t.setFont(Font.font("Calibri", weight, posture, base.getSize()));
        }
        return t;
    }

    /// Compiles the model into a simple scrollable Scene using a VBox of TextFlow nodes.
    ///
    /// Headings are rendered with larger font sizes; bold/italic are applied per inline fragment.
    ///
    /// @param model parsed AsciiDoc model
    ///
    /// @return JavaFX scene containing the rendered content
    public Scene apply(AsciiDocModel model) {
        Map<String, String> attributes = model.getAttributes();
        String theme = attributes.getOrDefault("preview-theme", "word");

        VBox pageContent = new VBox(10);
        pageContent.setBackground(new Background(new BackgroundFill(WORD_PAGE_BG, CornerRadii.EMPTY, Insets.EMPTY)));

        // Margins handling (default 25mm ~ 95px at 96dpi)
        double margin = 50;
        pageContent.setPadding(new Insets(margin));
        pageContent.setPrefWidth(600); // Simulated A4 width
        pageContent.setMinWidth(600);
        pageContent.setMaxWidth(600);

        VBox commentsPanel = new VBox(10);
        commentsPanel.setPadding(new Insets(margin, 10, margin, 10));
        commentsPanel.setPrefWidth(200);

        for (Block block : model.getBlocks()) {
            if (block instanceof MacroBlock mb && mb.name()
                                                    .equals("comment")) {
                commentsPanel.getChildren()
                             .add(createCommentNode(mb));
                continue;
            }

            TextFlow flow = new TextFlow();
            flow.setLineSpacing(2.0); // Word-like line spacing

            switch (block) {
                case Heading(_, int level, List<Inline> inlines) -> {
                    for (Inline inline : inlines) {
                        emitInline(flow, inline, fontForHeading(level), FontWeight.BOLD, FontPosture.REGULAR);
                    }
                    if (level == 1) {
                        flow.setPadding(new Insets(0, 0, 10, 0));
                    }
                }
                case Paragraph(_, List<Inline> inlines) -> {
                    for (Inline inline : inlines) {
                        emitInline(flow, inline, Font.font("Calibri", 11), FontWeight.NORMAL, FontPosture.REGULAR);
                    }
                }
                case UnorderedList(List<ListItem> items1) -> {
                    for (ListItem item : items1) {
                        TextFlow itemFlow = new TextFlow();
                        itemFlow.getChildren()
                                .add(new Text("• "));
                        for (Inline inline : item.inlines()) {
                            emitInline(itemFlow,
                                    inline,
                                    Font.font("Calibri", 11),
                                    FontWeight.NORMAL,
                                    FontPosture.REGULAR);
                        }
                        pageContent.getChildren()
                                   .add(itemFlow);
                    }
                    continue;
                }
                case OrderedList(List<ListItem> items) -> {
                    int i = 1;
                    for (ListItem item : items) {
                        TextFlow itemFlow = new TextFlow();
                        itemFlow.getChildren()
                                .add(new Text((i++) + ". "));
                        for (Inline inline : item.inlines()) {
                            emitInline(itemFlow,
                                    inline,
                                    Font.font("Calibri", 11),
                                    FontWeight.NORMAL,
                                    FontPosture.REGULAR);
                        }
                        pageContent.getChildren()
                                   .add(itemFlow);
                    }
                    continue;
                }
                case Blockquote(List<Inline> inlines) -> {
                    flow.setBorder(new Border(new BorderStroke(Color.LIGHTGRAY,
                            BorderStrokeStyle.SOLID,
                            CornerRadii.EMPTY,
                            new BorderWidths(0, 0, 0, 4))));
                    flow.setPadding(new Insets(0, 0, 0, 20));
                    for (Inline inline : inlines) {
                        emitInline(flow, inline, Font.font("Calibri", 11), FontWeight.NORMAL, FontPosture.ITALIC);
                    }
                }
                case CodeBlock cb -> {
                    flow.setBackground(new Background(new BackgroundFill(Color.web("#f4f4f4"),
                            new CornerRadii(3),
                            Insets.EMPTY)));
                    flow.setPadding(new Insets(5));
                    Text codeText = new Text(cb.content());
                    codeText.setFont(Font.font("Consolas", 10));
                    flow.getChildren()
                        .add(codeText);
                }
                default -> { /* Do NOTHING */ }
            }
            pageContent.getChildren()
                       .add(flow);
        }

        HBox docView = new HBox(pageContent);
        if (!commentsPanel.getChildren()
                          .isEmpty()) {
            docView.getChildren()
                   .add(commentsPanel);
        }
        docView.setAlignment(Pos.CENTER);
        docView.setPadding(new Insets(20));
        docView.setBackground(new Background(new BackgroundFill(WORD_WORKSPACE_BG, CornerRadii.EMPTY, Insets.EMPTY)));

        ScrollPane scroll = new ScrollPane(docView);
        scroll.setFitToWidth(true);

        VBox layout = new VBox();
        // Simple Header
        HBox header = new HBox(new Label("Office-stamper Preview - " + theme));
        header.setPadding(new Insets(5, 10, 5, 10));
        header.setBackground(new Background(new BackgroundFill(WORD_BLUE, CornerRadii.EMPTY, Insets.EMPTY)));
        ((Label) header.getChildren()
                       .getFirst()).setTextFill(Color.WHITE);

        layout.getChildren()
              .addAll(header, scroll);
        VBox.setVgrow(scroll, Priority.ALWAYS);

        return new Scene(layout, 900, 700);
    }

    private Node createCommentNode(MacroBlock mb) {
        VBox commentBox = new VBox(5);
        commentBox.setPadding(new Insets(8));
        commentBox.setBackground(new Background(new BackgroundFill(COMMENT_BG, new CornerRadii(3), Insets.EMPTY)));
        commentBox.setBorder(new Border(new BorderStroke(COMMENT_BORDER,
                BorderStrokeStyle.SOLID,
                new CornerRadii(3),
                new BorderWidths(1))));

        String author = "";
        String value = "";
        for (String attr : mb.list()) {
            if (attr.startsWith("author=")) author = attr.substring(attr.indexOf('=') + 1)
                                                         .replace("\"", "");
            if (attr.startsWith("value=")) value = attr.substring(attr.indexOf('=') + 1)
                                                       .replace("\"", "");
        }

        Label authorLabel = new Label(author.isEmpty() ? "Author" : author);
        authorLabel.setFont(Font.font("Calibri", FontWeight.BOLD, 10));

        Text commentText = new Text(value);
        commentText.setFont(Font.font("Calibri", 10));
        commentText.setWrappingWidth(180);

        commentBox.getChildren()
                  .addAll(authorLabel, commentText);
        return commentBox;
    }
}
