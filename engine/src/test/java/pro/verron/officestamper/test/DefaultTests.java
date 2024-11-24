package pro.verron.officestamper.test;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.expression.spel.SpelParserConfiguration;
import pro.verron.officestamper.api.OfficeStamperConfiguration;
import pro.verron.officestamper.preset.EvaluationContextConfigurers;
import pro.verron.officestamper.preset.ExceptionResolvers;
import pro.verron.officestamper.preset.OfficeStamperConfigurations;
import pro.verron.officestamper.preset.Resolvers;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.junit.jupiter.params.provider.Arguments.of;
import static pro.verron.officestamper.preset.OfficeStamperConfigurations.standard;
import static pro.verron.officestamper.preset.OfficeStamperConfigurations.standardWithPreprocessing;
import static pro.verron.officestamper.test.ContextFactory.mapContextFactory;
import static pro.verron.officestamper.test.ContextFactory.objectContextFactory;
import static pro.verron.officestamper.test.TestUtils.*;

/// DefaultTests class.
///
/// @author Joseph Verron
/// @version ${version}
/// @since 1.6.6
@DisplayName("Core Features") class DefaultTests {

    private static final Logger log = LoggerFactory.getLogger(DefaultTests.class);

    private static Stream<Arguments> tests() {
        return Stream.concat(factories().mapMulti((factory, pipe) -> {
            pipe.accept(ternary(factory));
            pipe.accept(repeatingRows(factory));
            pipe.accept(repeatingRowsWithLineBreak(factory));
            pipe.accept(replaceWordWithIntegrationTest(factory));
            pipe.accept(replaceNullExpressionTest(factory));
            pipe.accept(repeatTableRowKeepsFormatTest(factory));
            pipe.accept(repeatParagraphTest(factory));
            pipe.accept(repeatDocPartWithImageTestShouldImportImageDataInTheMainDocument(factory));
            pipe.accept(repeatDocPartWithImagesInSourceTestshouldReplicateImageFromTheMainDocumentInTheSubTemplate(
                    factory));
            pipe.accept(repeatDocPartTest(factory));
            pipe.accept(repeatDocPartNestingTest(factory));
            pipe.accept(
                    repeatDocPartAndCommentProcessorsIsolationTest_repeatDocPartShouldNotUseSameCommentProcessorInstancesForSubtemplate(
                            factory));
            pipe.accept(
                    changingPageLayoutTest_shouldKeepSectionBreakOrientationInRepeatParagraphWithoutSectionBreakInsideComment(
                            factory));
            pipe.accept(
                    changingPageLayoutTest_shouldKeepSectionBreakOrientationInRepeatParagraphWithSectionBreakInsideComment(
                            factory));
            pipe.accept(
                    changingPageLayoutTest_shouldKeepPageBreakOrientationInRepeatDocPartWithSectionBreaksInsideComment(
                            factory));
            pipe.accept(replaceNullExpressionTest2(factory));
            pipe.accept(
                    changingPageLayoutTest_shouldKeepPageBreakOrientationInRepeatDocPartWithSectionBreaksInsideCommentAndTableAsLastElement(
                            factory));
            pipe.accept(
                    changingPageLayoutTest_shouldKeepPageBreakOrientationInRepeatDocPartWithoutSectionBreaksInsideComment(
                            factory));
            pipe.accept(conditionalDisplayOfParagraphsTest_processorExpressionsInCommentsAreResolved(factory));
            pipe.accept(conditionalDisplayOfParagraphsTest_inlineProcessorExpressionsAreResolved(factory));
            pipe.accept(conditionalDisplayOfParagraphsTest_unresolvedInlineProcessorExpressionsAreRemoved(factory));
            pipe.accept(conditionalDisplayOfTableRowsTest(factory));
            pipe.accept(conditionalDisplayOfTableBug32Test(factory));
            pipe.accept(conditionalDisplayOfTableTest(factory));
            pipe.accept(customEvaluationContextConfigurerTest_customEvaluationContextConfigurerIsHonored(factory));
            pipe.accept(expressionReplacementInGlobalParagraphsTest(factory));
            pipe.accept(expressionReplacementInTablesTest(factory));
            pipe.accept(expressionReplacementWithFormattingTest(factory));
            pipe.accept(expressionWithSurroundingSpacesTest(factory));
            pipe.accept(expressionReplacementWithCommentTest(factory));
            pipe.accept(imageReplacementInGlobalParagraphsTest(factory));
            pipe.accept(imageReplacementInGlobalParagraphsTestWithMaxWidth(factory));
            pipe.accept(leaveEmptyOnExpressionErrorTest(factory));
            pipe.accept(lineBreakReplacementTest(factory));
            pipe.accept(mapAccessorAndReflectivePropertyAccessorTest_shouldResolveMapAndPropertyPlaceholders(factory));
            pipe.accept(nullPointerResolutionTest_testWithDefaultSpel(factory));
            pipe.accept(customCommentProcessor(factory));
            pipe.accept(controls(factory));
        }), Stream.of(nullPointerResolutionTest_testWithCustomSpel(ContextFactory.objectContextFactory())));
    }

