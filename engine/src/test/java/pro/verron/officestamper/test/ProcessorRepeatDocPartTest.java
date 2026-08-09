package pro.verron.officestamper.test;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.verron.officestamper.test.utils.*;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import static pro.verron.officestamper.preset.OfficeStamperConfigurations.full;
import static pro.verron.officestamper.preset.OfficeStamperConfigurations.standard;
import static pro.verron.officestamper.test.utils.ResourceUtils.getImage;
import static pro.verron.officestamper.test.utils.ResourceUtils.getWordResource;

class ProcessorRepeatDocPartTest extends OfficeStamperTestBase {
    public static final ObjectContextFactory FACTORY = new ObjectContextFactory();

    @DisplayName("In multiple layouts, keeps section orientation outside RepeatDocPart comment")
    @MethodSource("factories")
    @ParameterizedTest(name = "In multiple layouts, keeps section orientation outside RepeatDocPart comment: {argumentSetName}")
    void shouldKeepPageBreakOrientationWithoutSectionBreaksInsideComment(ContextFactory factory) {
        var config = standard();
        var context = Map.of("repeatValues", List.of(factory.name("Homer"), factory.name("Marge")));
        var template = getWordResource(Path.of("ProcessorRepeatDocPart_OutLayout.docx"));
        var expected = """
                First page is landscape.
                
                
                
                
                
                // section {docGrid={linePitch=360}, pgMar={bottom=1418, footer=709, header=709, left=1418, right=1418, top=1418}, pgSz={h=11906, orient=landscape, w=16838}, space=708}
                
                Second page is portrait, layout change should survive to repeatDocPart (Homer).
                
                
                
                <<<
                
                Without a break changing the layout in between (page break should be repeated).
                
                Second page is portrait, layout change should survive to repeatDocPart (Marge).
                
                
                
                <<<
                
                Without a break changing the layout in between (page break should be repeated).
                
                
                
                // section {docGrid={linePitch=360}, pgMar={bottom=1418, footer=709, header=709, left=1418, right=1418, top=1418}, pgSz={h=16838, w=11906}, space=708}
                
                Fourth page is set to landscape again.
                
                // section {docGrid={linePitch=360}, pgMar={bottom=1418, footer=709, header=709, left=1418, right=1418, top=1418}, pgSz={h=11906, orient=landscape, w=16838}, space=708}
                
                """;
        testStamper(config, context, template, expected);
    }

    @DisplayName(
            "RepeatDocPartAndCommentProcessorsIsolationTest_repeatDocPartShouldNotUseSameCommentProcessorInstancesForSubtemplate"
    )
    @MethodSource("factories")
    @ParameterizedTest(
            name = "RepeatDocPartAndCommentProcessorsIsolationTest_repeatDocPartShouldNotUseSameCommentProcessorInstancesForSubtemplate: {argumentSetName}"
    )
    void repeatDocPartShouldNotUseSameCommentProcessorInstancesForSubtemplate(ContextFactory factory) {
        var config = standard();
        var context = factory.tableContext();
        var template = getWordResource(Path.of("ProcessorRepeatDocPart_Isolation.docx"));
        var expected = """
                This will stay untouched.
                
                
                
                |===
                |firstTable value1
                |firstTable value2
                |===
                
                
                
                This will also stay untouched.
                
                
                
                Repeating paragraph :
                
                
                
                repeatDocPart value1
                
                Repeating paragraph :
                
                
                
                repeatDocPart value2
                
                Repeating paragraph :
                
                
                
                repeatDocPart value3
                
                
                
                |===
                |secondTable value1
                |secondTable value2
                |secondTable value3
                |secondTable value4
                |===
                
                
                
                This will stay untouched too.
                
                // section {docGrid={charSpace=-6145, linePitch=240}, pgMar={bottom=1134, left=1134, right=1134, top=1134}, pgSz={h=16838, w=11906}, space=720}
                
                """;
        testStamper(config, context, template, expected);
    }

