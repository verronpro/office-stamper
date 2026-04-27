package pro.verron.officestamper.imageio.emf;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Node;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
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
        /*IIORegistry.getDefaultInstance()
                   .registerServiceProvider(new EmfImageReaderSpi());*/
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

    @Test
    @Disabled("Disabled until metadata parsing is implemented")
    @DisplayName("sample-cat.emf: standard metadata contains Dimension with HorizontalPixelSize/VerticalPixelSize")
    void sampleCatEmf_standardMetadata_hasDimensionNode()
            throws Exception {
        Path emfPath = Path.of("..", "test", "sources", "sample-cat.emf");
        File emfFile = emfPath.toFile();
        assertTrue(emfFile.exists(), "Test EMF file not found: " + emfFile.getAbsolutePath());

        ImageIO.scanForPlugins();
        /*IIORegistry.getDefaultInstance()
                   .registerServiceProvider(new EmfImageReaderSpi());*/

        try (ImageInputStream iis = ImageIO.createImageInputStream(emfFile)) {
            assertNotNull(iis, "ImageInputStream is null");

            ImageReader reader = ImageIO.getImageReaders(iis)
                                        .next();
            reader.setInput(iis, false, true);

            // Sanity check: format and dimensions
            assertEquals("emf", reader.getFormatName());
            int expectedW = reader.getWidth(0);
            int expectedH = reader.getHeight(0);

            IIOMetadata metadata = reader.getImageMetadata(0);
            assertNotNull(metadata, "Image metadata must not be null");
            assertTrue(metadata.isStandardMetadataFormatSupported(),
                    "Standard metadata format should be supported");

            Node root = metadata.getAsTree("javax_imageio_1.0");
            assertNotNull(root, "Standard metadata tree should not be null");

            Node dimension = findChild(root, "Dimension");
            assertNotNull(dimension, "Standard metadata must contain a Dimension node");

            // According to the Java Image I/O standard metadata DTD, the Dimension node
            // contains HorizontalPixelSize/VerticalPixelSize (in millimeters per pixel),
            // not PixelWidth/PixelHeight counts.
            Node hps = findChild(dimension, "HorizontalPixelSize");
            Node vps = findChild(dimension, "VerticalPixelSize");
            assertNotNull(hps, "Dimension should contain HorizontalPixelSize child when physical size is known");
            assertNotNull(vps, "Dimension should contain VerticalPixelSize child when physical size is known");

            String hpsAttr = hps.getAttributes()
                                .getNamedItem("value")
                                .getNodeValue();
            String vpsAttr = vps.getAttributes()
                                .getNamedItem("value")
                                .getNodeValue();

            // Values are floats (millimeters per pixel). They should parse and be positive.
            assertDoesNotThrow(() -> Float.parseFloat(hpsAttr), "HorizontalPixelSize 'value' must be a float");
            assertDoesNotThrow(() -> Float.parseFloat(vpsAttr), "VerticalPixelSize 'value' must be a float");
            assertTrue(Float.parseFloat(hpsAttr) > 0f, "HorizontalPixelSize must be > 0");
            assertTrue(Float.parseFloat(vpsAttr) > 0f, "VerticalPixelSize must be > 0");

            // The standard metadata may also expose screen size in pixels.
            // Verify HorizontalScreenSize/VerticalScreenSize are present and
            // match the reader-reported pixel dimensions.
            Node hss = findChild(dimension, "HorizontalScreenSize");
            Node vss = findChild(dimension, "VerticalScreenSize");
            assertNotNull(hss, "Dimension should contain HorizontalScreenSize child when pixel dimensions are known");
            assertNotNull(vss, "Dimension should contain VerticalScreenSize child when pixel dimensions are known");

            String hssAttr = hss.getAttributes()
                                .getNamedItem("value")
                                .getNodeValue();
            String vssAttr = vss.getAttributes()
                                .getNamedItem("value")
                                .getNodeValue();
            assertDoesNotThrow(() -> Integer.parseInt(hssAttr), "HorizontalScreenSize 'value' must be an integer");
            assertDoesNotThrow(() -> Integer.parseInt(vssAttr), "VerticalScreenSize 'value' must be an integer");
            assertEquals(expectedW, Integer.parseInt(hssAttr),
                    "HorizontalScreenSize should match image width");
            assertEquals(expectedH, Integer.parseInt(vssAttr),
                    "VerticalScreenSize should match image height");

            reader.dispose();
        }
    }

    private static Node findChild(Node parent, String name) {
        for (Node n = parent.getFirstChild(); n != null; n = n.getNextSibling()) {
            if (name.equals(n.getNodeName())) {
                return n;
            }
        }
        return null;
    }
}
