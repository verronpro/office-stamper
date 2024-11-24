package pro.verron.officestamper.test;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import pro.verron.officestamper.api.OfficeStamperException;
import pro.verron.officestamper.preset.ExceptionResolvers;

import java.io.IOException;
import java.nio.file.Path;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.argumentSet;
import static pro.verron.officestamper.preset.OfficeStamperConfigurations.standard;
import static pro.verron.officestamper.test.ContextFactory.mapContextFactory;
import static pro.verron.officestamper.test.ContextFactory.objectContextFactory;
import static pro.verron.officestamper.test.TestUtils.getResource;


/// @author Joseph Verron
/// @author Tom Hombergs
class FailOnUnresolvedPlaceholderTest {

    static Stream<Arguments> factories() {
        return Stream.of(argumentSet("obj", objectContextFactory()), argumentSet("map", mapContextFactory()));
    }

    @MethodSource("factories")
    @ParameterizedTest
    void fails(ContextFactory factory)
            throws IOException {
        var context = factory.name("Homer");
        try (var template = getResource("FailOnUnresolvedExpressionTest.docx")) {
            var config = standard().setExceptionResolver(ExceptionResolvers.throwing());
            var stamper = new TestDocxStamper<>(config);
            assertThrows(OfficeStamperException.class, () -> stamper.stampAndLoad(template, context));
        }
    }

    @MethodSource("factories")
    @ParameterizedTest
    void doesNotFail(ContextFactory factory)
            throws IOException {
        var context = factory.name("Homer");
        try (var template = getResource(Path.of("FailOnUnresolvedExpressionTest.docx"))) {
            var config = standard().setExceptionResolver(ExceptionResolvers.passing());
            var stamper = new TestDocxStamper<>(config);
            assertDoesNotThrow(() -> stamper.stampAndLoad(template, context));
        }
    }
}
