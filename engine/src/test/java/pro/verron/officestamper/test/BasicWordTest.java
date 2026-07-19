package pro.verron.officestamper.test;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pro.verron.officestamper.api.OfficeStamperException;
import pro.verron.officestamper.test.utils.OfficeStamperTestBase;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static pro.verron.officestamper.preset.OfficeStamperConfigurations.full;
import static pro.verron.officestamper.preset.OfficeStampers.docxPackageStamper;
import static pro.verron.officestamper.test.utils.ResourceUtils.getWordResource;

@DisplayName("Basic Word Test")
class BasicWordTest extends OfficeStamperTestBase {
    @Test
    @DisplayName("Should stamp a Word document")
    void testStamper() {
        record Person(String name) {
        }
        var config = full();
        var context = new Person("Bart");
        var template = getWordResource(Path.of("word-base.docx"));
        var expected = """
                Hello, Bart!
                
                // section {docGrid={linePitch=360}, pgMar={bottom=1417, footer=708, header=708, left=1417, right=1417, top=1417}, pgSz={h=16838, w=11906}, space=708}
                
                """;
        testStamper(config, context, template, expected);
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
