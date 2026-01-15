package pro.verron.officestamper.asciidoc;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.*;
import javafx.scene.text.Text;

import java.util.List;

import static pro.verron.officestamper.asciidoc.AsciiDocModel.*;

/// Renders [AsciiDocModel] into a JavaFX [Scene].
public final class AsciiDocToFx {
    private AsciiDocToFx() {}

    /// Compiles the model into a simple scrollable Scene using a VBox of TextFlow nodes.
    ///
    /// Headings are rendered with larger font sizes; bold/italic are applied per inline fragment.
    ///
    /// @param model parsed AsciiDoc model
    ///
    /// @return JavaFX scene containing the rendered content
    public static Scene compileToScene(AsciiDocModel model) {
        var rootBox = new VBox(8.0);
        rootBox.setPadding(new Insets(16));

        for (Block block : model.getBlocks()) {
            TextFlow flow = new TextFlow();
            if (block instanceof Heading(int level, List<Inline> inlines)) {
                for (Inline inline : inlines) {
                    emitInline(flow, inline, fontForHeading(level), FontWeight.NORMAL, FontPosture.REGULAR);
                }
            }
            else if (block instanceof Paragraph(List<Inline> inlines)) {
                for (Inline inline : inlines) {
                    emitInline(flow, inline, Font.getDefault(), FontWeight.NORMAL, FontPosture.REGULAR);
                }
            }
            else if (block instanceof UnorderedList(List<ListItem> items1)) {
                for (ListItem item : items1) {
                    TextFlow itemFlow = new TextFlow();
                    itemFlow.getChildren()
                            .add(new Text("• "));
                    for (Inline inline : item.inlines()) {
                        emitInline(itemFlow, inline, Font.getDefault(), FontWeight.NORMAL, FontPosture.REGULAR);
                    }
                    rootBox.getChildren()
                           .add(itemFlow);
                }
                continue;
            }
            else if (block instanceof OrderedList(List<ListItem> items)) {
                int i = 1;
                for (ListItem item : items) {
                    TextFlow itemFlow = new TextFlow();
                    itemFlow.getChildren()
                            .add(new Text((i++) + ". "));
                    for (Inline inline : item.inlines()) {
                        emitInline(itemFlow, inline, Font.getDefault(), FontWeight.NORMAL, FontPosture.REGULAR);
                    }
                    rootBox.getChildren()
                           .add(itemFlow);
                }
                continue;
            }
            else if (block instanceof Blockquote(List<Inline> inlines)) {
                flow.setPadding(new Insets(0, 0, 0, 20));
                for (Inline inline : inlines) {
                    emitInline(flow, inline, Font.getDefault(), FontWeight.NORMAL, FontPosture.ITALIC);
                }
            }
            else if (block instanceof CodeBlock cb) {
                flow.setStyle("-fx-background-color: #f4f4f4; -fx-font-family: 'monospace';");
                flow.getChildren()
                    .add(new Text(cb.content()));
            }
            else if (block instanceof ImageBlock(String url, String altText)) {
                flow.getChildren()
                    .add(new Text("[Image: " + url + " - " + altText + "]"));
            }
            rootBox.getChildren()
                   .add(flow);
        }

        var scroll = new ScrollPane(rootBox);
        scroll.setFitToWidth(true);
        VBox.setVgrow(scroll, Priority.ALWAYS);
        return new Scene(scroll, 800, 600);
    }

    private static void emitInline(TextFlow flow, Inline inline, Font base, FontWeight weight, FontPosture posture) {
        if (inline instanceof AsciiDocModel.Text(String text)) {
            flow.getChildren()
                .add(styledText(text, base, weight, posture));
            return;
        }
        if (inline instanceof Bold(List<Inline> children)) {
            FontWeight nextW = FontWeight.BOLD; // bold overrides
            for (Inline child : children) {
                emitInline(flow, child, base, nextW, posture);
            }
            return;
        }
        if (inline instanceof Italic(List<Inline> children)) {
            FontPosture nextP = FontPosture.ITALIC;
            for (Inline child : children) {
                emitInline(flow, child, base, weight, nextP);
            }
            return;
        }
        if (inline instanceof Link link) {
            Text t = styledText(link.text(), base, weight, posture);
            t.setUnderline(true);
            t.setFill(javafx.scene.paint.Color.BLUE);
            flow.getChildren()
                .add(t);
        }
        if (inline instanceof InlineImage ii) {
            flow.getChildren()
                .add(new Text("[Image: " + ii.url() + "]"));
        }
    }

    private static Font fontForHeading(int level) {
        double size = switch (level) {
            case 1 -> 24;
            case 2 -> 20;
            case 3 -> 18;
            case 4 -> 16;
            case 5 -> 14;
            default -> 13;
        };
        return Font.font(Font.getDefault()
                             .getFamily(), FontWeight.BOLD, size);
    }

    private static Text styledText(String value, Font base, FontWeight weight, FontPosture posture) {
        Text t = new Text(value);
        t.setFont(Font.font(base.getFamily(), weight, posture, base.getSize()));
        return t;
    }
}
