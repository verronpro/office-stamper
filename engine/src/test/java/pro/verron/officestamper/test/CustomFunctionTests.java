package pro.verron.officestamper.test;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.argumentSet;
import static pro.verron.officestamper.preset.OfficeStamperConfigurations.minimal;
import static pro.verron.officestamper.preset.OfficeStamperConfigurations.standard;
import static pro.verron.officestamper.preset.OfficeStampers.docxPackageStamper;
import static pro.verron.officestamper.test.ContextFactory.mapContextFactory;
import static pro.verron.officestamper.test.ContextFactory.objectContextFactory;
import static pro.verron.officestamper.test.TestUtils.getWordResource;
import static pro.verron.officestamper.test.TestUtils.makeWordResource;

@DisplayName("Custom function features") class CustomFunctionTests {

    private static Stream<Arguments> factories() {
        return Stream.of(//
                argumentSet("Object-based", objectContextFactory()),//
                argumentSet("Map-based", mapContextFactory())//
        );
    }

    static Stream<Arguments> trifunctions() {
        return Stream.of(//
                argumentSet("Object-based, Chinese", objectContextFactory(), "ZH", "2024 四月\n"),
                argumentSet("Object-based, French", objectContextFactory(), "FR", "2024 avril\n"),
                argumentSet("Object-based, English", objectContextFactory(), "EN", "2024 April\n"),
                argumentSet("Object-based, Japanese", objectContextFactory(), "JA", "2024 4月\n"),
                argumentSet("Object-based, Hebrew", objectContextFactory(), "HE", "2024 אפריל\n"),
                argumentSet("Object-based, Italian", objectContextFactory(), "IT", "2024 aprile\n"),
                argumentSet("Map-based, Chinese", mapContextFactory(), "ZH", "2024 四月\n"),
                argumentSet("Map-based, French", mapContextFactory(), "FR", "2024 avril\n"),
                argumentSet("Map-based, English", mapContextFactory(), "EN", "2024 April\n"),
                argumentSet("Map-based, Japanese", mapContextFactory(), "JA", "2024 4月\n"),
                argumentSet("Map-based, Hebrew", mapContextFactory(), "HE", "2024 אפריל\n"),
                argumentSet("Map-based, Italian", mapContextFactory(), "IT", "2024 aprile\n"));
    }

    @MethodSource("factories")
    @DisplayName("Should allow to inject full interfaces")
    @ParameterizedTest(name = "Should allow to inject full interfaces ({argumentSetName})")
    void interfaces(ContextFactory factory) {
        var config = standard().exposeInterfaceToExpressionLanguage(UppercaseFunction.class,
                (UppercaseFunction) String::toUpperCase);
        var template = getWordResource(Path.of("CustomExpressionFunction.docx"));
        var context = factory.show();
        var stamper = docxPackageStamper(config);
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
        var stamped = stamper.stamp(template, context);
        var actual = Stringifier.stringifyWord(stamped);
        assertEquals(expected, actual);
    }

    @MethodSource("factories")
    @DisplayName("Should allow to inject lambda functions")
    @ParameterizedTest(name = "Should allow to inject lambda functions ({argumentSetName})")
    void functions(ContextFactory factory) {
        var config = standard().addCustomFunction("toUppercase", String.class)
                               .withImplementation(String::toUpperCase);
        var template = makeWordResource("${toUppercase(name)}");
        var context = factory.show();
        var stamper = docxPackageStamper(config);
        var expected = """
                THE SIMPSONS
                
                """;
        var stamped = stamper.stamp(template, context);
        var actual = Stringifier.stringifyWord(stamped);
        assertEquals(expected, actual);
    }

    @MethodSource("factories")
    @DisplayName("Should allow to inject lambda suppliers")
    @ParameterizedTest(name = "Should allow to inject lambda suppliers ({argumentSetName})")
    void suppliers(ContextFactory factory) {
        var config = standard();
        config.addCustomFunction("foo", () -> List.of("a", "b", "c"));
        var template = makeWordResource("${foo()}");
        var context = factory.empty();
        var stamper = docxPackageStamper(config);
        var expected = """
                [a, b, c]
                
                """;
        var stamped = stamper.stamp(template, context);
        var actual = Stringifier.stringifyWord(stamped);
        assertEquals(expected, actual);
    }

    @MethodSource("factories")
    @DisplayName("Should allow to inject lambda bifunctions.")
    @ParameterizedTest(name = "Should allow to inject lambda bifunctions. ({argumentSetName})")
    void bifunctions(ContextFactory factory) {
        var config = standard();
        config.addCustomFunction("Add", String.class, Integer.class)
              .withImplementation((s, i) -> new BigDecimal(s).add(new BigDecimal(i)));
        var template = makeWordResource("${Add('3.22', 4)}");
        var context = factory.empty();
        var stamper = docxPackageStamper(config);
        var expected = "7.22\n\n";
        var stamped = stamper.stamp(template, context);
        var actual = Stringifier.stringifyWord(stamped);
        assertEquals(expected, actual);
    }

    @MethodSource("trifunctions")
    @DisplayName("Should allow to inject lambda trifunctions")
    @ParameterizedTest(name = "Should allow to inject lambda trifunctions ({argumentSetName})")
    void trifunctions(ContextFactory factory, String tag, String expected) {
        var config = minimal().addCustomFunction("format", LocalDate.class, String.class, String.class)
                              .withImplementation((date, pattern, languageTag) -> {
                                  var locale = Locale.forLanguageTag(languageTag);
                                  var formatter = DateTimeFormatter.ofPattern(pattern, locale);
                                  return formatter.format(date);
                              });
        var template = makeWordResource("${format(date,'yyyy MMMM','%s')}".formatted(tag));
        var context = factory.date(LocalDate.of(2024, Month.APRIL, 1));
        var stamper = docxPackageStamper(config);
        var stamped = stamper.stamp(template, context);
        var actual = Stringifier.stringifyWord(stamped);
        assertEquals(expected + "\n", actual);
    }

    /// The UppercaseFunction interface defines a method for converting a string to uppercase.
    public interface UppercaseFunction {
        /// Converts the given string to uppercase.
        ///
        /// @param string the string to be converted to uppercase
        ///
        /// @return the uppercase representation of the given string
        String toUppercase(String string);
    }
}
