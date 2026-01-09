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
import pro.verron.officestamper.ContextFactory;
import pro.verron.officestamper.ObjectContextFactory;
import pro.verron.officestamper.api.OfficeStamperConfiguration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.argumentSet;
import static pro.verron.officestamper.ContextFactory.mapContextFactory;
import static pro.verron.officestamper.ContextFactory.objectContextFactory;
import static pro.verron.officestamper.preset.OfficeStamperConfigurations.full;
import static pro.verron.officestamper.preset.OfficeStamperConfigurations.standard;
import static pro.verron.officestamper.preset.OfficeStampers.docxPackageStamper;
import static pro.verron.officestamper.test.TestUtils.*;
import static pro.verron.officestamper.utils.wml.DocxRenderer.docxToString;

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
                First page is landscape.
                
                
                
                
                [section-break, {docGrid={linePitch=360},pgMar={bottom=1418,footer=709,gutter=0,header=709,left=1418,right=1418,top=1418},pgSz={h=11906,orient=LANDSCAPE,w=16838}}]
                <<<
                
                <0|Second page is portrait, layout change should survive to repeatDocPart (${name}).
                
                
                [page-break]
                <<<
                
                
                Without a break changing the layout in between (page break should be repeated).|0>❬<0|repeatDocPart(repeatValues)>❘{rStyle=Marquedecommentaire}❭
                
                
                [section-break, {docGrid={linePitch=360},pgMar={bottom=1418,footer=709,gutter=0,header=709,left=1418,right=1418,top=1418},pgSz={h=16838,w=11906}}]
                <<<
                
                Fourth page is set to landscape again.
                
                """, docxToString(template));

        var stamper = docxPackageStamper(config);
        var stamped = stamper.stamp(template, context);
        var tempFile = File.createTempFile("pre", ".docx");
        log.debug(tempFile.getAbsolutePath());
        stamped.save(tempFile);
        var actual = docxToString(stamped);
        assertEquals("""
                First page is landscape.
                
                
                
                
                [section-break, {docGrid={linePitch=360},pgMar={bottom=1418,footer=709,gutter=0,header=709,left=1418,right=1418,top=1418},pgSz={h=11906,orient=LANDSCAPE,w=16838}}]
                <<<
                
                Second page is portrait, layout change should survive to repeatDocPart (Homer).
                
                
                [page-break]
                <<<
                
                
                Without a break changing the layout in between (page break should be repeated).
                
                Second page is portrait, layout change should survive to repeatDocPart (Marge).
                
                
                [page-break]
                <<<
                
                
                Without a break changing the layout in between (page break should be repeated).
                
                
                [section-break, {docGrid={linePitch=360},pgMar={bottom=1418,footer=709,gutter=0,header=709,left=1418,right=1418,top=1418},pgSz={h=16838,w=11906}}]
                <<<
                
                Fourth page is set to landscape again.
                
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
                
                """;

        var stamper = docxPackageStamper(config);
        var stamped = stamper.stamp(template, context);
        var actual = docxToString(stamped);
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
                
                
                
                
                [section-break, {docGrid={linePitch=360},pgMar={bottom=1418,footer=709,gutter=0,header=709,left=1418,right=1418,top=1418},pgSz={h=16838,w=11906}}]
                <<<
                
                Second page is landscape, layout change should survive to repeatDocPart (Homer).
                
                
                
                
                [section-break, {docGrid={linePitch=360},pgMar={bottom=1418,footer=709,gutter=0,header=709,left=1418,right=1418,top=1418},pgSz={h=11906,orient=LANDSCAPE,w=16838}}]
                <<<
                
                With a break setting the layout to portrait in between.
                [section-break, {docGrid={linePitch=360},pgMar={bottom=1418,footer=709,gutter=0,header=709,left=1418,right=1418,top=1418},pgSz={h=16838,w=11906}}]
                <<<
                
                Second page is landscape, layout change should survive to repeatDocPart (Marge).
                
                
                
                
                [section-break, {docGrid={linePitch=360},pgMar={bottom=1418,footer=709,gutter=0,header=709,left=1418,right=1418,top=1418},pgSz={h=11906,orient=LANDSCAPE,w=16838}}]
                <<<
                
                With a break setting the layout to portrait in between.
                
                
                [section-break, {docGrid={linePitch=360},pgMar={bottom=1418,footer=709,gutter=0,header=709,left=1418,right=1418,top=1418},pgSz={h=16838,w=11906}}]
                <<<
                
                Fourth page is set to landscape again.
                
                """;

        var stamper = docxPackageStamper(config);
        var stamped = stamper.stamp(template, context);
        var tempFile = File.createTempFile("pre", ".docx");
        log.debug(tempFile.getAbsolutePath());
        stamped.save(tempFile);
        var actual = docxToString(stamped);
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
                [page-break]
                <<<
                
                
                Paragraph for test: Marge Simpson - Julie Kavner
                
                |===
                |Marge Simpson
                |Julie Kavner
                
                
                |===
                \s
                [page-break]
                <<<
                
                
                Paragraph for test: Bart Simpson - Nancy Cartwright
                
                |===
                |Bart Simpson
                |Nancy Cartwright
                
                
                |===
                \s
                [page-break]
                <<<
                
                
                Paragraph for test: Kent Brockman - Harry Shearer
                
                |===
                |Kent Brockman
                |Harry Shearer
                
                
                |===
                \s
                [page-break]
                <<<
                
                
                Paragraph for test: Disco Stu - Hank Azaria
                
                |===
                |Disco Stu
                |Hank Azaria
                
                
                |===
                \s
                [page-break]
                <<<
                
                
                Paragraph for test: Krusty the Clown - Dan Castellaneta
                
                |===
                |Krusty the Clown
                |Dan Castellaneta
                
                
                |===
                \s
                [page-break]
                <<<
                
                
                There are 6 characters.
                
                """;

        var stamper = docxPackageStamper(config);
        var stamped = stamper.stamp(template, context);
        var actual = docxToString(stamped);
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
                
                
                [Subtitle] Nested doc parts
                
                == List the students of all grades.
                
                
                South Park Primary School
                
                === Grade No.0
                
                
                Grade No.0 have 3 classes
                
                ==== Class No.0
                
                
                Class No.0 have 5 students
                
                |===
                |0
                |Bruce·No0
                |1
                
                |1
                |Bruce·No1
                |2
                
                |2
                |Bruce·No2
                |3
                
                |3
                |Bruce·No3
                |4
                
                |4
                |Bruce·No4
                |5
                
                
                |===
                ==== Class No.1
                
                
                Class No.1 have 5 students
                
                |===
                |0
                |Bruce·No0
                |1
                
                |1
                |Bruce·No1
                |2
                
                |2
                |Bruce·No2
                |3
                
                |3
                |Bruce·No3
                |4
                
                |4
                |Bruce·No4
                |5
                
                
                |===
                ==== Class No.2
                
                
                Class No.2 have 5 students
                
                |===
                |0
                |Bruce·No0
                |1
                
                |1
                |Bruce·No1
                |2
                
                |2
                |Bruce·No2
                |3
                
                |3
                |Bruce·No3
                |4
                
                |4
                |Bruce·No4
                |5
                
                
                |===
                === Grade No.1
                
                
                Grade No.1 have 3 classes
                
                ==== Class No.0
                
                
                Class No.0 have 5 students
                
                |===
                |0
                |Bruce·No0
                |1
                
                |1
                |Bruce·No1
                |2
                
                |2
                |Bruce·No2
                |3
                
                |3
                |Bruce·No3
                |4
                
                |4
                |Bruce·No4
                |5
                
                
                |===
                ==== Class No.1
                
                
                Class No.1 have 5 students
                
                |===
                |0
                |Bruce·No0
                |1
                
                |1
                |Bruce·No1
                |2
                
                |2
                |Bruce·No2
                |3
                
                |3
                |Bruce·No3
                |4
                
                |4
                |Bruce·No4
                |5
                
                
                |===
                ==== Class No.2
                
                
                Class No.2 have 5 students
                
                |===
                |0
                |Bruce·No0
                |1
                
                |1
                |Bruce·No1
                |2
                
                |2
                |Bruce·No2
                |3
                
                |3
                |Bruce·No3
                |4
                
                |4
                |Bruce·No4
                |5
                
                
                |===
                === Grade No.2
                
                
                Grade No.2 have 3 classes
                
                ==== Class No.0
                
                
                Class No.0 have 5 students
                
                |===
                |0
                |Bruce·No0
                |1
                
                |1
                |Bruce·No1
                |2
                
                |2
                |Bruce·No2
                |3
                
                |3
                |Bruce·No3
                |4
                
                |4
                |Bruce·No4
                |5
                
                
                |===
                ==== Class No.1
                
                
                Class No.1 have 5 students
                
                |===
                |0
                |Bruce·No0
                |1
                
                |1
                |Bruce·No1
                |2
                
                |2
                |Bruce·No2
                |3
                
                |3
                |Bruce·No3
                |4
                
                |4
                |Bruce·No4
                |5
                
                
                |===
                ==== Class No.2
                
                
                Class No.2 have 5 students
                
                |===
                |0
                |Bruce·No0
                |1
                
                |1
                |Bruce·No1
                |2
                
                |2
                |Bruce·No2
                |3
                
                |3
                |Bruce·No3
                |4
                
                |4
                |Bruce·No4
                |5
                
                
                |===
                ❬There are ❘{rStyle=lev}❭❬3❘{rStyle=lev}❭❬ grades.❘{rStyle=lev}❭<rPr={rStyle=lev}>
                
                """;

        var stamper = docxPackageStamper(config);
        var stamped = stamper.stamp(template, context);
        var tempFile = File.createTempFile("pre", ".docx");
        log.debug(tempFile.getAbsolutePath());
        stamped.save(tempFile);
        var actual = docxToString(stamped);
        assertEquals(expect, actual);
    }

    @MethodSource("factories")
    @DisplayName("Repeat doc part specifications")
    @ParameterizedTest(name = "Repeat doc part specifications: {argumentSetName}")
    void shouldImportImageDataInTheMainDocument(ContextFactory factory) {
        var stamper = docxPackageStamper(standard());
        var stamped = stamper.stamp(getWordResource(Path.of("ProcessorRepeatDocPart_Image.docx")),
                factory.units(getImage(Path.of("butterfly.png")), getImage(Path.of("map.jpg"))));
        var actual = docxToString(stamped);
        assertEquals("""
                
                
                /word/media/document_image_rId11.png:rId11:image/png:193.6 kB:sha1=t8UNAmo7yJgZJk9g7pLLIb3AvCA=:cy=$d:6120130
                
                /word/media/document_image_rId12.jpeg:rId12:image/jpeg:407.5 kB:sha1=Ujo3UzL8WmeZN/1K6weBydaI73I=:cy=$d:6120130
                
                
                
                
                
                
                
                Always rendered:
                
                /word/media/document_image_rId13.png:rId13:image/png:193.6 kB:sha1=t8UNAmo7yJgZJk9g7pLLIb3AvCA=:cy=$d:6120130
                
                
                
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
                
                /word/media/image1.png:rId4:image/png:193.6 kB:sha1=t8UNAmo7yJgZJk9g7pLLIb3AvCA=:cy=$d:5715000
                
                This should be repeated too
                
                This should be repeated : second doc part
                
                /word/media/image1.png:rId4:image/png:193.6 kB:sha1=t8UNAmo7yJgZJk9g7pLLIb3AvCA=:cy=$d:5715000
                
                This should be repeated too
                
                This is not repeated
                
                """;
        var stamper = docxPackageStamper(config);
        var stamped = stamper.stamp(template, context);
        var tempFile = File.createTempFile("pre", ".docx");
        log.debug(tempFile.getAbsolutePath());
        stamped.save(tempFile);
        var actual = docxToString(stamped);
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

        var actual = docxToString(stamped);
        var expected = """
                First page is portrait.
                
                
                
                
                [section-break, {docGrid={linePitch=360},pgMar={bottom=1418,footer=709,gutter=0,header=709,left=1418,right=1418,top=1418},pgSz={h=16838,w=11906}}]
                <<<
                
                Second page is landscape, layout change should survive to repeatDocPart (Homer).
                
                
                
                
                [section-break, {docGrid={linePitch=360},pgMar={bottom=1418,footer=709,gutter=0,header=709,left=1418,right=1418,top=1418},pgSz={h=11906,orient=LANDSCAPE,w=16838}}]
                <<<
                
                With a break setting the layout to portrait in between.
                
                |===
                |
                
                
                |===
                
                [section-break, {docGrid={linePitch=360},pgMar={bottom=1418,footer=709,gutter=0,header=709,left=1418,right=1418,top=1418},pgSz={h=16838,w=11906}}]
                <<<
                
                Second page is landscape, layout change should survive to repeatDocPart (Marge).
                
                
                
                
                [section-break, {docGrid={linePitch=360},pgMar={bottom=1418,footer=709,gutter=0,header=709,left=1418,right=1418,top=1418},pgSz={h=11906,orient=LANDSCAPE,w=16838}}]
                <<<
                
                With a break setting the layout to portrait in between.
                
                |===
                |
                
                
                |===
                
                [section-break, {docGrid={linePitch=360},pgMar={bottom=1418,footer=709,gutter=0,header=709,left=1418,right=1418,top=1418},pgSz={h=16838,w=11906}}]
                <<<
                
                Fourth page is set to landscape again.
                
                """;
        assertEquals(expected, actual);
    }

    @Test
    void shouldAcceptList() {
        var config = standard();
        var stamper = docxPackageStamper(config);
        var template = makeWordResource("""
                comment::1[start="0,0", end="0,7", value="repeatDocPart(names)"]
                ${name}
                """);
        var context = FACTORY.names(List.class, "Homer", "Marge", "Bart", "Lisa", "Maggie");
        var stamped = stamper.stamp(template, context);
        var actual = docxToString(stamped);
        var expected = """
                Homer
                
                Marge
                
                Bart
                
                Lisa
                
                Maggie
                
                """;
        assertEquals(expected, actual);
    }

    @Test
    void shouldAcceptSet() {
        var config = standard();
        var stamper = docxPackageStamper(config);
        var template = makeWordResource("""
                comment::1[start="0,0", end="0,7", value="repeatDocPart(names)"]
                ${name}
                """);
        var context = FACTORY.names(Set.class, "Homer", "Marge", "Bart", "Lisa", "Maggie");
        var stamped = stamper.stamp(template, context);
        var actual = docxToString(stamped);
        var expected = """
                Marge
                
                Homer
                
                Maggie
                
                Bart
                
                Lisa
                
                """;
        assertEquals(expected, actual);
    }

    @Test
    void shouldAcceptQueue() {
        var config = standard();
        var stamper = docxPackageStamper(config);
        var template = makeWordResource("""
                comment::1[start="0,0", end="0,7", value="repeatDocPart(names)"]
                ${name}
                """);
        var context = FACTORY.names(Queue.class, "Homer", "Marge", "Bart", "Lisa", "Maggie");
        var stamped = stamper.stamp(template, context);
        var actual = docxToString(stamped);
        var expected = """
                Homer
                
                Marge
                
                Bart
                
                Lisa
                
                Maggie
                
                """;
        assertEquals(expected, actual);
    }
}
