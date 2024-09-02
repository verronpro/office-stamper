package pro.verron.officestamper.test;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.context.expression.MapAccessor;
import org.springframework.expression.spel.SpelParserConfiguration;
import pro.verron.officestamper.api.OfficeStamperConfiguration;
import pro.verron.officestamper.preset.EvaluationContextConfigurers;
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
import static pro.verron.officestamper.test.Contexts.*;
import static pro.verron.officestamper.test.TestUtils.getImage;
import static pro.verron.officestamper.test.TestUtils.getResource;

/**
 * <p>DefaultTests class.</p>
 *
 * @author Joseph Verron
 * @version ${version}
 * @since 1.6.6
 */
@DisplayName("Core Features")
public class DefaultTests {

    /**
     * <p>tests.</p>
     *
     * @return a {@link java.util.stream.Stream} object
     */
    public static Stream<Arguments> tests() {
        return Stream.of(
                ternary(),
                repeatingRows(),
                repeatingRowsWithLineBreak(),
                replaceWordWithIntegrationTest(),
                replaceNullExpressionTest(),
                repeatTableRowKeepsFormatTest(),
                repeatParagraphTest(),
                repeatDocPartWithImageTestShouldImportImageDataInTheMainDocument(),
                repeatDocPartWithImagesInSourceTestshouldReplicateImageFromTheMainDocumentInTheSubTemplate(),
                repeatDocPartTest(),
                repeatDocPartNestingTest(),
                repeatDocPartAndCommentProcessorsIsolationTest_repeatDocPartShouldNotUseSameCommentProcessorInstancesForSubtemplate(),
                changingPageLayoutTest_shouldKeepSectionBreakOrientationInRepeatParagraphWithoutSectionBreakInsideComment(),
                changingPageLayoutTest_shouldKeepSectionBreakOrientationInRepeatParagraphWithSectionBreakInsideComment(),
                changingPageLayoutTest_shouldKeepPageBreakOrientationInRepeatDocPartWithSectionBreaksInsideComment(),
                replaceNullExpressionTest2(),
                changingPageLayoutTest_shouldKeepPageBreakOrientationInRepeatDocPartWithSectionBreaksInsideCommentAndTableAsLastElement(),
                changingPageLayoutTest_shouldKeepPageBreakOrientationInRepeatDocPartWithoutSectionBreaksInsideComment(),
                conditionalDisplayOfParagraphsTest_processorExpressionsInCommentsAreResolved(),
                conditionalDisplayOfParagraphsTest_inlineProcessorExpressionsAreResolved(),
                conditionalDisplayOfParagraphsTest_unresolvedInlineProcessorExpressionsAreRemoved(),
                conditionalDisplayOfTableRowsTest(),
                conditionalDisplayOfTableBug32Test(),
                conditionalDisplayOfTableTest(),
                customEvaluationContextConfigurerTest_customEvaluationContextConfigurerIsHonored(),
                customExpressionFunctionTest(),
                expressionReplacementInGlobalParagraphsTest(),
                expressionReplacementInTablesTest(),
                expressionReplacementWithFormattingTest(),
                expressionWithSurroundingSpacesTest(),
                expressionReplacementWithCommentTest(),
                imageReplacementInGlobalParagraphsTest(),
                imageReplacementInGlobalParagraphsTestWithMaxWidth(),
                leaveEmptyOnExpressionErrorTest(),
                lineBreakReplacementTest(),
                mapAccessorAndReflectivePropertyAccessorTest_shouldResolveMapAndPropertyPlaceholders(),
                nullPointerResolutionTest_testWithDefaultSpel(),
                nullPointerResolutionTest_testWithCustomSpel(),
                customCommentProcessor(),
                controls());
    }



    private static Arguments ternary() {
        return of("Ternary operators should function",
                OfficeStamperConfigurations.standard(),
                name("Homer"),
                getResource(Path.of("TernaryOperatorTest.docx")),
                """
                        Expression Replacement with ternary operator
                        This paragraph is untouched.
                        Some replacement before the ternary operator: Homer.
                        Homer <-- this should read "Homer".
                         <-- this should be empty.
                        """);
    }

    private static Arguments repeatingRows() {
        return of("Repeating table rows should be possible",
                OfficeStamperConfigurations.standard(),
                roles(role("Homer Simpson", "Dan Castellaneta"),
                        role("Marge Simpson", "Julie Kavner"),
                        role("Bart Simpson", "Nancy Cartwright"),
                        role("Kent Brockman", "Harry Shearer"),
                        role("Disco Stu", "Hank Azaria"),
                        role("Krusty the Clown", "Dan Castellaneta")),
                getResource(Path.of("RepeatTableRowTest.docx")),
                """
                        ❬Repeating Table Rows❘spacing={after=120,before=240}❭
                        ❬❬List of Simpsons characters❘b=true❭❘b=true,spacing={after=120,before=240}❭
                        ❬❬Character name❘b=true❭❘b=true❭
                        ❬❬Voice ❘b=true❭❬Actor❘b=true❭❘b=true❭
                        Homer Simpson
                        Dan Castellaneta
                        Marge Simpson
                        Julie Kavner
                        Bart Simpson
                        Nancy Cartwright
                        Kent Brockman
                        Harry Shearer
                        Disco Stu
                        Hank Azaria
                        Krusty the Clown
                        Dan Castellaneta
                        
                        ❬There are ❬6❘lang=de-DE❭ characters in the above table.❘lang=de-DE,spacing={after=140,before=0}❭
                        """);
    }

    private static Arguments repeatingRowsWithLineBreak() {
        return of("Repeating table rows should be possible while replacing various linebreaks",
                OfficeStamperConfigurations.standard()
                                           .setLineBreakPlaceholder("\n"),
                roles(role("Homer Simpson", "Dan Castellaneta"),
                        role("Marge Simpson", "Julie\nKavner"),
                        role("Bart Simpson", "Nancy\n\nCartwright"),
                        role("Kent Brockman", "Harry\n\n\nShearer"),
                        role("Disco Stu", "Hank\n\nAzaria"),
                        role("Krusty the Clown", "Dan\nCastellaneta")),
                getResource(Path.of("RepeatTableRowTest.docx")),
                """
                        ❬Repeating Table Rows❘spacing={after=120,before=240}❭
                        ❬❬List of Simpsons characters❘b=true❭❘b=true,spacing={after=120,before=240}❭
                        ❬❬Character name❘b=true❭❘b=true❭
                        ❬❬Voice ❘b=true❭❬Actor❘b=true❭❘b=true❭
                        Homer Simpson
                        Dan Castellaneta
                        Marge Simpson
                        Julie<break line>
                        Kavner
                        Bart Simpson
                        Nancy<break line>
                        <break line>
                        Cartwright
                        Kent Brockman
                        Harry<break line>
                        <break line>
                        <break line>
                        Shearer
                        Disco Stu
                        Hank<break line>
                        <break line>
                        Azaria
                        Krusty the Clown
                        Dan<break line>
                        Castellaneta
                        
                        ❬There are ❬6❘lang=de-DE❭ characters in the above table.❘lang=de-DE,spacing={after=140,before=0}❭
                        """);
    }

