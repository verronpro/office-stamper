package pro.verron.officestamper.test;

import org.junit.jupiter.api.Test;
import pro.verron.officestamper.api.OfficeStamperException;
import pro.verron.officestamper.preset.ExceptionResolvers;

import java.io.IOException;
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

    public static final ContextFactory FACTORY = new ContextFactory();

    @Test void fails()
            throws IOException {
        var context = FACTORY.name("Homer");
        try (var template = getResource("FailOnUnresolvedExpressionTest.docx")) {
            var config = standard().setExceptionResolver(ExceptionResolvers.throwing());
            var stamper = new TestDocxStamper<>(config);
            assertThrows(OfficeStamperException.class, () -> stamper.stampAndLoad(template, context));
        }
    }

    @Test void doesNotFail()
            throws IOException {
        var context = FACTORY.name("Homer");
        try (var template = getResource(Path.of("FailOnUnresolvedExpressionTest.docx"))) {
            var config = standard().setExceptionResolver(ExceptionResolvers.passing());
            var stamper = new TestDocxStamper<>(config);
            assertDoesNotThrow(() -> stamper.stampAndLoad(template, context));
        }
    }
}
