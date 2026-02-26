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
                [rowStyle=2048]
                [style=512]
                |Character
                [style=512]
                |Actor
                [rowStyle=32]
                [style=512]
                |Homer Simpson
                [style=512]
                |Dan Castellaneta
                [rowStyle=32]
                [style=512]
                |Marge Simpson
                [style=512]
                |Julie Kavner
                [rowStyle=32]
                [style=512]
                |Bart Simpson
                [style=512]
                |Nancy Cartwright
                [rowStyle=32]
                [style=512]
                |Kent Brockman
                [style=512]
                |Harry Shearer
                [rowStyle=32]
                [style=512]
                |Disco Stu
                [style=512]
                |Hank Azaria
                [rowStyle=32]
                [style=512]
                |Krusty the Clown
                [style=512]
                |Dan Castellaneta
                |===
                
                
                
                There are 6 characters in the above table.
                
                // section {docGrid={charSpace=-6145, linePitch=240}, pgMar={bottom=1134, left=1134, right=1134, top=1134}, pgSz={h=16838, w=11906}, space=720}
                
                """, string);
    }
}
