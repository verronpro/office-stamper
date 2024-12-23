package pro.verron.officestamper.test;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.verron.officestamper.api.OfficeStamperConfiguration;
import pro.verron.officestamper.preset.OfficeStamperConfigurations;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.junit.jupiter.params.provider.Arguments.of;
import static pro.verron.officestamper.preset.OfficeStamperConfigurations.standard;
import static pro.verron.officestamper.test.ContextFactory.mapContextFactory;
import static pro.verron.officestamper.test.ContextFactory.objectContextFactory;
import static pro.verron.officestamper.test.TestUtils.*;

class RepeatDocPartTest {
    private static final Logger log = LoggerFactory.getLogger(RepeatDocPartTest.class);
    public static final ObjectContextFactory FACTORY = new ObjectContextFactory();

    private static Stream<Arguments> tests() {
        return factories().mapMulti((factory, pipe) -> {
            pipe.accept(shouldImportImageDataInTheMainDocument(factory));
            pipe.accept(shouldReplicateImageFromTheMainDocumentInTheSubTemplate(factory));
            pipe.accept(repeatDocPartTest(factory));
            pipe.accept(repeatDocPartNestingTest(factory));
            pipe.accept(repeatDocPartShouldNotUseSameCommentProcessorInstancesForSubtemplate(factory));
            pipe.accept(shouldKeepPageBreakOrientationWithSectionBreaksInsideComment(factory));
            pipe.accept(shouldKeepPageBreakOrientationWithSectionBreaksInsideCommentAndTableLastElement(factory));
            pipe.accept(shouldKeepPageBreakOrientationWithoutSectionBreaksInsideComment(factory));
        });
    }

    static Stream<ContextFactory> factories() {
        return Stream.of(objectContextFactory(), mapContextFactory());
    }

    private static Arguments shouldImportImageDataInTheMainDocument(ContextFactory factory) {
        var butterfly = getImage(Path.of("butterfly.png"));
        var cartography = getImage(Path.of("map.jpg"));
        var context = factory.units(butterfly, cartography);
        var template = getResource(Path.of("RepeatDocPartWithImageTest.docx"));
        var expected = """
                
                /word/media/document_image_rId11.png:rId11:image/png:193.6kB:sha1=t8UNAmo7yJgZJk9g7pLLIb3AvCA=:cy=$d:6120130
                /word/media/document_image_rId12.jpeg:rId12:image/jpeg:407.5kB:sha1=Ujo3UzL8WmeZN/1K6weBydaI73I=:cy=$d:6120130
                
                
                
                Always rendered:
                /word/media/document_image_rId13.png:rId13:image/png:193.6kB:sha1=t8UNAmo7yJgZJk9g7pLLIb3AvCA=:cy=$d:6120130
                
                """;

        var config = standard();
        return of("repeatDocPartWithImageTestShouldImportImageDataInTheMainDocument",
                config,
                context,
                template,
                expected);
    }

    private static Arguments shouldReplicateImageFromTheMainDocumentInTheSubTemplate(
            ContextFactory factory
    ) {
        return of("repeatDocPartWithImagesInSourceTestshouldReplicateImageFromTheMainDocumentInTheSubTemplate",
                standard(),
                factory.subDocPartContext(),
                getResource(Path.of("RepeatDocPartWithImagesInSourceTest.docx")),
                """
                        This is not repeated
                        This should be repeated : first doc part
                        /word/media/document_image_rId12.png:rId12:image/png:193.6kB:sha1=t8UNAmo7yJgZJk9g7pLLIb3AvCA=:cy=$d:5760720
                        This should be repeated too
                        This should be repeated : second doc part
                        /word/media/document_image_rId13.png:rId13:image/png:193.6kB:sha1=t8UNAmo7yJgZJk9g7pLLIb3AvCA=:cy=$d:5760720
                        This should be repeated too
                        This is not repeated
                        """);
    }

    private static Arguments repeatDocPartTest(ContextFactory factory) {
        return of("Repeat Doc Part Integration test",
                standard(),
                factory.roles("Homer Simpson",
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
                        "Dan Castellaneta"),
                getResource(Path.of("RepeatDocPartTest.docx")),
                """
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
                        """);
    }

