package pro.verron.officestamper.test;

import org.junit.jupiter.api.Test;
import pro.verron.officestamper.preset.OfficeStamperConfigurations;
import pro.verron.officestamper.preset.PassingResolver;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
        var config = standard()
                .setExceptionResolver(new PassingResolver());
        var stamper = new TestDocxStamper<Name>(config);
        var actual = stamper.stampAndLoadAndExtract(template, context);
        String expected = """
                Expression Replacement in TextBoxes
                [❬Bart Simpson❘color=auto❭]
                This should resolve to a name:\s
                [❬${foo}❘color=auto❭]
                This should not resolve:\s
                """;
        assertEquals(expected, actual);
    }

    public record Name(String name) {
    }
}