    static Stream<ContextFactory> factories() {
        return Stream.of(objectContextFactory(), mapContextFactory());
    }

    private static Arguments ternary(ContextFactory factory) {
        return arguments("Ternary operators should function",
                standard(),
                factory.name("Homer"),
                getResource(Path.of("TernaryOperatorTest.docx")),
                """
                        Expression Replacement with ternary operator
                        This paragraph is untouched.
                        Some replacement before the ternary operator: Homer.
                        Homer <-- this should read "Homer".
                         <-- this should be empty.
                        """);
    }

    private static Arguments repeatingRows(ContextFactory factory) {
        return of("Repeating table rows should be possible",
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
                getResource(Path.of("RepeatTableRowTest.docx")),
                """
                        Repeating Table Rows
                        List of Simpsons characters
                        |===
                        |Character name
                        |Voice Actor<cnfStyle=100000000000>
                        
                        |Homer Simpson
                        |Dan Castellaneta<cnfStyle=000000100000>
                        
                        |Marge Simpson
                        |Julie Kavner<cnfStyle=000000100000>
                        
                        |Bart Simpson
                        |Nancy Cartwright<cnfStyle=000000100000>
                        
                        |Kent Brockman
                        |Harry Shearer<cnfStyle=000000100000>
                        
                        |Disco Stu
                        |Hank Azaria<cnfStyle=000000100000>
                        
                        |Krusty the Clown
                        |Dan Castellaneta<cnfStyle=000000100000>
                        
                        
                        |===
                        
                        There are 6 characters in the above table.
                        """);
    }

    private static Arguments repeatingRowsWithLineBreak(ContextFactory factory) {
        return of("Repeating table rows should be possible while replacing various linebreaks",
                standard().setLineBreakPlaceholder("\n"),
                factory.roles("Homer Simpson",
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
                        "Dan\nCastellaneta"),
                getResource(Path.of("RepeatTableRowTest.docx")),
                """
                        Repeating Table Rows
                        List of Simpsons characters
                        |===
                        |Character name
                        |Voice Actor<cnfStyle=100000000000>
                        
                        |Homer Simpson
                        |Dan Castellaneta<cnfStyle=000000100000>
                        
                        |Marge Simpson
                        |Julie<br/>
                        Kavner<cnfStyle=000000100000>
                        
                        |Bart Simpson
                        |Nancy<br/>
                        <br/>
                        Cartwright<cnfStyle=000000100000>
                        
                        |Kent Brockman
                        |Harry<br/>
                        <br/>
                        <br/>
                        Shearer<cnfStyle=000000100000>
                        
                        |Disco Stu
                        |Hank<br/>
                        <br/>
                        Azaria<cnfStyle=000000100000>
                        
                        |Krusty the Clown
                        |Dan<br/>
                        Castellaneta<cnfStyle=000000100000>
                        
                        
                        |===
                        
                        There are 6 characters in the above table.
                        """);
    }

