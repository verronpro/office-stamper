package pro.verron.officestamper.test.experimental;
import pro.verron.officestamper.test.Flavor;

import org.docx4j.dml.CTBlipFillProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.pptx4j.pml.Shape;
import pro.verron.officestamper.preset.Image;
import pro.verron.officestamper.test.utils.ResourceUtils;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.docx4j.openpackaging.packages.PresentationMLPackage;

@DisplayName("PPTX Image Stamping Test") class PptxImageTest {

    @Test
    @DisplayName("Should replace placeholder with image fill in PPTX")
    void testImageStamping()
            throws java.io.IOException {
        var stamper = Flavor.get().pptxPackageStamper();
        var template = ResourceUtils.getPowerPointResource(Path.of(
                "powerpoint-base.pptx"));

        var imagePath = Path.of("..",
                                    "src",
                                    "test",
                                    "resources",
                                    "sample-monalisa-20x20.png")
                            .normalize();
        var image = new Image(java.nio.file.Files.readAllBytes(imagePath));

        record Context(Image name) {}
        var context = new Context(image);

        var stamped = stamper.stamp(template, context);

        // 1. Check that the placeholder text is gone
        var actualText = Flavor.get().pptxToString(stamped.unwrap(PresentationMLPackage.class));
        assertFalse(actualText.contains("${name}"),
                "Placeholder should be gone");
        assertFalse(actualText.contains("Bart"),
                "Placeholder should not be replaced by string");

        // 2. Check that at least one shape has a blipFill
        List<Shape> shapes = Flavor.get().collectShapes(stamped, Shape.class);
        boolean foundImageFill = false;
        for (Shape shape : shapes) {
            if (shape.getSpPr() != null && shape.getSpPr()
                                                .getBlipFill() != null) {
                CTBlipFillProperties blipFill = shape.getSpPr()
                                                     .getBlipFill();
                if (blipFill.getBlip() != null && blipFill.getBlip()
                                                          .getEmbed() != null) {
                    foundImageFill = true;
                    break;
                }
            }
        }
        assertTrue(foundImageFill, "Should have found a shape with image fill");
    }
}


