package org.wickedsource.docxstamper.test;

import org.junit.jupiter.api.Test;
import org.wickedsource.docxstamper.DocxStamperConfiguration;
import pro.verron.docxstamper.test.utils.TestDocxStamper;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.wickedsource.docxstamper.test.DefaultTests.getResource;

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
        var stamper = new TestDocxStamper<Name>(new DocxStamperConfiguration().setFailOnUnresolvedExpression(
                false));
        var actual = stamper.stampAndLoadAndExtract(template, context);
        assertEquals("""
                             ❬❬This ❘lang=de-DE❭❬header ❘lang=de-DE❭❬paragraph is untouched.❘lang=de-DE❭❘lang=de-DE❭
                             ❬❬In this paragraph, the variable ❘lang=de-DE❭❬name❘b=true,lang=de-DE❭ should be resolved to the value ❬Homer Simpson❘lang=de-DE❭.❘lang=de-DE❭
                             ❬❬In this paragraph, the variable ❘lang=de-DE❭❬foo❘b=true,lang=de-DE❭❬ should not be resolved: ${foo}.❘lang=de-DE❭❘lang=de-DE,spacing={after=140,afterLines=140,before=140,beforeLines=140,line=140,lineRule=140}❭
                             ❬Expression Replacement in header and footer❘spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120}❭
                             ❬❘spacing={after=140,afterLines=140,before=140,beforeLines=140,line=140,lineRule=140}❭
                             ❬❬This ❘lang=de-DE❭❬footer ❘lang=de-DE❭❬paragraph is untouched.❘lang=de-DE❭❘lang=de-DE❭
                             ❬❬In this paragraph, the variable ❘lang=de-DE❭❬name❘b=true,lang=de-DE❭ should be resolved to the value ❬Homer Simpson❘lang=de-DE❭.❘lang=de-DE❭
                             ❬❬In this paragraph, the variable ❘lang=de-DE❭❬foo❘b=true,lang=de-DE❭❬ should not be resolved: ${foo}.❘lang=de-DE❭❘lang=de-DE,spacing={after=140,afterLines=140,before=140,beforeLines=140,line=140,lineRule=140}❭""",
                     actual);
    }

    public record Name(String name) {
    }
}
