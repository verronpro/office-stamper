package pro.verron.officestamper.test;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPartAbstractImage;
import org.junit.jupiter.api.Test;
import pro.verron.officestamper.core.DocxStamper;
import pro.verron.officestamper.preset.Resolvers;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static pro.verron.officestamper.preset.OfficeStamperConfigurations.standard;
import static pro.verron.officestamper.test.utils.DocxFactory.makeWordResource;
import static pro.verron.officestamper.test.utils.ResourceUtils.getImage;

public class ImageDeduplicationTest {

    @Test
    public void testImageDeduplicationEnabledByDefault()
            throws org.docx4j.openpackaging.exceptions.Docx4JException {
        var configuration = standard();
        var stamper = new DocxStamper(configuration);

        var monalisa20 = getImage(Path.of("sample-monalisa-20x20.jpg"));
        var monalisa50_1 = getImage(Path.of("sample-monalisa-50x50.jpg"));
        var monalisa50_2 = getImage(Path.of("sample-monalisa-50x50.jpg"));

        var context = Map.of("monalisa1", monalisa20, "monalisa2", monalisa50_1, "monalisa3", monalisa50_2);
        var template = makeWordResource("""
                ${monalisa1}
                
                ${monalisa2}
                
                ${monalisa3}
                
                """);

        var output = new ByteArrayOutputStream();
        var stamped = stamper.stamp(template, context);
        stamped.save(output);

        var resultDoc = WordprocessingMLPackage.load(new ByteArrayInputStream(output.toByteArray()));
        long imagePartCount = resultDoc.getParts()
                                       .getParts()
                                       .values()
                                       .stream()
                                       .filter(BinaryPartAbstractImage.class::isInstance)
                                       .count();

        // If deduplication, there should be only 2 image part even though we inserted 3 since 2 were the same image
        assertEquals(2, imagePartCount, "There should be only 1 image part in the document");
    }

    @Test
    public void testImageDeduplicationDisabled()
            throws org.docx4j.openpackaging.exceptions.Docx4JException {
        var configuration = standard().setResolvers(List.of(Resolvers.image(/*deduplicate*/false)));
        var stamper = new DocxStamper(configuration);

        var monalisa20 = getImage(Path.of("sample-monalisa-20x20.jpg"));
        var monalisa50_1 = getImage(Path.of("sample-monalisa-50x50.jpg"));
        var monalisa50_2 = getImage(Path.of("sample-monalisa-50x50.jpg"));

        var context = Map.of("monalisa1", monalisa20, "monalisa2", monalisa50_1, "monalisa3", monalisa50_2);
        var template = makeWordResource("""
                ${monalisa1}
                
                ${monalisa2}
                
                ${monalisa3}
                
                """);

        var output = new ByteArrayOutputStream();
        var stamped = stamper.stamp(template, context);
        stamped.save(output);

        var resultDoc = WordprocessingMLPackage.load(new ByteArrayInputStream(output.toByteArray()));
        long imagePartCount = resultDoc.getParts()
                                       .getParts()
                                       .values()
                                       .stream()
                                       .filter(BinaryPartAbstractImage.class::isInstance)
                                       .count();

        // If deduplication is deactivated, there should be 3 image part since we inserted 3
        assertEquals(3, imagePartCount, "There should be only 1 image part in the document");
    }
}
