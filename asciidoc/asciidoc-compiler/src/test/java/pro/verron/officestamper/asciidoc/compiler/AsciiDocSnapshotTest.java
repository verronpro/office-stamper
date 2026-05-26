package pro.verron.officestamper.asciidoc.compiler;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import pro.verron.officestamper.utils.test.SnapshotUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

class AsciiDocSnapshotTest {

    private static final Path GOLDEN_DIR = Path.of("src/test/resources/golden");
    private static final Path ACTUAL_DIR = Path.of("target/test-snapshots");

    @BeforeAll
    static void setup()
            throws IOException {
        Files.createDirectories(ACTUAL_DIR);
    }

    @Test
    void snapshotWordTheme()
            throws IOException {
        runSnapshotTest("word-basic.adoc", "word-basic.png");
    }

    private void runSnapshotTest(String adocName, String pngName)
            throws IOException {
        var asciidoc = """
                = Document Title
                
                This is a very long paragraph that should definitely wrap into multiple lines because it exceeds the maximum width of the page. We want to make sure that the text wrapping logic is working correctly for all themes and that the line height is preserved.
                
                * Item 1 with some long text that might also wrap if it is long enough for the current page width.
                * Item 2
                
                [source,java]
                ----
                public class Hello {
                    public static void main(String[] args) {
                        System.out.println("Hello World");
                    }
                }
                ----
                
                ____
                This is a blockquote that is also very long and should wrap into multiple lines. The vertical line on the left should grow to match the height of the wrapped text.
                ____
                
                comment::[id="c1", author="John Doe", value="This is a multi-line comment. It is long enough to wrap into several lines in the side panel.", start="1,0"]
                """;

        // Handle theme based on name
        if (adocName.startsWith("gdocs")) asciidoc = ":theme: gdocs\n" + asciidoc;
        else if (adocName.startsWith("libre")) asciidoc = ":theme: libre\n" + asciidoc;

        var actualPath = ACTUAL_DIR.resolve(pngName);
        AsciiDocCompiler.toImage(asciidoc, actualPath);

        var goldenPath = GOLDEN_DIR.resolve(pngName);
        SnapshotUtils.assertSnapshotMatch(actualPath, goldenPath, 0.02);
    }

    @Test
    void snapshotGoogleDocsTheme()
            throws IOException {
        runSnapshotTest("gdocs-basic.adoc", "gdocs-basic.png");
    }

    @Test
    void snapshotLibreOfficeTheme()
            throws IOException {
        runSnapshotTest("libre-basic.adoc", "libre-basic.png");
    }
}
