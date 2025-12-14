package pro.verron.officestamper.test;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import pro.verron.officestamper.preset.OfficeStamperConfigurations;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.argumentSet;
import static pro.verron.officestamper.preset.OfficeStampers.docxPackageStamper;
import static pro.verron.officestamper.test.ContextFactory.mapContextFactory;
import static pro.verron.officestamper.test.ContextFactory.objectContextFactory;
import static pro.verron.officestamper.test.TestUtils.getWordResource;

/// @author Joseph Verron
class MultiSectionTest {

    static Stream<Arguments> factories() {
        return Stream.of(argumentSet("obj", objectContextFactory()), argumentSet("map", mapContextFactory()));
    }

    @MethodSource("factories")
    @ParameterizedTest
    void expressionsInMultipleSections(ContextFactory factory) {
        var context = factory.sectionName("Homer", "Marge");
        var template = getWordResource("MultiSectionTest.docx");
        var configuration = OfficeStamperConfigurations.standard();
        var stamper = docxPackageStamper(configuration);
        var wordprocessingMLPackage = stamper.stamp(template, context);
        var actual = Stringifier.stringifyWord(wordprocessingMLPackage);
        String expected = """
                Homer
                
                
                
                
                [section-break, {docGrid={linePitch=360},pgMar={bottom=1417,footer=708,gutter=0,header=708,left=1417,right=1417,top=1417},pgSz={h=16838,w=11906}}]
                <<<
                
                Marge
                
                """;
        assertEquals(expected, actual);
    }
}
