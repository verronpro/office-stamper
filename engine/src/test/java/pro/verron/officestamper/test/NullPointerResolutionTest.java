package pro.verron.officestamper.test;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import pro.verron.officestamper.api.OfficeStamperException;
import pro.verron.officestamper.preset.OfficeStamperConfigurations;

import java.io.IOException;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.argumentSet;
import static pro.verron.officestamper.test.ContextFactory.mapContextFactory;
import static pro.verron.officestamper.test.ContextFactory.objectContextFactory;
import static pro.verron.officestamper.test.TestUtils.getResource;

/// @author Joseph Verron
class NullPointerResolutionTest {

    static Stream<Arguments> factories() {
        return Stream.of(argumentSet("obj", objectContextFactory()), argumentSet("map", mapContextFactory()));
    }

    @MethodSource("factories")
    @ParameterizedTest
    void nullPointerResolutionTest_testThrowingCase(ContextFactory factory)
            throws IOException {
        var context = factory.nullishContext();
        try (var template = getResource("NullPointerResolution.docx")) {
            var configuration = OfficeStamperConfigurations.standard();
            var stamper = new TestDocxStamper<>(configuration);
            assertThrows(OfficeStamperException.class, () -> stamper.stampAndLoadAndExtract(template, context));
        }
    }

}
