package pro.verron.officestamper.test;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import pro.verron.officestamper.api.OfficeStamperException;
import pro.verron.officestamper.preset.OfficeStamperConfigurations;

import java.io.IOException;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.argumentSet;
import static pro.verron.officestamper.test.ContextFactory.mapContextFactory;
import static pro.verron.officestamper.test.ContextFactory.objectContextFactory;
import static pro.verron.officestamper.test.TestUtils.getResource;


/// @author Joseph Verron
class SpelInjectionTest {

    static Stream<Arguments> factories() {
        return Stream.of(argumentSet("obj", objectContextFactory()), argumentSet("map", mapContextFactory()));
    }

    @DisplayName("Ensure dangerous SpeL injection throws an error, and not execute directly")
    @MethodSource("factories")
    @ParameterizedTest
    void spelInjectionTest(ContextFactory factory)
            throws IOException {
        var context = factory.empty();
        try (var template = getResource("SpelInjectionTest.docx")) {
            var configuration = OfficeStamperConfigurations.standard();
            var stamper = new TestDocxStamper<>(configuration);
            assertThrows(OfficeStamperException.class, () -> stamper.stampAndLoadAndExtract(template, context));
        }
        assertDoesNotThrow(() -> "Does not throw", "Since VM is still up.");
    }
}
