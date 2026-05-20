package pro.verron.officestamper.asciidoc.test;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import pro.verron.officestamper.asciidoc.AsciiDocCompiler;

import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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
                
                comment::[id="c1", author="John Doe", value="A comment"]
                """;

        Scene scene = AsciiDocCompiler.toScene(asciidoc);
        assertNotNull(scene);
        // The root is VBox, second child is ScrollPane, its content is HBox, its second child should be the comments
        // panel VBox
        var root = (VBox) scene.getRoot();
        var scroll = (javafx.scene.control.ScrollPane) root.getChildren()
                                                           .get(1);
        var docView = (javafx.scene.layout.HBox) scroll.getContent();
        assertEquals(2,
                docView.getChildren()
                       .size(),
                "Should have page content and comments panel");
    }
}
