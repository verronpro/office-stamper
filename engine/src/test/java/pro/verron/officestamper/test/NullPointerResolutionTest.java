package pro.verron.officestamper.test;

import org.junit.jupiter.api.Test;
import pro.verron.officestamper.api.OfficeStamperException;
import pro.verron.officestamper.preset.OfficeStamperConfigurations;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static pro.verron.officestamper.test.TestUtils.getResource;

/**
 * @author Joseph Verron
 */
class NullPointerResolutionTest {

    public static final ContextFactory FACTORY = new ContextFactory();

    @Test void nullPointerResolutionTest_testThrowingCase()
            throws IOException {
        var context = FACTORY.nullishContext();
        try (var template = getResource("NullPointerResolution.docx")) {
            var configuration = OfficeStamperConfigurations.standard();
            var stamper = new TestDocxStamper<>(configuration);
            assertThrows(OfficeStamperException.class, () -> stamper.stampAndLoadAndExtract(template, context));
        }
    }

}
