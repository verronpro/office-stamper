package pro.verron.officestamper.test;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.verron.officestamper.api.OfficeStamperConfiguration;
import pro.verron.officestamper.test.utils.ContextFactory;
import pro.verron.officestamper.test.utils.DocxFactory;
import pro.verron.officestamper.test.utils.ObjectContextFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.argumentSet;
import static pro.verron.officestamper.asciidoc.AsciiDocCompiler.toAsciidoc;
import static pro.verron.officestamper.preset.OfficeStamperConfigurations.full;
import static pro.verron.officestamper.preset.OfficeStamperConfigurations.standard;
import static pro.verron.officestamper.preset.OfficeStampers.docxPackageStamper;
import static pro.verron.officestamper.test.utils.ContextFactory.mapContextFactory;
import static pro.verron.officestamper.test.utils.ContextFactory.objectContextFactory;
import static pro.verron.officestamper.test.utils.ResourceUtils.getImage;
import static pro.verron.officestamper.test.utils.ResourceUtils.getWordResource;

class ProcessorRepeatDocPartTest {
    public static final ObjectContextFactory FACTORY = new ObjectContextFactory();
    private static final Logger log = LoggerFactory.getLogger(ProcessorRepeatDocPartTest.class);

    static Stream<Arguments> factories() {
        return Stream.of(argumentSet("Object-based", objectContextFactory()),
                argumentSet("Map-based", mapContextFactory()));
    }

    @DisplayName("In multiple layouts, keeps section orientation outside RepeatDocPart comment")
    @MethodSource("factories")
    @ParameterizedTest(name = "In multiple layouts, keeps section orientation outside RepeatDocPart comment: "
                              + "{argumentSetName}")
    void shouldKeepPageBreakOrientationWithoutSectionBreaksInsideComment(ContextFactory factory)
            throws IOException, Docx4JException {
        OfficeStamperConfiguration config = standard();
        Object context = Map.of("repeatValues", List.of(factory.name("Homer"), factory.name("Marge")));
        WordprocessingMLPackage template = getWordResource(Path.of("ProcessorRepeatDocPart_OutLayout.docx"));
        assertEquals("""
                comment::0[start="3,0", end="5,0", value="repeatDocPart(repeatValues)"]
                
                First page is landscape.
                
                
                
                
                
                // section {docGrid={linePitch=360}, pgMar={bottom=1418, footer=709, header=709, left=1418, right=1418, top=1418}, pgSz={h=11906, orient=landscape, w=16838}, space=708}
                
                Second page is portrait, layout change should survive to repeatDocPart (${name}).
                
                
                
                <<<
                
                Without a break changing the layout in between (page break should be repeated).
                
                
                
                // section {docGrid={linePitch=360}, pgMar={bottom=1418, footer=709, header=709, left=1418, right=1418, top=1418}, pgSz={h=16838, w=11906}, space=708}
                
                Fourth page is set to landscape again.
                
                // section {docGrid={linePitch=360}, pgMar={bottom=1418, footer=709, header=709, left=1418, right=1418, top=1418}, pgSz={h=11906, orient=landscape, w=16838}, space=708}
                
                """, toAsciidoc(template));

        var stamper = docxPackageStamper(config);
        var stamped = stamper.stamp(template, context);
        var tempFile = File.createTempFile("pre", ".docx");
        log.debug(tempFile.getAbsolutePath());
        stamped.save(tempFile);
        var actual = toAsciidoc(stamped);
        assertEquals("""
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
                
                """, actual);
    }

    @DisplayName(
            "RepeatDocPartAndCommentProcessorsIsolationTest_repeatDocPartShouldNotUseSameCommentProcessorInstancesForSubtemplate")
    @MethodSource("factories")
    @ParameterizedTest(name =
            "RepeatDocPartAndCommentProcessorsIsolationTest_repeatDocPartShouldNotUseSameCommentProcessorInstancesForSubtemplate: {argumentSetName}")
    void repeatDocPartShouldNotUseSameCommentProcessorInstancesForSubtemplate(ContextFactory factory) {
        OfficeStamperConfiguration config = standard();
        Object context = factory.tableContext();
        WordprocessingMLPackage template = getWordResource(Path.of("ProcessorRepeatDocPart_Isolation.docx"));
        String expected = """
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

        var stamper = docxPackageStamper(config);
        var stamped = stamper.stamp(template, context);
        var actual = toAsciidoc(stamped);
        assertEquals(expected, actual);
    }

    @DisplayName("In multiple layouts, keeps section orientations outside RepeatDocPart comments")
    @MethodSource("factories")
    @ParameterizedTest(name = "In multiple layouts, keeps section orientations outside RepeatDocPart comments: "
                              + "{argumentSetName}")
    void shouldKeepPageBreakOrientationWithSectionBreaksInsideComment(ContextFactory factory)
            throws IOException, Docx4JException {
        OfficeStamperConfiguration config = standard();
        Object context = Map.of("repeatValues", List.of(factory.name("Homer"), factory.name("Marge")));
        WordprocessingMLPackage template = getWordResource(Path.of("ProcessorRepeatDocPart_InLayout.docx"));
        String expected = """
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

        var stamper = docxPackageStamper(config);
        var stamped = stamper.stamp(template, context);
        var tempFile = File.createTempFile("pre", ".docx");
        log.debug(tempFile.getAbsolutePath());
        stamped.save(tempFile);
        var actual = toAsciidoc(stamped);
        assertEquals(expected, actual);
    }

