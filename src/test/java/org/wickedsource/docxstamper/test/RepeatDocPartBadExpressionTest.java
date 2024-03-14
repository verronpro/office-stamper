package org.wickedsource.docxstamper.test;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wickedsource.docxstamper.DocxStamperConfiguration;
import org.wickedsource.docxstamper.api.DocxStamperException;
import pro.verron.docxstamper.test.utils.TestDocxStamper;
import pro.verron.docxstamper.test.utils.context.Contexts.Characters;
import pro.verron.docxstamper.test.utils.context.Contexts.Role;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.wickedsource.docxstamper.test.DefaultTests.getResource;

/**
 * @author Jenei Attila
 * @author Joseph Verrron
 * @version ${version}
 * @since 1.6.6
 */
public class RepeatDocPartBadExpressionTest {
    private static final Logger logger =
            LoggerFactory.getLogger(RepeatDocPartBadExpressionTest.class);

    @Test
    @Timeout(10) // in the case of pipe lock because of unknown exceptions
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

        logger.info("Here is the exception info dump:", exception);
        String errorMessage = "Could not find the expected '%s' information"
                .formatted(expectedErrorInfo);
        assertTrue(findDirectInfo || findSuppressedInfo, errorMessage);
    }
}
