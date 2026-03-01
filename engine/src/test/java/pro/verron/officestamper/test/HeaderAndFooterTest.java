package pro.verron.officestamper.test;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import pro.verron.officestamper.preset.ExceptionResolvers;
import pro.verron.officestamper.preset.OfficeStampers;
import pro.verron.officestamper.test.utils.ContextFactory;

import java.nio.file.Path;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.argumentSet;
import static pro.verron.officestamper.asciidoc.AsciiDocCompiler.toAsciidoc;
import static pro.verron.officestamper.preset.OfficeStamperConfigurations.standard;
import static pro.verron.officestamper.test.utils.ContextFactory.mapContextFactory;
import static pro.verron.officestamper.test.utils.ContextFactory.objectContextFactory;
import static pro.verron.officestamper.test.utils.ResourceUtils.getImage;
import static pro.verron.officestamper.test.utils.ResourceUtils.getWordResource;


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
        var context = factory.imagedName("Homer Simpson", getImage(Path.of("butterfly.png")));
        var template = getWordResource("ExpressionReplacementInHeaderAndFooterTest.docx");
        var config = standard().setExceptionResolver(ExceptionResolvers.passing());
        var stamper = OfficeStampers.docxPackageStamper(config);
        var stamped = stamper.stamp(template, context);
        var actual = toAsciidoc(stamped);
        assertEquals("""
                [header]
                --
                [header]
                This header paragraph is untouched.
                
                [header]
                In this paragraph, the variable name should be resolved to the value Homer Simpson.
                
                [header]
                In this paragraph, the variable foo should not be resolved: ${foo}.
                
                [header]
                Here, the picture should be resolved image:rId1[cx=5760720, cy=2880360].
                
                --
                
                
                Expression Replacement in header and footer
                
                // section {docGrid={linePitch=360}, pgMar={bottom=1417, footer=708, header=708, left=1417, right=1417, top=1417}, pgSz={h=16838, w=11906}, space=708}
                
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
                
                
                """, actual);
    }
}
