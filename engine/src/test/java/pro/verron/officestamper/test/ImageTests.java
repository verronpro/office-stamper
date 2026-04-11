package pro.verron.officestamper.test;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.expression.spel.SpelParserConfiguration;
import pro.verron.officestamper.preset.Resolvers;
import pro.verron.officestamper.test.utils.ContextFactory;
import pro.verron.officestamper.test.utils.DocxFactory;
import pro.verron.officestamper.test.utils.ObjectContextFactory;

import java.nio.file.Path;

import static pro.verron.officestamper.preset.EvaluationContextFactories.noopFactory;
import static pro.verron.officestamper.preset.OfficeStamperConfigurations.standard;
import static pro.verron.officestamper.test.utils.ResourceUtils.getImage;
import static pro.verron.officestamper.test.utils.ResourceUtils.getWordResource;

@DisplayName("Image-related Features") class ImageTests
        extends OfficeStamperTest {

    @MethodSource("factories")
    @ParameterizedTest(name = "Null Pointer Resolution with Custom SpEL Parser Configuration")
    void nullPointerResolutionTest_testWithCustomSpel() {
        var contextFactory = new ObjectContextFactory();
        // Beware, this configuration only autogrows pojos and java beans,
        // so it will not work if your type has no default constructor and no setters.
        // It does not work with arrays, lists, maps, etc. since it cannot guess the types to auto grow at runtime
        var parserConfiguration = new SpelParserConfiguration(true, true);
        var configuration = standard().setParserConfiguration(parserConfiguration)
                                      .setEvaluationContextFactory(noopFactory())
                                      .addResolver(Resolvers.nullToDefault("Nullish value!!"));
        var context = contextFactory.nullishContext();
        var template = DocxFactory.makeWordResource("""
                Deal with null references
                
                
                Deal with: ${fullish_value ?: "Fullish value?!"}
                
                Deal with: ${fullish.value ?: "Fullish value?!"}
                
                Deal with: ${fullish.li[0] ?: "Fullish value?!"}
                
                Deal with: ${fullish.li[2] ?: "Fullish value?!"}
                
                
                Deal with: ${nullish_value ?: "Nullish value!!"}
                
                Deal with: ${nullish.value ?: "Nullish value!!"}
                
                Deal with: ${nullish.li[0] ?: "Nullish value!!"}
                
                Deal with: ${nullish.li[2] ?: "Nullish value!!"}
                
                """);
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
                
                // section {pgMar={bottom=1440, left=1440, right=1440, top=1440}, pgSz={code=9, h=16839, w=11907}}
                
                """;
        testStamper(configuration, context, template, expected);
    }

    @MethodSource("factories")
    @ParameterizedTest(name = "SVG Image Replacement in global paragraphs with max width")
    void svgReplacementInGlobalParagraphsTestWithMaxWidth(ContextFactory factory) {
        testStamper(standard(),
                factory.image(getImage(Path.of("sample-circle.svg"), 100)),
                getWordResource(Path.of("ImageReplacementInGlobalParagraphsTest.docx")),
                """
                        == Image Replacement in global paragraphs
                        
                        This paragraph is untouched.
                        
                        In this paragraph, an image of Mona Lisa is inserted: image:[cx=63500, cy=63500].
                        
                        This paragraph has the image image:[cx=63500, cy=63500] in the middle.
                        
                        // section {docGrid={charSpace=-6145, linePitch=240}, pgMar={bottom=1134, left=1134, right=1134, top=1134}, pgSz={h=16838, w=11906}, space=720}
                        
                        """);
    }

    @MethodSource("factories")
    @ParameterizedTest(name = "SVG Image Replacement in global paragraphs")
    void svgReplacementInGlobalParagraphsTest(ContextFactory factory) {
        testStamper(standard(),
                factory.image(getImage(Path.of("sample-circle.svg"))),
                getWordResource(Path.of("ImageReplacementInGlobalParagraphsTest.docx")),
                """
                        == Image Replacement in global paragraphs
                        
                        This paragraph is untouched.
                        
                        In this paragraph, an image of Mona Lisa is inserted: image:[cx=952500, cy=952500].
                        
                        This paragraph has the image image:[cx=952500, cy=952500] in the middle.
                        
                        // section {docGrid={charSpace=-6145, linePitch=240}, pgMar={bottom=1134, left=1134, right=1134, top=1134}, pgSz={h=16838, w=11906}, space=720}
                        
                        """);
    }

    @MethodSource("factories")
    @ParameterizedTest(name = "Image Replacement in global paragraphs with max width")
    void imageReplacementInGlobalParagraphsTestWithMaxWidth(ContextFactory factory) {
        testStamper(standard(),
                factory.image(getImage(Path.of("sample-monalisa-50x50.jpg"), 100)),
                getWordResource(Path.of("ImageReplacementInGlobalParagraphsTest.docx")),
                """
                        == Image Replacement in global paragraphs
                        
                        This paragraph is untouched.
                        
                        In this paragraph, an image of Mona Lisa is inserted: image:rId6[cx=63500, cy=63500].
                        
                        This paragraph has the image image:rId7[cx=63500, cy=63500] in the middle.
                        
                        // section {docGrid={charSpace=-6145, linePitch=240}, pgMar={bottom=1134, left=1134, right=1134, top=1134}, pgSz={h=16838, w=11906}, space=720}
                        
                        """);
    }

    @MethodSource("factories")
    @ParameterizedTest(name = "Image Replacement in global paragraphs")
    void imageReplacementInGlobalParagraphsTest(ContextFactory factory) {
        testStamper(standard(),
                factory.image(getImage(Path.of("sample-monalisa-50x50.jpg"))),
                getWordResource(Path.of("ImageReplacementInGlobalParagraphsTest.docx")),
                """
                        == Image Replacement in global paragraphs
                        
                        This paragraph is untouched.
                        
                        In this paragraph, an image of Mona Lisa is inserted: image:rId6[cx=476250, cy=476250].
                        
                        This paragraph has the image image:rId7[cx=476250, cy=476250] in the middle.
                        
                        // section {docGrid={charSpace=-6145, linePitch=240}, pgMar={bottom=1134, left=1134, right=1134, top=1134}, pgSz={h=16838, w=11906}, space=720}
                        
                        """);
    }
}
