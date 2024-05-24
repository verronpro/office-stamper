package pro.verron.officestamper.test;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static pro.verron.officestamper.preset.EvaluationContextConfigurers.noopConfigurer;
import static pro.verron.officestamper.preset.OfficeStamperConfigurations.standardWithPreprocessing;

public class SpelInstantiationTest {
    @Test
    public void testDateInstantiationAndResolution() {
        var stamperConfiguration = standardWithPreprocessing()
                .setEvaluationContextConfigurer(noopConfigurer());
        var stamper = new TestDocxStamper<>(stamperConfiguration);
        var templateStream = TestUtils.getResource(Path.of("date.docx"));
        var context = new Object();
        var actual = stamper.stampAndLoadAndExtract(templateStream, context);
        var expected = """
                01.01.1970
                2000-01-01
                12:00:00
                2000-01-01T12:00:00""";
        assertEquals(expected, actual);
    }
}
