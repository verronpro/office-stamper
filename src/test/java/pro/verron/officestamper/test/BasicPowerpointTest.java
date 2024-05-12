package pro.verron.officestamper.test;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.PresentationMLPackage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;

import static java.nio.file.Files.newInputStream;
import static pro.verron.officestamper.preset.ExperimentalStampers.pptxStamper;
import static pro.verron.officestamper.test.IOStreams.getInputStream;
import static pro.verron.officestamper.test.IOStreams.getOutputStream;

public class BasicPowerpointTest {
    @Test
    public void testStamper()
            throws IOException, Docx4JException {
        var stamper = pptxStamper();
        var templatePath = Path.of("test", "sources", "powerpoint-base.pptx");
        var templateStream = newInputStream(templatePath);
        record Person(String name) {}
        var context = new Person("Bart");
        PresentationMLPackage load = PresentationMLPackage.load(templateStream);
        OutputStream outputStream = getOutputStream();
        stamper.stamp(load, context, outputStream);
        InputStream inputStream = getInputStream(outputStream);
        PresentationMLPackage presentationMLPackage = PresentationMLPackage.load(inputStream);
        Assertions.assertEquals("""
                        Hello
                        Bart
                        """,
                Stringifier.stringifyPowerpoint(presentationMLPackage));
    }
}
