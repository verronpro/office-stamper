package pro.verron.officestamper.test;

import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import pro.verron.officestamper.api.OfficeStamperException;
import pro.verron.officestamper.preset.OfficeStamperConfigurations;
import pro.verron.officestamper.preset.OfficeStampers;

import java.util.Arrays;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.argumentSet;
import static pro.verron.officestamper.test.ContextFactory.mapContextFactory;
import static pro.verron.officestamper.test.ContextFactory.objectContextFactory;
import static pro.verron.officestamper.test.TestUtils.getWordResource;

/// @author Jenei Attila
/// @author Joseph Verrron
/// @version ${version}
/// @since 1.6.6
class ProcessorRepeatDocPart_BadPlaceholderTest {
    static Stream<Arguments> factories() {
        return Stream.of(argumentSet("obj", objectContextFactory()), argumentSet("map", mapContextFactory()));
    }

    @MethodSource("factories")
    @ParameterizedTest
    @Timeout(10)
        // in the case of pipe lock because of unknown exceptions
    void testBadExpressionShouldNotBlockCallerThread(ContextFactory factory) {
        var template = getWordResource("ProcessorRepeatDocPart_BadPlaceholder.docx");
        var context = factory.roles("Homer Simpson",
                "Dan Castellaneta",
                "Marge Simpson",
                "Julie Kavner",
                "Bart Simpson",
                "Nancy Cartwright");
        var configuration = OfficeStamperConfigurations.standard();
        var stamper = OfficeStampers.docxPackageStamper(configuration);

        var exception = assertThrows(OfficeStamperException.class, () -> stamper.stamp(template, context));

        String expectedErrorInfo = "someUnknownField";
        var exceptionMessage = exception.getMessage();
        var findDirectInfo = exceptionMessage.contains(expectedErrorInfo);
        var suppressedInfo = Arrays.stream(exception.getSuppressed())
                                   .map(Throwable::getMessage)
                                   .toList();
        var findSuppressedInfo = suppressedInfo.stream()
                                               .anyMatch(s -> s.contains(expectedErrorInfo));

        String errorMessage = "Could not find the expected '%s' information".formatted(expectedErrorInfo);
        assertTrue(findDirectInfo || findSuppressedInfo, errorMessage);
    }
}
