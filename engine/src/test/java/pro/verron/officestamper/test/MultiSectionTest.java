package pro.verron.officestamper.test;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import pro.verron.officestamper.test.utils.ContextFactory;
import pro.verron.officestamper.test.utils.OfficeStamperTestBase;

import static pro.verron.officestamper.preset.OfficeStamperConfigurations.standard;
import static pro.verron.officestamper.test.utils.ResourceUtils.getWordResource;

/// @author Joseph Verron
class MultiSectionTest extends OfficeStamperTestBase {

    @MethodSource("factories")
    @ParameterizedTest
    void expressionsInMultipleSections(ContextFactory factory) {
        var config = standard();
        var context = factory.sectionName("Homer", "Marge");
        var template = getWordResource("MultiSectionTest.docx");
        var expected = """
                Homer
                
                
                
                
                
                // section {docGrid={linePitch=360}, pgMar={bottom=1417, footer=708, header=708, left=1417, right=1417, top=1417}, pgSz={h=16838, w=11906}, space=708}
                
                Marge
                
                // section {docGrid={linePitch=360}, pgMar={bottom=1417, footer=708, header=708, left=1417, right=1417, top=1417}, pgSz={h=11906, orient=landscape, w=16838}, space=708}
                
                """;
        testStamper(config, context, template, expected);
    }
}