    @DisplayName("In multiple layouts, keeps section orientations outside RepeatDocPart comments")
    @MethodSource("factories")
    @ParameterizedTest(
            name = "In multiple layouts, keeps section orientations outside RepeatDocPart comments: " + "{argumentSetName}"
    )
    void shouldKeepPageBreakOrientationWithSectionBreaksInsideComment(ContextFactory factory) throws IOException, Docx4JException {
        var config = standard();
        var context = Map.of("repeatValues", List.of(factory.name("Homer"), factory.name("Marge")));
        var template = getWordResource(Path.of("ProcessorRepeatDocPart_InLayout.docx"));
        var expected = """
                First page is portrait.
                
                
                
                
                
                // section {docGrid={linePitch=360}, pgMar={bottom=1418, footer=709, header=709, left=1418, right=1418, top=1418}, pgSz={h=16838, w=11906}, space=708}
                
                Second page is landscape, layout change should survive to repeatDocPart (Homer).
                
                
                
                
                
                // section {docGrid={linePitch=360}, pgMar={bottom=1418, footer=709, header=709, left=1418, right=1418, top=1418}, pgSz={h=11906, orient=landscape, w=16838}, space=708}
                
                With a break setting the layout to portrait in between.
                
                // section {docGrid={linePitch=360}, pgMar={bottom=1418, footer=709, header=709, left=1418, right=1418, top=1418}, pgSz={h=16838, w=11906}, space=708}
                
                Second page is landscape, layout change should survive to repeatDocPart (Marge).
                
                
                
                
                
                // section {docGrid={linePitch=360}, pgMar={bottom=1418, footer=709, header=709, left=1418, right=1418, top=1418}, pgSz={h=11906, orient=landscape, w=16838}, space=708}
                
                With a break setting the layout to portrait in between.
                
                
                
                // section {docGrid={linePitch=360}, pgMar={bottom=1418, footer=709, header=709, left=1418, right=1418, top=1418}, pgSz={h=16838, w=11906}, space=708}
                
                Fourth page is set to landscape again.
                
                // section {docGrid={linePitch=360}, pgMar={bottom=1418, footer=709, header=709, left=1418, right=1418, top=1418}, pgSz={h=11906, orient=landscape, w=16838}, space=708}
                
                """;
        testStamper(config, context, template, expected);
    }

    @DisplayName("Repeat Doc Part Integration test")
    @MethodSource("factories")
    @ParameterizedTest(name = "Repeat Doc Part Integration test: {argumentSetName}")
    void repeatDocPartTest(ContextFactory factory) {
        var config = standard();
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
        var template = getWordResource(Path.of("ProcessorRepeatDocPart.docx"));
        var expected = """
                = Repeating Doc Part
                
                == List of Simpsons characters
                
                Paragraph for test: Homer Simpson - Dan Castellaneta
                
                |===
                |Homer Simpson
                |Dan Castellaneta
                |===
                
                \s
                
                <<<
                
                Paragraph for test: Marge Simpson - Julie Kavner
                
                |===
                |Marge Simpson
                |Julie Kavner
                |===
                
                \s
                
                <<<
                
                Paragraph for test: Bart Simpson - Nancy Cartwright
                
                |===
                |Bart Simpson
                |Nancy Cartwright
                |===
                
                \s
                
                <<<
                
                Paragraph for test: Kent Brockman - Harry Shearer
                
                |===
                |Kent Brockman
                |Harry Shearer
                |===
                
                \s
                
                <<<
                
                Paragraph for test: Disco Stu - Hank Azaria
                
                |===
                |Disco Stu
                |Hank Azaria
                |===
                
                \s
                
                <<<
                
                Paragraph for test: Krusty the Clown - Dan Castellaneta
                
                |===
                |Krusty the Clown
                |Dan Castellaneta
                |===
                
                \s
                
                <<<
                
                There are 6 characters.
                
                // section {docGrid={charSpace=-6145, linePitch=240}, pgMar={bottom=1134, left=1134, right=1134, top=1134}, pgSz={h=16838, w=11906}, space=720}
                
                """;
        testStamper(config, context, template, expected);
    }

