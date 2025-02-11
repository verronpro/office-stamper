package pro.verron.officestamper.test;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import pro.verron.officestamper.api.OfficeStamperConfiguration;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assumptions.abort;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static pro.verron.officestamper.preset.OfficeStamperConfigurations.standard;
import static pro.verron.officestamper.test.TestUtils.getResource;

class RegressionTests {
    public static final ObjectContextFactory FACTORY = new ObjectContextFactory();
    private static final Path TEMPLATE_52 = Path.of("#52.docx");

    public static Stream<Arguments> source52() {
        return Stream.of(arguments(Conditions.values(), ""),
                arguments(Conditions.values(true), "Start\nHello, World!\nEnd\n"),
                arguments(Conditions.values(false), "Start\nEnd\n"),
                arguments(Conditions.values(true, true), "Start\nHello, World!\nEnd\nStart\nHello, World!\nEnd\n"),
                arguments(Conditions.values(true, false), "Start\nHello, World!\nEnd\nStart\nEnd\n"),
                arguments(Conditions.values(false, true), "Start\nEnd\nStart\nHello, World!\nEnd\n"),
                arguments(Conditions.values(false, false), "Start\nEnd\nStart\nEnd\n"));
    }

    @Test
    void test64() {
        var configuration = givenConfiguration();
        var testFunction = new TestFunction.TestFunctionImpl();
        configuration.exposeInterfaceToExpressionLanguage(TestFunction.class, testFunction);
        var stamper = givenStamper(configuration);
        var template = givenTemplate("${test()}");
        var context = givenContext();
        var actual = stamper.stampAndLoadAndExtract(template, context);
        assertEquals("\n", actual);
        assertEquals(1, testFunction.counter());
    }

    private static OfficeStamperConfiguration givenConfiguration() {
        return standard();
    }

    private static TestDocxStamper<Object> givenStamper(OfficeStamperConfiguration configuration) {
        return new TestDocxStamper<>(configuration);
    }

    private static InputStream givenTemplate(String str) {
        return TestUtils.makeResource(str);
    }

    private static Object givenContext() {
        return new Object();
    }

    @Test
    void test114() {
        abort("Will be worked on after 2.7.0");
        var config = standard();
        var stamper = new TestDocxStamper<>(config);
        var template = getResource(Path.of("#114.docx"));
        var context = FACTORY.names(List.class, "Homer", "Marge", "Bart", "Lisa", "Maggie");
        var actual = stamper.stampAndLoadAndExtract(template, context);
        var expected = """
                = Issue #114
                
                |===
                |Name
                
                |Homer
                
                |Marge
                
                |Bart
                
                |Lisa
                
                |Maggie
                
                
                |===
                
                
                """;
        assertEquals(expected, actual);
    }

    @MethodSource("source52")
    @ParameterizedTest
    void test52(Conditions conditions, String expected) {
        var stamper = givenStamper(givenConfiguration());
        var template = givenTemplate(TEMPLATE_52);
        var actual = stamper.stampAndLoadAndExtract(template, conditions);
        assertEquals(expected, actual);
    }

    private static InputStream givenTemplate(Path path) {
        return TestUtils.getResource(path);
    }

    public interface TestFunction {
        void test();

        class TestFunctionImpl
                implements TestFunction {
            private int counter = 0;

            @Override
            public void test() {
                counter++;
            }

            public int counter() {
                return counter;
            }
        }
    }

    record Condition(boolean condition) {}

    record Conditions(List<Condition> conditions) {
        private static Conditions values(boolean... bits) {
            var elements = new ArrayList<Condition>(bits.length);
            for (var bit : bits) elements.add(new Condition(bit));
            return new Conditions(elements);
        }

        @Override
        public String toString() {
            return conditions.stream()
                             .map(Condition::condition)
                             .map(Objects::toString)
                             .collect(joining(",", "(", ")"));
        }
    }
}
