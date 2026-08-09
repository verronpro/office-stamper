package pro.verron.officestamper.test;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import pro.verron.officestamper.test.utils.ContextFactory;
import pro.verron.officestamper.test.utils.ObjectContextFactory;
import pro.verron.officestamper.test.utils.OfficeStamperTestBase;
import pro.verron.officestamper.test.utils.ResourceUtils;

import java.nio.file.Path;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import static pro.verron.officestamper.preset.OfficeStamperConfigurations.full;
import static pro.verron.officestamper.test.utils.DocxFactory.makeWordResource;
import static pro.verron.officestamper.test.utils.ResourceUtils.getWordResource;

class ProcessorRepeatTableRowTest extends OfficeStamperTestBase {
    private static final ObjectContextFactory FACTORY = new ObjectContextFactory();

    @MethodSource("factories")
    @ParameterizedTest(name = "Repeat Table row Integration test (keeps formatting)")
    public void repeatTableRowKeepsFormatTest(ContextFactory factory) {
        var config = full();
        var context = factory.show();
        var template = getWordResource(Path.of("ProcessorRepeatTableRow_KeepsFormatTest.docx"));
        var expected = """
                |===
                |1^st^ Homer Simpson-*Dan Castellaneta*
                |2^nd^ Marge Simpson-*Julie Kavner*
                |3^rd^ Bart Simpson-*Nancy Cartwright*
                |4^th^ Lisa Simpson-*Yeardley Smith*
                |5^th^ Maggie Simpson-*Julie Kavner*
                |===
                
                
                
                // section {docGrid={linePitch=360}, pgMar={bottom=1417, footer=708, header=708, left=1417, right=1417, top=1417}, pgSz={h=16838, w=11906}, space=708}
                
                """;
        testStamper(config, context, template, expected);
    }

    @MethodSource("factories")
    @ParameterizedTest(name = "Repeating table rows should be possible while replacing various linebreaks")
    public void repeatingRowsWithLineBreak(ContextFactory factory) {
        var config = full();
        var context = factory.roles("Homer Simpson",
                "Dan Castellaneta",
                "Marge Simpson",
                "Julie\nKavner",
                "Bart Simpson",
                "Nancy\n\nCartwright",
                "Kent Brockman",
                "Harry\n\n\nShearer",
                "Disco Stu",
                "Hank\n\nAzaria",
                "Krusty the Clown",
                "Dan\nCastellaneta"
        );
        var template = getWordResource(Path.of("ProcessorRepeatTableRow.docx"));
        var expected = """
                Repeating Table Rows
                
                List of Simpsons characters
                
                |===
                [rowStyle=2048]
                [style=512]
                |Character name
                |Voice Actor
                [rowStyle=32]
                [style=512]
                |Homer Simpson
                |Dan Castellaneta
                [rowStyle=32]
                [style=512]
                |Marge Simpson
                |Julie +
                Kavner
                [rowStyle=32]
                [style=512]
                |Bart Simpson
                |Nancy +
                 +
                Cartwright
                [rowStyle=32]
                [style=512]
                |Kent Brockman
                |Harry +
                 +
                 +
                Shearer
                [rowStyle=32]
                [style=512]
                |Disco Stu
                |Hank +
                 +
                Azaria
                [rowStyle=32]
                [style=512]
                |Krusty the Clown
                |Dan +
                Castellaneta
                |===
                
                
                
                There are 6 characters in the above table.
                
                // section {docGrid={charSpace=-6145, linePitch=240}, pgMar={bottom=1134, left=1134, right=1134, top=1134}, pgSz={h=16838, w=11906}, space=720}
                
                """;
        testStamper(config, context, template, expected);
    }

    @MethodSource("factories")
    @ParameterizedTest(name = "Repeating table rows should be possible")
    public void repeatingRows(ContextFactory factory) {
        var config = full();
        var context = factory.roles("Homer Simpson",
                "Dan Castellaneta",
                "Marge Simpson",
                "Julie Kavner",
                "Bart Simpson",
                "Nancy Cartwright",
                "Kent Brockman",
                "Harry Shearer",
                "Disco Stu",
                "Hank Azaria",
                "Krusty the Clown",
                "Dan Castellaneta"
        );
        var template = getWordResource(Path.of("ProcessorRepeatTableRow.docx"));
        var expected = """
                Repeating Table Rows
                
                List of Simpsons characters
                
                |===
                [rowStyle=2048]
                [style=512]
                |Character name
                |Voice Actor
                [rowStyle=32]
                [style=512]
                |Homer Simpson
                |Dan Castellaneta
                [rowStyle=32]
                [style=512]
                |Marge Simpson
                |Julie Kavner
                [rowStyle=32]
                [style=512]
                |Bart Simpson
                |Nancy Cartwright
                [rowStyle=32]
                [style=512]
                |Kent Brockman
                |Harry Shearer
                [rowStyle=32]
                [style=512]
                |Disco Stu
                |Hank Azaria
                [rowStyle=32]
                [style=512]
                |Krusty the Clown
                |Dan Castellaneta
                |===
                
                
                
                There are 6 characters in the above table.
                
                // section {docGrid={charSpace=-6145, linePitch=240}, pgMar={bottom=1134, left=1134, right=1134, top=1134}, pgSz={h=16838, w=11906}, space=720}
                
                """;
        testStamper(config, context, template, expected);
    }

    @Test
    void shouldAcceptList() {
        var config = full();
        var template = makeWordResource("""
                comment::1[start="0,0", end="0,7", value="repeatTableRow(names)"]
                |===
                |${name}
                |===
                """);
        var context = FACTORY.names(List.class, "Homer", "Marge", "Bart", "Lisa", "Maggie");
        var expected = """
                |===
                |Homer
                |Marge
                |Bart
                |Lisa
                |Maggie
                |===
                
                // section {pgMar={bottom=1440, left=1440, right=1440, top=1440}, pgSz={code=9, h=16839, w=11907}}
                
                """;
        testStamper(config, context, template, expected);
    }

    @Test
    void shouldAcceptSet() {
        var config = full();
        var template = makeWordResource("""
                comment::1[start="0,0", end="0,7", value="repeatTableRow(names)"]
                |===
                |${name}
                |===
                """);
        var context = FACTORY.names(Set.class, "Homer", "Marge", "Bart", "Lisa", "Maggie");
        var expected = """
                |===
                |Marge
                |Homer
                |Maggie
                |Bart
                |Lisa
                |===
                
                // section {pgMar={bottom=1440, left=1440, right=1440, top=1440}, pgSz={code=9, h=16839, w=11907}}
                
                """;
        testStamper(config, context, template, expected);
    }

    @Test
    void shouldAcceptQueue() {
        var config = full();
        var template = makeWordResource("""
                comment::1[start="0,0", end="0,7", value="repeatTableRow(names)"]
                |===
                |${name}
                
                |===
                """);
        var context = FACTORY.names(Queue.class, "Homer", "Marge", "Bart", "Lisa", "Maggie");
        var expected = """
                |===
                |Homer
                |Marge
                |Bart
                |Lisa
                |Maggie
                |===
                
                // section {pgMar={bottom=1440, left=1440, right=1440, top=1440}, pgSz={code=9, h=16839, w=11907}}
                
                """;
        testStamper(config, context, template, expected);
    }
}
