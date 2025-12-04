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
        FontWeight w = weight != null ? weight : FontWeight.NORMAL;
        FontPosture p = posture != null ? posture : FontPosture.REGULAR;
        Text t = new Text(value);
        t.setFont(Font.font(base.getFamily(), w, p, base.getSize()));
        return t;
    }
}
