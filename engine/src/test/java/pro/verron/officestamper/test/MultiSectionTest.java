package pro.verron.officestamper.test;

import org.docx4j.wml.STPageOrientation;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import pro.verron.officestamper.preset.OfficeStamperConfigurations;
import pro.verron.officestamper.test.utils.ContextFactory;

import java.math.BigInteger;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.argumentSet;
import static pro.verron.officestamper.test.utils.OfficeStamperTestBase.orientationOf;
import static pro.verron.officestamper.test.utils.OfficeStamperTestBase.sectionOrientations;
import static pro.verron.officestamper.test.utils.OfficeStamperTestBase.sectionProperties;
import static pro.verron.officestamper.test.utils.OfficeStamperTestBase.toAsciidoc;
import static pro.verron.officestamper.preset.OfficeStampers.docxPackageStamper;
import static pro.verron.officestamper.test.utils.ContextFactory.mapContextFactory;
import static pro.verron.officestamper.test.utils.ContextFactory.objectContextFactory;
import static pro.verron.officestamper.test.utils.ResourceUtils.getWordResource;

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
        var actual = toAsciidoc(wordprocessingMLPackage);
        String expected = """
                Homer
                
                
                
                
                
                [section]
                
                Marge
                
                """;
        assertEquals(expected, actual);

        var sections = sectionProperties(wordprocessingMLPackage);
        assertEquals(2, sections.size(), "The stamped document should keep both sections");
        var first = sections.get(0);
        assertEquals(STPageOrientation.PORTRAIT, orientationOf(first),
                "The first section should stay portrait");
        assertEquals(BigInteger.valueOf(11906), first.getPgSz()
                                                     .getW(), "portrait width");
        assertEquals(BigInteger.valueOf(16838), first.getPgSz()
                                                     .getH(), "portrait height");
        var second = sections.get(1);
        assertEquals(STPageOrientation.LANDSCAPE, orientationOf(second),
                "The second section should stay landscape");
        assertEquals(BigInteger.valueOf(16838), second.getPgSz()
                                                      .getW(), "landscape width");
        assertEquals(BigInteger.valueOf(11906), second.getPgSz()
                                                      .getH(), "landscape height");
        assertEquals(List.of(STPageOrientation.PORTRAIT, STPageOrientation.LANDSCAPE),
                sectionOrientations(wordprocessingMLPackage));
    }
}
