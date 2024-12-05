package pro.verron.officestamper.test;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import pro.verron.officestamper.api.OfficeStamperConfiguration;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static pro.verron.officestamper.preset.OfficeStamperConfigurations.standard;
import static pro.verron.officestamper.test.ContextFactory.mapContextFactory;
import static pro.verron.officestamper.test.ContextFactory.objectContextFactory;
import static pro.verron.officestamper.test.TestUtils.getResource;

class ConditionalDisplayTest {
    @TestFactory
    Stream<DynamicTest> tests() {
        return Stream.of(conditionalDisplayOfBart(objectContextFactory()),
                conditionalDisplayOfHomer(objectContextFactory()),
                conditionalDisplayOfAbsentValue(objectContextFactory()),
                conditionalDisplayOfParagraphsTest_inlineProcessorExpressionsAreResolved(objectContextFactory()),
                conditionalDisplayOfParagraphsTest_unresolvedInlineProcessorExpressionsAreRemoved(objectContextFactory()),
                conditionalDisplayOfTableRowsTest(objectContextFactory()),
                conditionalDisplayOfTableBug32Test(objectContextFactory()),
                conditionalDisplayOfTableTest(objectContextFactory()),
                conditionalDisplayOfBart(mapContextFactory()),
                conditionalDisplayOfHomer(mapContextFactory()),
                conditionalDisplayOfAbsentValue(mapContextFactory()),
                conditionalDisplayOfParagraphsTest_inlineProcessorExpressionsAreResolved(mapContextFactory()),
                conditionalDisplayOfParagraphsTest_unresolvedInlineProcessorExpressionsAreRemoved(mapContextFactory()),
                conditionalDisplayOfTableRowsTest(mapContextFactory()),
                conditionalDisplayOfTableBug32Test(mapContextFactory()),
                conditionalDisplayOfTableTest(mapContextFactory()));
    }

    private static DynamicTest conditionalDisplayOfBart(ContextFactory factory) {
        var context = factory.name("Bart");
        var template = getResource(Path.of("ConditionalDisplayTest.docx"));
        var expected = """
                == Conditional Display
                
                === Paragraphs
                
                This paragraph 1 stays untouched.
                This paragraph 2 stays if “name” is “Bart”.
                This paragraph 4 stays if “name” is “Bart”.
                This paragraph 6 stays if “name” is not null.
                This paragraph 7 stays if “name” is not null.
                ==== Paragraphs in table
                
                |===
                |Works in tables
                
                |
                |This paragraph 1.2 stays if “name” is “Bart”.
                
                |This paragraph 2.1 stays if “name” is “Bart”.
                |This paragraph 2.2 stays if “name” is not null.
                
                
                |===
                ==== Paragraphs in nested table
                
                |===
                |Works in nested tables
                
                ||===
                |Really
                
                |This paragraph stays if “name” is “Bart”.
                
                
                |===
                
                
                |===
                
                [page-break]
                <<<
                <rPr={color=0F4761,rFont={asciiTheme=majorHAnsi,cstheme=majorBidi,eastAsiaTheme=majorEastAsia,hAnsiTheme=majorHAnsi}}>
                === Table Rows
                
                ==== Rows in table
                
                |===
                |Works in tables
                
                |This row 1 is:
                |Untouched.
                
                |This row 2 stays:
                |if “name” is “Bart”.
                
                |This row 4 stays:
                |if “name” is “Bart”.
                
                |This row 6 stays:
                |if “name” is not null.
                
                |This row 7 stays:
                |if “name” is not null.
                
                
                |===
                ==== Rows in nested table
                
                |===
                |Works in nested tables
                
                ||===
                |Really'
                
                |This row stays if “name” is “Bart”.
                
                
                |===
                
                
                |===
                
                [page-break]
                <<<
                
                === Tables
                
                ==== Mono-cell fully commented.
                
                |===
                |A mono-cell table.
                
                
                |===
                ==== Mono-cell partially commented.
                
                |===
                |Another mono-cell table.
                
                
                |===
                ==== Multi-cell fully commented.
                
                |===
                |Cell 1.1
                |Cell 1.2
                
                |Cell 2.1
                |Cell 2.2
                
                
                |===
                ==== Multi-cell partially commented.
                
                |===
                |Cell 1.1
                |Cell 1.2
                
                |Cell 2.1
                |Cell 2.2
                
                
                |===
                ==== If present Case.
                
                |===
                |Cell 1.1
                |Cell 1.2
                
                |Cell 2.1
                |Cell 2.2
                
                
                |===
                ==== If absent Case.
                
                ==== Works in nested tables
                
                |===
                |Cell 1.1
                
                ||===
                |Cell 2.1, Sub cell 1.1
                
                |Cell 2.1, Sub cell 2.1
                
                
                |===
                
                
                |===
                
                [page-break]
                <<<
                
                === Words
                
                These words should appear conditionally:  Bart .
                These words should appear conditionally:   Bart Simpson .
                
                [page-break]
                <<<
                <rPr={color=0F4761,rFont={asciiTheme=majorHAnsi,cstheme=majorBidi,eastAsiaTheme=majorEastAsia,hAnsiTheme=majorHAnsi}}>
                === Doc Parts
                
                These 1❬sts❘{vertAlign=superscript}❭ multiple paragraph block stays untouched.
                To show how comments spanning multiple paragraphs works.
                These 2❬nd❘{vertAlign=superscript}❭ multiple paragraph block stays if “name” is “Bart”.
                To show how comments spanning multiple paragraphs works.
                These 4❬th❘{vertAlign=superscript}❭ multiple paragraph block stays if “name” is “Bart”.
                To show how comments spanning multiple paragraphs works.
                These 6❬th❘{vertAlign=superscript}❭ multiple paragraph block stays if “name” is not “null”.
                To show how comments spanning multiple paragraphs works.
                """;

        return getDynamicTest("Display Bart elements", standard(), context, template, expected);
    }

