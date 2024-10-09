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
                == Custom Expression Function
                
                In this paragraph, we uppercase a variable: THE SIMPSONS.
                In this paragraph, we uppercase some multiline text: IT ALSO WORKS WITH<br/>
                MULTILINE<br/>
                STRINGS OF TEXT.
                We toggle this paragraph display with a processor using the custom function.
                We check custom functions runs in placeholders after processing: HOMER SIMPSON.
                We check custom functions runs in placeholders after processing: MARGE SIMPSON.
                We check custom functions runs in placeholders after processing: BART SIMPSON.
                We check custom functions runs in placeholders after processing: LISA SIMPSON.
                We check custom functions runs in placeholders after processing: MAGGIE SIMPSON.
                |===
                |We check custom functions runs in placeholders after processing:
                
                |HOMER SIMPSON
                |DAN CASTELLANETA<cnfStyle=000000100000>
                
                |MARGE SIMPSON
                |JULIE KAVNER<cnfStyle=000000100000>
                
                |BART SIMPSON
                |NANCY CARTWRIGHT<cnfStyle=000000100000>
                
                |LISA SIMPSON
                |YEARDLEY SMITH<cnfStyle=000000100000>
                
                |MAGGIE SIMPSON
                |JULIE KAVNER<cnfStyle=000000100000>
                
                
                |===
                
                """;
        var actual = stamper.stampAndLoadAndExtract(template, context);
        assertEquals(expected, actual);
    }
}
