package pro.verron.officestamper.test;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static pro.verron.officestamper.preset.ExceptionResolvers.passing;
import static pro.verron.officestamper.preset.OfficeStamperConfigurations.standard;
import static pro.verron.officestamper.test.TestUtils.getResource;


/**
 * @author Joseph Verron
 * @author Thomas Oster
 */
class TextBoxesTest {
    @DisplayName("Placeholders in text boxes should be replaced") @Test
    void placeholders() {
        var context = new Name("Bart Simpson");
        var template = getResource("ExpressionReplacementInTextBoxesTest.docx");
        var config = standard().setExceptionResolver(passing());
        var stamper = new TestDocxStamper<Name>(config);
        var actual = stamper.stampAndLoadAndExtract(template, context);
        String expected = """
                == Expression Replacement in TextBoxes
                
                [Bart Simpson]
                This should resolve to a name:\s
                [${foo}]
                This should not resolve:\s
                """;
        assertEquals(expected, actual);
    }

    public record Name(String name) {}
}
