package pro.verron.officestamper.test;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static pro.verron.officestamper.preset.OfficeStamperConfigurations.standardWithPreprocessing;

class TestBug52 {

    private static final Path TEMPLATE_52 = Path.of("#52.docx");

    public static Stream<Arguments> source() {
        return Stream.of(arguments(Conditions.values(), ""),
                arguments(Conditions.values(true), "Start\nHello, World!\nEnd\n"),
                arguments(Conditions.values(false), "Start\nEnd\n"),
                arguments(Conditions.values(true, true), "Start\nHello, World!\nEnd\nStart\nHello, World!\nEnd\n"),
                arguments(Conditions.values(true, false), "Start\nHello, World!\nEnd\nStart\nEnd\n"),
                arguments(Conditions.values(false, true), "Start\nEnd\nStart\nHello, World!\nEnd\n"),
                arguments(Conditions.values(false, false), "Start\nEnd\nStart\nEnd\n"));
    }

    @MethodSource("source")
    @ParameterizedTest
    void test(Conditions conditions, String expected) {
        var stamper = givenStamper();
        var template = givenTemplate();
        var actual = stamper.stampAndLoadAndExtract(template, conditions);
        assertEquals(expected, actual);
    }

    private static TestDocxStamper<Object> givenStamper() {
        return new TestDocxStamper<>(standardWithPreprocessing());
    }

    private static InputStream givenTemplate() {
        return TestUtils.getResource(TEMPLATE_52);
    }

    record Condition(boolean condition) {}

    record Conditions(List<Condition> conditions) {
        private static Conditions values(boolean... bits) {
            var elements = new ArrayList<Condition>(bits.length);
            for (var bit : bits) elements.add(new Condition(bit));
            return new Conditions(elements);
        }

        @Override public String toString() {
            return conditions.stream()
                             .map(Condition::condition)
                             .map(Objects::toString)
                             .collect(joining(",", "(", ")"));
        }
    }
}