    private static Arguments repeatDocPartNestingTest(ContextFactory factory) {
        return of("Repeat Doc Part Integration Test, with nested comments",
                OfficeStamperConfigurations.standard(),
                factory.schoolContext(),
                getResource(Path.of("RepeatDocPartNestingTest.docx")),
                """
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
                        """);
    }

    private static Arguments repeatDocPartShouldNotUseSameCommentProcessorInstancesForSubtemplate(
            ContextFactory factory
    ) {
        var context = factory.tableContext();
        var template = getResource(Path.of("RepeatDocPartAndCommentProcessorsIsolationTest.docx"));
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
                """;

        var config = standard();

        return arguments(
                "RepeatDocPartAndCommentProcessorsIsolationTest_repeatDocPartShouldNotUseSameCommentProcessorInstancesForSubtemplate",
                config,
                context,
                template,
                expected);
    }

    private static Arguments shouldKeepPageBreakOrientationWithSectionBreaksInsideComment(
            ContextFactory factory
    ) {
        return arguments("In multiple layouts, keeps section orientations outside RepeatDocPart comments",
                standard(),
                Map.of("repeatValues", List.of(factory.name("Homer"), factory.name("Marge"))),
                getResource(Path.of("ChangingPageLayoutInRepeatDocPartTest.docx")),
                """
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
                        
                        [section-break, {docGrid={linePitch=360},pgMar={bottom=1418,footer=709,gutter=0,header=709,left=1418,right=1418,top=1418},pgSz={h=16838,w=11906}}]
                        <<<
                        Fourth page is set to landscape again.
                        """);
    }

    private static Arguments shouldKeepPageBreakOrientationWithSectionBreaksInsideCommentAndTableLastElement(
            ContextFactory factory
    ) {
        return arguments(
                "In multiple layouts, keeps section orientations inside RepeatDocPart comments with a table as last "
                + "element",
                standard(),

                Map.of("repeatValues", List.of(factory.name("Homer"), factory.name("Marge"))),
                getResource(Path.of("ChangingPageLayoutInRepeatDocPartWithTableLastElementTest.docx")),
                """
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
                        
                        [section-break, {docGrid={linePitch=360},pgMar={bottom=1418,footer=709,gutter=0,header=709,left=1418,right=1418,top=1418},pgSz={h=16838,w=11906}}]
                        <<<
                        Fourth page is set to landscape again.
                        """);
    }

    private static Arguments shouldKeepPageBreakOrientationWithoutSectionBreaksInsideComment(
            ContextFactory factory
    ) {
        return arguments("In multiple layouts, keeps section orientation outside RepeatDocPart comment",
                standard(),
                Map.of("repeatValues", List.of(factory.name("Homer"), factory.name("Marge"))),
                getResource(Path.of("ChangingPageLayoutOutsideRepeatDocPartTest.docx")),
                """
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
                        """);
    }

    @MethodSource("tests")
    @ParameterizedTest(name = "{0}")
    void features(
            String name,
            OfficeStamperConfiguration config,
            Object context,
            InputStream template,
            String expected
    ) {
        log.info(name);
        var stamper = new TestDocxStamper<>(config);
        var actual = stamper.stampAndLoadAndExtract(template, context);
        assertEquals(expected, actual);
    }

    @Test
    void shouldAcceptList() {
        var config = standard();
        var stamper = new TestDocxStamper<>(config);
        var template = makeResource("<1|>${name}<|1><1|repeatDocPart(names)>");
        var context = FACTORY.names(List.class, "Homer", "Marge", "Bart", "Lisa", "Maggie");
        var actual = stamper.stampAndLoadAndExtract(template, context);
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
        var stamper = new TestDocxStamper<>(config);
        var template = makeResource("<1|>${name}<|1><1|repeatDocPart(names)>");
        var context = FACTORY.names(Set.class, "Homer", "Marge", "Bart", "Lisa", "Maggie");
        var actual = stamper.stampAndLoadAndExtract(template, context);
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
        var stamper = new TestDocxStamper<>(config);
        var template = makeResource("<1|>${name}<|1><1|repeatDocPart(names)>");
        var context = FACTORY.names(Queue.class, "Homer", "Marge", "Bart", "Lisa", "Maggie");
        var actual = stamper.stampAndLoadAndExtract(template, context);
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
