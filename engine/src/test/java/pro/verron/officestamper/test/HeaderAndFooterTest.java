package pro.verron.officestamper.test;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import pro.verron.officestamper.ContextFactory;
import pro.verron.officestamper.preset.ExceptionResolvers;
import pro.verron.officestamper.preset.OfficeStampers;

import java.nio.file.Path;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.argumentSet;
import static pro.verron.officestamper.ContextFactory.mapContextFactory;
import static pro.verron.officestamper.ContextFactory.objectContextFactory;
import static pro.verron.officestamper.preset.OfficeStamperConfigurations.standard;
import static pro.verron.officestamper.test.TestUtils.getImage;
import static pro.verron.officestamper.test.TestUtils.getWordResource;
import static pro.verron.officestamper.utils.wml.DocxRenderer.docxToString;


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
        var actual = docxToString(stamped);
        assertEquals("""
                [header, name="/word/header2.xml"]
                ----
                [header] This header paragraph is untouched.
                
                [header] In this paragraph, the variable name should be resolved to the value Homer Simpson.
                
                [header] In this paragraph, the variable foo should not be resolved: ${foo}.
                
                [header] Here, the picture should be resolved /word/media/header2_image_rId1.png:rId1:image/png:193.6 kB:sha1=t8UNAmo7yJgZJk9g7pLLIb3AvCA=:cy=$d:5760720.
                
                
                ----
                
                Expression Replacement in header and footer
                
                
                [footer, name="/word/footer2.xml"]
                ----
                [footer] This footer paragraph is untouched.
                
                [footer] In this paragraph, the variable name should be resolved to the value Homer Simpson.
                
                [footer] In this paragraph, the variable foo should not be resolved: ${foo}.
                
                [footer] Here, the picture should be resolved /word/media/footer2_image_rId1.png:rId1:image/png:193.6 kB:sha1=t8UNAmo7yJgZJk9g7pLLIb3AvCA=:cy=$d:5760720.
                
                
                ----
                """, actual);
    }
}
