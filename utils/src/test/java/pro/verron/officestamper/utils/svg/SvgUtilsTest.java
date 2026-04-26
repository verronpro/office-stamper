package pro.verron.officestamper.utils.svg;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pro.verron.officestamper.utils.UtilsException;

import static org.junit.jupiter.api.Assertions.*;

class SvgUtilsTest {

    @Test
    @DisplayName("Parses a minimal, benign SVG successfully")
    void parsesMinimalSvg() {
        var svg = """
                <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 10 10">\
                  <rect width="10" height="10"/>\
                </svg>\
                """;
        var doc = SvgUtils.parseDocument(svg.getBytes());
        assertNotNull(doc);
        var documentElement = doc.getDocumentElement();
        assertEquals("svg", documentElement.getLocalName());
        assertEquals("http://www.w3.org/2000/svg", documentElement.getNamespaceURI());
    }

    @Test
    @DisplayName("Rejects SVG containing DOCTYPE (disallow-doctype-decl)")
    void rejectsDoctype() {
        var svg = """
                <!DOCTYPE svg PUBLIC "-//W3C//DTD SVG 1.1//EN" "http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd">\
                <svg xmlns="http://www.w3.org/2000/svg">\
                </svg>\
                """;
        assertThrows(UtilsException.class, () -> SvgUtils.parseDocument(svg.getBytes()));
    }

    @Test
    @DisplayName("Rejects SVG attempting XXE via external entity")
    void rejectsExternalEntityXXE() {
        var svg = """
                <!DOCTYPE svg [<!ENTITY xxe SYSTEM 'file:///etc/passwd'>]>\
                <svg xmlns="http://www.w3.org/2000/svg">\
                  &xxe;\
                </svg>\
                """;
        assertThrows(UtilsException.class, () -> SvgUtils.parseDocument(svg.getBytes()));
    }

    @Test
    @DisplayName("Rejects SVG with entity expansion (billion laughs)")
    void rejectsBillionLaughsLikePayload() {
        var svg = """
                <!DOCTYPE svg [<!ENTITY a 'LOL'><!ENTITY b '&a;&a;&a;&a;&a;&a;&a;&a;&a;&a;'>]>\
                <svg xmlns="http://www.w3.org/2000/svg">\
                  &b;\
                </svg>\
                """;
        assertThrows(UtilsException.class, () -> SvgUtils.parseDocument(svg.getBytes()));
    }

    @Test
    @DisplayName("When safe mode is disabled, internal DOCTYPE is accepted")
    void parsesInternalDoctypeWhenUnsafe() {
        var original = SvgUtils.isRestrictedMode();
        try {
            SvgUtils.disableSafeMode();
            var svg = """
                    <!DOCTYPE svg [<!ELEMENT svg ANY>]>\
                    <svg xmlns="http://www.w3.org/2000/svg">\
                    </svg>\
                    """;
            var doc = SvgUtils.parseDocument(svg.getBytes());
            assertNotNull(doc);
            var svgElement = doc.getDocumentElement();
            assertEquals("svg", svgElement.getLocalName());
        } finally {
            if (original) SvgUtils.enableSafeMode();
        }
    }
}