    @DisplayName("Repeat Doc Part Integration test")
    @MethodSource("factories")
    @ParameterizedTest(name = "Repeat Doc Part Integration test: {argumentSetName}")
    void repeatDocPartTest(ContextFactory factory) {
        OfficeStamperConfiguration config = standard();
        Object context = factory.roles("Homer Simpson",
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
                "Dan Castellaneta");
        WordprocessingMLPackage template = getWordResource(Path.of("ProcessorRepeatDocPart.docx"));
        String expect = """
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

        var stamper = docxPackageStamper(config);
        var stamped = stamper.stamp(template, context);
        var actual = toAsciidoc(stamped);
        assertEquals(expect, actual);
    }

    @DisplayName("Repeat Doc Part Integration Test, with nested comments")
    @MethodSource("factories")
    @ParameterizedTest(name = "Repeat Doc Part Integration Test, with nested comments: {argumentSetName}")
    void repeatDocPartNestingTest(ContextFactory factory)
            throws IOException, Docx4JException {
        OfficeStamperConfiguration config = full();
        Object context = factory.schoolContext();
        WordprocessingMLPackage template = getWordResource(Path.of("ProcessorRepeatDocPart_Nesting.docx"));
        String expect = """
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

        var stamper = docxPackageStamper(config);
        var stamped = stamper.stamp(template, context);
        var tempFile = File.createTempFile("pre", ".docx");
        log.debug(tempFile.getAbsolutePath());
        stamped.save(tempFile);
        var actual = toAsciidoc(stamped);
        assertEquals(expect, actual);
    }

    @MethodSource("factories")
    @DisplayName("Repeat doc part specifications")
    @ParameterizedTest(name = "Repeat doc part specifications: {argumentSetName}")
    void shouldImportImageDataInTheMainDocument(ContextFactory factory) {
        var stamper = docxPackageStamper(standard());
        var stamped = stamper.stamp(getWordResource(Path.of("ProcessorRepeatDocPart_Image.docx")),
                factory.units(getImage(Path.of("butterfly.png")), getImage(Path.of("map.jpg"))));
        var actual = toAsciidoc(stamped);
        assertEquals("""
                
                
                image:rId11[cx=6120130, cy=3060065]
                
                image:rId12[cx=6120130, cy=3761840]
                
                
                
                
                
                
                
                Always rendered:
                
                image:rId13[cx=6120130, cy=3060065]
                
                
                
                // section {docGrid={linePitch=100}, pgMar={bottom=1134, left=1134, right=1134, top=1134}, pgSz={h=16838, w=11906}, space=720}
                
                """, actual);
    }

    @MethodSource("factories")
    @DisplayName("Repeat doc part specifications with #self")
    @Test
    void shouldImportImageDataWithThisInTheMainDocument() {
        var stamper = docxPackageStamper(standard());
        var stamped = stamper.stamp(getWordResource(Path.of("ProcessorRepeatDocPart_Image2.docx")),
                Map.of("images", List.of(getImage(Path.of("butterfly.png")), getImage(Path.of("map.jpg")))));
        var actual = docxToString(stamped);
        assertEquals("""
                
                
                /word/media/document_image_rId11.png:rId11:image/png:193.6 kB:sha1=t8UNAmo7yJgZJk9g7pLLIb3AvCA=:cy=$d:6120130
                
                /word/media/document_image_rId12.jpeg:rId12:image/jpeg:407.5 kB:sha1=Ujo3UzL8WmeZN/1K6weBydaI73I=:cy=$d:6120130
                
                
                
                """, actual);
    }

