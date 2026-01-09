package pro.verron.officestamper.test;

import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import pro.verron.officestamper.ObjectContextFactory;
import pro.verron.officestamper.preset.OfficeStamperConfigurations;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static pro.verron.officestamper.preset.OfficeStamperConfigurations.full;
import static pro.verron.officestamper.preset.OfficeStamperConfigurations.standard;
import static pro.verron.officestamper.preset.OfficeStampers.docxPackageStamper;
import static pro.verron.officestamper.test.TestUtils.getWordResource;
import static pro.verron.officestamper.test.TestUtils.makeWordResource;
import static pro.verron.officestamper.utils.wml.DocxRenderer.docxToString;

class RegressionTests {
    public static final ObjectContextFactory FACTORY = new ObjectContextFactory();
    private static final Path TEMPLATE_52 = Path.of("#52.docx");

    public static Stream<Arguments> source52() {
        return Stream.of(arguments(Conditions.values(), ""),
                arguments(Conditions.values(true), "Start\n\nHello, World!\n\nEnd\n\n"),
                arguments(Conditions.values(false), "Start\n\nEnd\n\n"),
                arguments(Conditions.values(true, true),
                        "Start\n\nHello, World!\n\nEnd\n\nStart\n\nHello, World!\n\nEnd\n\n"),
                arguments(Conditions.values(true, false), "Start\n\nHello, World!\n\nEnd\n\nStart\n\nEnd\n\n"),
                arguments(Conditions.values(false, true), "Start\n\nEnd\n\nStart\n\nHello, World!\n\nEnd\n\n"),
                arguments(Conditions.values(false, false), "Start\n\nEnd\n\nStart\n\nEnd\n\n"));
    }

    /// Test that table of content specific instruction text (instrText) is not modified by error
    @Test
    void testTableOfContent() {
        var configuration = OfficeStamperConfigurations.standard();
        var stamper = docxPackageStamper(configuration);
        var template = getWordResource(Path.of("TOC.docx"));
        var context = new Object();
        var wordprocessingMLPackage = stamper.stamp(template, context);
        var actual = docxToString(wordprocessingMLPackage);
        var expected = """
                == Table Of Content
                
                
                [toc 1] [instrText= TOC \\o "1-3" \\h \\z \\u ][link data=❬Table Of Content❘{rStyle=Lienhypertexte}❭❬	❘{webHidden=true}❭❬[instrText= PAGEREF _Toc201699773 \\h ]❘{webHidden=true}❭❬1❘{webHidden=true}❭]<tabs=xxx>
                
                [toc 1] [link data=❬First Title❘{rStyle=Lienhypertexte}❭❬	❘{webHidden=true}❭❬[instrText= PAGEREF _Toc201699774 \\h ]❘{webHidden=true}❭❬1❘{webHidden=true}❭]<tabs=xxx>
                
                [toc 2] [link data=❬Subtitle 1.1❘{rStyle=Lienhypertexte}❭❬	❘{webHidden=true}❭❬[instrText= PAGEREF _Toc201699775 \\h ]❘{webHidden=true}❭❬1❘{webHidden=true}❭]<tabs=xxx>
                
                [toc 1] [link data=❬Second Title❘{rStyle=Lienhypertexte}❭❬	❘{webHidden=true}❭❬[instrText= PAGEREF _Toc201699776 \\h ]❘{webHidden=true}❭❬1❘{webHidden=true}❭]<tabs=xxx>
                
                [toc 2] [link data=❬Subtitle 2.1❘{rStyle=Lienhypertexte}❭❬	❘{webHidden=true}❭❬[instrText= PAGEREF _Toc201699777 \\h ]❘{webHidden=true}❭❬1❘{webHidden=true}❭]<tabs=xxx>
                
                [toc 2] [link data=❬Subtitle 2.2❘{rStyle=Lienhypertexte}❭❬	❘{webHidden=true}❭❬[instrText= PAGEREF _Toc201699778 \\h ]❘{webHidden=true}❭❬1❘{webHidden=true}❭]<tabs=xxx>
                
                [toc 2] [link data=❬Subtitle 2.3❘{rStyle=Lienhypertexte}❭❬	❘{webHidden=true}❭❬[instrText= PAGEREF _Toc201699779 \\h ]❘{webHidden=true}❭❬1❘{webHidden=true}❭]<tabs=xxx>
                
                [toc 1] [link data=❬Third Title❘{rStyle=Lienhypertexte}❭❬	❘{webHidden=true}❭❬[instrText= PAGEREF _Toc201699780 \\h ]❘{webHidden=true}❭❬1❘{webHidden=true}❭]<tabs=xxx>
                
                [toc 2] [link data=❬Subtitle 3.1❘{rStyle=Lienhypertexte}❭❬	❘{webHidden=true}❭❬[instrText= PAGEREF _Toc201699781 \\h ]❘{webHidden=true}❭❬1❘{webHidden=true}❭]<tabs=xxx>
                
                [toc 2] [link data=❬Subtitle 3.2❘{rStyle=Lienhypertexte}❭❬	❘{webHidden=true}❭❬[instrText= PAGEREF _Toc201699782 \\h ]❘{webHidden=true}❭❬1❘{webHidden=true}❭]<tabs=xxx>
                
                == First Title
                
                
                === Subtitle 1.1
                
                
                == Second Title
                
                
                === Subtitle 2.1
                
                
                === Subtitle 2.2
                
                
                === Subtitle 2.3
                
                
                == Third Title
                
                
                === Subtitle 3.1
                
                
                === Subtitle 3.2
                
                
                
                
                """;
        assertEquals(expected, actual);
    }

    @Test
    void test64() {
        var configuration = full();
        var testFunction = new TestFunction.TestFunctionImpl();
        configuration.exposeInterfaceToExpressionLanguage(TestFunction.class, testFunction);
        var stamper = docxPackageStamper(configuration);
        var template = makeWordResource("${test()}");
        var context = new Object();
        var wordprocessingMLPackage = stamper.stamp(template, context);
        var actual = docxToString(wordprocessingMLPackage);
        assertEquals("""
                
                
                """, actual);
        assertEquals(1, testFunction.counter());
    }

    @Test
    void test114() {
        var config = standard();
        var stamper = docxPackageStamper(config);
        var template = getWordResource(Path.of("#114.docx"));
        var context = FACTORY.names(List.class, "Homer", "Marge", "Bart", "Lisa", "Maggie");
        var stamped = stamper.stamp(template, context);
        var actual = docxToString(stamped);
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
                
                
                """;
        assertEquals(expected, actual);
    }

    @MethodSource("source52")
    @ParameterizedTest
    void test52(Conditions conditions, String expected) {
        var stamper = docxPackageStamper(OfficeStamperConfigurations.full());
        var template = TestUtils.getWordResource(TEMPLATE_52);
        var wordprocessingMLPackage = stamper.stamp(template, conditions);
        var actual = docxToString(wordprocessingMLPackage);
        assertEquals(expected, actual);
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
