package pro.verron.officestamper.test;

import org.junit.jupiter.api.Test;
import pro.verron.officestamper.preset.OfficeStamperConfigurations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static pro.verron.officestamper.test.TestUtils.getResource;

/**
 * @author Joseph Verron
 */
class MultiSectionTest {

    @Test
    void expressionsInMultipleSections() {
        var context = new NamesContext("Homer", "Marge");
        var template = getResource("MultiSectionTest.docx");
        var configuration = OfficeStamperConfigurations.standard();
        var stamper = new TestDocxStamper<NamesContext>(configuration);
        var actual = stamper.stampAndLoadAndExtract(template, context);
        String expected = """
                Homer
                
                
                [section-break, docGrid={linePitch=360},pgMar={bottom=1417,footer=708,gutter=0,header=708,left=1417,right=1417,top=1417},pgSz={h=16838,w=11906}]
                <<<
                Marge
                """;
        assertEquals(expected, actual);
    }

    public record NamesContext(String firstName, String secondName) {
    }
}
