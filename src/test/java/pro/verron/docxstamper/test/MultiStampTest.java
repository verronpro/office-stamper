package pro.verron.docxstamper.test;

import org.junit.jupiter.api.Test;
import pro.verron.docxstamper.preset.Configurations;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static pro.verron.docxstamper.test.Contexts.names;
import static pro.verron.docxstamper.test.DefaultTests.getResource;

/**
 * @author Joseph Verron
 * @author Tom Hombergs
 */
class MultiStampTest {
    @Test
    void expressionsAreResolvedOnMultiStamp() {
        var config = Configurations.standard();
        var context = names("Homer","Marge","Bart","Lisa","Maggie");
        var stamper = new TestDocxStamper<>(config);

        var templatePath = Path.of("MultiStampTest.docx");

        var template1 = getResource(templatePath);
        var document1 = stamper.stampAndLoadAndExtract(template1, context);
        assertEquals("""
                             ❬Multi-Stamp-Test❘spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120}❭
                             This table row should be expanded to multiple rows each with a different name each time the document is stamped: Homer.
                             This table row should be expanded to multiple rows each with a different name each time the document is stamped: Marge.
                             This table row should be expanded to multiple rows each with a different name each time the document is stamped: Bart.
                             This table row should be expanded to multiple rows each with a different name each time the document is stamped: Lisa.
                             This table row should be expanded to multiple rows each with a different name each time the document is stamped: Maggie.
                             ❬❘spacing={after=140,afterLines=140,before=140,beforeLines=140,line=140,lineRule=140}❭""",
                     document1);

        var template2 = getResource(templatePath);
        var document2 = stamper.stampAndLoadAndExtract(template2, context);
        assertEquals("""
                             ❬Multi-Stamp-Test❘spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120}❭
                             This table row should be expanded to multiple rows each with a different name each time the document is stamped: Homer.
                             This table row should be expanded to multiple rows each with a different name each time the document is stamped: Marge.
                             This table row should be expanded to multiple rows each with a different name each time the document is stamped: Bart.
                             This table row should be expanded to multiple rows each with a different name each time the document is stamped: Lisa.
                             This table row should be expanded to multiple rows each with a different name each time the document is stamped: Maggie.
                             ❬❘spacing={after=140,afterLines=140,before=140,beforeLines=140,line=140,lineRule=140}❭""",
                     document2);
    }
}
