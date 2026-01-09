package pro.verron.officestamper.test;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import pro.verron.officestamper.preset.OfficeStamperConfigurations;
import pro.verron.officestamper.preset.OfficeStampers;
import pro.verron.officestamper.test.utils.ContextFactory;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.argumentSet;
import static pro.verron.officestamper.test.utils.ContextFactory.mapContextFactory;
import static pro.verron.officestamper.test.utils.ContextFactory.objectContextFactory;
import static pro.verron.officestamper.test.utils.ResourceUtils.getWordResource;
import static pro.verron.officestamper.utils.wml.DocxRenderer.docxToString;


/// Verifies stampTable feature works correctly
class StampTableTest {

    static Stream<Arguments> factories() {
        return Stream.of(argumentSet("obj", objectContextFactory()), argumentSet("map", mapContextFactory()));
    }

    @DisplayName("Ensure the StampTable feature non-regression")
    @MethodSource("factories")
    @ParameterizedTest
    void stampTableTest(ContextFactory factory) {
        var template = getWordResource("StampTableTest.docx");

        var configuration = OfficeStamperConfigurations.standard();
        var stamper = OfficeStampers.docxPackageStamper(configuration);

        var context = factory.characterTable(List.of("Character", "Actor"),
                List.of(List.of("Homer Simpson", "Dan Castellaneta"),
                        List.of("Marge Simpson", "Julie Kavner"),
                        List.of("Bart Simpson", "Nancy Cartwright"),
                        List.of("Kent Brockman", "Harry Shearer"),
                        List.of("Disco Stu", "Hank Azaria"),
                        List.of("Krusty the Clown", "Dan Castellaneta")));
        var wordprocessingMLPackage = stamper.stamp(template, context);
        var string = docxToString(wordprocessingMLPackage);
        assertEquals("""
                Stamping Table
                
                List of Simpsons characters
                
                |===
                |Character
                |Actor
                
                |Homer Simpson
                |Dan Castellaneta
                
                |Marge Simpson
                |Julie Kavner
                
                |Bart Simpson
                |Nancy Cartwright
                
                |Kent Brockman
                |Harry Shearer
                
                |Disco Stu
                |Hank Azaria
                
                |Krusty the Clown
                |Dan Castellaneta
                
                
                |===
                
                
                There are 6 characters in the above table.
                
                """, string);
    }
}