    private static DynamicTest conditionalDisplayOfHomer(ContextFactory factory) {
        var context = factory.name("Homer");
        var template = getResource(Path.of("ConditionalDisplayTest.docx"));
        var expected = """
                == Conditional Display
                
                === Paragraphs
                
                This paragraph 1 stays untouched.
                This paragraph 3 stays if “name” is not “Bart”.
                This paragraph 5 stays if “name” is not “Bart”.
                This paragraph 6 stays if “name” is not null.
                This paragraph 7 stays if “name” is not null.
                ==== Paragraphs in table
                
                |===
                |Works in tables
                
                |
                |
                
                |
                |This paragraph 2.2 stays if “name” is not null.
                
                
                |===
                ==== Paragraphs in nested table
                
                |===
                |Works in nested tables
                
                ||===
                |Really
                
                |
                
                
                |===
                
                
                |===
                
                [page-break]
                <<<
                <rPr={color=0F4761,rFont={asciiTheme=majorHAnsi,cstheme=majorBidi,eastAsiaTheme=majorEastAsia,hAnsiTheme=majorHAnsi}}>
                === Table Rows
                
                ==== Rows in table
                
                |===
                |Works in tables
                
                |This row 1 is:
                |Untouched.
                
                |This row 3 stays:
                |if “name” is not “Bart”.
                
                |This row 5 stays:
                |if “name” is not “Bart”.
                
                |This row 6 stays:
                |if “name” is not null.
                
                |This row 7 stays:
                |if “name” is not null.
                
                
                |===
                ==== Rows in nested table
                
                |===
                |Works in nested tables
                
                ||===
                |Really'
                
                
                |===
                
                
                |===
                
                [page-break]
                <<<
                
                === Tables
                
                ==== Mono-cell fully commented.
                
                ==== Mono-cell partially commented.
                
                ==== Multi-cell fully commented.
                
                ==== Multi-cell partially commented.
                
                ==== If present Case.
                
                |===
                |Cell 1.1
                |Cell 1.2
                
                |Cell 2.1
                |Cell 2.2
                
                
                |===
                ==== If absent Case.
                
                ==== Works in nested tables
                
                |===
                |Cell 1.1
                
                |
                
                
                |===
                
                [page-break]
                <<<
                
                === Words
                
                These words should appear conditionally: Homer  .
                These words should appear conditionally: Homer Simpson   .
                
                [page-break]
                <<<
                <rPr={color=0F4761,rFont={asciiTheme=majorHAnsi,cstheme=majorBidi,eastAsiaTheme=majorEastAsia,hAnsiTheme=majorHAnsi}}>
                === Doc Parts
                
                These 1❬sts❘{vertAlign=superscript}❭ multiple paragraph block stays untouched.
                To show how comments spanning multiple paragraphs works.
                These 3❬rd❘{vertAlign=superscript}❭ multiple paragraph block stays if “name” is not “Bart”.
                To show how comments spanning multiple paragraphs works.
                These 5❬th❘{vertAlign=superscript}❭ multiple paragraph block stays if “name” is not “Bart”.
                To show how comments spanning multiple paragraphs works.
                These 6❬th❘{vertAlign=superscript}❭ multiple paragraph block stays if “name” is not “null”.
                To show how comments spanning multiple paragraphs works.
                """;

        return getDynamicTest("Display Homer elements", standard(), context, template, expected);
    }

