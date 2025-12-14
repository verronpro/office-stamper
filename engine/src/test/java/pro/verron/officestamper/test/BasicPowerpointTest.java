package pro.verron.officestamper.test;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static pro.verron.officestamper.preset.ExperimentalStampers.pptxStamper;

@DisplayName("Basic Powerpoint Test") class BasicPowerpointTest {
    @Test
    @DisplayName("Should stamp a PowerPoint document")
    void testStamper() {
        var stamper = pptxStamper();
        var template = TestUtils.getPowerPointResource(Path.of("powerpoint-base.pptx"));
        record Person(String name) {}
        var context = new Person("Bart");
        var stamped = stamper.stamp(template, context);
        var actual = Stringifier.stringifyPowerpoint(stamped);
        Assertions.assertEquals("""
                Hello
                Bart
                """, actual);
    }
}
