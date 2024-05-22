package pro.verron.officestamper.test;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static pro.verron.officestamper.preset.OfficeStamperConfigurations.standardWithPreprocessing;

public class TestBug52 {
    @CsvSource(
            {
                    "'',''",
                    """
                            true,'Start
                            Hello, World!
                            End'""",
                    """
                            false,'Start
                            End'""",
                    """
                            true/true,'Start
                            Hello, World!
                            End
                            Start
                            Hello, World!
                            End'""",
                    """
                            true/false,'Start
                            Hello, World!
                            End
                            Start
                            End'""",
                    """
                            false/true,'Start
                            End
                            Start
                            Hello, World!
                            End'""",
                    """
                            false/false,'Start
                            End
                            Start
                            End'""",
            })
    @ParameterizedTest
    @Disabled("Bug not solved yet")
    public void test(String input, String expected) {
        var stamperConfiguration = standardWithPreprocessing();
        var stamper = new TestDocxStamper<>(stamperConfiguration);
        var templateStream = TestUtils.getResource(Path.of("#52.docx"));

        record Element(boolean condition) {}
        record Context(List<Element> elements) {}
        var context = new Context(input.contains("true") || input.contains("false")
                ? Arrays.stream(input.split("/"))
                        .map(Boolean::parseBoolean)
                        .map(Element::new)
                        .toList()
                : Collections.emptyList());
        var actual = stamper.stampAndLoadAndExtract(templateStream, context);
        assertEquals(expected, actual);
    }
}
