package pro.verron.officestamper;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import picocli.CommandLine;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class MainCommandTest {

    @Test
    void shouldBindEnvVariables(@TempDir Path tempDir) throws Exception {
        // Create a template with ${env['PATH']} (or any other env var that should exist)
        WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
        pkg.getMainDocumentPart().addParagraphOfText("Path: ${env['PATH']}");
        Path template = tempDir.resolve("template.docx");
        pkg.save(template.toFile());

        Path data = tempDir.resolve("data.json");
        Files.writeString(data, "{}");

        // Should fail without --bind-env in dry-run because env is missing
        Main main1 = new Main();
        CommandLine cmd1 = new CommandLine(main1);
        int exitCode1 = cmd1.execute("-t", template.toString(), "-d", data.toString(), "--dry-run");
        assertNotEquals(0, exitCode1, "Should fail when env is not bound");

        // Should succeed with --bind-env
        Main main2 = new Main();
        CommandLine cmd2 = new CommandLine(main2);
        int exitCode2 = cmd2.execute("-t", template.toString(), "-d", data.toString(), "--dry-run", "--bind-env");
        assertEquals(0, exitCode2, "Should succeed when env is bound");
    }
}
