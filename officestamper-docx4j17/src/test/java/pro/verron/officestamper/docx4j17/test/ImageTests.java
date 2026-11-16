package pro.verron.officestamper.docx4j17.test;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import pro.verron.officestamper.docx4j17.test.utils.ContextFactory;
import pro.verron.officestamper.docx4j17.test.utils.OfficeStamperTestBase;

import java.nio.file.Path;

import static pro.verron.officestamper.docx4j17.DocxOfficeStampers.standard;
import static pro.verron.officestamper.docx4j17.test.utils.ResourceUtils.getImage;
import static pro.verron.officestamper.docx4j17.test.utils.ResourceUtils.getWordResource;

@DisplayName("Image-related Features") class ImageTests
        extends OfficeStamperTestBase {

    @MethodSource("factories")
    @ParameterizedTest(name = "Gif Image Replacement in global paragraphs with max width")
    void gifReplacementInGlobalParagraphsTestWithMaxWidth(ContextFactory factory) {
        var configuration = standard();
        var context = factory.image(getImage(Path.of("sample-1.gif"), 100));
        var template = getWordResource(Path.of("ImageReplacementInGlobalParagraphsTest.docx"));
        var expected = """
                == Image Replacement in global paragraphs
                
                This paragraph is untouched.
                
                In this paragraph, an image of Mona Lisa is inserted: image:rId7[cx=63500, cy=64537].
                
                This paragraph has the image image:rId7[cx=63500, cy=64537] in the middle.
                
                """;
        testStamper(configuration, context, template, expected);
    }

    @MethodSource("factories")
    @ParameterizedTest(name = "Bmp Image Replacement in global paragraphs with max width")
    void bmpReplacementInGlobalParagraphsTestWithMaxWidth(ContextFactory factory) {
        var configuration = standard();
        var context = factory.image(getImage(Path.of("sample-1mb.bmp"), 100));
        var template = getWordResource(Path.of("ImageReplacementInGlobalParagraphsTest.docx"));
        var expected = """
                == Image Replacement in global paragraphs
                
                This paragraph is untouched.
                
                In this paragraph, an image of Mona Lisa is inserted: image:rId7[cx=63500, cy=63500].
                
                This paragraph has the image image:rId7[cx=63500, cy=63500] in the middle.
                
                """;
        testStamper(configuration, context, template, expected);
    }

    @MethodSource("factories")
    @ParameterizedTest(name = "JPG Image Replacement in global paragraphs with max width")
    void jpgImageReplacementInGlobalParagraphsTestWithMaxWidth(ContextFactory factory) {
        var configuration = standard();
        var context = factory.image(getImage(Path.of("sample-monalisa-50x50.jpg"), 100));
        var template = getWordResource(Path.of("ImageReplacementInGlobalParagraphsTest.docx"));
        var expected = """
                == Image Replacement in global paragraphs
                
                This paragraph is untouched.
                
                In this paragraph, an image of Mona Lisa is inserted: image:rId7[cx=63500, cy=63500].
                
                This paragraph has the image image:rId7[cx=63500, cy=63500] in the middle.
                
                """;
        testStamper(configuration, context, template, expected);
    }

    @MethodSource("factories")
    @ParameterizedTest(name = "Image Replacement in global paragraphs")
    void jpgImageReplacementInGlobalParagraphsTest(ContextFactory factory) {
        var configuration = standard();
        var context = factory.image(getImage(Path.of("sample-monalisa-50x50.jpg")));
        var template = getWordResource(Path.of("ImageReplacementInGlobalParagraphsTest.docx"));
        var expected = """
                == Image Replacement in global paragraphs
                
                This paragraph is untouched.
                
                In this paragraph, an image of Mona Lisa is inserted: image:rId7[cx=476250, cy=476250].
                
                This paragraph has the image image:rId7[cx=476250, cy=476250] in the middle.
                
                """;
        testStamper(configuration, context, template, expected);
    }

    @MethodSource("factories")
    @ParameterizedTest(name = "TIFF Image Replacement in global paragraphs")
    void tiffImageReplacementInGlobalParagraphsTest(ContextFactory factory) {
        var configuration = standard();
        var context = factory.image(getImage(Path.of("sample-monalisa-50x50.tiff")));
        var template = getWordResource(Path.of("ImageReplacementInGlobalParagraphsTest.docx"));
        var expected = """
                == Image Replacement in global paragraphs
                
                This paragraph is untouched.
                
                In this paragraph, an image of Mona Lisa is inserted: image:rId7[cx=476250, cy=476250].
                
                This paragraph has the image image:rId7[cx=476250, cy=476250] in the middle.
                
                """;
        testStamper(configuration, context, template, expected);
    }
}


