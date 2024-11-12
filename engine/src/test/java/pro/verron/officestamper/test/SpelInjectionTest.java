package pro.verron.officestamper.test;

import org.junit.jupiter.api.Test;
import pro.verron.officestamper.api.OfficeStamperException;
import pro.verron.officestamper.preset.OfficeStamperConfigurations;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static pro.verron.officestamper.test.TestUtils.getResource;


/**
 * @author Joseph Verron
 */
class SpelInjectionTest {

    @Test
    void spelInjectionTest()
            throws IOException {
        var context = ContextFactory.empty();
        try (var template = getResource("SpelInjectionTest.docx")) {
            var configuration = OfficeStamperConfigurations.standard();
            var stamper = new TestDocxStamper<>(configuration);
            assertThrows(
                    OfficeStamperException.class,
                    () -> stamper.stampAndLoadAndExtract(template, context));
        }
        assertDoesNotThrow(() -> "Does not throw", "Since VM is still up.");
    }
}
