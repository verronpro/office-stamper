package pro.verron.officestamper.test;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import pro.verron.officestamper.api.OfficeStamperException;
import pro.verron.officestamper.preset.OfficeStamperConfigurations;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.argumentSet;
import static pro.verron.officestamper.preset.OfficeStampers.docxPackageStamper;
import static pro.verron.officestamper.test.ContextFactory.mapContextFactory;
import static pro.verron.officestamper.test.ContextFactory.objectContextFactory;
import static pro.verron.officestamper.test.TestUtils.getWordResource;


/// @author Joseph Verron
class SpelInjectionTest {

    static Stream<Arguments> factories() {
        return Stream.of(argumentSet("obj", objectContextFactory()), argumentSet("map", mapContextFactory()));
    }

    @DisplayName("Ensure dangerous SpeL injection throws an error, and not execute directly")
    @MethodSource("factories")
    @ParameterizedTest
    void spelInjectionTest(ContextFactory factory) {
        var context = factory.empty();
        var template = getWordResource("SpelInjectionTest.docx");
        var configuration = OfficeStamperConfigurations.standard();
        var stamper = docxPackageStamper(configuration);
        assertThrows(OfficeStamperException.class, () -> stamper.stamp(template, context));
        assertDoesNotThrow(() -> "Does not throw", "Since VM is still up.");
    }
}
