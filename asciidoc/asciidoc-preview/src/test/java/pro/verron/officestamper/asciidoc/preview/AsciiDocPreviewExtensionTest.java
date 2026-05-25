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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AsciiDocPreviewExtensionTest {

    @TempDir
    Path tempDir;

    @Test
    public void shouldGeneratePreviewImage()
            throws IOException {
        // Prepare a template adoc
        Path templatePath = tempDir.resolve("template.adoc");
        Files.writeString(templatePath, "= Template Title\n\nThis is a template paragraph.");

        // Main adoc with the macro
        String adoc = "preview::template.adoc[theme=word,format=png,dpi=96]";

        try (Asciidoctor asciidoctor = Asciidoctor.Factory.create()) {
            asciidoctor.javaExtensionRegistry()
                       .blockMacro(AsciiDocPreviewBlockMacro.class);
            Options options = Options.builder()
                                     .safe(SafeMode.UNSAFE)
                                     .baseDir(tempDir.toFile())
                                     .attributes(Attributes.builder()
                                                           .attribute("docdir",
                                                                   tempDir.toAbsolutePath()
                                                                          .toString())
                                                           .attribute("outdir",
                                                                   tempDir.toAbsolutePath()
                                                                          .toString())
                                                           .build())
                                     .build();

            String html = asciidoctor.convert(adoc, options);

            // Check if image was generated
            Path imagePath = tempDir.resolve("template-word-96.png");
            assertTrue(Files.exists(imagePath), "Image should be generated at " + imagePath);
            assertTrue(html.contains("src=\"template-word-96.png\""), "HTML should contain image tag");
        }
    }

    @Test
    public void shouldCachePreviewImage()
            throws IOException, InterruptedException {
        // Prepare a template adoc
        Path templatePath = tempDir.resolve("template.adoc");
        Files.writeString(templatePath, "= Template Title\n\nThis is a template paragraph.");

        // Main adoc with the macro
        String adoc = "preview::template.adoc[theme=word,format=png,dpi=96]";

        try (Asciidoctor asciidoctor = Asciidoctor.Factory.create()) {
            asciidoctor.javaExtensionRegistry()
                       .blockMacro(AsciiDocPreviewBlockMacro.class);
            Options options = Options.builder()
                                     .safe(SafeMode.UNSAFE)
                                     .baseDir(tempDir.toFile())
                                     .attributes(Attributes.builder()
                                                           .attribute("docdir",
                                                                   tempDir.toAbsolutePath()
                                                                          .toString())
                                                           .attribute("outdir",
                                                                   tempDir.toAbsolutePath()
                                                                          .toString())
                                                           .build())
                                     .build();

            // First run
            asciidoctor.convert(adoc, options);
            Path imagePath = tempDir.resolve("template-word-96.png");
            long firstModified = Files.getLastModifiedTime(imagePath)
                                      .toMillis();

            // Wait a bit to ensure time difference if it were rewritten
            Thread.sleep(100);

            // Second run
            asciidoctor.convert(adoc, options);
            long secondModified = Files.getLastModifiedTime(imagePath)
                                       .toMillis();

            assertEquals(firstModified, secondModified, "Image should not be regenerated if source hasn't changed");

            // Update source
            Thread.sleep(100);
            Files.writeString(templatePath, "= Updated Title\n\nNew content.");
            asciidoctor.convert(adoc, options);
            long thirdModified = Files.getLastModifiedTime(imagePath)
                                      .toMillis();

            assertTrue(thirdModified > firstModified, "Image should be regenerated if source changed");
        }
    }
}
