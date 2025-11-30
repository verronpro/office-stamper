package pro.verron.officestamper.test;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.PresentationMLPackage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;

import static pro.verron.officestamper.preset.ExperimentalStampers.pptxStamper;
import static pro.verron.officestamper.test.IOStreams.getInputStream;
import static pro.verron.officestamper.test.IOStreams.getOutputStream;

@DisplayName("Basic Powerpoint Test") class BasicPowerpointTest {
    @Test
    @DisplayName("Should stamp a PowerPoint document")
    void testStamper()
            throws IOException, Docx4JException {
        var stamper = pptxStamper();
        var templateStream = TestUtils.getResource(Path.of("powerpoint-base.pptx"));

        record Person(String name) {}
        var context = new Person("Bart");
        OutputStream outputStream = getOutputStream();
        stamper.stamp(templateStream, context, outputStream);
        InputStream inputStream = getInputStream(outputStream);
        PresentationMLPackage presentationMLPackage = PresentationMLPackage.load(inputStream);
        Assertions.assertEquals("""
                Hello
                Bart
                """, Stringifier.stringifyPowerpoint(presentationMLPackage));
    }
}
