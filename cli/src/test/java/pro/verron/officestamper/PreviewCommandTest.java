package pro.verron.officestamper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import picocli.CommandLine;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PreviewCommandTest {

    @Test
    void shouldGeneratePreviewViaCli(@TempDir Path tempDir)
            throws Exception {
        Path input = tempDir.resolve("test.adoc");
        Files.writeString(input, """
                = Test
                
                Content.
                """);
        Path output = tempDir.resolve("preview.png");

        Main main = new Main();
        CommandLine cmd = new CommandLine(main);
        int exitCode = cmd.execute("preview", "-i", input.toString(), "-o", output.toString(), "--theme", "gdocs");

        assertEquals(0, exitCode, "Command should succeed");
        assertTrue(Files.exists(output), "Output file should be created");
        assertTrue(Files.size(output) > 0, "Output file should not be empty");
    }

    @Test
    void shouldGenerateSvgPreviewViaCli(@TempDir Path tempDir)
            throws Exception {
        Path input = tempDir.resolve("test.adoc");
        Files.writeString(input, "= Test");
        Path output = tempDir.resolve("preview.svg");

        Main main = new Main();
        CommandLine cmd = new CommandLine(main);
        int exitCode = cmd.execute("preview", "-i", input.toString(), "-o", output.toString(), "--format", "svg");

        assertEquals(0, exitCode);
        assertTrue(Files.exists(output));
        String content = Files.readString(output);
        assertTrue(content.contains("<svg"));
    }
}
