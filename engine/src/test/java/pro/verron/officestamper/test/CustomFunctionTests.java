package pro.verron.officestamper.test;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pro.verron.officestamper.test.Functions.UppercaseFunction;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static pro.verron.officestamper.preset.OfficeStamperConfigurations.standard;
import static pro.verron.officestamper.test.TestUtils.getResource;

@DisplayName("Custom functions")
class CustomFunctionTests {


    @DisplayName("Should works with variables, multiline text, in comment content, inside comment, and in repetitions.")
    @Test()
    void features() {
        var config = standard()
                .exposeInterfaceToExpressionLanguage(UppercaseFunction.class, Functions.upperCase());
        var template = getResource(Path.of("CustomExpressionFunction.docx"));
        var context = Contexts.show();
        var stamper = new TestDocxStamper<>(config);
        var expected = """
                Custom Expression Function
                ❬In this paragraph, we uppercase a variable: THE SIMPSONS.❘b=true❭
                In this paragraph, we uppercase some multiline text: IT ALSO WORKS WITH<break line>
                MULTILINE<break line>
                STRINGS OF TEXT.
                To test that custom functions work together with comment expressions, we toggle visibility of this paragraph with a comment expression.
                To test that custom functions work together with comment expressions, we toggle it inside a repeated paragraph: HOMER SIMPSON.
                To test that custom functions work together with comment expressions, we toggle it inside a repeated paragraph: MARGE SIMPSON.
                To test that custom functions work together with comment expressions, we toggle it inside a repeated paragraph: BART SIMPSON.
                To test that custom functions work together with comment expressions, we toggle it inside a repeated paragraph: LISA SIMPSON.
                To test that custom functions work together with comment expressions, we toggle it inside a repeated paragraph: MAGGIE SIMPSON.
                |===
                |To test that custom functions work together with comment expressions, we toggle it inside a repeated row:
                
                |HOMER SIMPSON
                |DAN CASTELLANETA
                
                |MARGE SIMPSON
                |JULIE KAVNER
                
                |BART SIMPSON
                |NANCY CARTWRIGHT
                
                |LISA SIMPSON
                |YEARDLEY SMITH
                
                |MAGGIE SIMPSON
                |JULIE KAVNER
                
                
                |===
                
                """;
        var actual = stamper.stampAndLoadAndExtract(template, context);
        assertEquals(expected, actual);
    }
}
