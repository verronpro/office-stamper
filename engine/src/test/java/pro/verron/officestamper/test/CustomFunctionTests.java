package pro.verron.officestamper.test;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import pro.verron.officestamper.test.utils.ContextFactory;
import pro.verron.officestamper.test.utils.OfficeStamperTestBase;
import pro.verron.officestamper.test.utils.ResourceUtils;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Stream;

import static java.util.Locale.forLanguageTag;
import static org.junit.jupiter.params.provider.Arguments.argumentSet;
import static pro.verron.officestamper.preset.OfficeStamperConfigurations.minimal;
import static pro.verron.officestamper.preset.OfficeStamperConfigurations.standard;
import static pro.verron.officestamper.test.utils.ContextFactory.mapContextFactory;
import static pro.verron.officestamper.test.utils.ContextFactory.objectContextFactory;
import static pro.verron.officestamper.test.utils.DocxFactory.makeWordResource;
import static pro.verron.officestamper.test.utils.ResourceUtils.getWordResource;

@DisplayName("Custom function features")
class CustomFunctionTests extends OfficeStamperTestBase {

    static Stream<Arguments> trifunctions() {
        return Stream.of(//
                argumentSet("Object-based, Chinese", objectContextFactory(), "ZH", "2024 四月\n\n"),
                argumentSet("Object-based, French", objectContextFactory(), "FR", "2024 avril\n\n"),
                argumentSet("Object-based, English", objectContextFactory(), "EN", "2024 April\n\n"),
                argumentSet("Object-based, Japanese", objectContextFactory(), "JA", "2024 4月\n\n"),
                argumentSet("Object-based, Hebrew", objectContextFactory(), "HE", "2024 אפריל\n\n"),
                argumentSet("Object-based, Italian", objectContextFactory(), "IT", "2024 aprile\n\n"),
                argumentSet("Map-based, Chinese", mapContextFactory(), "ZH", "2024 四月\n\n"),
                argumentSet("Map-based, French", mapContextFactory(), "FR", "2024 avril\n\n"),
                argumentSet("Map-based, English", mapContextFactory(), "EN", "2024 April\n\n"),
                argumentSet("Map-based, Japanese", mapContextFactory(), "JA", "2024 4月\n\n"),
                argumentSet("Map-based, Hebrew", mapContextFactory(), "HE", "2024 אפריל\n\n"),
                argumentSet("Map-based, Italian", mapContextFactory(), "IT", "2024 aprile\n\n")
        );
    }

    @MethodSource("factories")
    @DisplayName("Should allow to inject full interfaces")
    @ParameterizedTest(name = "Should allow to inject full interfaces ({argumentSetName})")
    void interfaces(ContextFactory factory) {
        var customFunction = (UppercaseFunction) String::toUpperCase;
        var config = standard().exposeInterfaceToExpressionLanguage(UppercaseFunction.class, customFunction);
        var context = factory.show();
        var template = getWordResource(Path.of("CustomExpressionFunction.docx"));
        var expected = """
                == Custom Expression Function
                
                In this paragraph, we uppercase a variable: THE SIMPSONS.
                
                In this paragraph, we uppercase some multiline text: IT ALSO WORKS WITH +
                MULTILINE +
                STRINGS OF TEXT.
                
                We toggle this paragraph display with a processor using the custom function.
                
                We check custom functions runs in placeholders after processing: HOMER SIMPSON.
                
                We check custom functions runs in placeholders after processing: MARGE SIMPSON.
                
                We check custom functions runs in placeholders after processing: BART SIMPSON.
                
                We check custom functions runs in placeholders after processing: LISA SIMPSON.
                
                We check custom functions runs in placeholders after processing: MAGGIE SIMPSON.
                
                |===
                [rowStyle=2048]
                [style=516]
                |We check custom functions runs in placeholders after processing:
                [rowStyle=32]
                [style=512]
                |HOMER SIMPSON
                |DAN CASTELLANETA
                [rowStyle=32]
                [style=512]
                |MARGE SIMPSON
                |JULIE KAVNER
                [rowStyle=32]
                [style=512]
                |BART SIMPSON
                |NANCY CARTWRIGHT
                [rowStyle=32]
                [style=512]
                |LISA SIMPSON
                |YEARDLEY SMITH
                [rowStyle=32]
                [style=512]
                |MAGGIE SIMPSON
                |JULIE KAVNER
                |===
                
                
                
                // section {docGrid={charSpace=-6145, linePitch=240}, pgMar={bottom=1134, left=1134, right=1134, top=1134}, pgSz={h=16838, w=11906}, space=720}
                
                """;
        testStamper(config, context, template, expected);
    }

