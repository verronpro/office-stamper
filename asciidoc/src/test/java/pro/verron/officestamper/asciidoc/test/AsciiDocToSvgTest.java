package pro.verron.officestamper.asciidoc.test;

import org.junit.jupiter.api.Test;
import pro.verron.officestamper.asciidoc.AsciiDocCompiler;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AsciiDocToSvgTest {

    @Test
    void shouldRenderModelWithCommentToSvg() {
        String asciidoc = """
                = Document
                
                Some text.
                
                comment::[start="1,0", id="c1", author="John Doe", value="A comment"]
                """;
        String svg = AsciiDocCompiler.toSvg(asciidoc);

        assertNotNull(svg);
        assertTrue(svg.contains("<svg"), "Rendered output should be SVG");
        assertTrue(svg.contains("Document"), "SVG should include heading text");
        assertTrue(svg.contains("Some text."), "SVG should include body text");
        assertTrue(svg.contains("Word"), "SVG should simulate Word interface");
        assertTrue(svg.contains("#2b579a"), "SVG should have Word blue color");
        assertTrue(svg.contains("#fff2cc"), "Commented text should be highlighted");
        assertTrue(svg.contains("John Doe"), "Comment author should be present");
        assertTrue(svg.contains("A comment"), "Comment value should be present");
    }

    @Test
    void shouldRenderGoogleDocsTheme() {
        String asciidoc = """
                :theme: gdocs
                = GDocs Doc
                
                GDocs content.
                
                comment::[start="1,0", id="c1", author="Jane Smith", value="GDocs comment"]
                """;
        String svg = AsciiDocCompiler.toSvg(asciidoc);

        assertNotNull(svg);
        assertTrue(svg.contains("GDocs Doc"));
        assertTrue(svg.contains("#f8f9fa"), "Should have Google Docs background color");
        assertTrue(svg.contains("#c2e7ff"), "Should have Google Docs highlight color");
        assertTrue(svg.contains("Jane Smith"));
    }

    @Test
    void shouldRenderLibreOfficeTheme() {
        String asciidoc = """
                :theme: libre
                = Libre Doc
                
                Libre content.
                
                comment::[start="1,0", id="c1", author="Libre User", value="Libre comment"]
                """;
        String svg = AsciiDocCompiler.toSvg(asciidoc);

        assertNotNull(svg);
        assertTrue(svg.contains("Libre Doc"));
        assertTrue(svg.contains("LibreOffice Writer"));
        assertTrue(svg.contains("#dfdfdf"), "Should have LibreOffice background color");
        assertTrue(svg.contains("#ffff00"), "Should have LibreOffice highlight color");
    }

    @Test
    void shouldSaveSvgAsPng()
            throws Exception {
        String asciidoc = """
                = Document
                
                Some text.
                
                comment::[start="1,0", id="c1", author="John Doe", value="A comment"]
                """;
        Path path = Path.of("target/test-image.png");
        Files.deleteIfExists(path);

        AsciiDocCompiler.toImage(asciidoc, path);

        assertTrue(Files.exists(path), "PNG file should be created");
        assertTrue(Files.size(path) > 0, "PNG file should not be empty");
    }
}