    private static DynamicTest conditionalDisplayOfAbsentValue(ContextFactory factory) {
        var context = factory.name(null);
        var template = getResource(Path.of("ConditionalDisplayTest.docx"));
        var expected = """
                == Conditional Display
                
                === Paragraphs
                
                This paragraph 1 stays untouched.
                This paragraph 3 stays if “name” is not “Bart”.
                This paragraph 5 stays if “name” is not “Bart”.
                This paragraph 8 stays if “name” is null.
                This paragraph 9 stays if “name” is null.
                ==== Paragraphs in table
                
                |===
                |Works in tables
                
                |This paragraph 1.1 stays if “name” is null.
                |
                
                |
                |
                
                
                |===
                ==== Paragraphs in nested table
                
                |===
                |Works in nested tables
                
                ||===
                |Really
                
                |
                
                
                |===
                
                
                |===
                
                [page-break]
                <<<
                <rPr={color=0F4761,rFont={asciiTheme=majorHAnsi,cstheme=majorBidi,eastAsiaTheme=majorEastAsia,hAnsiTheme=majorHAnsi}}>
                === Table Rows
                
                ==== Rows in table
                
                |===
                |Works in tables
                
                |This row 1 is:
                |Untouched.
                
                |This row 3 stays:
                |if “name” is not “Bart”.
                
                |This row 5 stays:
                |if “name” is not “Bart”.
                
                |This row 8 stays:
                |if “name” is null.
                
                |This row 9 stays:
                |if “name” is null.
                
                
                |===
                ==== Rows in nested table
                
                |===
                |Works in nested tables
                
                ||===
                |Really'
                
                
                |===
                
                
                |===
                
                [page-break]
                <<<
                
                === Tables
                
                ==== Mono-cell fully commented.
                
                ==== Mono-cell partially commented.
                
                ==== Multi-cell fully commented.
                
                ==== Multi-cell partially commented.
                
                ==== If present Case.
                
                ==== If absent Case.
                
                |===
                |Cell 1.1
                |Cell 1.2
                
                |Cell 2.1
                |Cell 2.2
                
                
                |===
                ==== Works in nested tables
                
                |===
                |Cell 1.1
                
                |
                
                
                |===
                
                [page-break]
                <<<
                
                === Words
                
                   None.
                   No Simpsons.
                
                [page-break]
                <<<
                <rPr={color=0F4761,rFont={asciiTheme=majorHAnsi,cstheme=majorBidi,eastAsiaTheme=majorEastAsia,hAnsiTheme=majorHAnsi}}>
                === Doc Parts
                
                These 1❬sts❘{vertAlign=superscript}❭ multiple paragraph block stays untouched.
                To show how comments spanning multiple paragraphs works.
                These 3❬rd❘{vertAlign=superscript}❭ multiple paragraph block stays if “name” is not “Bart”.
                To show how comments spanning multiple paragraphs works.
                These 5❬th❘{vertAlign=superscript}❭ multiple paragraph block stays if “name” is not “Bart”.
                To show how comments spanning multiple paragraphs works.
                These 7❬th❘{vertAlign=superscript}❭ multiple paragraph block stays if “name” is “null”.
                To show how comments spanning multiple paragraphs works.
                """;

        return getDynamicTest("Display 'null' elements", standard(), context, template, expected);
    }

