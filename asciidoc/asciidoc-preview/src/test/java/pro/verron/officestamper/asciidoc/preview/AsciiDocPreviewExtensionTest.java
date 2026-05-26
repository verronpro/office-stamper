package pro.verron.officestamper.asciidoc.preview;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.Attributes;
import org.asciidoctor.Options;
import org.asciidoctor.SafeMode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.nio.file.Files.getLastModifiedTime;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AsciiDocPreviewExtensionTest {

    @TempDir Path tempDir;

    @Test
    public void shouldGeneratePreviewImage()
            throws IOException {
        // Prepare a template adoc
        var templatePath = tempDir.resolve("template.adoc");
        Files.writeString(templatePath, """
                = Template Title
                
                This is a template paragraph.""");

        // Main adoc with the macro
        var adoc = "preview::template.adoc[theme=word,format=png,dpi=96]";

        try (var asciidoctor = Asciidoctor.Factory.create()) {
            var extensionRegistry = asciidoctor.javaExtensionRegistry();
            extensionRegistry.blockMacro(AsciiDocPreviewBlockMacro.class);

            var tempDirAbsolutePath = tempDir.toAbsolutePath();
            var attributes = Attributes.builder()
                                       .attribute("docdir", tempDirAbsolutePath.toString())
                                       .attribute("outdir", tempDirAbsolutePath.toString())
                                       .build();
            var options = Options.builder()
                                 .safe(SafeMode.UNSAFE)
                                 .baseDir(tempDir.toFile())
                                 .attributes(attributes)
                                 .build();

            var html = asciidoctor.convert(adoc, options);

            // Check if image was generated
            var imagePath = tempDir.resolve("template-word-96.png");
            assertTrue(Files.exists(imagePath), "Image should be generated at " + imagePath);
            assertTrue(html.contains("src=\"template-word-96.png\""), "HTML should contain image tag");
        }
    }

    @Test
    public void shouldCachePreviewImage()
            throws IOException, InterruptedException {
        // Prepare a template adoc
        var templatePath = tempDir.resolve("template.adoc");
        Files.writeString(templatePath, """
                = Template Title
                
                This is a template paragraph.""");

        // Main adoc with the macro
        var adoc = "preview::template.adoc[theme=word,format=png,dpi=96]";

        try (var asciidoctor = Asciidoctor.Factory.create()) {
            var javaExtensionRegistry = asciidoctor.javaExtensionRegistry();
            javaExtensionRegistry.blockMacro(AsciiDocPreviewBlockMacro.class);

            var tempDirAbsolutePath = tempDir.toAbsolutePath();
            var attributes = Attributes.builder()
                                       .attribute("docdir", tempDirAbsolutePath.toString())
                                       .attribute("outdir", tempDirAbsolutePath.toString())
                                       .build();
            var options = Options.builder()
                                 .safe(SafeMode.UNSAFE)
                                 .baseDir(tempDir.toFile())
                                 .attributes(attributes)
                                 .build();

            // First run
            asciidoctor.convert(adoc, options);
            var imagePath = tempDir.resolve("template-word-96.png");
            var firstModified = getLastModifiedTime(imagePath).toMillis();

            // Wait a bit to ensure time difference if it were rewritten
            Thread.sleep(100);

            // Second run
            asciidoctor.convert(adoc, options);
            var secondModified = getLastModifiedTime(imagePath).toMillis();

            assertEquals(firstModified, secondModified, "Image should not be regenerated if source hasn't changed");

            // Update source
            Thread.sleep(100);
            Files.writeString(templatePath, "= Updated Title\n\nNew content.");
            asciidoctor.convert(adoc, options);
            var thirdModified = getLastModifiedTime(imagePath).toMillis();

            assertTrue(thirdModified > firstModified, "Image should be regenerated if source changed");
        }
    }
}
