package org.wickedsource.docxstamper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;
import org.wickedsource.docxstamper.api.DocxStamperException;
import pro.verron.docxstamper.utils.TestDocxStamper;
import pro.verron.docxstamper.utils.context.Contexts.Characters;
import pro.verron.docxstamper.utils.context.Contexts.Role;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.wickedsource.docxstamper.DefaultTests.getResource;

/**
 * <p>RepeatDocPartBadExpressionTest class.</p>
 *
 * @author jenei.attila
 * @version ${version}
 * @since 1.6.6
 */
public class RepeatDocPartBadExpressionTest {
    static Logger logger =
            LoggerFactory.getLogger(RepeatDocPartBadExpressionTest.class);

    /**
     * <p>testBadExpressionShouldNotBlockCallerThread.</p>
     */
    @Test
    @Timeout(10) // in case of pipe lock because of unknown exceptions
    public void testBadExpressionShouldNotBlockCallerThread() {
        var template = getResource(Path.of("RepeatDocPartBadExpressionTest.docx"));
        var context = new Characters(
                List.of(new Role("Homer Simpson", "Dan Castellaneta"),
                        new Role("Marge Simpson", "Julie Kavner"),
                        new Role("Bart Simpson", "Nancy Cartwright")));
        var stamper = new TestDocxStamper<>(new DocxStamperConfiguration());

        var exception = assertThrows(
                DocxStamperException.class,
                () -> stamper.stampAndLoadAndExtract(template, context));

        String expectedErrorInfo = "someUnknownField";
        var findDirectInfo = exception.getMessage()
                .contains(expectedErrorInfo);
        var findSuppressedInfo = Arrays.stream(exception.getSuppressed())
                .map(Throwable::getMessage)
                .anyMatch(s -> s.contains(expectedErrorInfo));

        logger.info(exception, () -> "Here is the exception info dump:");
        String errorMessage = "Could not find the expected '%s' information"
                .formatted(expectedErrorInfo);
        assertTrue(findDirectInfo || findSuppressedInfo, errorMessage);
    }
}
