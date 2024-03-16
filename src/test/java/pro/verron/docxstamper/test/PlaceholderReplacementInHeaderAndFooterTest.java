package pro.verron.docxstamper.test;

import org.junit.jupiter.api.Test;
import pro.verron.docxstamper.preset.Configurations;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static pro.verron.docxstamper.test.DefaultTests.getResource;

/**
 * @author Joseph Verron
 * @author Tom Hombergs
 */
class PlaceholderReplacementInHeaderAndFooterTest {
    @Test
    void expressionReplacementInHeaderAndFooterTest() {
        var context = new Name("Homer Simpson");
        var template = getResource(
                Path.of("ExpressionReplacementInHeaderAndFooterTest.docx"));
        var configuration = Configurations.standard()
                .setFailOnUnresolvedExpression(false);
        var stamper = new TestDocxStamper<Name>(configuration);
        var actual = stamper.stampAndLoadAndExtract(template, context);
        assertEquals("""
                             ❬❬This ❘lang=de-DE❭❬header ❘lang=de-DE❭❬paragraph is untouched.❘lang=de-DE❭❘lang=de-DE❭
                             ❬❬In this paragraph, the variable ❘lang=de-DE❭❬name❘b=true,lang=de-DE❭ should be resolved to the value ❬Homer Simpson❘lang=de-DE❭.❘lang=de-DE❭
                             ❬❬In this paragraph, the variable ❘lang=de-DE❭❬foo❘b=true,lang=de-DE❭❬ should not be resolved: ${foo}.❘lang=de-DE❭❘lang=de-DE,spacing={after=140,before=0}❭
                             ❬Expression Replacement in header and footer❘spacing={after=120,before=240}❭
                             ❬❘spacing={after=140,before=0}❭
                             ❬❬This ❘lang=de-DE❭❬footer ❘lang=de-DE❭❬paragraph is untouched.❘lang=de-DE❭❘lang=de-DE❭
                             ❬❬In this paragraph, the variable ❘lang=de-DE❭❬name❘b=true,lang=de-DE❭ should be resolved to the value ❬Homer Simpson❘lang=de-DE❭.❘lang=de-DE❭
                             ❬❬In this paragraph, the variable ❘lang=de-DE❭❬foo❘b=true,lang=de-DE❭❬ should not be resolved: ${foo}.❘lang=de-DE❭❘lang=de-DE,spacing={after=140,before=0}❭""",
                     actual);
    }

    public record Name(String name) {
    }
}
