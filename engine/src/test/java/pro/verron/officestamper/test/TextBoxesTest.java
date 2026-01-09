package pro.verron.officestamper.test;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import pro.verron.officestamper.ContextFactory;
import pro.verron.officestamper.preset.OfficeStampers;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.argumentSet;
import static pro.verron.officestamper.ContextFactory.mapContextFactory;
import static pro.verron.officestamper.ContextFactory.objectContextFactory;
import static pro.verron.officestamper.preset.ExceptionResolvers.passing;
import static pro.verron.officestamper.preset.OfficeStamperConfigurations.standard;
import static pro.verron.officestamper.utils.wml.DocxRenderer.docxToString;


/// @author Joseph Verron
/// @author Thomas Oster
class TextBoxesTest {

    static Stream<Arguments> factories() {
        return Stream.of(argumentSet("obj", objectContextFactory()), argumentSet("map", mapContextFactory()));
    }

    @DisplayName("Placeholders in text boxes should be replaced")
    @MethodSource("factories")
    @ParameterizedTest
    void placeholders(ContextFactory factory) {
        var context = factory.name("Bart Simpson");
        var template = TestUtils.getWordResource("ExpressionReplacementInTextBoxesTest.docx");
        var config = standard().setExceptionResolver(passing());
        var stamper = OfficeStampers.docxPackageStamper(config);
        var stamped = stamper.stamp(template, context);
        var actual = docxToString(stamped);
        String expected = """
                == Expression Replacement in TextBoxes
                
                
                Bart SimpsonThis should resolve to a name:\s
                
                ${foo}This should not resolve:\s
                
                """;
        assertEquals(expected, actual);
    }

}
