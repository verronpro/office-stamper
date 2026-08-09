package pro.verron.officestamper.test;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.pptx4j.pml.Shape;
import pro.verron.officestamper.preset.Image;
import pro.verron.officestamper.test.utils.ResourceUtils;
import pro.verron.officestamper.utils.pml.PowerpointCollector;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static pro.verron.officestamper.experimental.ExperimentalStampers.pptxPackageStamper;
import static pro.verron.officestamper.utils.pml.PptxRenderer.pptxToString;

@DisplayName("PPTX Image Stamping Test")
class PptxImageTest {

    @Test
    @DisplayName("Should replace placeholder with image fill in PPTX")
    void testImageStamping() throws java.io.IOException {
        var stamper = pptxPackageStamper();
        var template = ResourceUtils.getPowerPointResource(Path.of("powerpoint-base.pptx"));

        var imagePath = Path.of("..", "src", "test", "resources", "sample-monalisa-20x20.png").normalize();
        var image = new Image(java.nio.file.Files.readAllBytes(imagePath));

        record Context(Image name) {}
        var context = new Context(image);

        var stamped = stamper.stamp(template, context);

        // 1. Check that the placeholder text is gone
        var actualText = pptxToString(stamped.getPackage());
        assertFalse(actualText.contains("${name}"), "Placeholder should be gone");
        assertFalse(actualText.contains("Bart"), "Placeholder should not be replaced by string");

        // 2. Check that at least one shape has a blipFill
        List<Shape> shapes = PowerpointCollector.collect(stamped.getPackage(), Shape.class);
        boolean foundImageFill = false;
        for (Shape shape : shapes) {
            var shapeProperties = shape.getSpPr();
            if (shapeProperties == null) continue;
            var blipFill = shapeProperties.getBlipFill();
            if (blipFill == null) continue;
            var blip = blipFill.getBlip();
            if (blip == null) continue;
            if (blip.getEmbed() == null) continue;
            foundImageFill = true;
            break;
        }
        assertTrue(foundImageFill, "Should have found a shape with image fill");
    }
}
