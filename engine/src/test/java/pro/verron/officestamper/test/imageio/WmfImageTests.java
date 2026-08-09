package pro.verron.officestamper.test.imageio;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import pro.verron.officestamper.test.utils.ContextFactory;
import pro.verron.officestamper.test.utils.OfficeStamperTestBase;
import pro.verron.officestamper.test.utils.ResourceUtils;

import javax.imageio.spi.IIORegistry;
import javax.imageio.spi.ImageReaderSpi;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static pro.verron.officestamper.preset.OfficeStamperConfigurations.standard;


@DisplayName("WMF Image-related Features")
class WmfImageTests extends OfficeStamperTestBase {

    @BeforeAll
    static void setup() {
        List<ImageReaderSpi> providers = new ArrayList<>();
        IIORegistry.lookupProviders(ImageReaderSpi.class)
                .forEachRemaining(providers::add);
        System.out.println(providers);
    }

    @MethodSource("factories")
    @ParameterizedTest(name = "Wmf Image Replacement in global paragraphs with max width")
    void wmfReplacementInGlobalParagraphsTestWithMaxWidth(ContextFactory factory) {
        var configuration = standard();
        var context = factory.image(ResourceUtils.getImage(Path.of("sample.wmf"), 100));
        var template = ResourceUtils.getWordResource(Path.of("ImageReplacementInGlobalParagraphsTest.docx"));
        var expected = """
                == Image Replacement in global paragraphs
                
                This paragraph is untouched.
                
                In this paragraph, an image of Mona Lisa is inserted: image:rId7[cx=63500, cy=53521].
                
                This paragraph has the image image:rId7[cx=63500, cy=53521] in the middle.
                
                // section {docGrid={charSpace=-6145, linePitch=240}, pgMar={bottom=1134, left=1134, right=1134, top=1134}, pgSz={h=16838, w=11906}, space=720}
                
                """;
        testStamper(configuration, context, template, expected);
    }
}