    private static Arguments replaceWordWithIntegrationTest() {
        return of("replaceWordWithIntegrationTest",
                OfficeStamperConfigurations.standard(),
                name("Simpsons"),
                getResource(Path.of("ReplaceWordWithIntegrationTest.docx")),
                """
                        ReplaceWordWith Integration
                        ❬This variable ❬name❘b=true❭❬ ❘b=true❭should be resolved to the value Simpsons.❘b=true❭
                        This variable ❬name❘b=true❭ should be resolved to the value Simpsons.
                        
                        """);
    }

    private static Arguments replaceNullExpressionTest() {
        return of("Do not replace 'null' values",
                OfficeStamperConfigurations.standard()
                                           .addResolver(Resolvers.nullToPlaceholder()),
                name(null),
                getResource(Path.of("ReplaceNullExpressionTest.docx")),
                """
                        I am ${name}.
                        """);
    }

    private static Arguments repeatTableRowKeepsFormatTest() {
        return of("repeatTableRowKeepsFormatTest",
                OfficeStamperConfigurations.standard(),
                new Show(List.of(new CharacterRecord(1,
                                "st",
                                "Homer Simpson",
                                "Dan Castellaneta"),
                        new CharacterRecord(2,
                                "nd",
                                "Marge Simpson",
                                "Julie Kavner"),
                        new CharacterRecord(3,
                                "rd",
                                "Bart Simpson",
                                "Nancy Cartwright"),
                        new CharacterRecord(4,
                                "th",
                                "Lisa Simpson",
                                "Yeardley Smith"),
                        new CharacterRecord(5,
                                "th",
                                "Maggie Simpson",
                                "Julie Kavner"))),
                getResource(Path.of("RepeatTableRowKeepsFormatTest.docx")),
                """
                        1❬st❘vertAlign=superscript❭ Homer Simpson-❬Dan Castellaneta❘b=true❭
                        2❬nd❘vertAlign=superscript❭ Marge Simpson-❬Julie Kavner❘b=true❭
                        3❬rd❘vertAlign=superscript❭ Bart Simpson-❬Nancy Cartwright❘b=true❭
                        4❬th❘vertAlign=superscript❭ Lisa Simpson-❬Yeardley Smith❘b=true❭
                        5❬th❘vertAlign=superscript❭ Maggie Simpson-❬Julie Kavner❘b=true❭
                        
                        """);
    }

    private static Arguments repeatParagraphTest() {
        var context = new Contexts.Characters(List.of(
                new Contexts.Role("Homer Simpson", "Dan Castellaneta"),
                new Contexts.Role("Marge Simpson", "Julie Kavner"),
                new Contexts.Role("Bart Simpson", "Nancy Cartwright"),
                new Contexts.Role("Kent Brockman", "Harry Shearer"),
                new Contexts.Role("Disco Stu", "Hank Azaria"),
                new Contexts.Role("Krusty the Clown", "Dan Castellaneta")));
        var template = getResource(Path.of("RepeatParagraphTest.docx"));
        var expected = """
                Characters 1 line
                Homer Simpson: Dan Castellaneta
                Marge Simpson: Julie Kavner
                Bart Simpson: Nancy Cartwright
                Kent Brockman: Harry Shearer
                Disco Stu: Hank Azaria
                Krusty the Clown: Dan Castellaneta
                There are 6 characters.
                Characters multi-line
                Homer Simpson
                Actor: Dan Castellaneta
                Marge Simpson
                Actor: Julie Kavner
                Bart Simpson
                Actor: Nancy Cartwright
                Kent Brockman
                Actor: Harry Shearer
                Disco Stu
                Actor: Hank Azaria
                Krusty the Clown
                Actor: Dan Castellaneta
                There are 6 characters.
                """;

        return arguments("repeatParagraphTest",
                OfficeStamperConfigurations.standard(),
                context,
                template,
                expected);
    }

    private static Arguments repeatDocPartWithImageTestShouldImportImageDataInTheMainDocument() {
        var context = Map.of("units", Stream.of(getImage(Path.of("butterfly.png")),
                                                    getImage(Path.of("map.jpg")))
                                            .map(image -> Map.of("coverImage", image))
                                            .map(map -> Map.of("productionFacility", map))
                                            .toList());
        var template = getResource(Path.of("RepeatDocPartWithImageTest.docx"));
        var expected = """
                
                /word/media/document_image_rId11.png:rId11:image/png:193.6kB:sha1=t8UNAmo7yJgZJk9g7pLLIb3AvCA=:cy=$d:6120130
                /word/media/document_image_rId12.jpeg:rId12:image/jpeg:407.5kB:sha1=Ujo3UzL8WmeZN/1K6weBydaI73I=:cy=$d:6120130
                
                
                
                Always rendered:
                /word/media/document_image_rId13.png:rId13:image/png:193.6kB:sha1=t8UNAmo7yJgZJk9g7pLLIb3AvCA=:cy=$d:6120130
                
                """;

        var config = OfficeStamperConfigurations.standard()
                                                .setEvaluationContextConfigurer(
                                                        ctx -> ctx.addPropertyAccessor(new MapAccessor()));
        return of(
                "repeatDocPartWithImageTestShouldImportImageDataInTheMainDocument",
                config,
                context,
                template,
                expected);
    }

