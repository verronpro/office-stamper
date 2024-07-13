package pro.verron.officestamper.test;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static pro.verron.officestamper.preset.OfficeStamperConfigurations.standardWithPreprocessing;

class BasicWordTest {
    @Test
    void testStamper() {
        var stamperConfiguration = standardWithPreprocessing();
        var stamper = new TestDocxStamper<>(stamperConfiguration);
        var templateStream = TestUtils.getResource(Path.of("word-base.docx"));

        record Person(String name) {}
        var context = new Person("Bart");
        var actual = stamper.stampAndLoadAndExtract(templateStream, context);
        var expected = """
                Hello, Bart!
                """;
        assertEquals(expected, actual);
    }
}
