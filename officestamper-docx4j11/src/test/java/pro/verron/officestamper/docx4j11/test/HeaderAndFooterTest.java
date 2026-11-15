package pro.verron.officestamper.docx4j11.test;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import pro.verron.officestamper.preset.ExceptionResolvers;
import pro.verron.officestamper.docx4j11.DocxOfficeStampers;
import pro.verron.officestamper.docx4j11.test.utils.ContextFactory;

import java.nio.file.Path;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.argumentSet;
import static pro.verron.officestamper.docx4j11.test.utils.OfficeStamperTestBase.toAsciidoc;
import static pro.verron.officestamper.docx4j11.DocxOfficeStampers.standard;
import static pro.verron.officestamper.docx4j11.test.utils.ContextFactory.mapContextFactory;
import static pro.verron.officestamper.docx4j11.test.utils.ContextFactory.objectContextFactory;
import static pro.verron.officestamper.docx4j11.test.utils.ResourceUtils.getImage;
import static pro.verron.officestamper.docx4j11.test.utils.ResourceUtils.getWordResource;


/// @author Joseph Verron
/// @author Tom Hombergs
class HeaderAndFooterTest {
    static Stream<Arguments> factories() {
        return Stream.of(argumentSet("obj", objectContextFactory()), argumentSet("map", mapContextFactory()));
    }

    @DisplayName("Placeholders in headers and footers should be replaced")
    @MethodSource("factories")
    @ParameterizedTest
    void placeholders(ContextFactory factory) {
        var context = factory.imagedName("Homer Simpson", getImage(Path.of("sample-butterfly.png")));
        var template = getWordResource("ExpressionReplacementInHeaderAndFooterTest.docx");
        var config = standard().setExceptionResolver(ExceptionResolvers.passing());
        var stamper = DocxOfficeStampers.docxPackageStamper(config);
        var stamped = stamper.stamp(template, context);
        var actual = toAsciidoc(stamped);
        assertEquals("""
                [header-even]
                --
                [header]
                --
                
                [header]
                --
                [header]
                This header paragraph is untouched.
                
                [header]
                In this paragraph, the variable name should be resolved to the value Homer Simpson.
                
                [header]
                In this paragraph, the variable foo should not be resolved: ${foo}.
                
                [header]
                Here, the picture should be resolved image:rId2[cx=5760720, cy=2880360].
                --
                
                [footer-even]
                --
                [footer]
                --
                
                [footer]
                --
                [footer]
                This footer paragraph is untouched.
                
                [footer]
                In this paragraph, the variable name should be resolved to the value Homer Simpson.
                
                [footer]
                In this paragraph, the variable foo should not be resolved: ${foo}.
                
                [footer]
                Here, the picture should be resolved image:rId1[cx=5760720, cy=2880360].
                --
                
                [header-first]
                --
                [header]
                --
                
                [footer-first]
                --
                [footer]
                --
                
                Expression Replacement in header and footer
                
                """, actual);
    }
}


