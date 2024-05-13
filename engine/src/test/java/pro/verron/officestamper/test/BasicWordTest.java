package pro.verron.officestamper.test;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static pro.verron.officestamper.preset.OfficeStamperConfigurations.standardWithPreprocessing;

public class BasicWordTest {
    @Test
    public void testStamper()
            throws IOException {
        var stamperConfiguration = standardWithPreprocessing();
        var stamper = new TestDocxStamper<>(stamperConfiguration);
        var templatePath = Path.of("test", "sources", "word-base.docx");
        var templateStream = Files.newInputStream(templatePath);
        record Person(String name) {}
        var context = new Person("Bart");
        var actual = stamper.stampAndLoadAndExtract(templateStream
                , context);
        var expected = "Hello, Bart!";
        assertEquals(expected, actual);
    }
}
