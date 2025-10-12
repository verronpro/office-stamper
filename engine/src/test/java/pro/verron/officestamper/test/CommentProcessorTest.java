package pro.verron.officestamper.test;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static pro.verron.officestamper.preset.OfficeStamperConfigurations.standardWithPreprocessing;

public class CommentProcessorTest {
    @Test
    void notWorking1() {
        var stamperConfiguration = standardWithPreprocessing();
        var stamper = new TestDocxStamper<>(stamperConfiguration);
        var templateStream = TestUtils.getResource(Path.of("ReplaceWithCommentNotWorking1.docx"));

        var context = Map.of("name", "Homer Simpson");


        var actual = stamper.stampAndLoadAndExtract(templateStream, context);

        var expected = """
                [Normal] This variable name should be resolved to the value Homer Simpson.<jc=LEFT>
                """;

        assertEquals(expected, actual);
    }

    @Test
    void notWorking2() {
        var stamperConfiguration = standardWithPreprocessing();
        var stamper = new TestDocxStamper<>(stamperConfiguration);
        var templateStream = TestUtils.getResource(Path.of("ReplaceWithCommentNotWorking2.docx"));

        var context = Map.of("name", "Homer Simpson");


        var actual = stamper.stampAndLoadAndExtract(templateStream, context);

        var expected = """
                Homer Simpson
                """;

        assertEquals(expected, actual);
    }
}
