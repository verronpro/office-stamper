package pro.verron.officestamper.imageio.emf;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import pro.verron.officestamper.imageio.emf.utils.ContextFactory;
import pro.verron.officestamper.imageio.emf.utils.OfficeStamperTest;

import java.nio.file.Path;

import static pro.verron.officestamper.imageio.emf.utils.ResourceUtils.getImage;
import static pro.verron.officestamper.imageio.emf.utils.ResourceUtils.getWordResource;
import static pro.verron.officestamper.preset.OfficeStamperConfigurations.standard;

@DisplayName("Image-related Features") class ImageTests
        extends OfficeStamperTest {

    @MethodSource("factories")
    @ParameterizedTest(name = "Emf Image Replacement in global paragraphs with max width")
    void emfReplacementInGlobalParagraphsTestWithMaxWidth(ContextFactory factory) {
        var configuration = standard();
        var context = factory.image(getImage(Path.of("sample-cat.emf"), 1000));
        var template = getWordResource(Path.of("ImageReplacementInGlobalParagraphsTest.docx"));
        var expected = """
                == Image Replacement in global paragraphs
                
                This paragraph is untouched.
                
                In this paragraph, an image of Mona Lisa is inserted: image:rId7[cx=635000, cy=167860].
                
                This paragraph has the image image:rId7[cx=635000, cy=167860] in the middle.
                
                // section {docGrid={charSpace=-6145, linePitch=240}, pgMar={bottom=1134, left=1134, right=1134, top=1134}, pgSz={h=16838, w=11906}, space=720}
                
                """;
        testStamper(configuration, context, template, expected);
    }
}
