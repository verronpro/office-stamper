package pro.verron.officestamper.test;

import org.junit.jupiter.api.DisplayName;
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
import static pro.verron.officestamper.utils.wml.DocxRenderer.docxToString;

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
        var actual = docxToString(stamped);
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
                
                
                """, actual);

        var template2 = getWordResource(filename);
        var wordprocessingMLPackage = stamper.stamp(template2, context);
        var document2 = docxToString(wordprocessingMLPackage);
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