    @DisplayName("Repeat Doc Part Integration Test, with nested comments")
    @MethodSource("factories")
    @ParameterizedTest(name = "Repeat Doc Part Integration Test, with nested comments: {argumentSetName}")
    void repeatDocPartNestingTest(ContextFactory factory) throws IOException, Docx4JException {
        var config = full();
        var context = factory.schoolContext();
        var template = getWordResource(Path.of("ProcessorRepeatDocPart_Nesting.docx"));
        var expected = """
                = Repeating Doc Part
                
                [Subtitle]
                Nested doc parts
                
                == List the students of all grades.
                
                South Park Primary School
                
                === Grade No.0
                
                Grade No.0 have 3 classes
                
                ==== Class No.0
                
                Class No.0 have 5 students
                
                |===
                [rowStyle=32]
                |0
                |Bruce·No0
                |1
                [rowStyle=32]
                |1
                |Bruce·No1
                |2
                [rowStyle=32]
                |2
                |Bruce·No2
                |3
                [rowStyle=32]
                |3
                |Bruce·No3
                |4
                [rowStyle=32]
                |4
                |Bruce·No4
                |5
                |===
                
                ==== Class No.1
                
                Class No.1 have 5 students
                
                |===
                [rowStyle=32]
                |0
                |Bruce·No0
                |1
                [rowStyle=32]
                |1
                |Bruce·No1
                |2
                [rowStyle=32]
                |2
                |Bruce·No2
                |3
                [rowStyle=32]
                |3
                |Bruce·No3
                |4
                [rowStyle=32]
                |4
                |Bruce·No4
                |5
                |===
                
                ==== Class No.2
                
                Class No.2 have 5 students
                
                |===
                [rowStyle=32]
                |0
                |Bruce·No0
                |1
                [rowStyle=32]
                |1
                |Bruce·No1
                |2
                [rowStyle=32]
                |2
                |Bruce·No2
                |3
                [rowStyle=32]
                |3
                |Bruce·No3
                |4
                [rowStyle=32]
                |4
                |Bruce·No4
                |5
                |===
                
                === Grade No.1
                
                Grade No.1 have 3 classes
                
                ==== Class No.0
                
                Class No.0 have 5 students
                
                |===
                [rowStyle=32]
                |0
                |Bruce·No0
                |1
                [rowStyle=32]
                |1
                |Bruce·No1
                |2
                [rowStyle=32]
                |2
                |Bruce·No2
                |3
                [rowStyle=32]
                |3
                |Bruce·No3
                |4
                [rowStyle=32]
                |4
                |Bruce·No4
                |5
                |===
                
                ==== Class No.1
                
                Class No.1 have 5 students
                
                |===
                [rowStyle=32]
                |0
                |Bruce·No0
                |1
                [rowStyle=32]
                |1
                |Bruce·No1
                |2
                [rowStyle=32]
                |2
                |Bruce·No2
                |3
                [rowStyle=32]
                |3
                |Bruce·No3
                |4
                [rowStyle=32]
                |4
                |Bruce·No4
                |5
                |===
                
                ==== Class No.2
                
                Class No.2 have 5 students
                
                |===
                [rowStyle=32]
                |0
                |Bruce·No0
                |1
                [rowStyle=32]
                |1
                |Bruce·No1
                |2
                [rowStyle=32]
                |2
                |Bruce·No2
                |3
                [rowStyle=32]
                |3
                |Bruce·No3
                |4
                [rowStyle=32]
                |4
                |Bruce·No4
                |5
                |===
                
                === Grade No.2
                
                Grade No.2 have 3 classes
                
                ==== Class No.0
                
                Class No.0 have 5 students
                
                |===
                [rowStyle=32]
                |0
                |Bruce·No0
                |1
                [rowStyle=32]
                |1
                |Bruce·No1
                |2
                [rowStyle=32]
                |2
                |Bruce·No2
                |3
                [rowStyle=32]
                |3
                |Bruce·No3
                |4
                [rowStyle=32]
                |4
                |Bruce·No4
                |5
                |===
                
                ==== Class No.1
                
                Class No.1 have 5 students
                
                |===
                [rowStyle=32]
                |0
                |Bruce·No0
                |1
                [rowStyle=32]
                |1
                |Bruce·No1
                |2
                [rowStyle=32]
                |2
                |Bruce·No2
                |3
                [rowStyle=32]
                |3
                |Bruce·No3
                |4
                [rowStyle=32]
                |4
                |Bruce·No4
                |5
                |===
                
                ==== Class No.2
                
                Class No.2 have 5 students
                
                |===
                [rowStyle=32]
                |0
                |Bruce·No0
                |1
                [rowStyle=32]
                |1
                |Bruce·No1
                |2
                [rowStyle=32]
                |2
                |Bruce·No2
                |3
                [rowStyle=32]
                |3
                |Bruce·No3
                |4
                [rowStyle=32]
                |4
                |Bruce·No4
                |5
                |===
                
                [rStyle_lev]#There are #[rStyle_lev]#3#[rStyle_lev]# grades.#
                
                // section {cols={col=[{w=8640}]}, pgMar={bottom=720, footer=720, header=720, left=720, right=720, top=720}, pgSz={h=15840, w=12240}, space=720}
                
                """;
        testStamper(config, context, template, expected);
    }