    private static Arguments replaceWordWithIntegrationTest(ContextFactory factory) {
        return of("Replace Word With integration test",
                OfficeStamperConfigurations.standardWithPreprocessing(),
                factory.name("Simpsons"),
                getResource(Path.of("ReplaceWordWithIntegrationTest.docx")),
                """
                        == ReplaceWordWith Integration
                        
                        This variable name should be resolved to the value Simpsons.
                        |===
                        |This variable name should be resolved to the value Simpsons.
                        
                        
                        |===
                        
                        """);
    }

    private static Arguments replaceNullExpressionTest(ContextFactory factory) {
        return of("Do not replace 'null' values",
                standard().addResolver(Resolvers.nullToPlaceholder()),
                factory.name(null),
                getResource(Path.of("ReplaceNullExpressionTest.docx")),
                """
                        I am ${name}.
                        """);
    }

    static Arguments repeatTableRowKeepsFormatTest(ContextFactory factory) {
        return of("Repeat Table row Integration test (keeps formatting)",
                standard(),
                factory.show(),
                getResource(Path.of("RepeatTableRowKeepsFormatTest.docx")),
                """
                        |===
                        |1❬st❘{vertAlign=superscript}❭ Homer Simpson-❬Dan Castellaneta❘{b=true}❭
                        
                        |2❬nd❘{vertAlign=superscript}❭ Marge Simpson-❬Julie Kavner❘{b=true}❭
                        
                        |3❬rd❘{vertAlign=superscript}❭ Bart Simpson-❬Nancy Cartwright❘{b=true}❭
                        
                        |4❬th❘{vertAlign=superscript}❭ Lisa Simpson-❬Yeardley Smith❘{b=true}❭
                        
                        |5❬th❘{vertAlign=superscript}❭ Maggie Simpson-❬Julie Kavner❘{b=true}❭
                        
                        
                        |===
                        
                        """);
    }

    private static Arguments repeatParagraphTest(ContextFactory factory) {
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
                "Dan Castellaneta");
        var template = getResource(Path.of("RepeatParagraphTest.docx"));
        var expected = """
                == Characters 1 line
                
                Homer Simpson: Dan Castellaneta
                Marge Simpson: Julie Kavner
                Bart Simpson: Nancy Cartwright
                Kent Brockman: Harry Shearer
                Disco Stu: Hank Azaria
                Krusty the Clown: Dan Castellaneta
                There are 6 characters.
                == Characters multi-line
                
                === Homer Simpson
                
                Actor: Dan Castellaneta
                === Marge Simpson
                
                Actor: Julie Kavner
                === Bart Simpson
                
                Actor: Nancy Cartwright
                === Kent Brockman
                
                Actor: Harry Shearer
                === Disco Stu
                
                Actor: Hank Azaria
                === Krusty the Clown
                
                Actor: Dan Castellaneta
                There are 6 characters.
                """;

