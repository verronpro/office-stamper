package pro.verron.docxstamper.test;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.PresentationMLPackage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import pro.verron.docxstamper.preset.OfficeStampers;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class BasicPowerpointTest {
    @Test
    public void testStamper()
            throws IOException, Docx4JException {
        var stamper = OfficeStampers.pptxStamper();
        var templatePath = Path.of("test", "sources", "powerpoint-base.pptx");
        var templateStream = Files.newInputStream(templatePath);
        record Person(String name) {}
        var context = new Person("Bart");
        PresentationMLPackage load = PresentationMLPackage.load(templateStream);
        OutputStream outputStream = IOStreams.getOutputStream();
        stamper.stamp(load, context, outputStream);
        InputStream inputStream = IOStreams.getInputStream(outputStream);
        PresentationMLPackage presentationMLPackage = PresentationMLPackage.load(inputStream);
        Assertions.assertEquals("""
                        Hello
                        Bart
                        """,
                Stringifier.stringifyPowerpoint(presentationMLPackage));
    }
}
