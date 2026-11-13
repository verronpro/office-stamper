package pro.verron.officestamper.test;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.P;
import org.docx4j.wml.PPr;
import org.docx4j.wml.SectPr;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import pro.verron.officestamper.preset.OfficeStamperConfigurations;
import pro.verron.officestamper.test.utils.ContextFactory;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.argumentSet;
import static pro.verron.asciidoc.compiler.AsciiDocCompiler.toAsciidoc;
import static pro.verron.officestamper.preset.OfficeStampers.docxPackageStamper;
import static pro.verron.officestamper.test.utils.ContextFactory.mapContextFactory;
import static pro.verron.officestamper.test.utils.ContextFactory.objectContextFactory;
import static pro.verron.officestamper.test.utils.DocxFactory.makeWordResource;

/// Regression test for [#805]: a section without its own header references inherits
/// the previous section's header, and that header is processed more than once.
class HeaderInheritanceTest {
    static Stream<Arguments> factories() {
        return Stream.of(argumentSet("obj", objectContextFactory()), argumentSet("map", mapContextFactory()));
    }

    @DisplayName("A placeholder in a header inherited by a second section should stamp cleanly")
    @MethodSource("factories")
    @ParameterizedTest
    void headerPlaceholderInheritedBySecondSection(ContextFactory factory) {
        var template = twoSectionsWithInheritedHeader("""
                [header]
                --
                ${name}
                --
                
                First section content
                """);
        var configuration = OfficeStamperConfigurations.standard();
        var stamper = docxPackageStamper(configuration);
        var stamped = stamper.stamp(template, factory.name("World"));
        var actual = toAsciidoc(stamped);
        var expected = """
                [header]
                --
                World

                --


                First section content

                // section {pgMar={bottom=1440, left=1440, right=1440, top=1440}, pgSz={code=9, h=16839, w=11907}}

                Second section content

                """;
        assertEquals(expected.replace("\r\n", "\n"), actual.replace("\r\n", "\n"));
    }

    /// Builds a two-section template from an AsciiDoc source where section 1
    /// references the header and section 2 has no headerReference, so it
    /// inherits section 1's header.
    private static WordprocessingMLPackage twoSectionsWithInheritedHeader(String asciidoc) {
        var template = makeWordResource(asciidoc);
        var mdp = template.getMainDocumentPart();
        var body = mdp.getJaxbElement()
                      .getBody();
        // The AsciiDoc [header] block attached its reference to the body-level
        // sectPr. Demote that sectPr onto the last paragraph of section 1
        // (it becomes section 1's own sectPr, keeping the header reference —
        // this is how Word marks a section break), and leave a fresh
        // body-level sectPr with no references for section 2.
        var sectionOneSectPr = body.getSectPr();
        body.setSectPr(new SectPr());
        var content = mdp.getContent();
        var lastParagraph = (P) content.get(content.size() - 1);
        var pPr = new PPr();
        pPr.setSectPr(sectionOneSectPr);
        lastParagraph.setPPr(pPr);
        content.add(paragraph("Second section content"));
        return template;
    }

    private static P paragraph(String text) {
        var factory = new org.docx4j.wml.ObjectFactory();
        var p = factory.createP();
        var r = factory.createR();
        var t = factory.createText();
        t.setValue(text);
        t.setSpace("preserve");
        r.getContent().add(t);
        p.getContent().add(r);
        return p;
    }
}
