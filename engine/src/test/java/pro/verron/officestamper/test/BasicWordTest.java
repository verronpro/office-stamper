package pro.verron.officestamper.test;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pro.verron.officestamper.api.OfficeStamperException;
import pro.verron.officestamper.preset.OfficeStampers;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static pro.verron.officestamper.preset.OfficeStamperConfigurations.full;

@DisplayName("Basic Word Test") class BasicWordTest {
    @Test
    @DisplayName("Should stamp a Word document")
    void testStamper() {
        var configuration = full();
        var stamper = OfficeStampers.docxPackageStamper(configuration);
        var path = Path.of("word-base.docx");
        var template = TestUtils.getWordResource(path);
        record Person(String name) {}
        var context = new Person("Bart");
        var actual = stamper.stamp(template, context);
        var expected = """
                Hello, Bart!
                
                """;
        assertEquals(expected, Stringifier.stringifyWord(actual));
    }

    @Test
    @DisplayName("Should fail on malformed comment")
    void testMalformeStamper() {
        var configuration = full();
        var stamper = OfficeStampers.docxPackageStamper(configuration);
        var template = TestUtils.getWordResource(Path.of("malformed-comment.docx"));

        record Person(String name) {}
        var context = new Person("Bart");
        assertThrows(OfficeStamperException.class, () -> stamper.stamp(template, context));
    }
}
