package pro.verron.officestamper.test;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.HeaderPart;
import org.docx4j.wml.HeaderReference;
import org.docx4j.wml.Hdr;
import org.docx4j.wml.HdrFtrRef;
import org.docx4j.wml.P;
import org.docx4j.wml.PPr;
import org.docx4j.wml.R;
import org.docx4j.wml.SectPr;
import org.docx4j.wml.Text;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import pro.verron.officestamper.preset.OfficeStamperConfigurations;
import pro.verron.officestamper.test.utils.ContextFactory;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static pro.verron.asciidoc.compiler.AsciiDocCompiler.toAsciidoc;
import static pro.verron.officestamper.preset.OfficeStampers.docxPackageStamper;
import static pro.verron.officestamper.test.utils.ContextFactory.mapContextFactory;
import static pro.verron.officestamper.test.utils.ContextFactory.objectContextFactory;

/// Regression test for [#805]: a section without its own header references inherits
/// the previous section's header, and that header is processed more than once.
class HeaderInheritanceTest {
    static java.util.stream.Stream<org.junit.jupiter.params.provider.Arguments> factories() {
        return java.util.stream.Stream.of(
                org.junit.jupiter.params.provider.Arguments.argumentSet("obj", objectContextFactory()),
                org.junit.jupiter.params.provider.Arguments.argumentSet("map", mapContextFactory())
        );
    }

    @DisplayName("A placeholder in a header inherited by a second section should stamp cleanly")
    @MethodSource("factories")
    @ParameterizedTest
    void headerPlaceholderInheritedBySecondSection(ContextFactory factory) throws Exception {
        var template = twoSectionsWithInheritedHeader();
        var configuration = OfficeStamperConfigurations.standard();
        var stamper = docxPackageStamper(configuration);
        var stamped = stamper.stamp(template, factory.name("World"));
        var actual = toAsciidoc(stamped);
        assertTrue(actual.contains("World"), "Placeholder in inherited header should be resolved");
        assertFalse(actual.contains("${name}"), "Placeholder should not remain unresolved");
        assertTrue(actual.contains("Second section content"), "Body content should be stamped");
    }

    private static WordprocessingMLPackage twoSectionsWithInheritedHeader() throws Exception {
        var pkg = WordprocessingMLPackage.createPackage();
        var mdp = pkg.getMainDocumentPart();

        var header = new HeaderPart();
        var hdr = new Hdr();
        header.setJaxbElement(hdr);
        hdr.getContent().add(paragraph("${name}"));
        var headerRelationship = mdp.addTargetPart(header);

        // Section 1 references the header
        var sectPr1 = new SectPr();
        var headerReference = new HeaderReference();
        headerReference.setId(headerRelationship.getId());
        headerReference.setType(HdrFtrRef.DEFAULT);
        sectPr1.getEGHdrFtrReferences().add(headerReference);

        // Section 2 (body-level sectPr) has no headerReference: it inherits section 1's header
        mdp.getJaxbElement()
           .getBody()
           .setSectPr(new SectPr());

        mdp.getContent().add(paragraph("First section content"));
        mdp.getContent().add(sectionBreak(sectPr1));
        mdp.getContent().add(paragraph("Second section content"));
        return pkg;
    }

    private static P paragraph(String text) {
        var p = new P();
        var r = new R();
        var t = new Text();
        t.setValue(text);
        t.setSpace("preserve");
        r.getContent().add(t);
        p.getContent().add(r);
        return p;
    }

    private static P sectionBreak(SectPr sectPr) {
        var p = new P();
        var pPr = new PPr();
        pPr.setSectPr(sectPr);
        p.setPPr(pPr);
        return p;
    }
}
