package pro.verron.officestamper.test;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static pro.verron.officestamper.preset.OfficeStamperConfigurations.standard;
import static pro.verron.officestamper.test.TestUtils.getResource;


/**
 * @author Joseph Verron
 * @author Tom Hombergs
 */
class PlaceholderReplacementInHeaderAndFooterTest {
    @Test
    void expressionReplacementInHeaderAndFooterTest() {
        var context = new Name("Homer Simpson");
        var template = getResource("ExpressionReplacementInHeaderAndFooterTest.docx");
        var configuration = standard()
                .setFailOnUnresolvedExpression(false);
        var stamper = new TestDocxStamper<Name>(configuration);
        var actual = stamper.stampAndLoadAndExtract(template, context);
        assertEquals("""
                        This header paragraph is untouched.
                        In this paragraph, the variable name should be resolved to the value Homer Simpson.
                        In this paragraph, the variable foo should not be resolved: ${foo}.
                        ===
                        Expression Replacement in header and footer
                        ===
                        This footer paragraph is untouched.
                        In this paragraph, the variable name should be resolved to the value Homer Simpson.
                        In this paragraph, the variable foo should not be resolved: ${foo}.
                        """,
                actual);
    }

    public record Name(String name) {
    }
}
