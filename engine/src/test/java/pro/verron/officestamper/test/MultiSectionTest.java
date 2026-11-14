package pro.verron.officestamper.test;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import pro.verron.asciidoc.compiler.AsciiDocCompiler;
import pro.verron.officestamper.preset.OfficeStamperConfigurations;
import pro.verron.officestamper.test.utils.ContextFactory;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.argumentSet;
import static pro.verron.officestamper.preset.OfficeStampers.docxPackageStamper;
import static pro.verron.officestamper.test.utils.ContextFactory.mapContextFactory;
import static pro.verron.officestamper.test.utils.ContextFactory.objectContextFactory;
import static pro.verron.officestamper.test.utils.ResourceUtils.getWordResource;

/// @author Joseph Verron
class MultiSectionTest {

    static Stream<Arguments> factories() {
        return Stream.of(argumentSet("obj", objectContextFactory()), argumentSet("map", mapContextFactory()));
    }

    /// Rendered with the asciidoc compiler's default settings, so the
    /// `[section-properties]` blocks document the page geometry (size,
    /// orientation, margins) of every section of the stamped document. The
    /// expected text is itself valid AsciiDoc: feeding it to the asciidoc→docx
    /// compiler recreates a document with the same section layout.
    @MethodSource("factories")
    @ParameterizedTest
    void expressionsInMultipleSections(ContextFactory factory) {
        var context = factory.sectionName("Homer", "Marge");
        var template = getWordResource("MultiSectionTest.docx");
        var configuration = OfficeStamperConfigurations.standard();
        var stamper = docxPackageStamper(configuration);
        var wordprocessingMLPackage = stamper.stamp(template, context);
        var actual = AsciiDocCompiler.toAsciidoc(wordprocessingMLPackage);
        String expected = """
                Homer
                
                
                
                
                
                [section-properties]
                --
                cols={space=708}
                docGrid={linePitch=360}
                pgMar={bottom=1417, footer=708, header=708, left=1417, right=1417, top=1417}
                pgSz={h=16838, w=11906}
                --
                
                [section]
                
                Marge
                
                [section-properties]
                --
                cols={space=708}
                docGrid={linePitch=360}
                pgMar={bottom=1417, footer=708, header=708, left=1417, right=1417, top=1417}
                pgSz={h=11906, orient=landscape, w=16838}
                --
                
                """;
        assertEquals(expected, actual);
    }
}
