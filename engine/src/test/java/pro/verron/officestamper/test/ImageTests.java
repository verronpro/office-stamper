package pro.verron.officestamper.test;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.expression.spel.SpelParserConfiguration;
import pro.verron.officestamper.api.OfficeStamperConfiguration;
import pro.verron.officestamper.preset.Resolvers;
import pro.verron.officestamper.test.utils.ContextFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.ArgumentSet;
import static org.junit.jupiter.params.provider.Arguments.argumentSet;
import static pro.verron.officestamper.asciidoc.AsciiDocCompiler.toAsciidoc;
import static pro.verron.officestamper.preset.EvaluationContextFactories.noopFactory;
import static pro.verron.officestamper.preset.OfficeStamperConfigurations.standard;
import static pro.verron.officestamper.preset.OfficeStampers.docxPackageStamper;
import static pro.verron.officestamper.test.utils.ContextFactory.mapContextFactory;
import static pro.verron.officestamper.test.utils.ContextFactory.objectContextFactory;
import static pro.verron.officestamper.test.utils.ResourceUtils.getImage;
import static pro.verron.officestamper.test.utils.ResourceUtils.getWordResource;

@DisplayName("Image-related Features") class ImageTests {

    private static final Logger log = LoggerFactory.getLogger(ImageTests.class);

    private static Stream<ArgumentSet> tests() {
        return factories().mapMulti((factory, pipe) -> {
            pipe.accept(imageReplacementInGlobalParagraphsTest(factory));
            pipe.accept(imageReplacementInGlobalParagraphsTestWithMaxWidth(factory));
            pipe.accept(svgReplacementInGlobalParagraphsTest(factory));
            pipe.accept(svgReplacementInGlobalParagraphsTestWithMaxWidth(factory));
        });
    }

    private static Stream<ContextFactory> factories() {
        return Stream.of(objectContextFactory(), mapContextFactory());
    }

    private static ArgumentSet imageReplacementInGlobalParagraphsTest(ContextFactory factory) {
        return argumentSet("Image Replacement in global paragraphs",
                standard(),
                factory.image(getImage(Path.of("monalisa.jpg"))),
                getWordResource(Path.of("ImageReplacementInGlobalParagraphsTest.docx")),
                """
                        == Image Replacement in global paragraphs
                        
                        This paragraph is untouched.
                        
                        In this paragraph, an image of Mona Lisa is inserted: image:rId6[cx=1276350, cy=962025].
                        
                        This paragraph has the image image:rId7[cx=1276350, cy=962025] in the middle.
                        
                        // section {docGrid={charSpace=-6145, linePitch=240}, pgMar={bottom=1134, left=1134, right=1134, top=1134}, pgSz={h=16838, w=11906}, space=720}
                        
                        """);
    }

    private static ArgumentSet imageReplacementInGlobalParagraphsTestWithMaxWidth(ContextFactory factory) {
        return argumentSet("Image Replacement in global paragraphs with max width",
                standard(),
                factory.image(getImage(Path.of("monalisa.jpg"), 1000)),
                getWordResource(Path.of("ImageReplacementInGlobalParagraphsTest.docx")),
                """
                        == Image Replacement in global paragraphs
                        
                        This paragraph is untouched.
                        
                        In this paragraph, an image of Mona Lisa is inserted: image:rId6[cx=635000, cy=478619].
                        
                        This paragraph has the image image:rId7[cx=635000, cy=478619] in the middle.
                        
                        // section {docGrid={charSpace=-6145, linePitch=240}, pgMar={bottom=1134, left=1134, right=1134, top=1134}, pgSz={h=16838, w=11906}, space=720}
                        
                        """);
    }

    private static ArgumentSet svgReplacementInGlobalParagraphsTest(ContextFactory factory) {
        return argumentSet("SVG Image Replacement in global paragraphs",
                standard(),
                factory.image(getImage(Path.of("circle.svg"))),
                getWordResource(Path.of("ImageReplacementInGlobalParagraphsTest.docx")),
                """
                        == Image Replacement in global paragraphs
                        
                        This paragraph is untouched.
                        
                        In this paragraph, an image of Mona Lisa is inserted: image:[cx=952500, cy=952500].
                        
                        This paragraph has the image image:[cx=952500, cy=952500] in the middle.
                        
                        // section {docGrid={charSpace=-6145, linePitch=240}, pgMar={bottom=1134, left=1134, right=1134, top=1134}, pgSz={h=16838, w=11906}, space=720}
                        
                        """);
    }

    private static ArgumentSet svgReplacementInGlobalParagraphsTestWithMaxWidth(ContextFactory factory) {
        return argumentSet("SVG Image Replacement in global paragraphs with max width",
                standard(),
                factory.image(getImage(Path.of("circle.svg"), 100)),
                getWordResource(Path.of("ImageReplacementInGlobalParagraphsTest.docx")),
                """
                        == Image Replacement in global paragraphs
                        
                        This paragraph is untouched.
                        
                        In this paragraph, an image of Mona Lisa is inserted: image:[cx=63500, cy=63500].
                        
                        This paragraph has the image image:[cx=63500, cy=63500] in the middle.
                        
                        // section {docGrid={charSpace=-6145, linePitch=240}, pgMar={bottom=1134, left=1134, right=1134, top=1134}, pgSz={h=16838, w=11906}, space=720}
                        
                        """);
    }

    private static ArgumentSet nullPointerResolutionTest_testWithCustomSpel(ContextFactory factory) {
        // Beware, this configuration only autogrows pojos and java beans,
        // so it will not work if your type has no default constructor and no setters.
        var parserConfiguration = new SpelParserConfiguration(true, true);
        return argumentSet("Null Pointer Resolution with Custom SpEL Configuration",
                standard().setParserConfiguration(parserConfiguration)
                          .setEvaluationContextFactory(noopFactory())
                          .addResolver(Resolvers.nullToDefault("Nullish value!!")),
                factory.nullishContext(),
                getWordResource(Path.of("NullPointerResolution.docx")),
                """
                        Deal with null references
                        
                        
                        
                        Deal with: Fullish1
                        
                        Deal with: Fullish2
                        
                        Deal with: Fullish3
                        
                        Deal with: Fullish5
                        
                        
                        
                        Deal with: Nullish value!!
                        
                        Deal with: Nullish value!!
                        
                        Deal with: Nullish value!!
                        
                        Deal with: Nullish value!!
                        
                        
                        
                        // section {pgMar={bottom=1134, left=1134, right=1134, top=1134}, pgSz={h=16838, w=11906}, space=720}
                        
                        """);
    }

    @MethodSource("tests")
    @DisplayName("Core Features")
    @ParameterizedTest(name = "Core Features: {argumentSetName}")
    void features(OfficeStamperConfiguration config, Object context, WordprocessingMLPackage template, String expected)
            throws IOException, Docx4JException {
        var stamper = docxPackageStamper(config);
        var wordprocessingMLPackage = stamper.stamp(template, context);
        writeOutputFile(wordprocessingMLPackage);
        var actual = toAsciidoc(wordprocessingMLPackage);
        assertEquals(expected.replace("\r\n", "\n"), actual.replace("\r\n", "\n"));
    }

    private static void writeOutputFile(WordprocessingMLPackage wordprocessingMLPackage)
            throws IOException, Docx4JException {
        if (!Boolean.parseBoolean(System.getProperty("keepOutputFile"))) return;
        var tempFile = Files.createTempFile("stamper", ".docx");
        log.info("Write to {}", tempFile.toString());
        wordprocessingMLPackage.save(tempFile.toFile());
    }
}
