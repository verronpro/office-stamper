package pro.verron.officestamper.docx4j11.test;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import pro.verron.officestamper.preset.OfficeStamperConfigurations;
import pro.verron.officestamper.docx4j11.test.utils.ContextFactory;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.argumentSet;
import pro.verron.officestamper.docx4j11.DocxOfficeStampers;
import static pro.verron.officestamper.docx4j11.test.utils.OfficeStamperTestBase.toAsciidoc;
import pro.verron.officestamper.docx4j11.DocxOfficeStampers;
import static pro.verron.officestamper.docx4j11.DocxOfficeStampers.docxPackageStamper;
import pro.verron.officestamper.docx4j11.DocxOfficeStampers;
import static pro.verron.officestamper.docx4j11.test.utils.ContextFactory.mapContextFactory;
import pro.verron.officestamper.docx4j11.DocxOfficeStampers;
import static pro.verron.officestamper.docx4j11.test.utils.ContextFactory.objectContextFactory;
import pro.verron.officestamper.docx4j11.DocxOfficeStampers;
import static pro.verron.officestamper.docx4j11.test.utils.DocxFactory.makeWordResource;

/// Regression test for [#805]: a section without its own header references inherits
/// the previous section's header, and that header is processed more than once.
///
/// The template uses the AsciiDoc `[section]` marker (asciidoc 3.10) to build a
/// two-section document where the second section has no `[header]` declaration and
/// therefore inherits the first one's header.
class HeaderInheritanceTest {
    static Stream<Arguments> factories() {
        return Stream.of(argumentSet("obj", objectContextFactory()), argumentSet("map", mapContextFactory()));
    }

    @DisplayName("A placeholder in a header inherited by a second section should stamp cleanly")
    @MethodSource("factories")
    @ParameterizedTest
    void headerPlaceholderInheritedBySecondSection(ContextFactory factory) {
        var template = makeWordResource("""
                [header]
                --
                ${name}
                --
                
                First section content
                
                [section]
                
                Second section content
                """);
        var configuration = DocxOfficeStampers.standard();
        var stamper = docxPackageStamper(configuration);
        var stamped = stamper.stamp(template, factory.name("World"));
        var actual = toAsciidoc(stamped);
        var expected = """
                [header]
                --
                World
                --

                First section content

                [section]

                Second section content

                """;
        assertEquals(expected.replace("\r\n", "\n"), actual.replace("\r\n", "\n"));
    }
}

