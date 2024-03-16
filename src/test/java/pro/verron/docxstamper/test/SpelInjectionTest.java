package pro.verron.docxstamper.test;

import org.junit.jupiter.api.Test;
import pro.verron.docxstamper.api.DocxStamperException;
import pro.verron.docxstamper.preset.Configurations;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static pro.verron.docxstamper.test.DefaultTests.getResource;

/**
 * @author Joseph Verron
 */
class SpelInjectionTest {

    @Test
    void spelInjectionTest() throws IOException {
        var context = Contexts.empty();
        try (var template = getResource(Path.of("SpelInjectionTest.docx"))) {
            var configuration = Configurations.standard();
            var stamper = new TestDocxStamper<>(configuration);
            assertThrows(DocxStamperException.class, () -> stamper.stampAndLoadAndExtract(template, context));
        }
        assertDoesNotThrow(() -> "Does not throw", "Since VM is still up.");
    }
}
