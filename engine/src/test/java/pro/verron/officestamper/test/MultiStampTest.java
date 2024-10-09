package pro.verron.officestamper.test;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pro.verron.officestamper.preset.OfficeStamperConfigurations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static pro.verron.officestamper.test.Contexts.names;
import static pro.verron.officestamper.test.TestUtils.getResource;

/**
 * @author Joseph Verron
 * @author Tom Hombergs
 */
class MultiStampTest {
    @Test @DisplayName("The same stamper instance can stamp several times") void repeatDocPart() {
        var config = OfficeStamperConfigurations.standard();
        var context = names("Homer", "Marge", "Bart", "Lisa", "Maggie");
        var stamper = new TestDocxStamper<>(config);

        var filename = "MultiStampTest.docx";
        var template1 = getResource(filename);
        var document1 = stamper.stampAndLoadAndExtract(template1, context);
        assertEquals("""
                == Multi-Stamp-Test
                
                |===
                |The next row will repeat multiple times with a different name:
                
                |Homer
                
                |Marge
                
                |Bart
                
                |Lisa
                
                |Maggie
                
                
                |===
                
                """, document1);

        var template2 = getResource(filename);
        var document2 = stamper.stampAndLoadAndExtract(template2, context);
        assertEquals("""
                == Multi-Stamp-Test
                
                |===
                |The next row will repeat multiple times with a different name:
                
                |Homer
                
                |Marge
                
                |Bart
                
                |Lisa
                
                |Maggie
                
                
                |===
                
                """, document2);
    }
}
