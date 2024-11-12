package pro.verron.officestamper.test;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.verron.officestamper.api.OfficeStamperException;
import pro.verron.officestamper.preset.OfficeStamperConfigurations;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static pro.verron.officestamper.test.TestUtils.getResource;

/**
 * @author Jenei Attila
 * @author Joseph Verrron
 * @version ${version}
 * @since 1.6.6
 */
class RepeatDocPartBadPlaceholderTest {
    public static final ContextFactory FACTORY = new ContextFactory();
    private static final Logger logger = LoggerFactory.getLogger(RepeatDocPartBadPlaceholderTest.class);

    @Test @Timeout(10) // in the case of pipe lock because of unknown exceptions
    void testBadExpressionShouldNotBlockCallerThread() {
        var template = getResource("RepeatDocPartBadExpressionTest.docx");
        var context = FACTORY.roles("Homer Simpson",
                "Dan Castellaneta",
                "Marge Simpson",
                "Julie Kavner",
                "Bart Simpson",
                "Nancy Cartwright");
        var configuration = OfficeStamperConfigurations.standard();
        var stamper = new TestDocxStamper<>(configuration);

        var exception = assertThrows(OfficeStamperException.class,
                () -> stamper.stampAndLoadAndExtract(template, context));

        String expectedErrorInfo = "someUnknownField";
        var findDirectInfo = exception.getMessage()
                                      .contains(expectedErrorInfo);
        var findSuppressedInfo = Arrays.stream(exception.getSuppressed())
                                       .map(Throwable::getMessage)
                                       .anyMatch(s -> s.contains(expectedErrorInfo));

        logger.info("Here is the exception info dump:", exception);
        String errorMessage = "Could not find the expected '%s' information".formatted(expectedErrorInfo);
        assertTrue(findDirectInfo || findSuppressedInfo, errorMessage);
    }
}