    private static DynamicTest conditionalDisplayOfParagraphsTest_inlineProcessorExpressionsAreResolved(ContextFactory factory) {
        var context = factory.name("Homer");
        var template = getResource(Path.of("ConditionalDisplayOfParagraphsWithoutCommentTest.docx"));
        var expected = """
                == Conditional Display of Paragraphs
                
                Paragraph 1 stays untouched.
                Paragraph 3 stays untouched.
                |===
                |=== Conditional Display of paragraphs also works in tables
                
                |Paragraph 4 in cell 2,1 stays untouched.
                |
                
                ||===
                |=== Also works in nested tables
                
                |Paragraph 6 in cell 2,1 in cell 3,1 stays untouched.
                
                
                |===
                
                
                |===
                
                """;
        return getDynamicTest("Display Paragraph If Integration test (off case) + Inline processors Integration test",
                standard(),
                context,
                template,
                expected);
    }

    private static DynamicTest conditionalDisplayOfParagraphsTest_unresolvedInlineProcessorExpressionsAreRemoved(
            ContextFactory factory
    ) {
        var context = factory.name("Bart");
        var template = getResource(Path.of("ConditionalDisplayOfParagraphsWithoutCommentTest.docx"));
        var expected = """
                == Conditional Display of Paragraphs
                
                Paragraph 1 stays untouched.
                Paragraph 2 is only included if the “name” is “Bart”.
                Paragraph 3 stays untouched.
                |===
                |=== Conditional Display of paragraphs also works in tables
                
                |Paragraph 4 in cell 2,1 stays untouched.
                |Paragraph 5 in cell 2,2 is only included if the “name” is “Bart”.
                
                ||===
                |=== Also works in nested tables
                
                |Paragraph 6 in cell 2,1 in cell 3,1 stays untouched.
                Paragraph 7  in cell 2,1 in cell 3,1 is only included if the “name” is “Bart”.
                
                
                |===
                
                
                |===
                
                """;
        return getDynamicTest("Display Paragraph If Integration test (on case) + Inline processors Integration test",
                standard(),
                context,
                template,
                expected);
    }

    private static DynamicTest conditionalDisplayOfTableRowsTest(ContextFactory factory) {
        var context = factory.name("Homer");
        var template = getResource(Path.of("ConditionalDisplayOfTableRowsTest.docx"));
        var expected = """
                == Conditional Display of Table Rows
                
                This paragraph stays untouched.
                |===
                |This row stays untouched.
                
                |This row stays untouched.
                
                ||===
                |Also works on nested Tables
                
                |This row stays untouched.
                
                
                |===
                
                
                |===
                
                """;
        return getDynamicTest("Display Table Row If Integration test", standard(), context, template, expected);
    }

    private static DynamicTest conditionalDisplayOfTableBug32Test(ContextFactory factory) {
        var context = factory.name("Homer");
        var template = getResource(Path.of("ConditionalDisplayOfTablesBug32Test.docx"));
        var expected = """
                == Conditional Display of Tables
                
                This paragraph stays untouched.
                
                |===
                |This table stays untouched.
                |<cnfStyle=100000000000>
                
                |
                |<cnfStyle=000000100000>
                
                
                |===
                
                |===
                |Also works on nested tables
                
                |
                
                
                |===
                
                This paragraph stays untouched.
                """;
        return getDynamicTest("Display Table If Bug32 Regression test", standard(), context, template, expected);
    }

    private static DynamicTest conditionalDisplayOfTableTest(ContextFactory factory) {
        var context = factory.name("Homer");
        var template = getResource(Path.of("ConditionalDisplayOfTablesTest.docx"));
        var expected = """
                == Conditional Display of Tables
                
                This paragraph stays untouched.
                
                |===
                |This table stays untouched.
                |
                
                |
                |
                
                
                |===
                
                |===
                |Also works on nested tables
                
                |
                
                
                |===
                
                This paragraph stays untouched.
                """;
        return getDynamicTest("Display Table If Integration test", standard(), context, template, expected);
    }

    private static DynamicTest getDynamicTest(
            String displayName,
            OfficeStamperConfiguration config,
            Object context,
            InputStream template,
            Object expected
    ) {
        return DynamicTest.dynamicTest(displayName, () -> {
            TestDocxStamper<Object> stamper = new TestDocxStamper<>(config);
            var actual = stamper.stampAndLoadAndExtract(template, context);
            assertEquals(expected, actual);
        });
    }

}
