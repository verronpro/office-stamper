package pro.verron.officestamper.asciidoc.test;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import pro.verron.officestamper.asciidoc.AsciiDocCompiler;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.*;

class AsciiDocToFxTest {

    @BeforeAll
    static void initJavaFX()
            throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        try {
            Platform.startup(latch::countDown);
        } catch (IllegalStateException e) {
            // JavaFX already started
            latch.countDown();
        }
        latch.await();
    }

    @Test
    void shouldRenderModelWithCommentToScene() {
        String asciidoc = """
                = Document
                
                Some text.
                
                comment::[start="0,0", end="0,1", id="c1", author="John Doe", value="A comment"]
                """;
        Scene scene = AsciiDocCompiler.toScene(asciidoc);
        if (Boolean.getBoolean("office-stamper.test.show-preview")) AsciiDocCompiler.show(scene);

        assertNotNull(scene);
        // The root is VBox,
        // second child is ScrollPane, its content is HBox,
        // its second child should be the comments panel VBox
        var root = (VBox) scene.getRoot();
        var rootChildren = root.getChildren();
        var scroll = (ScrollPane) rootChildren.get(1);
        var docView = (HBox) scroll.getContent();
        var docViewChildren = docView.getChildren();
        assertEquals(2, docViewChildren.size(), "Should have page content and comments panel");
    }

    @Test
    void shouldSaveSceneAsImage()
            throws Exception {
        String asciidoc = "= Test";
        Scene scene = AsciiDocCompiler.toScene(asciidoc);
        Path path = Path.of("target/test-image.png");
        Files.deleteIfExists(path);

        AsciiDocCompiler.toImage(scene, path);

        assertTrue(Files.exists(path), "Image file should be created");
        assertTrue(Files.size(path) > 0, "Image file should not be empty");
        Files.deleteIfExists(path);
    }
}