    @MethodSource("factories")
    @DisplayName("repeatDocPartWithImagesInSourceTestshouldReplicateImageFromTheMainDocumentInTheSubTemplate")
    @ParameterizedTest(name =
            "repeatDocPartWithImagesInSourceTestshouldReplicateImageFromTheMainDocumentInTheSubTemplate: "
            + "{argumentSetName}")
    void shouldReplicateImageFromTheMainDocumentInTheSubTemplate(ContextFactory factory)
            throws Docx4JException, IOException {
        OfficeStamperConfiguration config = full();
        Object context = factory.subDocPartContext();
        WordprocessingMLPackage template = getWordResource(Path.of("ProcessorRepeatDocPart_ImageSubTemplate.docx"));
        String expected = """
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
        var stamper = docxPackageStamper(config);
        var stamped = stamper.stamp(template, context);
        var tempFile = File.createTempFile("pre", ".docx");
        log.debug(tempFile.getAbsolutePath());
        stamped.save(tempFile);
        var actual = toAsciidoc(stamped);
        assertEquals(expected, actual);
    }

    @DisplayName("In multiple layouts, keeps section orientations inside RepeatDocPart comments with a table as last "
                 + "element")
    @MethodSource("factories")
    @ParameterizedTest(name =
            "In multiple layouts, keeps section orientations inside RepeatDocPart comments with a table as last "
            + "element: {argumentSetName}")
    void shouldKeepPageBreakOrientationWithSectionBreaksInsideCommentAndTableLastElement(ContextFactory factory)
            throws IOException, Docx4JException {
        var stamper = docxPackageStamper(standard());
        var template = getWordResource(Path.of("ProcessorRepeatDocPart_InLayoutAndTable.docx"));
        var context = Map.of("repeatValues", List.of(factory.name("Homer"), factory.name("Marge")));

        var stamped = stamper.stamp(template, context);

        var tempFile = File.createTempFile("pre", ".docx");
        log.debug(tempFile.getAbsolutePath());
        stamped.save(tempFile);

        var actual = toAsciidoc(stamped);
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
        assertEquals(expected, actual);
    }

    @Test
    void shouldAcceptList() {
        var config = standard();
        var stamper = docxPackageStamper(config);
        var template = DocxFactory.makeWordResource("""
                comment::1[start="0,0", end="0,7", value="repeatDocPart(names)"]
                ${name}
                """);
        var context = FACTORY.names(List.class, "Homer", "Marge", "Bart", "Lisa", "Maggie");
        var stamped = stamper.stamp(template, context);
        var actual = toAsciidoc(stamped);
        var expected = """
                Homer
                
                Marge
                
                Bart
                
                Lisa
                
                Maggie
                
                // section {pgMar={bottom=1440, left=1440, right=1440, top=1440}, pgSz={code=9, h=16839, w=11907}}
                
                """;
        assertEquals(expected, actual);
    }

    @Test
    void shouldAcceptSet() {
        var config = standard();
        var stamper = docxPackageStamper(config);
        var template = DocxFactory.makeWordResource("""
                comment::1[start="0,0", end="0,7", value="repeatDocPart(names)"]
                ${name}
                """);
        var context = FACTORY.names(Set.class, "Homer", "Marge", "Bart", "Lisa", "Maggie");
        var stamped = stamper.stamp(template, context);
        var actual = toAsciidoc(stamped);
        var expected = """
                Marge
                
                Homer
                
                Maggie
                
                Bart
                
                Lisa
                
                // section {pgMar={bottom=1440, left=1440, right=1440, top=1440}, pgSz={code=9, h=16839, w=11907}}
                
                """;
        assertEquals(expected, actual);
    }

    @Test
    void shouldAcceptQueue() {
        var config = standard();
        var stamper = docxPackageStamper(config);
        var template = DocxFactory.makeWordResource("""
                comment::1[start="0,0", end="0,7", value="repeatDocPart(names)"]
                ${name}
                """);
        var context = FACTORY.names(Queue.class, "Homer", "Marge", "Bart", "Lisa", "Maggie");
        var stamped = stamper.stamp(template, context);
        var actual = toAsciidoc(stamped);
        var expected = """
                Homer
                
                Marge
                
                Bart
                
                Lisa
                
                Maggie
                
                // section {pgMar={bottom=1440, left=1440, right=1440, top=1440}, pgSz={code=9, h=16839, w=11907}}
                
                """;
        assertEquals(expected, actual);
    }
}
