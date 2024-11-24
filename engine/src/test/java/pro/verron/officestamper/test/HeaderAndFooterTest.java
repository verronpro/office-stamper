package pro.verron.officestamper.test;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pro.verron.officestamper.preset.ExceptionResolvers;
import pro.verron.officestamper.preset.Image;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static pro.verron.officestamper.preset.OfficeStamperConfigurations.standard;
import static pro.verron.officestamper.test.TestUtils.getImage;
import static pro.verron.officestamper.test.TestUtils.getResource;


/// @author Joseph Verron
/// @author Tom Hombergs
class HeaderAndFooterTest {
    @Test @DisplayName("Placeholders in headers and footers should be replaced") void placeholders() {
        var context = new Name("Homer Simpson", getImage(Path.of("butterfly.png")));
        var template = getResource("ExpressionReplacementInHeaderAndFooterTest.docx");
        var config = standard().setExceptionResolver(ExceptionResolvers.passing());
        var stamper = new TestDocxStamper<Name>(config);
        var actual = stamper.stampAndLoadAndExtract(template, context);
        assertEquals("""
                [header, name="/word/header2.xml"]
                ----
                [header] This header paragraph is untouched.
                [header] In this paragraph, the variable name should be resolved to the value Homer Simpson.
                [header] In this paragraph, the variable foo should not be resolved: ${foo}.
                [header] Here, the picture should be resolved /word/media/header2_image_rId1.png:rId1:image/png:193.6kB:sha1=t8UNAmo7yJgZJk9g7pLLIb3AvCA=:cy=$d:5760720.
                
                ----
                
                Expression Replacement in header and footer
                
                [footer, name="/word/footer2.xml"]
                ----
                [footer] This footer paragraph is untouched.
                [footer] In this paragraph, the variable name should be resolved to the value Homer Simpson.
                [footer] In this paragraph, the variable foo should not be resolved: ${foo}.
                [footer] Here, the picture should be resolved /word/media/header2_image_rId1.png:rId1:image/png:193.6kB:sha1=t8UNAmo7yJgZJk9g7pLLIb3AvCA=:cy=$d:5760720.
                
                ----
                """, actual);
    }

    public record Name(String name, Image butterfly) {}
}