    @MethodSource("factories")
    @DisplayName("Should allow to inject lambda functions")
    @ParameterizedTest(name = "Should allow to inject lambda functions ({argumentSetName})")
    void functions(ContextFactory factory) {
        var config = standard().addCustomFunction("toUppercase", String.class).withImplementation(String::toUpperCase);
        var context = factory.show();
        var template = makeWordResource("${toUppercase(name)}");
        var expected = """
                THE SIMPSONS
                
                // section {pgMar={bottom=1440, left=1440, right=1440, top=1440}, pgSz={code=9, h=16839, w=11907}}
                
                """;
        testStamper(config, context, template, expected);
    }

    @MethodSource("factories")
    @DisplayName("Should allow to inject lambda suppliers")
    @ParameterizedTest(name = "Should allow to inject lambda suppliers ({argumentSetName})")
    void suppliers(ContextFactory factory) {
        var config = standard().addCustomFunction("foo", () -> List.of("a", "b", "c"));
        var context = factory.empty();
        var template = makeWordResource("${foo()}");
        var expected = """
                [a, b, c]
                
                // section {pgMar={bottom=1440, left=1440, right=1440, top=1440}, pgSz={code=9, h=16839, w=11907}}
                
                """;
        testStamper(config, context, template, expected);
    }

    @MethodSource("factories")
    @DisplayName("Should allow to inject lambda bifunctions.")
    @ParameterizedTest(name = "Should allow to inject lambda bifunctions. ({argumentSetName})")
    void bifunctions(ContextFactory factory) {
        var config = standard().addCustomFunction("Add", String.class, Integer.class)
                .withImplementation((s, i) -> new BigDecimal(s).add(new BigDecimal(i)));
        var context = factory.empty();
        var template = makeWordResource("${Add('3.22', 4)}");
        var expected = """
                7.22
                
                // section {pgMar={bottom=1440, left=1440, right=1440, top=1440}, pgSz={code=9, h=16839, w=11907}}
                
                """;
        testStamper(config, context, template, expected);
    }

    @MethodSource("trifunctions")
    @DisplayName("Should allow to inject lambda trifunctions")
    @ParameterizedTest(name = "Should allow to inject lambda trifunctions ({argumentSetName})")
    void trifunctions(ContextFactory factory, String tag, String expected) {
        var config = minimal().addCustomFunction("format", LocalDate.class, String.class, String.class)
                .withImplementation((date, pattern, languageTag) -> {
                    var locale = forLanguageTag(languageTag);
                    var formatter = DateTimeFormatter.ofPattern(pattern, locale);
                    return formatter.format(date);
                });
        var context = factory.date(LocalDate.of(2024, Month.APRIL, 1));
        var template = makeWordResource("${format(date,'yyyy MMMM','%s')}".formatted(tag));
        testStamper(config, context, template, expected, true);
    }

    /// The UppercaseFunction interface defines a method for converting a string to uppercase.
    public interface UppercaseFunction {
        /// Converts the given string to uppercase.
        ///
        /// @param string the string to be converted to uppercase
        /// @return the uppercase representation of the given string
        String toUppercase(String string);
    }
}
