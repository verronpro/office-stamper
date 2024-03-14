package org.wickedsource.docxstamper.test;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.wickedsource.docxstamper.DocxStamper;
import org.wickedsource.docxstamper.DocxStamperConfiguration;
import org.wickedsource.docxstamper.api.UnresolvedExpressionException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.wickedsource.docxstamper.test.DefaultTests.getResource;

/**
 * @author Joseph Verron
 * @author Tom Hombergs
 */
class FailOnUnresolvedExpressionTest {
    @Test
    void fails() throws IOException {
        var context = new Name("Homer");
        try (var template = getResource(Path.of("FailOnUnresolvedExpressionTest" +
                                                ".docx"))) {
            var config = new DocxStamperConfiguration()
                    .setFailOnUnresolvedExpression(true);
            var stamper = new DocxStamper<Name>(config);
            var outputStream = new ByteArrayOutputStream();
            assertThrows(UnresolvedExpressionException.class, () -> stamper.stamp(template, context, outputStream));
        }
    }

    @Test
    void doesNotFail() throws IOException {
        Name context = new Name("Homer");
        try (InputStream template = getResource(Path.of(
                "FailOnUnresolvedExpressionTest.docx"))) {
            var config = new DocxStamperConfiguration()
                    .setFailOnUnresolvedExpression(false);
            var stamper = new DocxStamper<Name>(config);
            Assertions.assertDoesNotThrow(() -> stamper.stamp(template, context, new ByteArrayOutputStream()));
        }
    }

    public record Name(String name) {
    }

}
