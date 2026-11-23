package pro.verron.officestamper.docx4j17.test;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import pro.verron.officestamper.api.OfficeStamperException;
import pro.verron.officestamper.preset.ExceptionResolvers;
import pro.verron.officestamper.docx4j17.test.utils.ContextFactory;

import java.nio.file.Path;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.argumentSet;
import static pro.verron.officestamper.docx4j17.DocxOfficeStampers.standard;
import static pro.verron.officestamper.preset.OfficeStampers.docxStamper;
import static pro.verron.officestamper.docx4j17.test.utils.ContextFactory.mapContextFactory;
import static pro.verron.officestamper.docx4j17.test.utils.ContextFactory.objectContextFactory;
import static pro.verron.officestamper.docx4j17.test.utils.ResourceUtils.getWordResource;


/// @author Joseph Verron
/// @author Tom Hombergs
class FailOnUnresolvedPlaceholderTest {

    static Stream<Arguments> factories() {
        return Stream.of(argumentSet("obj", objectContextFactory()), argumentSet("map", mapContextFactory()));
    }

    @MethodSource("factories")
    @ParameterizedTest
    void fails(ContextFactory factory) {
        var context = factory.name("Homer");
        var template = getWordResource("FailOnUnresolvedExpressionTest.docx");
        var config = standard().setExceptionResolver(ExceptionResolvers.throwing());
        var stamper = docxStamper(config);
        assertThrows(OfficeStamperException.class, () -> stamper.stamp(template, context));
    }

    @MethodSource("factories")
    @ParameterizedTest
    void doesNotFail(ContextFactory factory) {
        var context = factory.name("Homer");
        var template = getWordResource(Path.of("FailOnUnresolvedExpressionTest.docx"));
        var config = standard().setExceptionResolver(ExceptionResolvers.passing());
        var stamper = docxStamper(config);
        assertDoesNotThrow(() -> stamper.stamp(template, context));
    }
}


