package pro.verron.officestamper.test;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import pro.verron.officestamper.test.utils.ContextFactory;
import pro.verron.officestamper.test.utils.OfficeStamperTestBase;

import static pro.verron.officestamper.preset.ExceptionResolvers.passing;
import static pro.verron.officestamper.preset.OfficeStamperConfigurations.standard;
import static pro.verron.officestamper.test.utils.ResourceUtils.getWordResource;


/// @author Joseph Verron
/// @author Thomas Oster
class TextBoxesTest
        extends OfficeStamperTestBase {

    @DisplayName("Placeholders in text boxes should be replaced")
    @MethodSource("factories")
    @ParameterizedTest
    void placeholders(ContextFactory factory) {
        var context = factory.name("Bart Simpson");
        var template = getWordResource(
                "ExpressionReplacementInTextBoxesTest.docx");
        var config = standard().setExceptionResolver(passing());
        String expected = """
                == Expression Replacement in TextBoxes
                
                pict:363CAD83[]This should resolve to a name:\s
                
                pict:6826989D[]This should not resolve:\s
                
                // section {docGrid={linePitch=240}, pgMar={bottom=1134, left=1134, right=1134, top=1134}, pgSz={h=16838, w=11906}, space=720}
                
                [pict, anchor=363CAD83]
                --
                roundrect
                	shadow
                	textbox
                		Bart Simpson
                
                --
                
                
                [pict, anchor=6826989D]
                --
                roundrect
                	shadow
                	textbox
                		${foo}
                
                --
                
                
                """;
        testStamper(config, context, template, expected);
    }

    @DisplayName("Placeholders in modern text boxes should be replaced")
    @MethodSource("factories")
    @ParameterizedTest
    void placeholders_in_modern_textboxes(ContextFactory factory) {
        var context = factory.name("Bart Simpson");
        var template = getWordResource(
                "ExpressionReplacementInTextBoxesTest.Modern.docx");
        var config = standard().setExceptionResolver(passing());
        String expected = """
                == Expression Replacement in TextBoxes
                
                alternateContent:1[]This should resolve to a name:\s
                
                alternateContent:2[]This should not resolve:\s
                
                // section {docGrid={linePitch=240}, pgMar={bottom=1134, left=1134, right=1134, top=1134}, pgSz={h=16838, w=11906}, space=720}
                
                [alternateContent, anchor=1]
                --
                choice wps
                	drawing
                		anchor
                			graphic
                				wordprocessingshape
                					textbox
                						Bart Simpson
                
                fallback
                	pict
                		roundrect
                			shadow
                			textbox
                				Bart Simpson
                
                --
                
                
                [alternateContent, anchor=2]
                --
                choice wps
                	drawing
                		anchor
                			graphic
                				wordprocessingshape
                					textbox
                						${foo}
                
                fallback
                	pict
                		roundrect
                			shadow
                			textbox
                				${foo}
                
                --
                
                
                """;
        testStamper(config, context, template, expected);
    }

}
