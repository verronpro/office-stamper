package pro.verron.officestamper.asciidoc.docx;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.FooterPart;
import org.docx4j.openpackaging.parts.WordprocessingML.HeaderPart;
import org.docx4j.wml.SectPr;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pro.verron.officestamper.asciidoc.core.AsciiDocParser;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AsciiDoc Header/Footer Tests")
public class AsciiDocHeaderFooterTest {

    @Test
    @DisplayName("Should map [header] block to Word header")
    void testHeaderMapping() {
        String adoc = """
                [header]
                --
                This is the header content
                --
                
                Main document content
                """;

        var model = new AsciiDocParser().apply(adoc);
        WordprocessingMLPackage pkg = new AsciiDocToDocx().apply(model);

        List<HeaderPart> headers = pkg.getParts().getParts().values().stream()
                .filter(HeaderPart.class::isInstance)
                .map(HeaderPart.class::cast)
                .toList();

        assertFalse(headers.isEmpty(), "Header part should be created");

        // Simple check for content in HeaderPart
        String headerXml = headers.get(0).getXML();
        assertTrue(headerXml.contains("This is the header content"));

        String mainXml = pkg.getMainDocumentPart().getXML();
        assertFalse(mainXml.contains("This is the header content"), "Header content should not be in main body");
        assertTrue(mainXml.contains("Main document content"));
    }

    @Test
    @DisplayName("Should map [footer] block to Word footer")
    void testFooterMapping() {
        String adoc = """
                [footer]
                --
                This is the footer content
                --
                
                Main document content
                """;

        var model = new AsciiDocParser().apply(adoc);
        WordprocessingMLPackage pkg = new AsciiDocToDocx().apply(model);

        List<FooterPart> footers = pkg.getParts().getParts().values().stream()
                .filter(FooterPart.class::isInstance)
                .map(FooterPart.class::cast)
                .toList();

        assertFalse(footers.isEmpty(), "Footer part should be created");

        String footerXml = footers.get(0).getXML();
        assertTrue(footerXml.contains("This is the footer content"));

        String mainXml = pkg.getMainDocumentPart().getXML();
        assertFalse(mainXml.contains("This is the footer content"), "Footer content should not be in main body");
        assertTrue(mainXml.contains("Main document content"));
    }

    @Test
    @DisplayName("Should map [header] and [header-even] to distinct Word headers")
    void testOddEvenHeaderMapping() {
        String adoc = """
                [header]
                --
                Odd header
                --
                
                [header-even]
                --
                Even header
                --
                
                Main content
                """;

        var model = new AsciiDocParser().apply(adoc);
        WordprocessingMLPackage pkg = new AsciiDocToDocx().apply(model);

        List<HeaderPart> headers = pkg.getParts().getParts().values().stream()
                .filter(HeaderPart.class::isInstance)
                .map(HeaderPart.class::cast)
                .toList();

        assertEquals(2, headers.size(), "Should have 2 header parts");

        boolean foundOdd = headers.stream().anyMatch(h -> h.getXML().contains("Odd header"));
        boolean foundEven = headers.stream().anyMatch(h -> h.getXML().contains("Even header"));

        assertTrue(foundOdd, "Should find odd header");
        assertTrue(foundEven, "Should find even header");
    }

    @Test
    @DisplayName("Should map [header-first] and [footer-first] to first page Word headers/footers")
    void testFirstPageHeaderFooterMapping() {
        String adoc = """
                [header-first]
                --
                First header
                --
                
                [footer-first]
                --
                First footer
                --
                
                Main content
                """;

        var model = new AsciiDocParser().apply(adoc);
        WordprocessingMLPackage pkg = new AsciiDocToDocx().apply(model);

        List<HeaderPart> headers = pkg.getParts().getParts().values().stream()
                .filter(HeaderPart.class::isInstance)
                .map(HeaderPart.class::cast)
                .toList();
        List<FooterPart> footers = pkg.getParts().getParts().values().stream()
                .filter(FooterPart.class::isInstance)
                .map(FooterPart.class::cast)
                .toList();

        assertEquals(1, headers.size(), "Should have 1 header part");
        assertEquals(1, footers.size(), "Should have 1 footer part");

        assertTrue(headers.get(0).getXML().contains("First header"));
        assertTrue(footers.get(0).getXML().contains("First footer"));

        SectPr sectPr = pkg.getMainDocumentPart().getJaxbElement().getBody().getSectPr();
        assertTrue(sectPr.getTitlePg().isVal(), "titlePg should be true");
    }
}
