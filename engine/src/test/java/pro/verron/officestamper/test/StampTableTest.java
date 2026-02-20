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
import static pro.verron.officestamper.asciidoc.AsciiDocCompiler.toAsciidoc;
import static pro.verron.officestamper.test.utils.ContextFactory.mapContextFactory;
import static pro.verron.officestamper.test.utils.ContextFactory.objectContextFactory;
import static pro.verron.officestamper.test.utils.ResourceUtils.getWordResource;


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
        var string = toAsciidoc(wordprocessingMLPackage);
        assertEquals("""
                Stamping Table
                
                List of Simpsons characters
                
                |===
                [cnfStyle=100000000000]
                |Character<cnfStyle=001000000000>
                |Actor<cnfStyle=001000000000>
                [cnfStyle=000000100000]
                |Homer Simpson<cnfStyle=001000000000>
                |Dan Castellaneta<cnfStyle=001000000000>
                [cnfStyle=000000100000]
                |Marge Simpson<cnfStyle=001000000000>
                |Julie Kavner<cnfStyle=001000000000>
                [cnfStyle=000000100000]
                |Bart Simpson<cnfStyle=001000000000>
                |Nancy Cartwright<cnfStyle=001000000000>
                [cnfStyle=000000100000]
                |Kent Brockman<cnfStyle=001000000000>
                |Harry Shearer<cnfStyle=001000000000>
                [cnfStyle=000000100000]
                |Disco Stu<cnfStyle=001000000000>
                |Hank Azaria<cnfStyle=001000000000>
                [cnfStyle=000000100000]
                |Krusty the Clown<cnfStyle=001000000000>
                |Dan Castellaneta<cnfStyle=001000000000>
                |===
                
                
                
                There are 6 characters in the above table.
                
                """, string);
    }
}
