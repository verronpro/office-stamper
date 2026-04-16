package pro.verron.officestamper.imageio.svg;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.spi.IIORegistry;
import javax.imageio.stream.ImageInputStream;
import java.io.File;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/// Unit tests for verifying the functionality of the `SvgImageReader` implementation.
/// These tests focus on reading SVG format metadata using `ImageIO` and ensuring that
/// the reader correctly identifies the format and extracts the width and height attributes
/// from the SVG file.
@DisplayName("SVG ImageIO reader metadata tests") public class SvgImageReaderTest {

    @Test
    @DisplayName("sample-cat.svg: reader detects format and extracts width/height attributes")
    void sampleSvg_dimensionsFromAttributes()
            throws Exception {
        Path path = Path.of("..", "test", "sources", "sample-circle.svg");
        File file = path.toFile();
        assertTrue(file.exists(), "Test SVG file not found: " + file.getAbsolutePath());

        ImageIO.scanForPlugins();
        IIORegistry.getDefaultInstance()
                   .registerServiceProvider(new SvgImageReaderSpi());
        try (ImageInputStream iis = ImageIO.createImageInputStream(file)) {
            assertNotNull(iis, "ImageInputStream is null");
            ImageReader reader = ImageIO.getImageReaders(iis)
                                        .next();
            reader.setInput(iis, false, true);
            assertEquals("svg", reader.getFormatName());
            int w = reader.getWidth(0);
            int h = reader.getHeight(0);
            reader.dispose();

            assertEquals(100, w);
            assertEquals(100, h);
        }
    }
}
