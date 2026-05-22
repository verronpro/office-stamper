package pro.verron.officestamper.asciidoc;

import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.jspecify.annotations.Nullable;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import static java.lang.Thread.currentThread;

/// Facade utilities to parse AsciiDoc and compile it to different targets.
public final class AsciiDocCompiler {

    private static final AsciiDocToHtml MODEL_TO_HTML = new AsciiDocToHtml();
    private static final AsciiDocToFx MODEL_TO_SCENE = new AsciiDocToFx();
    private static final AsciiDocParser ASCIIDOC_TO_MODEL = new AsciiDocParser();
    private static final AsciiDocToDocx MODEL_TO_DOCX = new AsciiDocToDocx();

    private AsciiDocCompiler() {
        throw new IllegalStateException("Utility class");
    }

    /// Compiles the AsciiDoc source text directly to a WordprocessingMLPackage.
    ///
    /// @param asciidoc source text
    ///
    /// @return package with rendered content
    public static WordprocessingMLPackage toDocx(String asciidoc) {
        return toDocx(toModel(asciidoc));
    }

    /// Compiles the parsed model to a WordprocessingMLPackage.
    ///
    /// @param model parsed model
    ///
    /// @return package with rendered content
    public static WordprocessingMLPackage toDocx(AsciiDocModel model) {
        return MODEL_TO_DOCX.apply(model);
    }

    /// Parses AsciiDoc source text into an [AsciiDocModel].
    ///
    /// @param asciidoc source text
    ///
    /// @return parsed model
    public static AsciiDocModel toModel(String asciidoc) {
        return ASCIIDOC_TO_MODEL.apply(asciidoc);
    }

    /// Compiles the parsed model to a JavaFX Scene.
    ///
    /// @param model parsed model
    ///
    /// @return scene with rendered content
    public static Scene toScene(AsciiDocModel model) {
        return MODEL_TO_SCENE.apply(model);
    }

    /// Compiles the AsciiDoc source text directly to HTML.
    ///
    /// @param asciidoc source text
    ///
    /// @return HTML representation
    public static String toHtml(String asciidoc) {
        var model = ASCIIDOC_TO_MODEL.apply(asciidoc);
        return MODEL_TO_HTML.apply(model);
    }

    /// Compiles the parsed model to HTML.
    ///
    /// @param model parsed model
    ///
    /// @return HTML representation
    public static String toHtml(AsciiDocModel model) {
        return MODEL_TO_HTML.apply(model);
    }

    /// Compiles a WordprocessingMLPackage into the textual AsciiDoc representation used by tests. This mirrors the
    /// legacy Stringifier output to preserve expectations.
    ///
    /// @param pkg a Word document package
    ///
    /// @return textual representation
    public static String toAsciidoc(WordprocessingMLPackage pkg) {
        return toAsciidoc(pkg, false);
    }

    /// Converts the given WordprocessingMLPackage into its textual AsciiDoc representation.
    ///
    /// @param pkg a Word document package
    /// @param skipComments whether to omit comments from the output
    /// @return the textual AsciiDoc representation of the Word document package
    public static String toAsciidoc(WordprocessingMLPackage pkg, boolean skipComments) {
        var model = toModel(pkg);
        return toAsciidoc(model, skipComments);
    }

    /// Parses a Word document into an [AsciiDocModel].
    ///
    /// @param pkg a Word document package
    ///
    /// @return parsed model
    public static AsciiDocModel toModel(WordprocessingMLPackage pkg) {
        var compiler = new DocxToAsciiDoc(pkg);
        return compiler.apply(pkg);
    }

    /// Converts the given AsciiDoc model into its textual AsciiDoc representation.
    ///
    /// @param model the parsed AsciiDoc model to be converted
    /// @param skipComments whether to omit comments from the output
    /// @return the textual AsciiDoc representation of the model
    public static String toAsciidoc(AsciiDocModel model, boolean skipComments) {
        return new AsciiDocToText(skipComments).apply(model);
    }

    /// Converts the given AsciiDoc model into its textual AsciiDoc representation.
    ///
    /// @param model the parsed AsciiDoc model to be converted
    /// @return the textual AsciiDoc representation of the model
    public static String toAsciidoc(AsciiDocModel model) {
        return new AsciiDocToText(false).apply(model);
    }

    /// Compiles the AsciiDoc source text directly to a PNG image file.
    ///
    /// @param asciidoc source text
    /// @param path path to save the image
    public static void toImage(String asciidoc, Path path) {
        toImage(toScene(asciidoc), path);
    }

    /// Saves the given JavaFX Scene as a PNG image file.
    ///
    /// @param scene JavaFX scene
    /// @param path path to save the image
    public static void toImage(Scene scene, Path path) {
        if (Platform.isFxApplicationThread()) saveSnapshot(scene, path);
        else saveSnapshotInFxApplicationThread(scene, path);
    }

    /// Compiles the AsciiDoc source text directly to a JavaFX Scene.
    ///
    /// @param asciidoc source text
    ///
    /// @return scene with rendered content
    public static Scene toScene(String asciidoc) {
        var model = ASCIIDOC_TO_MODEL.apply(asciidoc);
        return MODEL_TO_SCENE.apply(model);
    }

    private static void saveSnapshot(Scene scene, Path path) {
        try {
            var image = scene.snapshot(null);
            var bufferedImage = SwingFXUtils.fromFXImage(image, null);
            ImageIO.write(bufferedImage, "png", path.toFile());
        } catch (IOException e) {
            throw new RuntimeException("IO error while saving scene as image", e);
        }
    }

    private static void saveSnapshotInFxApplicationThread(Scene scene, Path path) {
        Runnable runnable = () -> saveSnapshot(scene, path);
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<@Nullable Throwable> error = new AtomicReference<@Nullable Throwable>();

        Platform.runLater(() -> {
            try {
                runnable.run();
            } catch (Throwable t) {
                error.set(t);
            } finally {
                latch.countDown();
            }
        });
        try {
            latch.await();
            if (error.get() != null) throw new RuntimeException("Failed to save scene as image", error.get());
        } catch (InterruptedException e) {
            currentThread().interrupt();
            throw new RuntimeException("Interrupted while saving scene as image", e);
        }
    }

    /// Compiles the AsciiDoc source text directly to a JavaFX Scene and displays it in a new Stage.
    /// This method is primarily intended for debugging and manual verification.
    /// It waits for the Stage to be closed before returning.
    ///
    /// @param asciidoc source text
    public static void show(String asciidoc) {
        show(toScene(asciidoc));
    }

    /// Displays the given JavaFX Scene in a new Stage.
    /// This method is primarily intended for debugging and manual verification.
    /// It waits for the Stage to be closed before returning.
    ///
    /// @param scene JavaFX scene to display
    public static void show(Scene scene) {
        var latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            var stage = new Stage();
            stage.setScene(scene);
            stage.setTitle("Office-stamper Preview");
            stage.setOnCloseRequest(e -> latch.countDown());
            stage.show();
        });
        try {
            latch.await();
        } catch (InterruptedException e) {
            currentThread().interrupt();
        }
    }
}
