package pro.verron.officestamper.test;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import pro.verron.officestamper.preset.OfficeStamperConfigurations;
import pro.verron.officestamper.test.utils.ContextFactory;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.argumentSet;
import static pro.verron.officestamper.asciidoc.AsciiDocCompiler.toAsciidoc;
import static pro.verron.officestamper.preset.OfficeStampers.docxPackageStamper;
import static pro.verron.officestamper.test.utils.ContextFactory.mapContextFactory;
import static pro.verron.officestamper.test.utils.ContextFactory.objectContextFactory;
import static pro.verron.officestamper.test.utils.ResourceUtils.getWordResource;

/// @author Joseph Verron
/// @author Tom Hombergs
class MultiStampTest {

    static Stream<Arguments> factories() {
        return Stream.of(argumentSet("obj", objectContextFactory()), argumentSet("map", mapContextFactory()));
    }

    @DisplayName("The same stamper instance can stamp several times")
    @MethodSource("factories")
    @ParameterizedTest
    void repeatDocPart(ContextFactory factory) {
        var config = OfficeStamperConfigurations.standard();
        var context = factory.names("Homer", "Marge", "Bart", "Lisa", "Maggie");
        var stamper = docxPackageStamper(config);

        var filename = "MultiStampTest.docx";
        var template = getWordResource(filename);
        var stamped = stamper.stamp(template, context);
        var actual = toAsciidoc(stamped);
        assertEquals("""
                == Multi-Stamp-Test
                
                |===
                [rowStyle=2048]
                |The next row will repeat multiple times with a different name:
                [rowStyle=32]
                |Homer
                [rowStyle=32]
                |Marge
                [rowStyle=32]
                |Bart
                [rowStyle=32]
                |Lisa
                [rowStyle=32]
                |Maggie
                |===
                
                
                
                // section {docGrid={charSpace=-6145, linePitch=240}, pgMar={bottom=1134, left=1134, right=1134, top=1134}, pgSz={h=16838, w=11906}, space=720}
                
                """, actual);

        var template2 = getWordResource(filename);
        var wordprocessingMLPackage = stamper.stamp(template2, context);
        var document2 = toAsciidoc(wordprocessingMLPackage);
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
