package pro.verron.officestamper.test;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pro.verron.officestamper.api.OfficeStamperException;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static pro.verron.officestamper.preset.OfficeStamperConfigurations.full;
import static pro.verron.officestamper.preset.OfficeStampers.docxPackageStamper;
import static pro.verron.officestamper.test.TestUtils.getWordResource;

@DisplayName("Basic Word Test") class BasicWordTest {
    @Test
    @DisplayName("Should stamp a Word document")
    void testStamper() {
        var configuration = full();
        var stamper = docxPackageStamper(configuration);
        var path = Path.of("word-base.docx");
        var template = getWordResource(path);
        record Person(String name) {}
        var context = new Person("Bart");
        var stamped = stamper.stamp(template, context);
        var expected = """
                Hello, Bart!
                
                """;
        var actual = Stringifier.stringifyWord(stamped);
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Should fail on malformed comment")
    void testMalformedStamper() {
        var configuration = full();
        var stamper = docxPackageStamper(configuration);
        var template = getWordResource("malformed-comment.docx");

        record Person(String name) {}
        var context = new Person("Bart");
        assertThrows(OfficeStamperException.class, () -> stamper.stamp(template, context));
    }
}
