package org.wickedsource.docxstamper.test;

import org.junit.jupiter.api.Test;
import pro.verron.docxstamper.preset.Configurations;
import pro.verron.docxstamper.test.utils.TestDocxStamper;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.wickedsource.docxstamper.test.DefaultTests.getResource;

/**
 * @author Joseph Verron
 */
class MultiSectionTest {

    @Test
    void expressionsInMultipleSections() {
        var context = new NamesContext("Homer", "Marge");
        var template = getResource(Path.of("MultiSectionTest.docx"));
        var configuration = Configurations.standard();
        var stamper = new TestDocxStamper<NamesContext>(configuration);
        var actual = stamper.stampAndLoadAndExtract(template, context);
        String expected = """
                Homer
                        
                ❬❘docGrid=xxx,eGHdrFtrReferences=xxx,pgMar=xxx,pgSz={h=16838,w=11906}❭
                Marge""";
        assertEquals(expected, actual);
    }


    public record NamesContext(String firstName, String secondName) {
    }
}
