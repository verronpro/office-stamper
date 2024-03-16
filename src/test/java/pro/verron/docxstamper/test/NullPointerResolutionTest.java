package pro.verron.docxstamper.test;

import org.junit.jupiter.api.Test;
import pro.verron.docxstamper.api.OpcStamperException;
import pro.verron.docxstamper.preset.OpcStamperConfigurations;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static pro.verron.docxstamper.test.DefaultTests.getResource;

/**
 * @author Joseph Verron
 */
class NullPointerResolutionTest {
    @Test
    void nullPointerResolutionTest_testThrowingCase() throws IOException {
        var subContext = new SubContext("Fullish2",
                                        List.of("Fullish3", "Fullish4",
                                                "Fullish5"));
        var context = new NullishContext("Fullish1", subContext, null, null);
        try (var template =
                     getResource(Path.of("NullPointerResolution.docx"))) {
            var configuration = OpcStamperConfigurations.standard();
            var stamper = new TestDocxStamper<NullishContext>(configuration);
            assertThrows(
                    OpcStamperException.class,
                    () -> stamper.stampAndLoadAndExtract(template, context)
            );
        }
    }
}
