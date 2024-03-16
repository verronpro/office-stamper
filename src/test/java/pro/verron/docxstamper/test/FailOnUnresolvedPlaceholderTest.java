package pro.verron.docxstamper.test;

import org.junit.jupiter.api.Test;
import pro.verron.docxstamper.api.OpcStamperException;
import pro.verron.docxstamper.preset.OpcStamperConfigurations;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static pro.verron.docxstamper.test.DefaultTests.getResource;

/**
 * @author Joseph Verron
 * @author Tom Hombergs
 */
class FailOnUnresolvedPlaceholderTest {
    @Test
    void fails() throws IOException {
        var context = new Name("Homer");
        try (var template = getResource(Path.of("FailOnUnresolvedExpressionTest" +
                                                ".docx"))) {
            var config = OpcStamperConfigurations.standard()
                    .setFailOnUnresolvedExpression(true);
            var stamper = new TestDocxStamper<>(config);
            assertThrows(OpcStamperException.class,
                         () -> stamper.stampAndLoad(template, context));
        }
    }

    @Test
    void doesNotFail() throws IOException {
        Name context = new Name("Homer");
        try (InputStream template = getResource(Path.of(
                "FailOnUnresolvedExpressionTest.docx"))) {
            var config = OpcStamperConfigurations.standard()
                    .setFailOnUnresolvedExpression(false);
            var stamper = new TestDocxStamper<>(config);
            assertDoesNotThrow(() -> stamper.stampAndLoad(template, context));
        }
    }

    public record Name(String name) {
    }

}