        return arguments("Repeat Paragraph Integration test", standard(), context, template, expected);
    }

    private static Arguments repeatDocPartWithImageTestShouldImportImageDataInTheMainDocument(ContextFactory factory) {
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

    private static Arguments repeatDocPartWithImagesInSourceTestshouldReplicateImageFromTheMainDocumentInTheSubTemplate(
            ContextFactory factory
    ) {
        return of("repeatDocPartWithImagesInSourceTestshouldReplicateImageFromTheMainDocumentInTheSubTemplate",
                standard(),
                factory.subDocPartContext(),
                getResource(Path.of("RepeatDocPartWithImagesInSourceTest" + ".docx")),
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
                OfficeStamperConfigurations.standardWithPreprocessing(),
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

    private static Arguments repeatDocPartAndCommentProcessorsIsolationTest_repeatDocPartShouldNotUseSameCommentProcessorInstancesForSubtemplate(
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

    private static Arguments changingPageLayoutTest_shouldKeepSectionBreakOrientationInRepeatParagraphWithoutSectionBreakInsideComment(
            ContextFactory factory
    ) {
        return arguments("In multiple layouts, keeps section orientations outside RepeatParagraph comments",
                standard(),
                Map.of("repeatValues", List.of(factory.name("Homer"), factory.name("Marge"))),
                getResource(Path.of("ChangingPageLayoutOutsideRepeatParagraphTest.docx")),
                """
                        First page is landscape.
                        
                        
                        [section-break, {docGrid={linePitch=360},pgMar={bottom=1418,footer=709,gutter=0,header=709,left=1418,right=1418,top=1418},pgSz={h=11906,orient=LANDSCAPE,w=16838}}]
                        <<<
                        Second page is portrait, layout change should survive to repeatParagraph processor (Homer).
                        
                        Without a section break changing the layout in between, but a page break instead.
                        [page-break]
                        <<<
                        
                        Second page is portrait, layout change should survive to repeatParagraph processor (Marge).
                        
                        Without a section break changing the layout in between, but a page break instead.
                        [page-break]
                        <<<
                        
                        
                        [section-break, {docGrid={linePitch=360},pgMar={bottom=1418,footer=709,gutter=0,header=709,left=1418,right=1418,top=1418},pgSz={h=16838,w=11906}}]
                        <<<
                        Fourth page is set to landscape again.
                        """);
    }

    private static Arguments changingPageLayoutTest_shouldKeepSectionBreakOrientationInRepeatParagraphWithSectionBreakInsideComment(
            ContextFactory factory
    ) {
        var context = factory.coupleContext();
        var template = getResource(Path.of("ChangingPageLayoutInRepeatParagraphTest.docx"));
        var expected = """
                First page is landscape.
                
                
                [section-break, {docGrid={linePitch=360},pgMar={bottom=1418,footer=709,gutter=0,header=709,left=1418,right=1418,top=1418},pgSz={h=11906,orient=LANDSCAPE,w=16838}}]
                <<<
                Second page is portrait, layout change should survive to repeatParagraph processor (Homer).
                
                
                [section-break, {docGrid={linePitch=360},pgMar={bottom=1418,footer=709,gutter=0,header=709,left=1418,right=1418,top=1418},pgSz={h=16838,w=11906}}]
                <<<
                With a page break changing the layout in between.
                [section-break, {docGrid={linePitch=360},pgMar={bottom=1418,footer=709,gutter=0,header=709,left=1418,right=1418,top=1418},pgSz={h=11906,orient=LANDSCAPE,w=16838}}]
                <<<
                Second page is portrait, layout change should survive to repeatParagraph processor (Marge).
                
                
                [section-break, {docGrid={linePitch=360},pgMar={bottom=1418,footer=709,gutter=0,header=709,left=1418,right=1418,top=1418},pgSz={h=16838,w=11906}}]
                <<<
                With a page break changing the layout in between.
                
                [section-break, {docGrid={linePitch=360},pgMar={bottom=1418,footer=709,gutter=0,header=709,left=1418,right=1418,top=1418},pgSz={h=11906,orient=LANDSCAPE,w=16838}}]
                <<<
                Fourth page is set to portrait again.
                """;

        var config = standard();
        return arguments("In multiple layouts, keeps section orientations inside RepeatParagraph comments",
                config,
                context,
                template,
                expected);
    }

    private static Arguments changingPageLayoutTest_shouldKeepPageBreakOrientationInRepeatDocPartWithSectionBreaksInsideComment(
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

    private static Arguments replaceNullExpressionTest2(ContextFactory factory) {
        return of("Do replace 'null' values",
                standard().addResolver(Resolvers.nullToEmpty()),
                factory.name(null),
                getResource(Path.of("ReplaceNullExpressionTest.docx")),
                """
                        I am .
                        """);
    }

    private static Arguments changingPageLayoutTest_shouldKeepPageBreakOrientationInRepeatDocPartWithSectionBreaksInsideCommentAndTableAsLastElement(
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

    private static Arguments changingPageLayoutTest_shouldKeepPageBreakOrientationInRepeatDocPartWithoutSectionBreaksInsideComment(
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

    private static Arguments conditionalDisplayOfParagraphsTest_processorExpressionsInCommentsAreResolved(ContextFactory factory) {
        var context = factory.name("Homer");
        var template = getResource(Path.of("ConditionalDisplayOfParagraphsTest.docx"));
        var expected = """
                == Conditional Display of Paragraphs
                
                This paragraph stays untouched.
                This paragraph stays untouched.
                |===
                |=== Conditional Display of paragraphs also works in tables
                
                |This paragraph stays untouched.
                |
                
                ||===
                |=== Also works in nested tables
                
                |This paragraph stays untouched.
                
                
                |===
                
                
                |===
                
                """;

        return arguments("Display Paragraph If Integration test", standard(), context, template, expected);
    }

    private static Arguments conditionalDisplayOfParagraphsTest_inlineProcessorExpressionsAreResolved(ContextFactory factory) {
        var context = factory.name("Homer");
        var template = getResource(Path.of("ConditionalDisplayOfParagraphsWithoutCommentTest.docx"));
        var expected = """
                == Conditional Display of Paragraphs
                
                Paragraph 1 stays untouched.
                Paragraph 3 stays untouched.
                |===
                |=== Conditional Display of paragraphs also works in tables
                
                |Paragraph 4 in cell 2,1 stays untouched.
                |
                
                ||===
                |=== Also works in nested tables
                
                |Paragraph 6 in cell 2,1 in cell 3,1 stays untouched.
                
                
                |===
                
                
                |===
                
                """;
        return arguments("Display Paragraph If Integration test (off case) + Inline processors Integration test",
                standard(),
                context,
                template,
                expected);
    }

    private static Arguments conditionalDisplayOfParagraphsTest_unresolvedInlineProcessorExpressionsAreRemoved(
            ContextFactory factory
    ) {
        var context = factory.name("Bart");
        var template = getResource(Path.of("ConditionalDisplayOfParagraphsWithoutCommentTest.docx"));
        var expected = """
                == Conditional Display of Paragraphs
                
                Paragraph 1 stays untouched.
                Paragraph 2 is only included if the “name” is “Bart”.
                Paragraph 3 stays untouched.
                |===
                |=== Conditional Display of paragraphs also works in tables
                
                |Paragraph 4 in cell 2,1 stays untouched.
                |Paragraph 5 in cell 2,2 is only included if the “name” is “Bart”.
                
                ||===
                |=== Also works in nested tables
                
                |Paragraph 6 in cell 2,1 in cell 3,1 stays untouched.
                Paragraph 7  in cell 2,1 in cell 3,1 is only included if the “name” is “Bart”.
                
                
                |===
                
                
                |===
                
                """;
        return arguments("Display Paragraph If Integration test (on case) + Inline processors Integration test",
                standard(),
                context,
                template,
                expected);
    }

    private static Arguments conditionalDisplayOfTableRowsTest(ContextFactory factory) {
        var context = factory.name("Homer");
        var template = getResource(Path.of("ConditionalDisplayOfTableRowsTest.docx"));
        var expected = """
                == Conditional Display of Table Rows
                
                This paragraph stays untouched.
                |===
                |This row stays untouched.
                
                |This row stays untouched.
                
                ||===
                |Also works on nested Tables
                
                |This row stays untouched.
                
                
                |===
                
                
                |===
                
                """;
        return arguments("Display Table Row If Integration test", standard(), context, template, expected);
    }

    private static Arguments conditionalDisplayOfTableBug32Test(ContextFactory factory) {
        var context = factory.name("Homer");
        var template = getResource(Path.of("ConditionalDisplayOfTablesBug32Test.docx"));
        var expected = """
                == Conditional Display of Tables
                
                This paragraph stays untouched.
                
                |===
                |This table stays untouched.
                |<cnfStyle=100000000000>
                
                |
                |<cnfStyle=000000100000>
                
                
                |===
                
                |===
                |Also works on nested tables
                
                |
                
                
                |===
                
                This paragraph stays untouched.
                """;
        return arguments("Display Table If Bug32 Regression test", standard(), context, template, expected);
    }

    private static Arguments conditionalDisplayOfTableTest(ContextFactory factory) {
        var context = factory.name("Homer");
        var template = getResource(Path.of("ConditionalDisplayOfTablesTest" + ".docx"));
        var expected = """
                == Conditional Display of Tables
                
                This paragraph stays untouched.
                
                |===
                |This table stays untouched.
                |
                
                |
                |
                
                
                |===
                
                |===
                |Also works on nested tables
                
                |
                
                
                |===
                
                This paragraph stays untouched.
                """;
        return arguments("Display Table If Integration test", standard(), context, template, expected);
    }

    private static Arguments customEvaluationContextConfigurerTest_customEvaluationContextConfigurerIsHonored(
            ContextFactory factory
    ) {
        var context = factory.empty();
        var template = makeResource("""
                Custom EvaluationContextConfigurer Test
                This paragraph stays untouched.
                The variable foo has the value ${foo}.
                """);
        var expected = """
                Custom EvaluationContextConfigurer Test
                This paragraph stays untouched.
                The variable foo has the value bar.
                """;
        var config =
                standard().setEvaluationContextConfigurer(evalContext -> evalContext.addPropertyAccessor(new SimpleGetter(
                        "foo",
                        "bar")));

        return arguments("customEvaluationContextConfigurerTest_customEvaluationContextConfigurerIsHonored",
                config,
                context,
                template,
                expected);
    }

    private static Arguments expressionReplacementInGlobalParagraphsTest(ContextFactory factory) {
        var context = factory.name("Homer Simpson");
        var template = makeResource("""
                Expression Replacement in global paragraphs
                This paragraph is untouched.
                In this paragraph, the variable name should be resolved to the value ${name}.
                In this paragraph, the variable foo should not be resolved: ${foo}.""");
        var expected = """
                Expression Replacement in global paragraphs
                This paragraph is untouched.
                In this paragraph, the variable name should be resolved to the value Homer Simpson.
                In this paragraph, the variable foo should not be resolved: ${foo}.
                """;
        OfficeStamperConfiguration config = standard().setExceptionResolver(ExceptionResolvers.passing());
        return arguments("expressionReplacementInGlobalParagraphsTest", config, context, template, expected);
    }

    private static Arguments expressionReplacementInTablesTest(ContextFactory factory) {
        var context = factory.name("Bart Simpson");
        var template = getResource(Path.of("ExpressionReplacementInTablesTest.docx"));

        var expected = """
                == Expression Replacement in Tables
                
                |===
                |This should resolve to a name:
                |Bart Simpson
                
                |This should not resolve:
                |${foo}
                
                |Nested Table:
                |===
                |This should resolve to a name:
                |Bart Simpson
                
                |This should not resolve:
                |${foo}
                
                
                |===
                
                
                |===
                
                """;
        var config = standard().setExceptionResolver(ExceptionResolvers.passing());
        return arguments("Placeholder replacement in tables", config, context, template, expected);
    }

    private static Arguments expressionReplacementWithFormattingTest(ContextFactory factory) {
        var context = factory.name("Homer Simpson");
        var template = getResource(Path.of("ExpressionReplacementWithFormattingTest.docx"));
        var expected = """
                == Expression Replacement with text format
                
                The text format should be kept intact when an expression is replaced.
                It should be bold: ❬Homer Simpson❘{b=true}❭.
                It should be italic: ❬Homer Simpson❘{i=true}❭.
                It should be superscript: ❬Homer Simpson❘{vertAlign=superscript}❭.
                It should be subscript: ❬Homer Simpson❘{vertAlign=subscript}❭.
                It should be striked: ❬Homer Simpson❘{strike=true}❭.
                It should be underlined: ❬Homer Simpson❘{u=single}❭.
                It should be doubly underlined: ❬Homer Simpson❘{u=double}❭.
                It should be thickly underlined: ❬Homer Simpson❘{u=thick}❭.
                It should be dot underlined: ❬Homer Simpson❘{u=dotted}❭.
                It should be dash underlined: ❬Homer Simpson❘{u=dash}❭.
                It should be dot and dash underlined: ❬Homer Simpson❘{u=dotDash}❭.
                It should be dot, dot and dash underlined: ❬Homer Simpson❘{u=dotDotDash}❭.
                It should be highlighted yellow: ❬Homer Simpson❘{highlight=yellow}❭.
                It should be white over darkblue: ❬Homer Simpson❘{color=FFFFFF,highlight=darkBlue}❭.
                It should be with header formatting: ❬Homer Simpson❘{rStyle=TitreCar}❭.
                """;
        return arguments("Placeholder replacement integration test (keep formatting)",
                standard(),
                context,
                template,
                expected);
    }

    private static Arguments expressionWithSurroundingSpacesTest(ContextFactory factory) {
        var spacyContext = factory.spacy();
        var template = getResource(Path.of("ExpressionWithSurroundingSpacesTest.docx"));
        var expected = """
                == Expression Replacement when expression has leading and/or trailing spaces
                
                When an expression within a paragraph is resolved, the spaces between the replacement and the surrounding text should be as expected. The following paragraphs should all look the same.
                Before Expression After.
                Before Expression After.
                Before Expression After.
                Before Expression After.
                Before Expression After.
                Before Expression After.
                Before Expression After.
                """;
        return arguments("Placeholder replacement test, spaces management",
                standard(),
                spacyContext,
                template,
                expected);
    }

    private static Arguments expressionReplacementWithCommentTest(ContextFactory factory) {
        var context = factory.name("Homer Simpson");
        var template = getResource(Path.of("ExpressionReplacementWithCommentsTest.docx"));
        var expected = """
                == Expression Replacement with comments
                
                This paragraph is untouched.
                In this paragraph, the variable name should be resolved to the value Homer Simpson.
                In this paragraph, the variable foo should not be resolved: <1|unresolvedValueWithComment|1><1|replaceWordWith(foo)>.
                """;
        var config = standardWithPreprocessing().setExceptionResolver(ExceptionResolvers.passing());
        return arguments("Replace Word With Integration test", config, context, template, expected);
    }

    /**
     * <p>testDateInstantiationAndResolution.</p>
     */
    private static Arguments imageReplacementInGlobalParagraphsTest(ContextFactory factory) {
        var context = factory.image(getImage(Path.of("monalisa.jpg")));
        var template = getResource(Path.of("ImageReplacementInGlobalParagraphsTest.docx"));
        var expected = """
                == Image Replacement in global paragraphs
                
                This paragraph is untouched.
                In this paragraph, an image of Mona Lisa is inserted: /word/media/document_image_rId6.jpeg:rId6:image/jpeg:8.8kB:sha1=XMpVtDbetKjZTkPhy598GdJQM/4=:cy=$d:1276350.
                This paragraph has the image /word/media/document_image_rId7.jpeg:rId7:image/jpeg:8.8kB:sha1=XMpVtDbetKjZTkPhy598GdJQM/4=:cy=$d:1276350 in the middle.
                """;
        return arguments("Image Type resolver integration test", standard(), context, template, expected);
    }

    private static Arguments imageReplacementInGlobalParagraphsTestWithMaxWidth(ContextFactory factory) {
        var context = factory.image(getImage(Path.of("monalisa.jpg"), 1000));
        var template = getResource(Path.of("ImageReplacementInGlobalParagraphsTest.docx"));
        var expected = """
                == Image Replacement in global paragraphs
                
                This paragraph is untouched.
                In this paragraph, an image of Mona Lisa is inserted: /word/media/document_image_rId6.jpeg:rId6:image/jpeg:8.8kB:sha1=XMpVtDbetKjZTkPhy598GdJQM/4=:cy=$d:635000.
                This paragraph has the image /word/media/document_image_rId7.jpeg:rId7:image/jpeg:8.8kB:sha1=XMpVtDbetKjZTkPhy598GdJQM/4=:cy=$d:635000 in the middle.
                """;
        return arguments("Image Type resolver integration test (with max width)",
                standard(),
                context,
                template,
                expected);
    }

    private static Arguments leaveEmptyOnExpressionErrorTest(ContextFactory factory) {
        var context = factory.name("Homer Simpson");
        var template = getResource(Path.of("LeaveEmptyOnExpressionErrorTest.docx"));
        var expected = "Leave me empty .\n";
        var config = standard().setExceptionResolver(ExceptionResolvers.defaulting());
        return arguments("Default Exception Resolver Integration test, with empty value",
                config,
                context,
                template,
                expected);
    }

    private static Arguments lineBreakReplacementTest(ContextFactory factory) {
        var config = standard().setLineBreakPlaceholder("#");
        var context = factory.name(null);
        var template = getResource(Path.of("LineBreakReplacementTest.docx"));
        var expected = """
                Line Break Replacement
                This paragraph is untouched.
                This paragraph should be <br/>
                 split in <br/>
                 three lines.
                This paragraph is untouched.
                """;
        return arguments("lineBreakReplacementTest", config, context, template, expected);
    }

    private static Arguments mapAccessorAndReflectivePropertyAccessorTest_shouldResolveMapAndPropertyPlaceholders(
            ContextFactory factory
    ) {
        var context = factory.mapAndReflectiveContext();
        var template = getResource(Path.of("MapAccessorAndReflectivePropertyAccessorTest.docx"));
        var expected = """
                Flat string : Flat string has been resolved
                
                |===
                |Values
                
                |first value
                
                |second value
                
                
                |===
                
                
                Paragraph start
                first value
                Paragraph end
                Paragraph start
                second value
                Paragraph end
                
                """;

        var defaultValue = "N/C";
        var config = standard().setLineBreakPlaceholder("\n")
                               .addResolver(Resolvers.nullToDefault(defaultValue))
                               .setExceptionResolver(ExceptionResolvers.defaulting(defaultValue));

        return arguments("Should be able to stamp from a Map<String, Object> context",
                config,
                context,
                template,
                expected);
    }

    private static Arguments nullPointerResolutionTest_testWithDefaultSpel(ContextFactory factory) {
        var context = factory.nullishContext();
        var template = getResource(Path.of("NullPointerResolution.docx"));
        var expected = """
                Deal with null references
                
                Deal with: Fullish1
                Deal with: Fullish2
                Deal with: Fullish3
                Deal with: Fullish5
                
                Deal with: Nullish value!!
                Deal with: ${nullish.value ?: "Nullish value!!"}
                Deal with: ${nullish.li[0] ?: "Nullish value!!"}
                Deal with: ${nullish.li[2] ?: "Nullish value!!"}
                
                """;

        var config = standard().setExceptionResolver(ExceptionResolvers.passing());

        return arguments("nullPointerResolutionTest_testWithDefaultSpel", config, context, template, expected);
    }

    private static Arguments customCommentProcessor(ContextFactory factory) {
        return arguments("Custom processor Integration test",
                standard().addCommentProcessor(ICustomCommentProcessor.class, CustomCommentProcessor::new),
                factory.empty(),
                getResource(Path.of("CustomCommentProcessorTest.docx")),
                """     
                        == Custom Comment Processor Test
                        
                        Visited
                        This paragraph is untouched.
                        Visited
                        """);
    }

    private static Arguments controls(ContextFactory factory) {
        return of("Form controls should be replaced as well",
                standard(),
                factory.name("Homer"),
                getResource(Path.of("form-controls.docx")),
                """
                        == Expression Replacement in Form Controls
                        
                        [Rich text control line Homer]
                        Rich text control inlined [Homer]
                        [Raw text control line Homer]
                        Raw text control inlined [Homer]
                        [Homer]
                        
                        """);
    }

    private static Arguments nullPointerResolutionTest_testWithCustomSpel(ContextFactory factory) {
        var context = factory.nullishContext();
        var template = getResource(Path.of("NullPointerResolution.docx"));
        var expected = """
                Deal with null references
                
                Deal with: Fullish1
                Deal with: Fullish2
                Deal with: Fullish3
                Deal with: Fullish5
                
                Deal with: Nullish value!!
                Deal with: Nullish value!!
                Deal with: Nullish value!!
                Deal with: Nullish value!!
                
                """;

        // Beware, this configuration only autogrows pojos and java beans,
        // so it will not work if your type has no default constructor and no setters.
        var config = standard().setSpelParserConfiguration(new SpelParserConfiguration(true, true))
                               .setEvaluationContextConfigurer(EvaluationContextConfigurers.noopConfigurer())
                               .addResolver(Resolvers.nullToDefault("Nullish value!!"));

        return arguments("nullPointerResolutionTest_testWithCustomSpel", config, context, template, expected);
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
}
