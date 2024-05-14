package pro.verron.officestamper.test;

import org.junit.jupiter.api.Test;
import pro.verron.officestamper.api.OfficeStamperException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static pro.verron.officestamper.preset.OfficeStamperConfigurations.standard;
import static pro.verron.officestamper.test.TestUtils.getResource;


/**
 * @author Joseph Verron
 * @author Tom Hombergs
 */
class FailOnUnresolvedPlaceholderTest {
    @Test
    void fails()
            throws IOException {
        var context = new Name("Homer");
        try (var template = getResource("FailOnUnresolvedExpressionTest.docx")) {
            var config = standard()
                    .setFailOnUnresolvedExpression(true);
            var stamper = new TestDocxStamper<>(config);
            assertThrows(OfficeStamperException.class,
                    () -> stamper.stampAndLoad(template, context));
        }
    }

    @Test
    void doesNotFail()
            throws IOException {
        Name context = new Name("Homer");
        try (
                InputStream template = getResource(Path.of(
                        "FailOnUnresolvedExpressionTest.docx"))
        ) {
            var config = standard()
                    .setFailOnUnresolvedExpression(false);
            var stamper = new TestDocxStamper<>(config);
            assertDoesNotThrow(() -> stamper.stampAndLoad(template, context));
        }
    }

    public record Name(String name) {
    }

}
