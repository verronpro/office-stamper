package org.wickedsource.docxstamper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.context.expression.MapAccessor;
import org.springframework.expression.spel.SpelParserConfiguration;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.wickedsource.docxstamper.el.NoOpEvaluationContextConfigurer;
import org.wickedsource.docxstamper.replace.typeresolver.image.Image;
import pro.verron.docxstamper.Functions;
import pro.verron.docxstamper.accessors.SimpleGetter;
import pro.verron.docxstamper.commentProcessors.CustomCommentProcessor;
import pro.verron.docxstamper.commentProcessors.ICustomCommentProcessor;
import pro.verron.docxstamper.preset.resolver.Resolvers;
import pro.verron.docxstamper.resolver.CustomTypeResolver;
import pro.verron.docxstamper.utils.TestDocxStamper;
import pro.verron.docxstamper.utils.context.Contexts;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.junit.jupiter.params.provider.Arguments.of;
import static pro.verron.docxstamper.utils.context.Contexts.*;

/**
 * <p>DefaultTests class.</p>
 *
 * @author Joseph Verron
 * @version 1.6.6
 * @since 1.6.6
 */
@DisplayName("Core Features")
public class DefaultTests {

    /**
     * <p>getResource.</p>
     *
     * @param path a {@link java.nio.file.Path} object
     * @return a {@link java.io.InputStream} object
     */
    public static InputStream getResource(Path path) {
        try {
            var testRoot = Path.of("test", "sources");
            var resolve = testRoot.resolve(path);
            return Files.newInputStream(resolve);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static Arguments replaceWordWithIntegrationTest() {
        return of("replaceWordWithIntegrationTest",
                  new DocxStamperConfiguration(),
                  name("Simpsons"),
                  getResource(Path.of("integration",
                                      "ReplaceWordWithIntegrationTest.docx")),
                  """
                          ReplaceWordWith Integration
                          ❬This variable ❬name❘b=true❭❬ ❘b=true❭should be resolved to the value Simpsons.❘b=true❭
                          This variable ❬name❘b=true❭ should be resolved to the value Simpsons.
                          """);
    }

    private static Arguments repeatingRows() {
        return of("Repeating table rows should be possible",
                  new DocxStamperConfiguration(),
                  roles(role("Homer Simpson", "Dan Castellaneta"),
                        role("Marge Simpson", "Julie Kavner"),
                        role("Bart Simpson", "Nancy Cartwright"),
                        role("Kent Brockman", "Harry Shearer"),
                        role("Disco Stu", "Hank Azaria"),
                        role("Krusty the Clown", "Dan Castellaneta")),
                  getResource(Path.of("RepeatTableRowTest.docx")),
                  """
                          ❬Repeating Table Rows❘spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120}❭
                          ❬❬List of Simpsons characters❘b=true❭❘b=true,spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120}❭
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
                                                                     
                          ❬There are ❬6❘lang=de-DE❭ characters in the above table.❘lang=de-DE,spacing={after=140,afterLines=140,before=140,beforeLines=140,line=140,lineRule=140}❭""");
    }

    private static Arguments ternary() {
        return of("Ternary operators should function",
                  new DocxStamperConfiguration(),
                  name("Homer"),
                  getResource(Path.of("TernaryOperatorTest.docx")),
                  """
                          Expression Replacement with ternary operator
                          This paragraph is untouched.
                          Some replacement before the ternary operator: Homer.
                          Homer <-- this should read "Homer".
                           <-- this should be empty.""");
    }

    private static Arguments whitespaces() {
        return of("White spaces should be preserved",
                  new DocxStamperConfiguration(),
                  name("Homer Simpson"),
                  getResource(Path.of("TabsIndentationTest.docx")),
                  """
                          ❬❬Tab❘lang=en-US❭❬|TAB|❘lang=en-US❭❬Homer Simpson❘lang=en-US❭❘lang=en-US❭
                          ❬❬Space❘lang=en-US❭❬ ❘lang=en-US❭❬Homer Simpson❘lang=en-US❭❘lang=en-US❭""");
    }

    private static Arguments tabulations() {
        return of("Tabulation should be preserved",
                  new DocxStamperConfiguration(),
                  name("Homer Simpson"),
                  getResource(Path.of("TabsIndentationTest.docx")),
                  """
                          ❬❬Tab❘lang=en-US❭❬|TAB|❘lang=en-US❭❬Homer Simpson❘lang=en-US❭❘lang=en-US❭
                          ❬❬Space❘lang=en-US❭❬ ❘lang=en-US❭❬Homer Simpson❘lang=en-US❭❘lang=en-US❭""");
    }

    private static Arguments replaceNullExpressionTest() {
        return of("Do not replace 'null' values",
                  new DocxStamperConfiguration()
                          .addResolver(Resolvers.nullToPlaceholder()),
                  name(null),
                  getResource(Path.of("ReplaceNullExpressionTest.docx")),
                  "I am ${name}.");
    }

    private static Arguments replaceNullExpressionTest2() {
        return of("Do replace 'null' values",
                  new DocxStamperConfiguration()
                          .addResolver(Resolvers.nullToEmpty()),
                  name(null),
                  getResource(Path.of("ReplaceNullExpressionTest.docx")),
                  "I am .");
    }

    private static Arguments repeatTableRowKeepsFormatTest() {
        return of("repeatTableRowKeepsFormatTest",
                  new DocxStamperConfiguration(),
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
                  getResource(Path.of("integration",
                                      "RepeatTableRowKeepsFormatTest.docx")),
                  """
                          1❬st❘vertAlign=superscript❭ Homer Simpson-❬Dan Castellaneta❘b=true❭
                          2❬nd❘vertAlign=superscript❭ Marge Simpson-❬Julie Kavner❘b=true❭
                          3❬rd❘vertAlign=superscript❭ Bart Simpson-❬Nancy Cartwright❘b=true❭
                          4❬th❘vertAlign=superscript❭ Lisa Simpson-❬Yeardley Smith❘b=true❭
                          5❬th❘vertAlign=superscript❭ Maggie Simpson-❬Julie Kavner❘b=true❭
                          """);
    }

    private static Arguments repeatParagraphTest() {
        var context = new Contexts.Characters(List.of(new Contexts.Role(
                                                              "Homer Simpson",
                                                              "Dan Castellaneta"),
                                                      new Contexts.Role(
                                                              "Marge Simpson",
                                                              "Julie Kavner"),
                                                      new Contexts.Role(
                                                              "Bart Simpson",
                                                              "Nancy Cartwright"),
                                                      new Contexts.Role(
                                                              "Kent Brockman",
                                                              "Harry Shearer"),
                                                      new Contexts.Role(
                                                              "Disco Stu",
                                                              "Hank Azaria"),
                                                      new Contexts.Role(
                                                              "Krusty the Clown",
                                                              "Dan Castellaneta")));
        var template = getResource(Path.of("RepeatParagraphTest.docx"));
        var expected = """
                ❬Repeating Paragraphs❘spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120}❭
                ❬❬List of Simpsons characters❘b=true❭❘spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120}❭
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
                     
                ❬There are ❬6❘lang=de-DE❭ characters.❘spacing={after=140,afterLines=140,before=140,beforeLines=140,line=140,lineRule=140}❭""";

        return arguments("repeatParagraphTest",
                         new DocxStamperConfiguration(),
                         context,
                         template,
                         expected);
    }

    private static Arguments repeatDocPartWithImageTestShouldImportImageDataInTheMainDocument() {
        var context = Map.of("units", Stream.of(getImage(Path.of("butterfly" +
                                                                 ".png")),
                                                getImage(Path.of("map.jpg")))
                .map(image -> Map.of("coverImage", image))
                .map(map -> Map.of("productionFacility", map))
                .toList());
        var template = getResource(Path.of("RepeatDocPartWithImageTest.docx"));
        var expected = """
                                
                rId11:image/png:193.6kB:sha1=t8UNAmo7yJgZJk9g7pLLIb3AvCA=:cy=$d:6120130
                rId12:image/jpeg:407.5kB:sha1=Ujo3UzL8WmeZN/1K6weBydaI73I=:cy=$d:6120130
                                
                                
                                
                Always rendered:
                rId13:image/png:193.6kB:sha1=t8UNAmo7yJgZJk9g7pLLIb3AvCA=:cy=$d:6120130
                """;

        var config = new DocxStamperConfiguration().setEvaluationContextConfigurer(
                ctx -> ctx.addPropertyAccessor(new MapAccessor()));
        return of(
                "repeatDocPartWithImageTestShouldImportImageDataInTheMainDocument",
                config,
                context,
                template,
                expected);
    }

    private static Image getImage(Path path) {
        try {
            return new Image(getResource(path));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static Arguments repeatDocPartWithImagesInSourceTestshouldReplicateImageFromTheMainDocumentInTheSubTemplate() {
        return of(
                "repeatDocPartWithImagesInSourceTestshouldReplicateImageFromTheMainDocumentInTheSubTemplate",
                new DocxStamperConfiguration().setEvaluationContextConfigurer((StandardEvaluationContext ctx) -> ctx.addPropertyAccessor(
                        new MapAccessor())),
                Contexts.subDocPartContext(),
                getResource(Path.of("RepeatDocPartWithImagesInSourceTest" +
                                    ".docx")),
                """
                        This is not repeated
                        This should be repeated : first doc part
                        rId12:image/png:193.6kB:sha1=t8UNAmo7yJgZJk9g7pLLIb3AvCA=:cy=$d:5760720
                        This should be repeated too
                        This should be repeated : second doc part
                        rId13:image/png:193.6kB:sha1=t8UNAmo7yJgZJk9g7pLLIb3AvCA=:cy=$d:5760720
                        This should be repeated too
                        This is not repeated""");
    }

    private static Arguments repeatDocPartTest() {
        return of("repeatDocPartTest",
                  new DocxStamperConfiguration(),
                  new Characters(List.of(new Role("Homer Simpson",
                                                  "Dan Castellaneta"),
                                         new Role("Marge Simpson",
                                                  "Julie Kavner"),
                                         new Role("Bart Simpson",
                                                  "Nancy Cartwright"),
                                         new Role("Kent Brockman",
                                                  "Harry Shearer"),
                                         new Role("Disco Stu", "Hank Azaria"),
                                         new Role("Krusty the Clown",
                                                  "Dan Castellaneta"))),
                  getResource(Path.of("RepeatDocPartTest.docx")),
                  """
                          Repeating Doc Part
                          ❬❬List ❘b=true❭❬of❘b=true❭❬ Simpsons ❘b=true❭❬characters❘b=true❭❘b=true,spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120}❭
                          ❬Paragraph for test: Homer Simpson - Dan Castellaneta❘spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120}❭
                          ❬Homer Simpson❘jc=center❭
                          ❬Dan Castellaneta❘jc=center❭
                          ❬ |BR|❘suppressAutoHyphens=xxx,widowControl=xxx❭
                          ❬Paragraph for test: Marge Simpson - Julie Kavner❘spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120}❭
                          ❬Marge Simpson❘jc=center❭
                          ❬Julie Kavner❘jc=center❭
                          ❬ |BR|❘suppressAutoHyphens=xxx,widowControl=xxx❭
                          ❬Paragraph for test: Bart Simpson - Nancy Cartwright❘spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120}❭
                          ❬Bart Simpson❘jc=center❭
                          ❬Nancy Cartwright❘jc=center❭
                          ❬ |BR|❘suppressAutoHyphens=xxx,widowControl=xxx❭
                          ❬Paragraph for test: Kent Brockman - Harry Shearer❘spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120}❭
                          ❬Kent Brockman❘jc=center❭
                          ❬Harry Shearer❘jc=center❭
                          ❬ |BR|❘suppressAutoHyphens=xxx,widowControl=xxx❭
                          ❬Paragraph for test: Disco Stu - Hank Azaria❘spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120}❭
                          ❬Disco Stu❘jc=center❭
                          ❬Hank Azaria❘jc=center❭
                          ❬ |BR|❘suppressAutoHyphens=xxx,widowControl=xxx❭
                          ❬Paragraph for test: Krusty the Clown - Dan Castellaneta❘spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120}❭
                          ❬Krusty the Clown❘jc=center❭
                          ❬Dan Castellaneta❘jc=center❭
                          ❬ |BR|❘suppressAutoHyphens=xxx,widowControl=xxx❭
                          There are 6 characters.""");
    }

    private static Arguments repeatDocPartNestingTest() {
        return of("repeatDocPartNestingTest",
                  new DocxStamperConfiguration(),
                  Contexts.schoolContext(),
                  getResource(Path.of("RepeatDocPartNestingTest.docx")),
                  """
                          ❬Repeating ❬N❘lang=en-US❭ested Doc Part ❘suppressAutoHyphens=xxx,widowControl=xxx❭
                          ❬List of All the s❬tu❘lang=en-US❭❬dent’s of all grades❘lang=null❭❘lang=null,suppressAutoHyphens=xxx,widowControl=xxx❭
                          ❬❬South Park Primary School❘lang=null❭❘lang=null,suppressAutoHyphens=xxx,widowControl=xxx❭
                          ❬❬Grade No.❘b=true,lang=null❭❬0❘b=true,lang=null❭❬ ❘b=true,lang=null❭❬t❘lang=null❭here are 3 classes❘b=true,lang=null,suppressAutoHyphens=xxx,widowControl=xxx❭
                          ❬Class No.0 ❬t❘lang=null❭here are 5 students❘suppressAutoHyphens=xxx,widowControl=xxx❭
                          ❬0❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                          ❬Bruce·No0❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                          ❬1❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                          ❬1❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                          ❬Bruce·No1❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                          ❬2❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                          ❬2❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                          ❬Bruce·No2❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                          ❬3❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                          ❬3❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                          ❬Bruce·No3❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                          ❬4❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                          ❬4❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                          ❬Bruce·No4❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                          ❬5❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                          ❬Class No.1 ❬t❘lang=null❭here are 5 students❘suppressAutoHyphens=xxx,widowControl=xxx❭
                          ❬0❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                          ❬Bruce·No0❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                          ❬1❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                          ❬1❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                          ❬Bruce·No1❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                          ❬2❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                          ❬2❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                          ❬Bruce·No2❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                          ❬3❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                          ❬3❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                          ❬Bruce·No3❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                          ❬4❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                          ❬4❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                          ❬Bruce·No4❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                          ❬5❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                          ❬Class No.2 ❬t❘lang=null❭here are 5 students❘suppressAutoHyphens=xxx,widowControl=xxx❭
                          ❬0❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                          ❬Bruce·No0❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                          ❬1❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                          ❬1❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                          ❬Bruce·No1❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                          ❬2❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                          ❬2❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                          ❬Bruce·No2❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                          ❬3❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                          ❬3❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                          ❬Bruce·No3❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                          ❬4❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                          ❬4❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                          ❬Bruce·No4❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                          ❬5❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                          ❬❬Grade No.❘b=true,lang=null❭❬1❘b=true,lang=null❭❬ ❘b=true,lang=null❭❬t❘lang=null❭here are 3 classes❘b=true,lang=null,suppressAutoHyphens=xxx,widowControl=xxx❭
                          ❬Class No.0 ❬t❘lang=null❭here are 5 students❘suppressAutoHyphens=xxx,widowControl=xxx❭
                          ❬0❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                          ❬Bruce·No0❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                          ❬1❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                          ❬1❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                          ❬Bruce·No1❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                          ❬2❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                          ❬2❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                          ❬Bruce·No2❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                          ❬3❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                          ❬3❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                          ❬Bruce·No3❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                          ❬4❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                          ❬4❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                          ❬Bruce·No4❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                          ❬5❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                          ❬Class No.1 ❬t❘lang=null❭here are 5 students❘suppressAutoHyphens=xxx,widowControl=xxx❭
                          ❬0❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                          ❬Bruce·No0❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                          ❬1❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                          ❬1❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                          ❬Bruce·No1❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                          ❬2❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                          ❬2❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                          ❬Bruce·No2❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                          ❬3❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                          ❬3❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                          ❬Bruce·No3❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                          ❬4❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                          ❬4❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                          ❬Bruce·No4❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                          ❬5❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                          ❬Class No.2 ❬t❘lang=null❭here are 5 students❘suppressAutoHyphens=xxx,widowControl=xxx❭
                          ❬0❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                          ❬Bruce·No0❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                          ❬1❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                          ❬1❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                          ❬Bruce·No1❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                          ❬2❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                          ❬2❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                          ❬Bruce·No2❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                          ❬3❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                          ❬3❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                          ❬Bruce·No3❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                          ❬4❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                          ❬4❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                          ❬Bruce·No4❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                          ❬5❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                          ❬❬Grade No.❘b=true,lang=null❭❬2❘b=true,lang=null❭❬ ❘b=true,lang=null❭❬t❘lang=null❭here are 3 classes❘b=true,lang=null,suppressAutoHyphens=xxx,widowControl=xxx❭
                          ❬Class No.0 ❬t❘lang=null❭here are 5 students❘suppressAutoHyphens=xxx,widowControl=xxx❭
                          ❬0❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                          ❬Bruce·No0❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                          ❬1❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                          ❬1❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                          ❬Bruce·No1❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                          ❬2❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                          ❬2❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                          ❬Bruce·No2❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                          ❬3❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                          ❬3❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                          ❬Bruce·No3❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                          ❬4❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                          ❬4❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                          ❬Bruce·No4❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                          ❬5❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                          ❬Class No.1 ❬t❘lang=null❭here are 5 students❘suppressAutoHyphens=xxx,widowControl=xxx❭
                          ❬0❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                          ❬Bruce·No0❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                          ❬1❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                          ❬1❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                          ❬Bruce·No1❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                          ❬2❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                          ❬2❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                          ❬Bruce·No2❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                          ❬3❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                          ❬3❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                          ❬Bruce·No3❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                          ❬4❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                          ❬4❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                          ❬Bruce·No4❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                          ❬5❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                          ❬Class No.2 ❬t❘lang=null❭here are 5 students❘suppressAutoHyphens=xxx,widowControl=xxx❭
                          ❬0❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                          ❬Bruce·No0❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                          ❬1❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                          ❬1❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                          ❬Bruce·No1❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                          ❬2❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                          ❬2❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                          ❬Bruce·No2❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                          ❬3❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                          ❬3❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                          ❬Bruce·No3❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                          ❬4❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                          ❬4❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                          ❬Bruce·No4❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                          ❬5❘ind=0,jc=left,keepLines=false,keepNext=true,outlineLvl=9,pageBreakBefore=false,spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120},suppressAutoHyphens=xxx,textAlignment=xxx,topLinePunct=xxx,widowControl=xxx,wordWrap=xxx❭
                          There are 3 grades.""");
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
                                
                This will stay untouched too.""";

        var config = new DocxStamperConfiguration();
        config.setEvaluationContextConfigurer((ctx) -> ctx.addPropertyAccessor(
                new MapAccessor()));

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
                new DocxStamperConfiguration().setEvaluationContextConfigurer(
                        ctx -> ctx.addPropertyAccessor(new MapAccessor())),
                Map.of("repeatValues",
                       List.of(new Name("Homer"), new Name("Marge"))),
                getResource(Path.of(
                        "ChangingPageLayoutOutsideRepeatParagraphTest.docx")),
                """
                        First page is landscape.
                                        
                        ❬❘docGrid=xxx,eGHdrFtrReferences=xxx,pgMar=xxx,pgSz={h=11906,orient=landscape,w=16838}❭
                        Second page is portrait, layout change should survive to repeatParagraph processor (Homer).
                                        
                        Without a section break changing the layout in between, but a page break instead.|BR|
                        Second page is portrait, layout change should survive to repeatParagraph processor (Marge).
                                        
                        Without a section break changing the layout in between, but a page break instead.|BR|
                        ❬❘docGrid=xxx,eGHdrFtrReferences=xxx,pgMar=xxx,pgSz={h=16838,w=11906}❭
                        Fourth page is set to landscape again.""");
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
                Fourth page is set to portrait again.""";

        var config = new DocxStamperConfiguration().setEvaluationContextConfigurer(
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
                new DocxStamperConfiguration().setEvaluationContextConfigurer(
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
                        Fourth page is set to landscape again.""");
    }

    private static Arguments changingPageLayoutTest_shouldKeepPageBreakOrientationInRepeatDocPartWithSectionBreaksInsideCommentAndTableAsLastElement() {
        return arguments(
                "changingPageLayoutTest_shouldKeepPageBreakOrientationInRepeatDocPartWithSectionBreaksInsideCommentAndTableAsLastElement",
                new DocxStamperConfiguration().setEvaluationContextConfigurer(
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
                        Fourth page is set to landscape again.""");
    }

    private static Arguments changingPageLayoutTest_shouldKeepPageBreakOrientationInRepeatDocPartWithoutSectionBreaksInsideComment() {
        return arguments(
                "changingPageLayoutTest_shouldKeepPageBreakOrientationInRepeatDocPartWithoutSectionBreaksInsideComment",
                new DocxStamperConfiguration().setEvaluationContextConfigurer(
                        ctx -> ctx.addPropertyAccessor(new MapAccessor())),
                Map.of("repeatValues",
                       List.of(new Name("Homer"), new Name("Marge"))),
                getResource(Path.of(
                        "ChangingPageLayoutOutsideRepeatDocPartTest.docx")),
                """
                        First page is landscape.
                                        
                        ❬❘docGrid=xxx,eGHdrFtrReferences=xxx,pgMar=xxx,pgSz={h=11906,orient=landscape,w=16838}❭
                        Second page is portrait, layout change should survive to repeatDocPart (Homer).
                        |BR|
                        Without a break changing the layout in between (page break should be repeated).
                        Second page is portrait, layout change should survive to repeatDocPart (Marge).
                        |BR|
                        Without a break changing the layout in between (page break should be repeated).
                        ❬❘docGrid=xxx,eGHdrFtrReferences=xxx,pgMar=xxx,pgSz={h=16838,w=11906}❭
                        Fourth page is set to landscape again.""");
    }

    private static Arguments conditionalDisplayOfParagraphsTest_processorExpressionsInCommentsAreResolved() {
        var context = new Contexts.Name("Homer");
        var template = getResource(Path.of(
                "ConditionalDisplayOfParagraphsTest.docx"));
        var expected = """
                ❬Conditional Display of Paragraphs❘spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120}❭
                ❬❬This paragraph stays untouched.❘lang=de-DE❭❘lang=de-DE❭
                ❬❬This paragraph stays untouched.❘lang=de-DE❭❘lang=de-DE,spacing={after=140,afterLines=140,before=140,beforeLines=140,line=140,lineRule=140}❭
                ❬❬Conditional Display of paragraphs also works in tables❘b=true❭❘b=true❭
                This paragraph stays untouched.
                                                                                                                            
                ❬❬Also works in nested tables❘b=true❭❘b=true❭
                This paragraph stays untouched.
                                                                                                                            
                ❬❘spacing={after=140,afterLines=140,before=140,beforeLines=140,line=140,lineRule=140}❭""";

        return arguments(
                "conditionalDisplayOfParagraphsTest_processorExpressionsInCommentsAreResolved",
                new DocxStamperConfiguration(),
                context,
                template,
                expected);
    }

    private static Arguments conditionalDisplayOfParagraphsTest_inlineProcessorExpressionsAreResolved() {
        var context = new Contexts.Name("Homer");
        var template = getResource(
                Path.of("ConditionalDisplayOfParagraphsWithoutCommentTest" +
                        ".docx"));
        var expected = """
                Conditional Display of Paragraphs
                This paragraph stays untouched.
                This paragraph stays untouched.
                ❬❬Conditional Display of paragraphs also works in tables❘b=true❭❘b=true❭
                This paragraph stays untouched.
                                
                ❬❬Also works in nested tables❘b=true❭❘b=true❭
                This paragraph stays untouched.
                                
                """;
        return arguments(
                "conditionalDisplayOfParagraphsTest_inlineProcessorExpressionsAreResolved",
                new DocxStamperConfiguration(),
                context,
                template,
                expected);
    }

    private static Arguments conditionalDisplayOfParagraphsTest_unresolvedInlineProcessorExpressionsAreRemoved() {
        var context = new Contexts.Name("Bart");
        var template = getResource(
                Path.of("ConditionalDisplayOfParagraphsWithoutCommentTest" +
                        ".docx"));
        var expected = """
                Conditional Display of Paragraphs
                This paragraph stays untouched.
                This paragraph is only included in the resulting document if the variable „name“ has the value „Bart“.
                This paragraph stays untouched.
                ❬❬Conditional Display of paragraphs also works in tables❘b=true❭❘b=true❭
                This paragraph stays untouched.
                This paragraph is only included if the name is „Bart“.
                ❬❬Also works in nested tables❘b=true❭❘b=true❭
                This paragraph stays untouched.
                This paragraph is only included if the name is „Bart“.
                                
                """;
        return arguments(
                "conditionalDisplayOfParagraphsTest_unresolvedInlineProcessorExpressionsAreRemoved",
                new DocxStamperConfiguration(),
                context,
                template,
                expected);
    }

    private static Arguments conditionalDisplayOfTableRowsTest() {
        var context = new Contexts.Name("Homer");
        var template = getResource(Path.of("ConditionalDisplayOfTableRowsTest" +
                                           ".docx"));
        var expected = """
                ❬Conditional Display of Table Rows❘spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120}❭
                ❬❬This paragraph stays untouched.❘lang=de-DE❭❘lang=de-DE❭
                This row stays untouched.
                This row stays untouched.
                ❬❬Also works on nested Tables❘b=true❭❘b=true❭
                This row stays untouched.
                                                                                                                            
                ❬❘spacing={after=140,afterLines=140,before=140,beforeLines=140,line=140,lineRule=140}❭""";
        return arguments("conditionalDisplayOfTableRowsTest",
                         new DocxStamperConfiguration(),
                         context,
                         template,
                         expected);
    }

    private static Arguments conditionalDisplayOfTablesBug32Test() {
        var context = new Contexts.Name("Homer");
        var template = getResource(Path.of(
                "ConditionalDisplayOfTablesBug32Test.docx"));
        var expected = """
                ❬Conditional Display of Tables❘spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120}❭
                ❬This paragraph stays untouched.❘lang=de-DE❭
                                 
                ❬This table stays untouched.❘widowControl=xxx❭
                ❬❘widowControl=xxx❭
                ❬❘widowControl=xxx❭
                ❬❘widowControl=xxx❭
                                 
                ❬❬Also works on nested tables❘b=true❭❘b=true,widowControl=xxx❭
                ❬❘b=true,widowControl=xxx❭
                                 
                ❬❬This paragraph stays untouched.❘lang=de-DE❭❘spacing={after=140,afterLines=140,before=140,beforeLines=140,line=140,lineRule=140}❭""";
        return arguments("conditionalDisplayOfTablesBug32Test",
                         new DocxStamperConfiguration(),
                         context,
                         template,
                         expected);
    }

    private static Arguments conditionalDisplayOfTablesTest() {
        var context = new Contexts.Name("Homer");
        var template = getResource(Path.of("ConditionalDisplayOfTablesTest" +
                                           ".docx"));
        var expected = """
                ❬Conditional Display of Tables❘spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120}❭
                ❬❬This paragraph stays untouched.❘lang=de-DE❭❘lang=de-DE❭
                                                                                                                                                                                   
                This table stays untouched.
                                                                                                                                                                                   
                                                                                                                                                                                   
                                                                                                                                                                                   
                                                                                                                                                                                   
                ❬❬Also works on nested tables❘b=true❭❘b=true❭
                ❬❘b=true❭
                                                                                                                                                                                   
                ❬❬This paragraph stays untouched.❘lang=de-DE❭❘lang=de-DE,spacing={after=140,afterLines=140,before=140,beforeLines=140,line=140,lineRule=140}❭""";
        return arguments("conditionalDisplayOfTablesTest",
                         new DocxStamperConfiguration(),
                         context,
                         template,
                         expected);
    }

    private static Arguments customEvaluationContextConfigurerTest_customEvaluationContextConfigurerIsHonored() {
        var context = new Contexts.EmptyContext();
        var template = getResource(Path.of(
                "CustomEvaluationContextConfigurerTest.docx"));
        var expected = """
                ❬Custom EvaluationContextConfigurer Test❘spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120}❭
                ❬❬This paragraph stays untouched.❘lang=de-DE❭❘lang=de-DE❭
                ❬The variable foo has the value ❬bar❘lang=de-DE❭.❘spacing={after=140,afterLines=140,before=140,beforeLines=140,line=140,lineRule=140}❭""";
        var config = new DocxStamperConfiguration();
        config.setEvaluationContextConfigurer(evalContext -> evalContext.addPropertyAccessor(
                new SimpleGetter("foo", "bar")));

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
                ❬Custom Expression Function❘spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120}❭
                ❬❬This paragraph is untouched.❘lang=de-DE❭❘lang=de-DE❭
                ❬❬In this paragraph, a custom expression function is used to uppercase a String: |BR|❘lang=de-DE❭❬HOMER SIMPSON❘b=true,lang=de-DE❭❬.❘lang=de-DE❭❘spacing={after=140,afterLines=140,before=140,beforeLines=140,line=140,lineRule=140}❭
                ❬❬To test that custom functions work together with comment expressions, we toggle visibility of this paragraph with a comment expression.❘lang=de-DE❭❘spacing={after=140,afterLines=140,before=140,beforeLines=140,line=140,lineRule=140}❭""";
        var config = new DocxStamperConfiguration().exposeInterfaceToExpressionLanguage(
                Functions.UppercaseFunction.class,
                Functions.upperCase());
        return arguments("customExpressionFunctionTest",
                         config,
                         context,
                         template,
                         expected);
    }

    private static Arguments customTypeResolverTest() {
        return arguments("customTypeResolverTest",
                         new DocxStamperConfiguration()
                                 .addResolver(new CustomTypeResolver()),
                         new Context(new CustomType()),
                         getResource(Path.of("CustomTypeResolverTest.docx")),
                         """
                                 ❬Custom TypeResolver Test❘spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120}❭
                                 ❬This paragraph is untouched.❘lang=de-DE❭
                                 ❬The name should be resolved to ❘lang=de-DE❭❬foo❘b=true,lang=de-DE❭❬.❘lang=de-DE❭
                                 ❬❬This paragraph is untouched.❘lang=de-DE❭❘spacing={after=140,afterLines=140,before=140,beforeLines=140,line=140,lineRule=140}❭""");
    }

    private static Arguments dateReplacementTest() {
        var context = Contexts.nowContext();
        var template = getResource(Path.of("DateReplacementTest.docx"));
        var defaultFormat = new SimpleDateFormat("dd.MM.yyyy");
        var formattedDate = defaultFormat.format(context.date());
        var expected = """
                ❬Replacing date expressions❘spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120}❭
                Today is: %s""".formatted(formattedDate);

        return arguments("dateReplacementTest",
                         new DocxStamperConfiguration(),
                         context,
                         template,
                         expected);
    }

    private static Arguments expressionReplacementInGlobalParagraphsTest() {
        var context = new Contexts.Name("Homer Simpson");
        var template = getResource(
                Path.of("ExpressionReplacementInGlobalParagraphsTest.docx"));
        var expected = """
                ❬Expression Replacement in global paragraphs❘spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120}❭
                ❬❬This paragraph is untouched.❘lang=de-DE❭❘lang=de-DE❭
                ❬In this paragraph, the variable ❘lang=de-DE❭❬name❘b=true,lang=de-DE❭ should be resolved to the value ❬Homer Simpson❘lang=de-DE❭.
                ❬❬In this paragraph, the variable ❘lang=de-DE❭❬foo❘b=true,lang=de-DE❭❬ should not be resolved: ${foo}.❘lang=de-DE❭❘spacing={after=140,afterLines=140,before=140,beforeLines=140,line=140,lineRule=140}❭""";
        DocxStamperConfiguration config = new DocxStamperConfiguration().setFailOnUnresolvedExpression(
                false);
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
                ❬Expression Replacement in Tables❘spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120}❭
                This should resolve to a name:
                Bart Simpson
                This should not resolve:
                ${foo}
                ❬❬Nested Table:❘b=true❭❘b=true❭
                This should resolve to a name:
                Bart Simpson
                This should not resolve:
                ${foo}
                                                                                                                            
                ❬❘spacing={after=140,afterLines=140,before=140,beforeLines=140,line=140,lineRule=140}❭""";
        DocxStamperConfiguration config = new DocxStamperConfiguration().setFailOnUnresolvedExpression(
                false);
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
                ❬It should be with header formatting: ❬Homer Simpson❘rStyle=TitreCar❭❘b=true❭""";
        return arguments("expressionReplacementWithFormattingTest",
                         new DocxStamperConfiguration(),
                         context,
                         template,
                         expected);
    }

    private static Arguments expressionWithSurroundingSpacesTest() {
        var spacyContext = new Contexts.SpacyContext();
        var template = getResource(Path.of(
                "ExpressionWithSurroundingSpacesTest.docx"));
        var expected = """
                ❬Expression Replacement when expression has leading and/or trailing spaces❘spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120}❭
                When an expression within a paragraph is resolved, the spaces between the replacement and the surrounding text should be as expected. The following paragraphs should all look the same.
                Before Expression After.
                Before Expression After.
                Before Expression After.
                Before Expression After.
                Before Expression After.
                Before Expression After.
                ❬Before Expression After.❘spacing={after=140,afterLines=140,before=140,beforeLines=140,line=140,lineRule=140}❭""";
        return arguments("expressionWithSurroundingSpacesTest",
                         new DocxStamperConfiguration(),
                         spacyContext,
                         template,
                         expected);
    }

    private static Arguments expressionReplacementWithCommentsTest() {
        var context = new Contexts.Name("Homer Simpson");
        var template = getResource(Path.of(
                "ExpressionReplacementWithCommentsTest.docx"));
        var expected = """
                ❬Expression Replacement with comments❘spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120}❭
                This paragraph is untouched.
                In this paragraph, the variable ❬name❘b=true❭ should be resolved to the value Homer Simpson.
                ❬In this paragraph, the variable ❬foo❘b=true❭ should not be resolved: unresolvedValueWithCommentreplaceWordWith(foo).❘spacing={after=140,afterLines=140,before=140,beforeLines=140,line=140,lineRule=140}❭""";
        var config = new DocxStamperConfiguration().setFailOnUnresolvedExpression(
                false);
        return arguments("expressionReplacementWithCommentsTest",
                         config,
                         context,
                         template,
                         expected);
    }

    /**
     * <p>test.</p>
     */
    private static Arguments imageReplacementInGlobalParagraphsTest() {
        var context = new Contexts.ImageContext(getImage(Path.of("monalisa" +
                                                                 ".jpg")));
        var template = getResource(Path.of(
                "ImageReplacementInGlobalParagraphsTest.docx"));
        var expected = """
                ❬Image Replacement in global paragraphs❘spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120}❭
                ❬❬This paragraph is untouched.❘lang=de-DE❭❘lang=de-DE❭
                ❬In this paragraph, an image of Mona Lisa is inserted: ❬rId4:image/jpeg:8.8kB:sha1=XMpVtDbetKjZTkPhy598GdJQM/4=:cy=$d:1276350❘lang=de-DE❭.❘lang=de-DE❭
                ❬This paragraph has the image ❬rId5:image/jpeg:8.8kB:sha1=XMpVtDbetKjZTkPhy598GdJQM/4=:cy=$d:1276350❘lang=de-DE❭ in the middle.❘lang=de-DE,spacing={after=140,afterLines=140,before=140,beforeLines=140,line=140,lineRule=140}❭""";
        return arguments("imageReplacementInGlobalParagraphsTest",
                         new DocxStamperConfiguration(),
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
                ❬Image Replacement in global paragraphs❘spacing={after=120,afterLines=120,before=120,beforeLines=120,line=120,lineRule=120}❭
                ❬❬This paragraph is untouched.❘lang=de-DE❭❘lang=de-DE❭
                ❬In this paragraph, an image of Mona Lisa is inserted: ❬rId4:image/jpeg:8.8kB:sha1=XMpVtDbetKjZTkPhy598GdJQM/4=:cy=$d:635000❘lang=de-DE❭.❘lang=de-DE❭
                ❬This paragraph has the image ❬rId5:image/jpeg:8.8kB:sha1=XMpVtDbetKjZTkPhy598GdJQM/4=:cy=$d:635000❘lang=de-DE❭ in the middle.❘lang=de-DE,spacing={after=140,afterLines=140,before=140,beforeLines=140,line=140,lineRule=140}❭""";
        return arguments("imageReplacementInGlobalParagraphsTestWithMaxWidth",
                         new DocxStamperConfiguration(),
                         context,
                         template,
                         expected);
    }

    private static Image getImage(Path path, int size) {
        try {
            return new Image(getResource(path), size);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static Arguments leaveEmptyOnExpressionErrorTest() {
        var context = new Contexts.Name("Homer Simpson");
        var template = getResource(Path.of("LeaveEmptyOnExpressionErrorTest" +
                                           ".docx"));
        var expected = """
                Leave me empty .
                ❬❘u=single❭""";
        var config = new DocxStamperConfiguration().setFailOnUnresolvedExpression(
                        false)
                .leaveEmptyOnExpressionError(true);
        return arguments("leaveEmptyOnExpressionErrorTest",
                         config,
                         context,
                         template,
                         expected);
    }

    private static Arguments lineBreakReplacementTest() {
        var config = new DocxStamperConfiguration();
        config.setLineBreakPlaceholder("#");
        var context = new Contexts.Name(null);
        var template = getResource(Path.of("LineBreakReplacementTest.docx"));
        var expected = """
                ❬❬Line Break Replacement❘lang=en-US❭❘lang=en-US❭
                ❬❬This paragraph is untouched.❘lang=en-US❭❘lang=en-US❭
                ❬This paragraph should be ❬|BR|❘lang=en-US❭ split in ❬|BR|❘lang=en-US❭❬ three❘lang=en-US❭❬ lines.❘lang=en-US❭❘lang=en-US❭
                ❬❬This paragraph is untouched.❘lang=en-US❭❘lang=en-US❭""";
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

        var config = new DocxStamperConfiguration()
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

        var config = new DocxStamperConfiguration().setFailOnUnresolvedExpression(
                false);

        return arguments("nullPointerResolutionTest_testWithDefaultSpel",
                         config,
                         context,
                         template,
                         expected);
    }

    private static Arguments customCommentProcessor() {
        return arguments("customCommentProcessor",
                         new DocxStamperConfiguration().addCommentProcessor(
                                 ICustomCommentProcessor.class,
                                 CustomCommentProcessor::new),
                         Contexts.empty(),
                         getResource(Path.of("CustomCommentProcessorTest" +
                                             ".docx")),
                         """
                                 Custom CommentProcessor Test
                                 Visited.
                                 This paragraph is untouched.
                                 Visited.""");
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

        var config = new DocxStamperConfiguration()
                .setSpelParserConfiguration(
                        new SpelParserConfiguration(true, true))
                .setEvaluationContextConfigurer(new NoOpEvaluationContextConfigurer())
                .addResolver(Resolvers.nullToDefault("Nullish value!!"));

        return arguments("nullPointerResolutionTest_testWithCustomSpel",
                         config,
                         context,
                         template,
                         expected);
    }

    /**
     * <p>tests.</p>
     *
     * @return a {@link java.util.stream.Stream} object
     */
    public static Stream<Arguments> tests() {
        return Stream.of(tabulations(),
                         whitespaces(),
                         ternary(),
                         repeatingRows(),
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
                         conditionalDisplayOfTablesBug32Test(),
                         conditionalDisplayOfTablesTest(),
                         customEvaluationContextConfigurerTest_customEvaluationContextConfigurerIsHonored(),
                         customExpressionFunctionTest(),
                         customTypeResolverTest(),
                         dateReplacementTest(),
                         expressionReplacementInGlobalParagraphsTest(),
                         expressionReplacementInTablesTest(),
                         expressionReplacementWithFormattingTest(),
                         expressionWithSurroundingSpacesTest(),
                         expressionReplacementWithCommentsTest(),
                         imageReplacementInGlobalParagraphsTest(),
                         imageReplacementInGlobalParagraphsTestWithMaxWidth(),
                         leaveEmptyOnExpressionErrorTest(),
                         lineBreakReplacementTest(),
                         mapAccessorAndReflectivePropertyAccessorTest_shouldResolveMapAndPropertyPlaceholders(),
                         nullPointerResolutionTest_testWithDefaultSpel(),
                         nullPointerResolutionTest_testWithCustomSpel(),
                         customCommentProcessor());
    }

    @MethodSource("tests")
    @ParameterizedTest(name = "{0}")
    void features(
            String ignoredName,
            DocxStamperConfiguration config,
            Object context,
            InputStream template,
            String expected
    ) {
        var stamper = new TestDocxStamper<>(config);
        var actual = stamper.stampAndLoadAndExtract(template, context);
        assertEquals(expected, actual);
    }
}