    private static Arguments repeatDocPartWithImagesInSourceTestshouldReplicateImageFromTheMainDocumentInTheSubTemplate() {
        return of(
                "repeatDocPartWithImagesInSourceTestshouldReplicateImageFromTheMainDocumentInTheSubTemplate",
                OfficeStamperConfigurations.standard()
                                           .setEvaluationContextConfigurer(ctx -> ctx.addPropertyAccessor(new MapAccessor())),
                Contexts.subDocPartContext(),
                getResource(Path.of("RepeatDocPartWithImagesInSourceTest" +
                                    ".docx")),
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

    private static Arguments repeatDocPartTest() {
        return of("repeatDocPartTest",
                OfficeStamperConfigurations.standard(),
                new Characters(List.of(
                        new Role("Homer Simpson", "Dan Castellaneta"),
                        new Role("Marge Simpson", "Julie Kavner"),
                        new Role("Bart Simpson", "Nancy Cartwright"),
                        new Role("Kent Brockman", "Harry Shearer"),
                        new Role("Disco Stu", "Hank Azaria"),
                        new Role("Krusty the Clown", "Dan Castellaneta"))),
                getResource(Path.of("RepeatDocPartTest.docx")),
                """
                        Repeating Doc Part
                        ❬❬List ❘b=true❭❬of❘b=true❭❬ Simpsons ❘b=true❭❬characters❘b=true❭❘b=true,spacing={after=120,before=240}❭
                        ❬Paragraph for test: Homer Simpson - Dan Castellaneta❘spacing={after=120,before=240}❭
                        ❬Homer Simpson❘jc=center❭
                        ❬Dan Castellaneta❘jc=center❭
                        ❬ <break page>
                        ❘suppressAutoHyphens=xxx,widowControl=xxx❭
                        ❬Paragraph for test: Marge Simpson - Julie Kavner❘spacing={after=120,before=240}❭
                        ❬Marge Simpson❘jc=center❭
                        ❬Julie Kavner❘jc=center❭
                        ❬ <break page>
                        ❘suppressAutoHyphens=xxx,widowControl=xxx❭
                        ❬Paragraph for test: Bart Simpson - Nancy Cartwright❘spacing={after=120,before=240}❭
                        ❬Bart Simpson❘jc=center❭
                        ❬Nancy Cartwright❘jc=center❭
                        ❬ <break page>
                        ❘suppressAutoHyphens=xxx,widowControl=xxx❭
                        ❬Paragraph for test: Kent Brockman - Harry Shearer❘spacing={after=120,before=240}❭
                        ❬Kent Brockman❘jc=center❭
                        ❬Harry Shearer❘jc=center❭
                        ❬ <break page>
                        ❘suppressAutoHyphens=xxx,widowControl=xxx❭
                        ❬Paragraph for test: Disco Stu - Hank Azaria❘spacing={after=120,before=240}❭
                        ❬Disco Stu❘jc=center❭
                        ❬Hank Azaria❘jc=center❭
                        ❬ <break page>
                        ❘suppressAutoHyphens=xxx,widowControl=xxx❭
                        ❬Paragraph for test: Krusty the Clown - Dan Castellaneta❘spacing={after=120,before=240}❭
                        ❬Krusty the Clown❘jc=center❭
                        ❬Dan Castellaneta❘jc=center❭
                        ❬ <break page>
                        ❘suppressAutoHyphens=xxx,widowControl=xxx❭
                        There are 6 characters.
                        """);
    }

    private static Arguments repeatDocPartNestingTest() {
        return of("repeatDocPartNestingTest",
                OfficeStamperConfigurations.standard(),
                Contexts.schoolContext(),
                getResource(Path.of("RepeatDocPartNestingTest.docx")),
                """
                        ❬Repeating ❬N❘lang=en-US❭ested Doc Part ❘suppressAutoHyphens=xxx,widowControl=xxx❭
                        ❬List of All the s❬tu❘lang=en-US❭❬dent’s of all grades❘lang=null❭❘lang=null,suppressAutoHyphens=xxx,widowControl=xxx❭
                        ❬❬South Park Primary School❘lang=null❭❘lang=null,suppressAutoHyphens=xxx,widowControl=xxx❭
                        ❬❬Grade No.❘b=true,lang=null❭❬0❘b=true,lang=null❭❬ ❘b=true,lang=null❭❬t❘lang=null❭here are 3 classes❘b=true,lang=null,suppressAutoHyphens=xxx,widowControl=xxx❭
                        ❬Class No.0 ❬t❘lang=null❭here are 5 students❘suppressAutoHyphens=xxx,widowControl=xxx❭
                        ❬0❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,before=240,line=15,lineRule=AUTO},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                        ❬Bruce·No0❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,before=240,line=15,lineRule=AUTO},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                        ❬1❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,before=240,line=15,lineRule=AUTO},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                        ❬1❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,before=240,line=15,lineRule=AUTO},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                        ❬Bruce·No1❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,before=240,line=15,lineRule=AUTO},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                        ❬2❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,before=240,line=15,lineRule=AUTO},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                        ❬2❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,before=240,line=15,lineRule=AUTO},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                        ❬Bruce·No2❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,before=240,line=15,lineRule=AUTO},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                        ❬3❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,before=240,line=15,lineRule=AUTO},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                        ❬3❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,before=240,line=15,lineRule=AUTO},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                        ❬Bruce·No3❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,before=240,line=15,lineRule=AUTO},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                        ❬4❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,before=240,line=15,lineRule=AUTO},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                        ❬4❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,before=240,line=15,lineRule=AUTO},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                        ❬Bruce·No4❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,before=240,line=15,lineRule=AUTO},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                        ❬5❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,before=240,line=15,lineRule=AUTO},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                        ❬Class No.1 ❬t❘lang=null❭here are 5 students❘suppressAutoHyphens=xxx,widowControl=xxx❭
                        ❬0❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,before=240,line=15,lineRule=AUTO},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                        ❬Bruce·No0❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,before=240,line=15,lineRule=AUTO},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                        ❬1❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,before=240,line=15,lineRule=AUTO},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                        ❬1❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,before=240,line=15,lineRule=AUTO},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                        ❬Bruce·No1❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,before=240,line=15,lineRule=AUTO},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                        ❬2❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,before=240,line=15,lineRule=AUTO},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                        ❬2❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,before=240,line=15,lineRule=AUTO},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                        ❬Bruce·No2❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,before=240,line=15,lineRule=AUTO},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                        ❬3❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,before=240,line=15,lineRule=AUTO},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                        ❬3❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,before=240,line=15,lineRule=AUTO},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                        ❬Bruce·No3❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,before=240,line=15,lineRule=AUTO},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                        ❬4❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,before=240,line=15,lineRule=AUTO},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                        ❬4❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,before=240,line=15,lineRule=AUTO},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                        ❬Bruce·No4❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,before=240,line=15,lineRule=AUTO},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                        ❬5❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,before=240,line=15,lineRule=AUTO},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                        ❬Class No.2 ❬t❘lang=null❭here are 5 students❘suppressAutoHyphens=xxx,widowControl=xxx❭
                        ❬0❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,before=240,line=15,lineRule=AUTO},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                        ❬Bruce·No0❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,before=240,line=15,lineRule=AUTO},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                        ❬1❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,before=240,line=15,lineRule=AUTO},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                        ❬1❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,before=240,line=15,lineRule=AUTO},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                        ❬Bruce·No1❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,before=240,line=15,lineRule=AUTO},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                        ❬2❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,before=240,line=15,lineRule=AUTO},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                        ❬2❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,before=240,line=15,lineRule=AUTO},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                        ❬Bruce·No2❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,before=240,line=15,lineRule=AUTO},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                        ❬3❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,before=240,line=15,lineRule=AUTO},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                        ❬3❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,before=240,line=15,lineRule=AUTO},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                        ❬Bruce·No3❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,before=240,line=15,lineRule=AUTO},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                        ❬4❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,before=240,line=15,lineRule=AUTO},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                        ❬4❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,before=240,line=15,lineRule=AUTO},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                        ❬Bruce·No4❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,before=240,line=15,lineRule=AUTO},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                        ❬5❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,before=240,line=15,lineRule=AUTO},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                        ❬❬Grade No.❘b=true,lang=null❭❬1❘b=true,lang=null❭❬ ❘b=true,lang=null❭❬t❘lang=null❭here are 3 classes❘b=true,lang=null,suppressAutoHyphens=xxx,widowControl=xxx❭
                        ❬Class No.0 ❬t❘lang=null❭here are 5 students❘suppressAutoHyphens=xxx,widowControl=xxx❭
                        ❬0❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,before=240,line=15,lineRule=AUTO},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                        ❬Bruce·No0❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,before=240,line=15,lineRule=AUTO},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                        ❬1❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,before=240,line=15,lineRule=AUTO},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                        ❬1❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,before=240,line=15,lineRule=AUTO},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                        ❬Bruce·No1❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,before=240,line=15,lineRule=AUTO},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                        ❬2❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,before=240,line=15,lineRule=AUTO},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                        ❬2❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,before=240,line=15,lineRule=AUTO},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                        ❬Bruce·No2❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,before=240,line=15,lineRule=AUTO},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                        ❬3❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,before=240,line=15,lineRule=AUTO},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                        ❬3❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,before=240,line=15,lineRule=AUTO},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                        ❬Bruce·No3❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,before=240,line=15,lineRule=AUTO},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                        ❬4❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,before=240,line=15,lineRule=AUTO},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                        ❬4❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,before=240,line=15,lineRule=AUTO},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                        ❬Bruce·No4❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,before=240,line=15,lineRule=AUTO},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                        ❬5❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,before=240,line=15,lineRule=AUTO},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                        ❬Class No.1 ❬t❘lang=null❭here are 5 students❘suppressAutoHyphens=xxx,widowControl=xxx❭
                        ❬0❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,before=240,line=15,lineRule=AUTO},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                        ❬Bruce·No0❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,before=240,line=15,lineRule=AUTO},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                        ❬1❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,before=240,line=15,lineRule=AUTO},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                        ❬1❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,before=240,line=15,lineRule=AUTO},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                        ❬Bruce·No1❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,before=240,line=15,lineRule=AUTO},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                        ❬2❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,before=240,line=15,lineRule=AUTO},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                        ❬2❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,before=240,line=15,lineRule=AUTO},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                        ❬Bruce·No2❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,before=240,line=15,lineRule=AUTO},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                        ❬3❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,before=240,line=15,lineRule=AUTO},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                        ❬3❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,before=240,line=15,lineRule=AUTO},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                        ❬Bruce·No3❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,before=240,line=15,lineRule=AUTO},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                        ❬4❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,before=240,line=15,lineRule=AUTO},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                        ❬4❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,before=240,line=15,lineRule=AUTO},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                        ❬Bruce·No4❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,before=240,line=15,lineRule=AUTO},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                        ❬5❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,before=240,line=15,lineRule=AUTO},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                        ❬Class No.2 ❬t❘lang=null❭here are 5 students❘suppressAutoHyphens=xxx,widowControl=xxx❭
                        ❬0❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,before=240,line=15,lineRule=AUTO},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                        ❬Bruce·No0❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,before=240,line=15,lineRule=AUTO},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                        ❬1❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,before=240,line=15,lineRule=AUTO},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                        ❬1❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,before=240,line=15,lineRule=AUTO},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                        ❬Bruce·No1❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,before=240,line=15,lineRule=AUTO},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                        ❬2❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,before=240,line=15,lineRule=AUTO},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                        ❬2❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,before=240,line=15,lineRule=AUTO},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                        ❬Bruce·No2❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,before=240,line=15,lineRule=AUTO},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                        ❬3❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,before=240,line=15,lineRule=AUTO},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                        ❬3❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,before=240,line=15,lineRule=AUTO},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                        ❬Bruce·No3❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,before=240,line=15,lineRule=AUTO},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                        ❬4❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,before=240,line=15,lineRule=AUTO},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                        ❬4❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,before=240,line=15,lineRule=AUTO},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                        ❬Bruce·No4❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,before=240,line=15,lineRule=AUTO},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                        ❬5❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,before=240,line=15,lineRule=AUTO},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                        ❬❬Grade No.❘b=true,lang=null❭❬2❘b=true,lang=null❭❬ ❘b=true,lang=null❭❬t❘lang=null❭here are 3 classes❘b=true,lang=null,suppressAutoHyphens=xxx,widowControl=xxx❭
                        ❬Class No.0 ❬t❘lang=null❭here are 5 students❘suppressAutoHyphens=xxx,widowControl=xxx❭
                        ❬0❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,before=240,line=15,lineRule=AUTO},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                        ❬Bruce·No0❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,before=240,line=15,lineRule=AUTO},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                        ❬1❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,before=240,line=15,lineRule=AUTO},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                        ❬1❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,before=240,line=15,lineRule=AUTO},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                        ❬Bruce·No1❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,before=240,line=15,lineRule=AUTO},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                        ❬2❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,before=240,line=15,lineRule=AUTO},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                        ❬2❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,before=240,line=15,lineRule=AUTO},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                        ❬Bruce·No2❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,before=240,line=15,lineRule=AUTO},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                        ❬3❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,before=240,line=15,lineRule=AUTO},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                        ❬3❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,before=240,line=15,lineRule=AUTO},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                        ❬Bruce·No3❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,before=240,line=15,lineRule=AUTO},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                        ❬4❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,before=240,line=15,lineRule=AUTO},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                        ❬4❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,before=240,line=15,lineRule=AUTO},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                        ❬Bruce·No4❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,before=240,line=15,lineRule=AUTO},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                        ❬5❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,before=240,line=15,lineRule=AUTO},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                        ❬Class No.1 ❬t❘lang=null❭here are 5 students❘suppressAutoHyphens=xxx,widowControl=xxx❭
                        ❬0❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,before=240,line=15,lineRule=AUTO},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                        ❬Bruce·No0❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,before=240,line=15,lineRule=AUTO},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                        ❬1❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,before=240,line=15,lineRule=AUTO},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                        ❬1❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,before=240,line=15,lineRule=AUTO},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                        ❬Bruce·No1❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,before=240,line=15,lineRule=AUTO},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                        ❬2❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,before=240,line=15,lineRule=AUTO},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                        ❬2❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,before=240,line=15,lineRule=AUTO},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                        ❬Bruce·No2❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,before=240,line=15,lineRule=AUTO},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                        ❬3❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,before=240,line=15,lineRule=AUTO},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                        ❬3❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,before=240,line=15,lineRule=AUTO},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                        ❬Bruce·No3❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,before=240,line=15,lineRule=AUTO},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                        ❬4❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,before=240,line=15,lineRule=AUTO},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                        ❬4❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,before=240,line=15,lineRule=AUTO},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                        ❬Bruce·No4❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,before=240,line=15,lineRule=AUTO},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                        ❬5❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,before=240,line=15,lineRule=AUTO},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                        ❬Class No.2 ❬t❘lang=null❭here are 5 students❘suppressAutoHyphens=xxx,widowControl=xxx❭
                        ❬0❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,before=240,line=15,lineRule=AUTO},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                        ❬Bruce·No0❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,before=240,line=15,lineRule=AUTO},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                        ❬1❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,before=240,line=15,lineRule=AUTO},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                        ❬1❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,before=240,line=15,lineRule=AUTO},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                        ❬Bruce·No1❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,before=240,line=15,lineRule=AUTO},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                        ❬2❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,before=240,line=15,lineRule=AUTO},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                        ❬2❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,before=240,line=15,lineRule=AUTO},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                        ❬Bruce·No2❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,before=240,line=15,lineRule=AUTO},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                        ❬3❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,before=240,line=15,lineRule=AUTO},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                        ❬3❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,before=240,line=15,lineRule=AUTO},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                        ❬Bruce·No3❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,before=240,line=15,lineRule=AUTO},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                        ❬4❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,before=240,line=15,lineRule=AUTO},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                        ❬4❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,before=240,line=15,lineRule=AUTO},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                        ❬Bruce·No4❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,before=240,line=15,lineRule=AUTO},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                        ❬5❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,before=240,line=15,lineRule=AUTO},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                        There are 3 grades.
                        """);
    }

    private static Arguments repeatDocPartAndCommentProcessorsIsolationTest_repeatDocPartShouldNotUseSameCommentProcessorInstancesForSubtemplate() {
        var context = Contexts.tableContext();
        var template = getResource(
                Path.of("RepeatDocPartAndCommentProcessorsIsolationTest.docx"));
        var expected = """
                This will stay untouched.
                
                firstTable value1
                firstTable value2
                
                This will also stay untouched.
                
                Repeating paragraph :
                
                repeatDocPart value1
                Repeating paragraph :
                
                repeatDocPart value2
                Repeating paragraph :
                
                repeatDocPart value3
                
                secondTable value1
                secondTable value2
                secondTable value3
                secondTable value4
                
                This will stay untouched too.
                """;

        var config = OfficeStamperConfigurations.standard()
                                                .setEvaluationContextConfigurer(
                                                        ctx -> ctx.addPropertyAccessor(new MapAccessor()));

        return arguments(
                "RepeatDocPartAndCommentProcessorsIsolationTest_repeatDocPartShouldNotUseSameCommentProcessorInstancesForSubtemplate",
                config,
                context,
                template,
                expected);
    }

    private static Arguments changingPageLayoutTest_shouldKeepSectionBreakOrientationInRepeatParagraphWithoutSectionBreakInsideComment() {
        return arguments(
                "changingPageLayoutTest_shouldKeepSectionBreakOrientationInRepeatParagraphWithoutSectionBreakInsideComment",
                OfficeStamperConfigurations.standard()
                                           .setEvaluationContextConfigurer(
                                                   ctx -> ctx.addPropertyAccessor(new MapAccessor())),
                Map.of("repeatValues",
                        List.of(new Name("Homer"), new Name("Marge"))),
                getResource(Path.of(
                        "ChangingPageLayoutOutsideRepeatParagraphTest.docx")),
                """
                        First page is landscape.
                        
                        ❬❘docGrid=xxx,eGHdrFtrReferences=xxx,pgMar=xxx,pgSz={h=11906,orient=landscape,w=16838}❭
                        Second page is portrait, layout change should survive to repeatParagraph processor (Homer).
                        
                        Without a section break changing the layout in between, but a page break instead.<break page>
                        
                        Second page is portrait, layout change should survive to repeatParagraph processor (Marge).
                        
                        Without a section break changing the layout in between, but a page break instead.<break page>
                        
                        ❬❘docGrid=xxx,eGHdrFtrReferences=xxx,pgMar=xxx,pgSz={h=16838,w=11906}❭
                        Fourth page is set to landscape again.
                        """);
    }

    private static Arguments changingPageLayoutTest_shouldKeepSectionBreakOrientationInRepeatParagraphWithSectionBreakInsideComment() {
        var context = Contexts.coupleContext();
        var template = getResource(
                Path.of("ChangingPageLayoutInRepeatParagraphTest.docx"));
        var expected = """
                First page is landscape.
                
                ❬❘docGrid=xxx,eGHdrFtrReferences=xxx,pgMar=xxx,pgSz={h=11906,orient=landscape,w=16838}❭
                Second page is portrait, layout change should survive to repeatParagraph processor (Homer).
                
                ❬❘docGrid=xxx,eGHdrFtrReferences=xxx,pgMar=xxx,pgSz={h=16838,w=11906}❭
                ❬With a page break changing the layout in between.❘docGrid=xxx,eGHdrFtrReferences=xxx,pgMar=xxx,pgSz={h=11906,orient=landscape,w=16838}❭
                Second page is portrait, layout change should survive to repeatParagraph processor (Marge).
                
                ❬❘docGrid=xxx,eGHdrFtrReferences=xxx,pgMar=xxx,pgSz={h=16838,w=11906}❭
                With a page break changing the layout in between.
                ❬❘docGrid=xxx,eGHdrFtrReferences=xxx,pgMar=xxx,pgSz={h=11906,orient=landscape,w=16838}❭
                Fourth page is set to portrait again.
                """;

        var config = OfficeStamperConfigurations.standard()
                                                .setEvaluationContextConfigurer(
                                                        ctx -> ctx.addPropertyAccessor(new MapAccessor()));
        return arguments(
                "changingPageLayoutTest_shouldKeepSectionBreakOrientationInRepeatParagraphWithSectionBreakInsideComment",
                config,
                context,
                template,
                expected);
    }

    private static Arguments changingPageLayoutTest_shouldKeepPageBreakOrientationInRepeatDocPartWithSectionBreaksInsideComment() {
        return arguments(
                "changingPageLayoutTest_shouldKeepPageBreakOrientationInRepeatDocPartWithSectionBreaksInsideComment",
                OfficeStamperConfigurations.standard()
                                           .setEvaluationContextConfigurer(
                                                   ctx -> ctx.addPropertyAccessor(new MapAccessor())),
                Map.of("repeatValues",
                        List.of(new Name("Homer"), new Name("Marge"))),
                getResource(Path.of("ChangingPageLayoutInRepeatDocPartTest" +
                                    ".docx")),
                """
                        First page is portrait.
                        
                        ❬❘docGrid=xxx,eGHdrFtrReferences=xxx,pgMar=xxx,pgSz={h=16838,w=11906}❭
                        Second page is landscape, layout change should survive to repeatDocPart (Homer).
                        
                        ❬❘docGrid=xxx,eGHdrFtrReferences=xxx,pgMar=xxx,pgSz={h=11906,orient=landscape,w=16838}❭
                        ❬With a break setting the layout to portrait in between.❘docGrid=xxx,eGHdrFtrReferences=xxx,pgMar=xxx,pgSz={h=16838,w=11906}❭
                        Second page is landscape, layout change should survive to repeatDocPart (Marge).
                        
                        ❬❘docGrid=xxx,eGHdrFtrReferences=xxx,pgMar=xxx,pgSz={h=11906,orient=landscape,w=16838}❭
                        ❬With a break setting the layout to portrait in between.❘docGrid=xxx,eGHdrFtrReferences=xxx,pgMar=xxx,pgSz={h=16838,w=11906}❭
                        ❬❘docGrid=xxx,eGHdrFtrReferences=xxx,pgMar=xxx,pgSz={h=16838,w=11906}❭
                        Fourth page is set to landscape again.
                        """);
    }

    private static Arguments replaceNullExpressionTest2() {
        return of("Do replace 'null' values",
                OfficeStamperConfigurations.standard()
                                           .addResolver(Resolvers.nullToEmpty()),
                name(null),
                getResource(Path.of("ReplaceNullExpressionTest.docx")),
                """
                        I am .
                        """);
    }

    private static Arguments changingPageLayoutTest_shouldKeepPageBreakOrientationInRepeatDocPartWithSectionBreaksInsideCommentAndTableAsLastElement() {
        return arguments(
                "changingPageLayoutTest_shouldKeepPageBreakOrientationInRepeatDocPartWithSectionBreaksInsideCommentAndTableAsLastElement",
                OfficeStamperConfigurations.standard()
                                           .setEvaluationContextConfigurer(
                                                   ctx -> ctx.addPropertyAccessor(new MapAccessor())),
                Map.of("repeatValues",
                        List.of(new Name("Homer"), new Name("Marge"))),
                getResource(
                        Path.of(
                                "ChangingPageLayoutInRepeatDocPartWithTableLastElementTest.docx")),
                """
                        First page is portrait.
                        
                        ❬❘docGrid=xxx,eGHdrFtrReferences=xxx,pgMar=xxx,pgSz={h=16838,w=11906}❭
                        Second page is landscape, layout change should survive to repeatDocPart (Homer).
                        
                        ❬❘docGrid=xxx,eGHdrFtrReferences=xxx,pgMar=xxx,pgSz={h=11906,orient=landscape,w=16838}❭
                        With a break setting the layout to portrait in between.
                        
                        ❬❘docGrid=xxx,eGHdrFtrReferences=xxx,pgMar=xxx,pgSz={h=16838,w=11906}❭
                        Second page is landscape, layout change should survive to repeatDocPart (Marge).
                        
                        ❬❘docGrid=xxx,eGHdrFtrReferences=xxx,pgMar=xxx,pgSz={h=11906,orient=landscape,w=16838}❭
                        With a break setting the layout to portrait in between.
                        
                        ❬❘docGrid=xxx,eGHdrFtrReferences=xxx,pgMar=xxx,pgSz={h=16838,w=11906}❭
                        ❬❘docGrid=xxx,eGHdrFtrReferences=xxx,pgMar=xxx,pgSz={h=16838,w=11906}❭
                        Fourth page is set to landscape again.
                        """);
    }

    private static Arguments changingPageLayoutTest_shouldKeepPageBreakOrientationInRepeatDocPartWithoutSectionBreaksInsideComment() {
        return arguments(
                "changingPageLayoutTest_shouldKeepPageBreakOrientationInRepeatDocPartWithoutSectionBreaksInsideComment",
                OfficeStamperConfigurations.standard()
                                           .setEvaluationContextConfigurer(
                                                   ctx -> ctx.addPropertyAccessor(new MapAccessor())),
                Map.of("repeatValues",
                        List.of(new Name("Homer"), new Name("Marge"))),
                getResource(Path.of(
                        "ChangingPageLayoutOutsideRepeatDocPartTest.docx")),
                """
                        First page is landscape.
                        
                        ❬❘docGrid=xxx,eGHdrFtrReferences=xxx,pgMar=xxx,pgSz={h=11906,orient=landscape,w=16838}❭
                        Second page is portrait, layout change should survive to repeatDocPart (Homer).
                        <break page>
                        
                        Without a break changing the layout in between (page break should be repeated).
                        Second page is portrait, layout change should survive to repeatDocPart (Marge).
                        <break page>
                        
                        Without a break changing the layout in between (page break should be repeated).
                        ❬❘docGrid=xxx,eGHdrFtrReferences=xxx,pgMar=xxx,pgSz={h=16838,w=11906}❭
                        Fourth page is set to landscape again.
                        """);
    }

    private static Arguments conditionalDisplayOfParagraphsTest_processorExpressionsInCommentsAreResolved() {
        var context = new Contexts.Name("Homer");
        var template = getResource(Path.of(
                "ConditionalDisplayOfParagraphsTest.docx"));
        var expected = """
                Conditional Display of Paragraphs
                This paragraph stays untouched.
                This paragraph stays untouched.
                Conditional Display of paragraphs also works in tables
                This paragraph stays untouched.
                
                Also works in nested tables
                This paragraph stays untouched.
                
                """;

        return arguments(
                "conditionalDisplayOfParagraphsTest_processorExpressionsInCommentsAreResolved",
                OfficeStamperConfigurations.standard(),
                context,
                template,
                expected);
    }

    private static Arguments conditionalDisplayOfParagraphsTest_inlineProcessorExpressionsAreResolved() {
        var context = new Contexts.Name("Homer");
        var template = getResource(Path.of("ConditionalDisplayOfParagraphsWithoutCommentTest.docx"));
        var expected = """
                Conditional Display of Paragraphs
                Paragraph 1 stays untouched.
                Paragraph 3 stays untouched.
                Conditional Display of paragraphs also works in tables
                Paragraph 4 in cell 2,1 stays untouched.
                
                Also works in nested tables
                Paragraph 6 in cell 2,1 in cell 3,1 stays untouched.
                
                """;
        return arguments("conditionalDisplayOfParagraphsTest_inlineProcessorExpressionsAreResolved",
                OfficeStamperConfigurations.standard(),
                context,
                template,
                expected);
    }

    private static Arguments conditionalDisplayOfParagraphsTest_unresolvedInlineProcessorExpressionsAreRemoved() {
        var context = new Contexts.Name("Bart");
        var template = getResource(Path.of("ConditionalDisplayOfParagraphsWithoutCommentTest.docx"));
        var expected = """
                Conditional Display of Paragraphs
                Paragraph 1 stays untouched.
                Paragraph 2 is only included if the “name” is “Bart”.
                Paragraph 3 stays untouched.
                Conditional Display of paragraphs also works in tables
                Paragraph 4 in cell 2,1 stays untouched.
                Paragraph 5 in cell 2,2 is only included if the “name” is “Bart”.
                Also works in nested tables
                Paragraph 6 in cell 2,1 in cell 3,1 stays untouched.
                Paragraph 7  in cell 2,1 in cell 3,1 is only included if the “name” is “Bart”.
                
                """;
        return arguments("conditionalDisplayOfParagraphsTest_unresolvedInlineProcessorExpressionsAreRemoved",
                OfficeStamperConfigurations.standard(),
                context,
                template,
                expected);
    }

    private static Arguments conditionalDisplayOfTableRowsTest() {
        var context = new Contexts.Name("Homer");
        var template = getResource(Path.of("ConditionalDisplayOfTableRowsTest" +
                                           ".docx"));
        var expected = """
                ❬Conditional Display of Table Rows❘spacing={after=120,before=240}❭
                ❬❬This paragraph stays untouched.❘lang=de-DE❭❘lang=de-DE❭
                This row stays untouched.
                This row stays untouched.
                ❬❬Also works on nested Tables❘b=true❭❘b=true❭
                This row stays untouched.
                ❬❘spacing={after=140,before=0}❭
                """;
        return arguments("conditionalDisplayOfTableRowsTest",
                OfficeStamperConfigurations.standard(),
                context,
                template,
                expected);
    }

    private static Arguments conditionalDisplayOfTableBug32Test() {
        var context = new Contexts.Name("Homer");
        var template = getResource(Path.of(
                "ConditionalDisplayOfTablesBug32Test.docx"));
        var expected = """
                ❬Conditional Display of Tables❘spacing={after=120,before=240}❭
                ❬This paragraph stays untouched.❘lang=de-DE❭
                
                ❬This table stays untouched.❘widowControl=xxx❭
                ❬❘widowControl=xxx❭
                ❬❘widowControl=xxx❭
                ❬❘widowControl=xxx❭
                
                ❬❬Also works on nested tables❘b=true❭❘b=true,widowControl=xxx❭
                ❬❘b=true,widowControl=xxx❭
                
                ❬❬This paragraph stays untouched.❘lang=de-DE❭❘spacing={after=140,before=0}❭
                """;
        return arguments("conditionalDisplayOfTableBug32Test",
                OfficeStamperConfigurations.standard(),
                context,
                template,
                expected);
    }

    private static Arguments conditionalDisplayOfTableTest() {
        var context = new Contexts.Name("Homer");
        var template = getResource(Path.of("ConditionalDisplayOfTablesTest" +
                                           ".docx"));
        var expected = """
                ❬Conditional Display of Tables❘spacing={after=120,before=240}❭
                ❬❬This paragraph stays untouched.❘lang=de-DE❭❘lang=de-DE❭
                
                This table stays untouched.
                
                
                
                
                ❬❬Also works on nested tables❘b=true❭❘b=true❭
                ❬❘b=true❭
                
                ❬❬This paragraph stays untouched.❘lang=de-DE❭❘lang=de-DE,spacing={after=140,before=0}❭
                """;
        return arguments("conditionalDisplayOfTablesTest",
                OfficeStamperConfigurations.standard(),
                context,
                template,
                expected);
    }

    private static Arguments customEvaluationContextConfigurerTest_customEvaluationContextConfigurerIsHonored() {
        var context = new Contexts.EmptyContext();
        var template = getResource(Path.of(
                "CustomEvaluationContextConfigurerTest.docx"));
        var expected = """
                ❬Custom EvaluationContextConfigurer Test❘spacing={after=120,before=240}❭
                ❬❬This paragraph stays untouched.❘lang=de-DE❭❘lang=de-DE❭
                ❬The variable foo has the value ❬bar❘lang=de-DE❭.❘spacing={after=140,before=0}❭
                """;
        var config = OfficeStamperConfigurations.standard()
                                                .setEvaluationContextConfigurer(
                                                        evalContext -> evalContext.addPropertyAccessor(new SimpleGetter(
                                                                "foo",
                                                                "bar")));

        return arguments(
                "customEvaluationContextConfigurerTest_customEvaluationContextConfigurerIsHonored",
                config,
                context,
                template,
                expected);
    }

    private static Arguments customExpressionFunctionTest() {
        var context = new Contexts.Name("Homer Simpson");
        var template = getResource(Path.of("CustomExpressionFunction.docx"));
        var expected = """
                Custom Expression Function
                This paragraph is untouched.
                ❬In this paragraph, a custom expression function is used to uppercase a String: ❬HOMER SIMPSON❘b=true❭❬.❘b=true❭❘b=true❭
                ❬IT ALSO WORKS WITH<break line>
                MULTILINE<break line>
                STRINGS OF TEXT❘b=true❭❬.❘b=true❭
                To test that custom functions work together with comment expressions, we toggle visibility of this paragraph with a comment expression.
                """;
        var config = OfficeStamperConfigurations.standard()
                                                .exposeInterfaceToExpressionLanguage(
                                                        Functions.UppercaseFunction.class,
                                                        Functions.upperCase());
        return arguments("customExpressionFunctionTest",
                config,
                context,
                template,
                expected);
    }

    private static Arguments expressionReplacementInGlobalParagraphsTest() {
        var context = new Contexts.Name("Homer Simpson");
        var template = getResource(Path.of("ExpressionReplacementInGlobalParagraphsTest.docx"));
        var expected = """
                ❬Expression Replacement in global paragraphs❘spacing={after=120,before=240}❭
                ❬❬This paragraph is untouched.❘lang=de-DE❭❘lang=de-DE❭
                ❬In this paragraph, the variable ❘lang=de-DE❭❬name❘b=true,lang=de-DE❭ should be resolved to the value ❬Homer Simpson❘lang=de-DE❭.
                ❬❬In this paragraph, the variable ❘lang=de-DE❭❬foo❘b=true,lang=de-DE❭❬ should not be resolved: ${foo}.❘lang=de-DE❭❘spacing={after=140,before=0}❭
                """;
        OfficeStamperConfiguration config = OfficeStamperConfigurations.standard()
                                                                       .setFailOnUnresolvedExpression(false);
        return arguments("expressionReplacementInGlobalParagraphsTest",
                config,
                context,
                template,
                expected);
    }

    private static Arguments expressionReplacementInTablesTest() {
        var context = new Contexts.Name("Bart Simpson");
        var template = getResource(Path.of("ExpressionReplacementInTablesTest" +
                                           ".docx"));

        var expected = """
                Expression Replacement in Tables
                This should resolve to a name:
                Bart Simpson
                This should not resolve:
                ${foo}
                ❬❬Nested Table:❘b=true❭❘b=true❭
                This should resolve to a name:
                Bart Simpson
                This should not resolve:
                ${foo}
                
                """;
        OfficeStamperConfiguration config = OfficeStamperConfigurations.standard()
                                                                       .setFailOnUnresolvedExpression(false);
        return arguments("expressionReplacementInTablesTest",
                config,
                context,
                template,
                expected);
    }

    private static Arguments expressionReplacementWithFormattingTest() {
        var context = new Contexts.Name("Homer Simpson");
        var template = getResource(
                Path.of("ExpressionReplacementWithFormattingTest.docx"));
        var expected = """
                 Expression Replacement with text format
                The text format should be kept intact when an expression is replaced.
                ❬It should be bold: ❬Homer Simpson❘b=true❭❘b=true❭
                ❬It should be italic: ❬Homer Simpson❘i=true❭❘i=true❭
                ❬It should be superscript: ❬Homer Simpson❘vertAlign=superscript❭❘i=true❭
                ❬It should be subscript: ❬Homer Simpson❘vertAlign=subscript❭❘vertAlign=subscript❭
                ❬It should be striked: ❬Homer Simpson❘strike=true❭❘i=true❭
                ❬It should be underlined: ❬Homer Simpson❘u=single❭❘i=true❭
                ❬It should be doubly underlined: ❬Homer Simpson❘u=double❭❘i=true❭
                ❬It should be thickly underlined: ❬Homer Simpson❘u=thick❭❘i=true❭
                ❬It should be dot underlined: ❬Homer Simpson❘u=dotted❭❘i=true❭
                ❬It should be dash underlined: ❬Homer Simpson❘u=dash❭❘i=true❭
                ❬It should be dot and dash underlined: ❬Homer Simpson❘u=dotDash❭❘i=true❭
                ❬It should be dot, dot and dash underlined: ❬Homer Simpson❘u=dotDotDash❭❘i=true❭
                It should be highlighted yellow: ❬Homer Simpson❘highlight=yellow❭
                ❬It should be white over darkblue: ❬Homer Simpson❘color=FFFFFF,highlight=darkBlue❭❘b=true❭
                ❬It should be with header formatting: ❬Homer Simpson❘rStyle=TitreCar❭❘b=true❭
                """;
        return arguments("expressionReplacementWithFormattingTest",
                OfficeStamperConfigurations.standard(),
                context,
                template,
                expected);
    }

    private static Arguments expressionWithSurroundingSpacesTest() {
        var spacyContext = new Contexts.SpacyContext();
        var template = getResource(Path.of(
                "ExpressionWithSurroundingSpacesTest.docx"));
        var expected = """
                ❬Expression Replacement when expression has leading and/or trailing spaces❘spacing={after=120,before=240}❭
                When an expression within a paragraph is resolved, the spaces between the replacement and the surrounding text should be as expected. The following paragraphs should all look the same.
                Before Expression After.
                Before Expression After.
                Before Expression After.
                Before Expression After.
                Before Expression After.
                Before Expression After.
                ❬Before Expression After.❘spacing={after=140,before=0}❭
                """;
        return arguments("expressionWithSurroundingSpacesTest",
                OfficeStamperConfigurations.standard(),
                spacyContext,
                template,
                expected);
    }

    private static Arguments expressionReplacementWithCommentTest() {
        var context = new Contexts.Name("Homer Simpson");
        var template = getResource(Path.of("ExpressionReplacementWithCommentsTest.docx"));
        var expected = """
                Expression Replacement with comments
                This paragraph is untouched.
                In this paragraph, the variable ❬name❘b=true❭ should be resolved to the value Homer Simpson.
                In this paragraph, the variable ❬foo❘b=true❭ should not be resolved: unresolvedValueWithCommentreplaceWordWith(foo)
                
                .
                """;
        var config = OfficeStamperConfigurations.standard()
                                                .setFailOnUnresolvedExpression(false);
        return arguments("expressionReplacementWithCommentsTest",
                config,
                context,
                template,
                expected);
    }

    /**
     * <p>testDateInstantiationAndResolution.</p>
     */
    private static Arguments imageReplacementInGlobalParagraphsTest() {
        var context = new Contexts.ImageContext(getImage(Path.of("monalisa" +
                                                                 ".jpg")));
        var template = getResource(Path.of(
                "ImageReplacementInGlobalParagraphsTest.docx"));
        var expected = """
                ❬Image Replacement in global paragraphs❘spacing={after=120,before=240}❭
                ❬❬This paragraph is untouched.❘lang=de-DE❭❘lang=de-DE❭
                ❬In this paragraph, an image of Mona Lisa is inserted: ❬/word/media/document_image_rId4.jpeg:rId4:image/jpeg:8.8kB:sha1=XMpVtDbetKjZTkPhy598GdJQM/4=:cy=$d:1276350❘lang=de-DE❭.❘lang=de-DE❭
                ❬This paragraph has the image ❬/word/media/document_image_rId5.jpeg:rId5:image/jpeg:8.8kB:sha1=XMpVtDbetKjZTkPhy598GdJQM/4=:cy=$d:1276350❘lang=de-DE❭ in the middle.❘lang=de-DE,spacing={after=140,before=0}❭
                """;
        return arguments("imageReplacementInGlobalParagraphsTest",
                OfficeStamperConfigurations.standard(),
                context,
                template,
                expected);
    }

    private static Arguments imageReplacementInGlobalParagraphsTestWithMaxWidth() {
        var context = new Contexts.ImageContext(getImage(Path.of("monalisa" +
                                                                 ".jpg"),
                1000));
        var template = getResource(Path.of(
                "ImageReplacementInGlobalParagraphsTest.docx"));
        var expected = """
                ❬Image Replacement in global paragraphs❘spacing={after=120,before=240}❭
                ❬❬This paragraph is untouched.❘lang=de-DE❭❘lang=de-DE❭
                ❬In this paragraph, an image of Mona Lisa is inserted: ❬/word/media/document_image_rId4.jpeg:rId4:image/jpeg:8.8kB:sha1=XMpVtDbetKjZTkPhy598GdJQM/4=:cy=$d:635000❘lang=de-DE❭.❘lang=de-DE❭
                ❬This paragraph has the image ❬/word/media/document_image_rId5.jpeg:rId5:image/jpeg:8.8kB:sha1=XMpVtDbetKjZTkPhy598GdJQM/4=:cy=$d:635000❘lang=de-DE❭ in the middle.❘lang=de-DE,spacing={after=140,before=0}❭
                """;
        return arguments("imageReplacementInGlobalParagraphsTestWithMaxWidth",
                OfficeStamperConfigurations.standard(),
                context,
                template,
                expected);
    }

    private static Arguments leaveEmptyOnExpressionErrorTest() {
        var context = new Contexts.Name("Homer Simpson");
        var template = getResource(Path.of("LeaveEmptyOnExpressionErrorTest" +
                                           ".docx"));
        var expected = """
                Leave me empty .
                ❬❘u=single❭
                """;
        var config = OfficeStamperConfigurations.standard()
                                                .setFailOnUnresolvedExpression(false)
                                                .leaveEmptyOnExpressionError(true);
        return arguments("leaveEmptyOnExpressionErrorTest",
                config,
                context,
                template,
                expected);
    }

    private static Arguments lineBreakReplacementTest() {
        var config = OfficeStamperConfigurations.standard()
                                                .setLineBreakPlaceholder("#");
        var context = new Contexts.Name(null);
        var template = getResource(Path.of("LineBreakReplacementTest.docx"));
        var expected = """
                Line Break Replacement
                This paragraph is untouched.
                This paragraph should be <break line>
                 split in <break line>
                 three lines.
                This paragraph is untouched.
                """;
        return arguments("lineBreakReplacementTest",
                config,
                context,
                template,
                expected);
    }

    private static Arguments mapAccessorAndReflectivePropertyAccessorTest_shouldResolveMapAndPropertyPlaceholders() {
        var context = Contexts.mapAndReflectiveContext();
        var template = getResource(
                Path.of("MapAccessorAndReflectivePropertyAccessorTest.docx"));
        var expected = """
                Flat string : Flat string has been resolved
                
                Values
                first value
                second value
                
                
                Paragraph start
                first value
                Paragraph end
                Paragraph start
                second value
                Paragraph end
                
                """;

        var config = OfficeStamperConfigurations.standard()
                                                .setFailOnUnresolvedExpression(false)
                                                .setLineBreakPlaceholder("\n")
                                                .addResolver(Resolvers.nullToDefault("N/C"))
                                                .replaceUnresolvedExpressions(true)
                                                .unresolvedExpressionsDefaultValue("N/C")
                                                .setEvaluationContextConfigurer(ctx -> ctx.addPropertyAccessor(
                                                        new MapAccessor()));

        return arguments(
                "mapAccessorAndReflectivePropertyAccessorTest_shouldResolveMapAndPropertyPlaceholders",
                config,
                context,
                template,
                expected);
    }

    private static Arguments nullPointerResolutionTest_testWithDefaultSpel() {
        var context = Contexts.nullishContext();
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

        var config = OfficeStamperConfigurations.standard()
                                                .setFailOnUnresolvedExpression(false);

        return arguments("nullPointerResolutionTest_testWithDefaultSpel",
                config,
                context,
                template,
                expected);
    }

    private static Arguments nullPointerResolutionTest_testWithCustomSpel() {
        var context = Contexts.nullishContext();
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

        var config = OfficeStamperConfigurations.standard()
                                                .setSpelParserConfiguration(new SpelParserConfiguration(true,
                                                        true))
                                                .setEvaluationContextConfigurer(EvaluationContextConfigurers.noopConfigurer())
                                                .addResolver(Resolvers.nullToDefault("Nullish value!!"));

        return arguments("nullPointerResolutionTest_testWithCustomSpel",
                config,
                context,
                template,
                expected);
    }

    private static Arguments customCommentProcessor() {
        return arguments(
                "customCommentProcessor",
                OfficeStamperConfigurations.standard()
                                           .addCommentProcessor(
                                                   ICustomCommentProcessor.class,
                                                   CustomCommentProcessor::new),
                Contexts.empty(),
                getResource(Path.of("CustomCommentProcessorTest.docx")),
                """     
                        Custom CommentProcessor Test
                        Visited
                        This paragraph is untouched.
                        Visited
                        """);
    }

    private static Arguments controls() {
        return of("Form controls should be replaced as well",
                OfficeStamperConfigurations.standard(),
                name("Homer"),
                getResource(Path.of("form-controls.docx")),
                """
                        Expression Replacement in Form Controls
                        [Rich text control line Homer]
                        Rich text control inlined [Homer]
                        [Raw text control line Homer]
                        Raw text control inlined [Homer]
                        [Homer]
                        
                        """);
    }

    @MethodSource("tests")
    @ParameterizedTest(name = "{0}")
    void features(
            String ignoredName,
            OfficeStamperConfiguration config,
            Object context,
            InputStream template,
            String expected
    ) {
        var stamper = new TestDocxStamper<>(config);
        var actual = stamper.stampAndLoadAndExtract(template, context);
        assertEquals(expected, actual);
    }
}
