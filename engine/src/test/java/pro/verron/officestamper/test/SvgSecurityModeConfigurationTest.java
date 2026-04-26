package pro.verron.officestamper.test;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pro.verron.officestamper.api.OfficeStamperException;
import pro.verron.officestamper.api.SecurityMode;
import pro.verron.officestamper.preset.Image;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static pro.verron.officestamper.preset.OfficeStamperConfigurations.standard;
import static pro.verron.officestamper.preset.OfficeStampers.docxPackageStamper;
import static pro.verron.officestamper.test.utils.ContextFactory.objectContextFactory;
import static pro.verron.officestamper.test.utils.ResourceUtils.getWordResource;

/// Black-box tests verifying SVG security mode behavior by stamping a document
/// with an SVG payload that includes an internal DOCTYPE.
///
/// - In RESTRICTED mode, stamping must fail (parser rejects DOCTYPE).
/// - In PERMISSIVE mode, stamping must succeed (parser accepts DOCTYPE).
class SvgSecurityModeConfigurationTest {
    private String originalSvgModeProp;

    @BeforeEach
    void resetSystemProperty() {
        originalSvgModeProp = System.getProperty("officestamper.svg.mode");
        // Ensure presets don't override our explicit configuration
        System.clearProperty("officestamper.svg.mode");
    }

    @AfterEach
    void restoreSystemProperty() {
        if (originalSvgModeProp == null) System.clearProperty("officestamper.svg.mode");
        else System.setProperty("officestamper.svg.mode", originalSvgModeProp);
    }

    @Test
    @DisplayName("RESTRICTED mode: stamping fails for SVG containing internal DOCTYPE")
    void restrictedMode_blocksInternalDoctypeSvg() {
        var cfg = standard().setSvgSecurityMode(SecurityMode.RESTRICTED);
        var stamper = docxPackageStamper(cfg);
        var template = getWordResource("ImageReplacementInGlobalParagraphsTest.docx");
        var context = objectContextFactory().image(new Image(svgWithInternalDoctype()));

        assertThrows(OfficeStamperException.class, () -> stamper.stamp(template, context));
    }

    private static byte[] svgWithInternalDoctype() {
        var svg = """
                <!DOCTYPE svg [<!ELEMENT svg ANY>]>
                <svg xmlns="http://www.w3.org/2000/svg" width="10" height="10"></svg>
                """;
        return svg.getBytes();
    }

    @Test
    @DisplayName("PERMISSIVE mode: stamping succeeds for SVG containing internal DOCTYPE")
    void permissiveMode_allowsInternalDoctypeSvg() {
        var cfg = standard().setSvgSecurityMode(SecurityMode.PERMISSIVE);
        var stamper = docxPackageStamper(cfg);
        var template = getWordResource("ImageReplacementInGlobalParagraphsTest.docx");
        var context = objectContextFactory().image(new Image(svgWithInternalDoctype()));

        assertDoesNotThrow(() -> stamper.stamp(template, context));
    }
}
