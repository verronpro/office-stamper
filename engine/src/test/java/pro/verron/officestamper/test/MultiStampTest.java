package pro.verron.officestamper.test;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import pro.verron.officestamper.preset.OfficeStamperConfigurations;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.argumentSet;
import static pro.verron.officestamper.test.ContextFactory.mapContextFactory;
import static pro.verron.officestamper.test.ContextFactory.objectContextFactory;
import static pro.verron.officestamper.test.TestUtils.getResource;

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
