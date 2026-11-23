package pro.verron.officestamper.docx4j11.test.experimental;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pro.verron.officestamper.docx4j11.test.utils.ResourceUtils;

import java.nio.file.Path;

import org.docx4j.openpackaging.packages.PresentationMLPackage;
import pro.verron.officestamper.docx4j11.experimental.PptxDocument;

import static pro.verron.officestamper.docx4j11.experimental.ExperimentalStampers.pptxPackageStamper;
import static pro.verron.officestamper.docx4j11.pml.PptxRenderer.pptxToString;

@DisplayName("Basic Powerpoint Test") class BasicPowerpointTest {
    @Test
    @DisplayName("Should stamp a PowerPoint document")
    void testStamper() {
        var stamper = pptxPackageStamper();
        var template = ResourceUtils.getPowerPointResource(Path.of("powerpoint-base.pptx"));
        record Person(String name) {}
        var context = new Person("Bart");
        var stamped = stamper.stamp(new PptxDocument(template), context);
        var actual = pptxToString(stamped.unwrap(PresentationMLPackage.class));
        Assertions.assertEquals("""
                Hello
                Bart
                """, actual);
    }
}


