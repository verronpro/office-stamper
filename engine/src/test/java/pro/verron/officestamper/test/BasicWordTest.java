package pro.verron.officestamper.test;

import org.junit.jupiter.api.Test;
import pro.verron.officestamper.api.OfficeStamperException;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static pro.verron.officestamper.preset.OfficeStamperConfigurations.standard;

class BasicWordTest {
    @Test
    void testStamper() {
        var stamperConfiguration = standard();
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

    @Test
    void testMalformeStamper() {
        var stamperConfiguration = standard();
        var stamper = new TestDocxStamper<>(stamperConfiguration);
        var templateStream = TestUtils.getResource(Path.of("malformed-comment.docx"));

        record Person(String name) {}
        var context = new Person("Bart");
        assertThrows(OfficeStamperException.class, () -> stamper.stampAndLoadAndExtract(templateStream, context));
    }
}