    @MethodSource("factories")
    @DisplayName("Repeat doc part specifications")
    @ParameterizedTest(name = "Repeat doc part specifications: {argumentSetName}")
    void shouldImportImageDataInTheMainDocument(ContextFactory factory) {
        var config = standard();
        var template = getWordResource(Path.of("ProcessorRepeatDocPart_Image.docx"));
        var context = factory.units(getImage(Path.of("sample-butterfly.png")), getImage(Path.of("sample-map.jpg")));
        var expected = """
                
                
                image:rId12[cx=6120130, cy=3060065]
                
                image:rId14[cx=6120130, cy=3761840]
                
                
                
                
                
                
                
                Always rendered:
                
                image:rId12[cx=6120130, cy=3060065]
                
                
                
                // section {docGrid={linePitch=100}, pgMar={bottom=1134, left=1134, right=1134, top=1134}, pgSz={h=16838, w=11906}, space=720}
                
                """;
        testStamper(config, context, template, expected);
    }

    @MethodSource("factories")
    @DisplayName("Repeat doc part specifications with #self")
    @ParameterizedTest(name = "Repeat doc part specifications with #self: {argumentSetName}")
    void shouldImportImageDataWithThisInTheMainDocument() {
        var config = standard();
        var template = DocxFactory.makeWordResource("""
                comment::1[start="0,0", end="1,18", value="repeatDocPart(images)"]
                ${#this}
                
                ${#root.images[0]}
                """);
        var butterflyImage = Path.of("sample-butterfly.png");
        var mapImage = Path.of("sample-map.jpg");
        var context = Map.of("images", List.of(getImage(butterflyImage), getImage(mapImage)));
        var expected = """
                image:rId5[cx=5732145, cy=2866073]
                
                image:rId5[cx=5732145, cy=2866073]
                
                image:rId8[cx=5732145, cy=3523358]
                
                image:rId5[cx=5732145, cy=2866073]
                
                // section {pgMar={bottom=1440, left=1440, right=1440, top=1440}, pgSz={code=9, h=16839, w=11907}}
                
                """;
        testStamper(config, context, template, expected);
    }

    @MethodSource("factories")
    @DisplayName("repeatDocPartWithImagesInSourceTestshouldReplicateImageFromTheMainDocumentInTheSubTemplate")
    @ParameterizedTest(
            name = "repeatDocPartWithImagesInSourceTestshouldReplicateImageFromTheMainDocumentInTheSubTemplate: " + "{argumentSetName}"
    )
    void shouldReplicateImageFromTheMainDocumentInTheSubTemplate(ContextFactory factory) throws Docx4JException, IOException {
        var config = full();
        var context = factory.subDocPartContext();
        var template = getWordResource(Path.of("ProcessorRepeatDocPart_ImageSubTemplate.docx"));
        var expected = """
                This is not repeated
                
                This should be repeated : first doc part
                
                image:rId4[cx=5715000, cy=2857500]
                
                This should be repeated too
                
                This should be repeated : second doc part
                
                image:rId4[cx=5715000, cy=2857500]
                
                This should be repeated too
                
                This is not repeated
                
                // section {docGrid={linePitch=360}, pgMar={bottom=1417, footer=708, header=708, left=1417, right=1417, top=1417}, pgSz={h=16838, w=11906}, space=708}
                
                """;
        testStamper(config, context, template, expected);
    }

    @DisplayName("List of Lists resolution")
    @Test
    void shouldResolveListOfLists() throws IOException {
        var config = full();
        var context = List.of(List.of("S1, Episode 1", "S1, Episode 2"),
                List.of("S2, Episode 1", "S2, Episode 2", "S2, Episode 3", "S2, Episode 4")
        );
        var template = getWordResource(Path.of("ProcessorRepeatDocPart_ListOfList.docx"));
        var expected = """
                = List of Lists
                
                == List of Simpsons Seasons & Episodes
                
                === Season 1
                
                Episode S1, Episode 1
                
                Episode S1, Episode 2
                
                NB Episodes: 2
                
                === Season 2
                
                Episode S2, Episode 1
                
                Episode S2, Episode 2
                
                Episode S2, Episode 3
                
                Episode S2, Episode 4
                
                NB Episodes: 4
                
                // section {docGrid={charSpace=-6145, linePitch=299}, pgMar={bottom=1417, left=1417, right=1417, top=1417}, pgSz={code=9, h=16838, w=11906}, space=720}
                
                """;
        testStamper(config, context, template, expected);
    }

