package pro.verron.officestamper.test.experimental;
import pro.verron.officestamper.test.Flavor;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pro.verron.officestamper.test.utils.ResourceUtils;

import java.nio.file.Path;

import org.docx4j.openpackaging.packages.PresentationMLPackage;

@DisplayName("Basic Powerpoint Test") class BasicPowerpointTest {
    @Test
    @DisplayName("Should stamp a PowerPoint document")
    void testStamper() {
        var stamper = Flavor.get().pptxPackageStamper();
        var template = ResourceUtils.getPowerPointResource(Path.of("powerpoint-base.pptx"));
        record Person(String name) {}
        var context = new Person("Bart");
        var stamped = stamper.stamp(template, context);
        var actual = Flavor.get().pptxToString(stamped.unwrap(PresentationMLPackage.class));
        Assertions.assertEquals("""
                Hello
                Bart
                """, actual);
    }
}


