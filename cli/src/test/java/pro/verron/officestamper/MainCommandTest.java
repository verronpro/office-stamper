package pro.verron.officestamper;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import picocli.CommandLine;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class MainCommandTest {

    static String templatePath;
    static String dataPath;

    @BeforeAll
    static void setup(@TempDir Path tempDir) throws Docx4JException, IOException {
        // Create a template with ${env['PATH']} (or any other env var that should exist)
        var template = createTemplate(tempDir.resolve("template.docx"), "Path: ${env['PATH']}");
        templatePath = template.toString();

        var data = tempDir.resolve("data.json");
        var emptyJson = "{}";
        Files.writeString(data, emptyJson);
        var dataPath = data.toString();
    }

    private static Path createTemplate(Path path, String paragraph) throws Docx4JException {
        var pkg = WordprocessingMLPackage.createPackage();
        var mainDocumentPart = pkg.getMainDocumentPart();
        mainDocumentPart.addParagraphOfText(paragraph);
        pkg.save(path.toFile());
        return path;
    }

    @Test
    void shouldFailWhenNeedingEnv() {
        var main = new Main();
        var cmd = new CommandLine(main);
        int exitCode = cmd.execute("stamp", "-t", templatePath, "-d", dataPath, "--dry-run");
        assertNotEquals(0, exitCode, "Should fail when env is not bound");
    }

    @Test
    void shouldBindEnvVariables(@TempDir Path tempDir) {
        Main main = new Main();
        CommandLine cmd = new CommandLine(main);
        int exitCode = cmd.execute("stamp", "-t", templatePath, "-d", dataPath, "--dry-run", "--bind-env");
        assertEquals(0, exitCode, "Should succeed when env is bound");
    }
}