    @DisplayName(
            "In multiple layouts, keeps section orientations inside RepeatDocPart comments with a table as last " + "element"
    )
    @MethodSource("factories")
    @ParameterizedTest(
            name = "In multiple layouts, keeps section orientations inside RepeatDocPart comments with a table as last " + "element: {argumentSetName}"
    )
    void shouldKeepPageBreakOrientationWithSectionBreaksInsideCommentAndTableLastElement(ContextFactory factory) throws IOException, Docx4JException {
        var config = standard();
        var template = getWordResource(Path.of("ProcessorRepeatDocPart_InLayoutAndTable.docx"));
        var context = Map.of("repeatValues", List.of(factory.name("Homer"), factory.name("Marge")));
        var expected = """
                First page is portrait.
                
                
                
                
                
                // section {docGrid={linePitch=360}, pgMar={bottom=1418, footer=709, header=709, left=1418, right=1418, top=1418}, pgSz={h=16838, w=11906}, space=708}
                
                Second page is landscape, layout change should survive to repeatDocPart (Homer).
                
                
                
                
                
                // section {docGrid={linePitch=360}, pgMar={bottom=1418, footer=709, header=709, left=1418, right=1418, top=1418}, pgSz={h=11906, orient=landscape, w=16838}, space=708}
                
                With a break setting the layout to portrait in between.
                
                |===
                |
                |===
                
                
                
                // section {docGrid={linePitch=360}, pgMar={bottom=1418, footer=709, header=709, left=1418, right=1418, top=1418}, pgSz={h=16838, w=11906}, space=708}
                
                Second page is landscape, layout change should survive to repeatDocPart (Marge).
                
                
                
                
                
                // section {docGrid={linePitch=360}, pgMar={bottom=1418, footer=709, header=709, left=1418, right=1418, top=1418}, pgSz={h=11906, orient=landscape, w=16838}, space=708}
                
                With a break setting the layout to portrait in between.
                
                |===
                |
                |===
                
                
                
                // section {docGrid={linePitch=360}, pgMar={bottom=1418, footer=709, header=709, left=1418, right=1418, top=1418}, pgSz={h=16838, w=11906}, space=708}
                
                Fourth page is set to landscape again.
                
                // section {docGrid={linePitch=360}, pgMar={bottom=1418, footer=709, header=709, left=1418, right=1418, top=1418}, pgSz={h=11906, orient=landscape, w=16838}, space=708}
                
                """;
        testStamper(config, context, template, expected);
    }

    @Test
    void shouldAcceptList() {
        var config = standard();
        var template = DocxFactory.makeWordResource("""
                comment::1[start="0,0", end="0,7", value="repeatDocPart(names)"]
                ${name}
                """);
        var context = FACTORY.names(List.class, "Homer", "Marge", "Bart", "Lisa", "Maggie");
        var expected = """
                Homer
                
                Marge
                
                Bart
                
                Lisa
                
                Maggie
                
                // section {pgMar={bottom=1440, left=1440, right=1440, top=1440}, pgSz={code=9, h=16839, w=11907}}
                
                """;
        testStamper(config, context, template, expected);
    }

    @Test
    void shouldAcceptSet() {
        var config = standard();
        var template = DocxFactory.makeWordResource("""
                comment::1[start="0,0", end="0,7", value="repeatDocPart(names)"]
                ${name}
                """);
        var context = FACTORY.names(Set.class, "Homer", "Marge", "Bart", "Lisa", "Maggie");
        var expected = """
                Marge
                
                Homer
                
                Maggie
                
                Bart
                
                Lisa
                
                // section {pgMar={bottom=1440, left=1440, right=1440, top=1440}, pgSz={code=9, h=16839, w=11907}}
                
                """;
        testStamper(config, context, template, expected);
    }

    @Test
    void shouldAcceptQueue() {
        var config = standard();
        var template = DocxFactory.makeWordResource("""
                comment::1[start="0,0", end="0,7", value="repeatDocPart(names)"]
                ${name}
                """);
        var context = FACTORY.names(Queue.class, "Homer", "Marge", "Bart", "Lisa", "Maggie");
        var expected = """
                Homer
                
                Marge
                
                Bart
                
                Lisa
                
                Maggie
                
                // section {pgMar={bottom=1440, left=1440, right=1440, top=1440}, pgSz={code=9, h=16839, w=11907}}
                
                """;
        testStamper(config, context, template, expected);
    }
}
