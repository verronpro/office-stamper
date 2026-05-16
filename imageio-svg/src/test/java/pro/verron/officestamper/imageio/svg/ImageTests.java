package pro.verron.officestamper.imageio.svg;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import pro.verron.officestamper.imageio.svg.utils.ContextFactory;
import pro.verron.officestamper.imageio.svg.utils.OfficeStamperTest;

import java.nio.file.Path;

import static pro.verron.officestamper.imageio.svg.utils.ResourceUtils.getImage;
import static pro.verron.officestamper.imageio.svg.utils.ResourceUtils.getWordResource;
import static pro.verron.officestamper.preset.OfficeStamperConfigurations.standard;


@DisplayName("Image-related Features") class ImageTests
        extends OfficeStamperTest {

    @MethodSource("factories")
    @ParameterizedTest(name = "SVG Image Replacement in global paragraphs with max width")
    void svgReplacementInGlobalParagraphsTestWithMaxWidth(ContextFactory factory) {
        var configuration = standard();
        var context = factory.image(getImage(Path.of("sample-circle.svg"), 100));
        var template = getWordResource(Path.of("ImageReplacementInGlobalParagraphsTest.docx"));
        var expected = """
                == Image Replacement in global paragraphs
                
                This paragraph is untouched.
                
                In this paragraph, an image of Mona Lisa is inserted: image:rId7[cx=63500, cy=63500].
                
                This paragraph has the image image:rId7[cx=63500, cy=63500] in the middle.
                
                // section {docGrid={charSpace=-6145, linePitch=240}, pgMar={bottom=1134, left=1134, right=1134, top=1134}, pgSz={h=16838, w=11906}, space=720}
                
                """;
        testStamper(configuration, context, template, expected);
    }

    @MethodSource("factories")
    @ParameterizedTest(name = "SVG Image Replacement in global paragraphs")
    void svgReplacementInGlobalParagraphsTest(ContextFactory factory) {
        var configuration = standard();
        var context = factory.image(getImage(Path.of("sample-circle.svg")));
        var template = getWordResource(Path.of("ImageReplacementInGlobalParagraphsTest.docx"));
        var expected = """
                == Image Replacement in global paragraphs
                
                This paragraph is untouched.
                
                In this paragraph, an image of Mona Lisa is inserted: image:rId7[cx=952500, cy=952500].
                
                This paragraph has the image image:rId7[cx=952500, cy=952500] in the middle.
                
                // section {docGrid={charSpace=-6145, linePitch=240}, pgMar={bottom=1134, left=1134, right=1134, top=1134}, pgSz={h=16838, w=11906}, space=720}
                
                """;
        testStamper(configuration, context, template, expected);
    }
}
