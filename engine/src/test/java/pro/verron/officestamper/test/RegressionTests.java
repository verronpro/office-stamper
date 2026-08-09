package pro.verron.officestamper.test;

import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import pro.verron.officestamper.preset.OfficeStamperConfigurations;
import pro.verron.officestamper.test.utils.ObjectContextFactory;
import pro.verron.officestamper.test.utils.OfficeStamperTestBase;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static pro.verron.officestamper.preset.OfficeStamperConfigurations.full;
import static pro.verron.officestamper.preset.OfficeStamperConfigurations.standard;
import static pro.verron.officestamper.test.utils.DocxFactory.makeWordResource;
import static pro.verron.officestamper.test.utils.ResourceUtils.getWordResource;

class RegressionTests extends OfficeStamperTestBase {
    public static final ObjectContextFactory FACTORY = new ObjectContextFactory();
    private static final Path TEMPLATE_52 = Path.of("#52.docx");

    public static Stream<Arguments> source52() {
        return Stream.of(arguments(Conditions.values(), ""),
                arguments(Conditions.values(true), "Start\n\nHello, World!\n\nEnd\n\n"),
                arguments(Conditions.values(false), "Start\n\nEnd\n\n"),
                arguments(Conditions.values(true, true),
                        "Start\n\nHello, World!\n\nEnd\n\nStart\n\nHello, World!\n\nEnd\n\n"
                ),
                arguments(Conditions.values(true, false), "Start\n\nHello, World!\n\nEnd\n\nStart\n\nEnd\n\n"),
                arguments(Conditions.values(false, true), "Start\n\nEnd\n\nStart\n\nHello, World!\n\nEnd\n\n"),
                arguments(Conditions.values(false, false), "Start\n\nEnd\n\nStart\n\nEnd\n\n")
        );
    }

    /// Test that table of content specific instruction text (instrText) is not modified by error
    @Test
    void testTableOfContent() {
        var config = OfficeStamperConfigurations.standard();
        var context = new Object();
        var template = getWordResource(Path.of("TOC.docx"));
        var expected = """
                == Table Of Content
                
                [toc 1]
                 TOC \\o "1-3" \\h \\z \\u [rStyle_Lienhypertexte]#Table Of Content#	 PAGEREF _Toc201699773 \\h 1
                
                [toc 1]
                [rStyle_Lienhypertexte]#First Title#	 PAGEREF _Toc201699774 \\h 1
                
                [toc 2]
                [rStyle_Lienhypertexte]#Subtitle 1.1#	 PAGEREF _Toc201699775 \\h 1
                
                [toc 1]
                [rStyle_Lienhypertexte]#Second Title#	 PAGEREF _Toc201699776 \\h 1
                
                [toc 2]
                [rStyle_Lienhypertexte]#Subtitle 2.1#	 PAGEREF _Toc201699777 \\h 1
                
                [toc 2]
                [rStyle_Lienhypertexte]#Subtitle 2.2#	 PAGEREF _Toc201699778 \\h 1
                
                [toc 2]
                [rStyle_Lienhypertexte]#Subtitle 2.3#	 PAGEREF _Toc201699779 \\h 1
                
                [toc 1]
                [rStyle_Lienhypertexte]#Third Title#	 PAGEREF _Toc201699780 \\h 1
                
                [toc 2]
                [rStyle_Lienhypertexte]#Subtitle 3.1#	 PAGEREF _Toc201699781 \\h 1
                
                [toc 2]
                [rStyle_Lienhypertexte]#Subtitle 3.2#	 PAGEREF _Toc201699782 \\h 1
                
                == First Title
                
                === Subtitle 1.1
                
                == Second Title
                
                === Subtitle 2.1
                
                === Subtitle 2.2
                
                === Subtitle 2.3
                
                == Third Title
                
                === Subtitle 3.1
                
                === Subtitle 3.2
                
                
                
                // section {docGrid={linePitch=360}, pgMar={bottom=1417, footer=708, header=708, left=1417, right=1417, top=1417}, pgSz={h=16838, w=11906}, space=708}
                
                """;
        testStamper(config, context, template, expected);
    }

    @Test
    void test64() {
        var config = full();
        var testFunction = new TestFunction.TestFunctionImpl();
        config.exposeInterfaceToExpressionLanguage(TestFunction.class, testFunction);
        var context = new Object();
        var template = makeWordResource("${test()}");
        var expected = """
                
                
                // section {pgMar={bottom=1440, left=1440, right=1440, top=1440}, pgSz={code=9, h=16839, w=11907}}
                
                """;
        testStamper(config, context, template, expected);
        assertEquals(1, testFunction.counter());
    }

    @Test
    void test114() {
        var config = standard();
        var template = getWordResource(Path.of("#114.docx"));
        var context = FACTORY.names(List.class, "Homer", "Marge", "Bart", "Lisa", "Maggie");
        var expected = """
                = Issue #114
                
                |===
                |Name
                |Homer
                |Marge
                |Bart
                |Lisa
                |Maggie
                |So…
                |===
                
                
                
                // section {docGrid={linePitch=360}, pgMar={bottom=1417, footer=708, header=708, left=1417, right=1417, top=1417}, pgSz={h=16838, w=11906}, space=708}
                
                """;
        testStamper(config, context, template, expected);
    }

    @MethodSource("source52")
    @ParameterizedTest
    void test52(Object context, String expected) {
        var config = full();
        var template = getWordResource(TEMPLATE_52);
        expected = expected + """
                // section {docGrid={linePitch=360}, pgMar={bottom=1417, footer=708, header=708, left=1417, right=1417, top=1417}, pgSz={h=16838, w=11906}, space=708}
                
                """;
        testStamper(config, context, template, expected);
    }

    public interface TestFunction {
        void test();

        class TestFunctionImpl implements TestFunction {
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

    record Condition(boolean condition) {
        @Override
        @NonNull
        public String toString() {
            return String.valueOf(condition);
        }
    }

    record Conditions(List<Condition> conditions) {
        private static Conditions values(boolean... bits) {
            var elements = new ArrayList<Condition>(bits.length);
            for (var bit : bits) elements.add(new Condition(bit));
            return new Conditions(elements);
        }

        @Override
        @NonNull
        public String toString() {
            return String.valueOf(conditions);
        }
    }
}
