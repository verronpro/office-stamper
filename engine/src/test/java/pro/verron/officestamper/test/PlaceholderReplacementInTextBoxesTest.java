package pro.verron.officestamper.test;

import org.docx4j.dml.wordprocessingDrawing.Anchor;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static pro.verron.officestamper.preset.OfficeStamperConfigurations.standard;
import static pro.verron.officestamper.test.TestUtils.getResource;


/**
 * @author Joseph Verron
 * @author Thomas Oster
 */
class PlaceholderReplacementInTextBoxesTest {
    @Test
    void expressionReplacementInTextBoxesTest() {
        var context = new Name("Bart Simpson");
        var template = getResource("ExpressionReplacementInTextBoxesTest.docx");
        var configuration = standard()
                .setFailOnUnresolvedExpression(false);
        var stamper = new TestDocxStamper<Name>(configuration);
        var actual = stamper.stampAndLoadAndExtract(template, context, Anchor.class);
        List<String> expected = List.of(
                "❬Bart Simpson❘color=auto❭",
                "❬${foo}❘color=auto❭"
        );
        assertIterableEquals(expected, actual);
    }

    public record Name(String name) {
    }
}
