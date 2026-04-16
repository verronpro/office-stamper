package pro.verron.officestamper.imageio.emf;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.spi.IIORegistry;
import javax.imageio.stream.ImageInputStream;
import java.io.File;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/// Test class for validating the functionality of the EMF ImageIO reader.
/// This class includes tests to ensure that the reader correctly identifies EMF
/// image files, reads the format metadata, and verifies image dimensions against
/// expected values. It also ensures that the ImageInputStream is properly initialized and
/// integrates with the ImageIO framework as intended.
/// The tests use the \`EmfImageReaderSpi\` service provider to register the EMF reader
/// dynamically and validate its integration with the ImageIO API.
/// Key aspects tested include:
/// - Format detection of EMF images.
/// - Validation of non-null and valid dimensions.
/// - Comparison of actual dimensions with expected values.
@DisplayName("EMF ImageIO reader metadata tests") public class EmfImageReaderTest {

    @Test
    @DisplayName("sample-cat.emf: reader detects format and matches FreeHEP dimensions")
    void sampleCatEmf_metadataMatchesFreeHEP()
            throws Exception {
        Path emfPath = Path.of("..", "test", "sources", "sample-cat.emf");
        File emfFile = emfPath.toFile();
        assertTrue(emfFile.exists(), "Test EMF file not found: " + emfFile.getAbsolutePath());

        ImageIO.scanForPlugins();
        // Register the SPI programmatically to avoid relying on test classpath resources
        IIORegistry.getDefaultInstance()
                   .registerServiceProvider(new EmfImageReaderSpi());
        try (ImageInputStream iis = ImageIO.createImageInputStream(emfFile)) {
            assertNotNull(iis, "ImageInputStream is null");
            var imageReaders = ImageIO.getImageReaders(iis);
            ImageReader reader = imageReaders.next();
            reader.setInput(iis, false, true);
            assertEquals("emf", reader.getFormatName());
            int w = reader.getWidth(0);
            int h = reader.getHeight(0);
            reader.dispose();

            assertTrue(w > 0 && h > 0, "Non-positive dimensions returned");
            assertEquals(5716, w, "Width should match expectations");
            assertEquals(1511, h, "Height should match expectations");
        }
    }
}
