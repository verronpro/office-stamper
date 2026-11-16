package pro.verron.officestamper.docx4j11.test.experimental;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pro.verron.officestamper.docx4j11.test.utils.ResourceUtils;

import java.nio.file.Path;

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
        var stamped = stamper.stamp(template, context);
        var actual = pptxToString(stamped);
        Assertions.assertEquals("""
                Hello
                Bart
                """, actual);
    }
}


