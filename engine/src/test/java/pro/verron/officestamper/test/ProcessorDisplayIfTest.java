package pro.verron.officestamper.test;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import pro.verron.officestamper.test.utils.ContextFactory;

import java.nio.file.Path;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static pro.verron.officestamper.asciidoc.AsciiDocCompiler.toAsciidoc;
import static pro.verron.officestamper.preset.OfficeStamperConfigurations.full;
import static pro.verron.officestamper.preset.OfficeStamperConfigurations.standard;
import static pro.verron.officestamper.preset.OfficeStampers.docxPackageStamper;
import static pro.verron.officestamper.test.utils.ContextFactory.mapContextFactory;
import static pro.verron.officestamper.test.utils.ContextFactory.objectContextFactory;
import static pro.verron.officestamper.test.utils.ResourceUtils.getWordResource;

class ProcessorDisplayIfTest {

    public static Stream<ContextFactory> factories() {
        return Stream.of(objectContextFactory(), mapContextFactory());
    }

    @DisplayName("Display Bart elements")
    @ParameterizedTest
    @MethodSource("factories")
    void conditionalDisplayOfBart(ContextFactory factory) {
        var context = factory.name("Bart");
        var template = getWordResource(Path.of("ProcessorDisplayIf.docx"));
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
                a|!===
                !Really
                !This paragraph stays if “name” is “Bart”.
                !===
                |===
                
                // runPr {color=0F4761, rFonts={asciiTheme=majorHAnsi, cs=majorBidi, eastAsia=majorEastAsia, hAnsi=majorHAnsi}, sz=32, szCs=32}
                
                
                
                <<<
                
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
                a|!===
                !Really'
                !This row stays if “name” is “Bart”.
                !===
                |===
                
                
                
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
                a|!===
                !Cell 2.1, Sub cell 1.1
                !Cell 2.1, Sub cell 2.1
                !===
                |===
                
                
                
                <<<
                
                === Words
                
                These words should appear conditionally:  Bart .
                
                These words should appear conditionally:   Bart Simpson .
                
                // runPr {color=0F4761, rFonts={asciiTheme=majorHAnsi, cs=majorBidi, eastAsia=majorEastAsia, hAnsi=majorHAnsi}, sz=32, szCs=32}
                
                
                
                <<<
                
                === Doc Parts
                
                These 1^sts^ multiple paragraph block stays untouched.
                
                To show how comments spanning multiple paragraphs works.
                
                These 2^nd^ multiple paragraph block stays if “name” is “Bart”.
                
                To show how comments spanning multiple paragraphs works.
                
                These 4^th^ multiple paragraph block stays if “name” is “Bart”.
                
                To show how comments spanning multiple paragraphs works.
                
                These 6^th^ multiple paragraph block stays if “name” is not “null”.
                
                To show how comments spanning multiple paragraphs works.
                
                // section {docGrid={charSpace=-6145, linePitch=240}, pgMar={bottom=1134, left=1134, right=1134, top=1134}, pgSz={h=16838, w=11906}, space=720}
                
                """;

        var config = standard();
        var stamper = docxPackageStamper(config);
        var stamped = stamper.stamp(template, context);
        var actual = toAsciidoc(stamped);
        assertEquals(expected, actual);
    }

    @DisplayName("Display footnotes elements")
    @ParameterizedTest
    @MethodSource("factories")
    void conditionalDisplayOfFootnotes(ContextFactory factory) {
        var context = factory.name("Bart");
        var template = getWordResource(Path.of("ProcessorDisplayIf_Footnotes.docx"));
        var expected = """
                = Springfield Chronicles: The Simpsons Edition
                
                == Introduction
                
                ____
                "Springfield, USA is a town like no other, brought to life through the antics of the Simpson family. Here, in the heart of Springfield, every day is an adventure."
                ____
                
                == Homer Simpson's Favorite Pastimes
                
                == Marge Simpson: The Heart of the Family
                
                Marge Simpson, with her iconic blue hair, is the moral center of the family. She manages the household with the chaos around her, Marge always finds a way to keep the family together.
                
                |===
                [rowStyle=2048]
                [style=512]
                |Character
                |Role
                |Fun Fact
                [style=512]
                |Marge Simpson
                |Matriarch
                |Her hair once hid an entire toolbox[rStyle_Appelnotedebasdep]#footnote:6[]#.
                [rowStyle=32]
                [style=512]
                |Bart Simpson
                |Eldest Child
                |Bart's famous catchphrase is "Eat my shorts!"[rStyle_Appelnotedebasdep]#footnote:7[]#.
                [style=512]
                |Lisa Simpson
                |Middle Child
                |Lisa is a talented saxophonist[rStyle_Appelnotedebasdep]#footnote:8[]#.
                [rowStyle=32]
                [style=512]
                |Maggie Simpson
                |Youngest Child
                |Maggie is known for her pacifier and silent wisdom[rStyle_Appelnotedebasdep]#footnote:9[]#.
                |===
                
                == Conclusion
                
                ____
                "From the simplicity of everyday life to the extraordinary events in Springfield, The Simpsons continue to entertain audiences with their unique charm and wit."
                ____
                
                // section {docGrid={linePitch=360}, pgMar={bottom=1417, footer=708, header=708, left=1417, right=1417, top=1417}, pgSz={h=16838, w=11906}, space=708}
                
                [footnotes]
                --
                6::
                
                 Marge's hairdo was designed to hide various items, a nod to cartoon logic.
                
                7::
                
                 Bart's rebellious attitude is encapsulated in this catchphrase.
                
                8::
                
                 Lisa's musical talent often shines through her saxophone solos.
                
                9::
                
                 Despite her silence, Maggie has saved her family on multiple occasions.
                
                --
                
                
                """;

        var configuration = full();
        var stamper = docxPackageStamper(configuration);
        var stamped = stamper.stamp(template, context);
        var actual = toAsciidoc(stamped);
        assertEquals(expected, actual);
    }

    @DisplayName("Display endnotes elements")
    @ParameterizedTest
    @MethodSource("factories")
    void conditionalDisplayOfEndnotes(ContextFactory factory) {
        var context = factory.name("Bart");
        var template = getWordResource(Path.of("ProcessorDisplayIf_Endnotes.docx"));
        var expected = """
                = Springfield Chronicles: The Simpsons Edition
                
                == Introduction
                
                ____
                "Springfield, USA is a town like no other, brought to life through the antics of the Simpson family. Here, in the heart of Springfield, every day is an adventure."
                ____
                
                == Homer Simpson's Favorite Pastimes
                
                == Marge Simpson: The Heart of the Family
                
                Marge Simpson, with her iconic blue hair, is the moral center of the family. She manages the household with the chaos around her, Marge always finds a way to keep the family together.
                
                |===
                [rowStyle=2048]
                [style=512]
                |Character
                |Role
                |Fun Fact
                [style=512]
                |Marge Simpson
                |Matriarch
                |Her hair once hid an entire toolbox[rStyle_Appeldenotedefin]#footnote:6[]#.
                [rowStyle=32]
                [style=512]
                |Bart Simpson
                |Eldest Child
                |Bart's famous catchphrase is "Eat my shorts!"[rStyle_Appeldenotedefin]#footnote:7[]#.
                [style=512]
                |Lisa Simpson
                |Middle Child
                |Lisa is a talented saxophonist[rStyle_Appeldenotedefin]#footnote:8[]#.
                [rowStyle=32]
                [style=512]
                |Maggie Simpson
                |Youngest Child
                |Maggie is known for her pacifier and silent wisdom[rStyle_Appeldenotedefin]#footnote:9[]#.
                |===
                
                == Conclusion
                
                ____
                "From the simplicity of everyday life to the extraordinary events in Springfield, The Simpsons continue to entertain audiences with their unique charm and wit."
                ____
                
                // section {docGrid={linePitch=360}, pgMar={bottom=1417, footer=708, header=708, left=1417, right=1417, top=1417}, pgSz={h=16838, w=11906}, space=708}
                
                [endnotes]
                --
                6::
                
                 Marge's hairdo was designed to hide various items, a nod to cartoon logic.
                
                7::
                
                 Bart's rebellious attitude is encapsulated in this catchphrase.
                
                8::
                
                 Lisa's musical talent often shines through her saxophone solos.
                
                9::
                
                 Despite her silence, Maggie has saved her family on multiple occasions.
                
                --
                
                
                """;

        var configuration = full();
        var stamper = docxPackageStamper(configuration);
        var stamped = stamper.stamp(template, context);
        var actual = toAsciidoc(stamped);
        assertEquals(expected, actual);
    }

    @DisplayName("Display Homer elements")
    @ParameterizedTest
    @MethodSource("factories")
    void conditionalDisplayOfHomer(ContextFactory factory) {
        var context = factory.name("Homer");
        var template = getWordResource(Path.of("ProcessorDisplayIf.docx"));
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
                a|!===
                !Really
                !
                !===
                |===
                
                // runPr {color=0F4761, rFonts={asciiTheme=majorHAnsi, cs=majorBidi, eastAsia=majorEastAsia, hAnsi=majorHAnsi}, sz=32, szCs=32}
                
                
                
                <<<
                
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
                a|!===
                !Really'
                !===
                |===
                
                
                
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
                
                
                
                <<<
                
                === Words
                
                These words should appear conditionally: Homer  .
                
                These words should appear conditionally: Homer Simpson   .
                
                // runPr {color=0F4761, rFonts={asciiTheme=majorHAnsi, cs=majorBidi, eastAsia=majorEastAsia, hAnsi=majorHAnsi}, sz=32, szCs=32}
                
                
                
                <<<
                
                === Doc Parts
                
                These 1^sts^ multiple paragraph block stays untouched.
                
                To show how comments spanning multiple paragraphs works.
                
                These 3^rd^ multiple paragraph block stays if “name” is not “Bart”.
                
                To show how comments spanning multiple paragraphs works.
                
                These 5^th^ multiple paragraph block stays if “name” is not “Bart”.
                
                To show how comments spanning multiple paragraphs works.
                
                These 6^th^ multiple paragraph block stays if “name” is not “null”.
                
                To show how comments spanning multiple paragraphs works.
                
                // section {docGrid={charSpace=-6145, linePitch=240}, pgMar={bottom=1134, left=1134, right=1134, top=1134}, pgSz={h=16838, w=11906}, space=720}
                
                """;

        var config = standard();
        var stamper = docxPackageStamper(config);
        var stamped = stamper.stamp(template, context);
        var actual = toAsciidoc(stamped);
        assertEquals(expected, actual);
    }

    @DisplayName("Display 'null' elements")
    @ParameterizedTest
    @MethodSource("factories")
    void conditionalDisplayOfAbsentValue(ContextFactory factory) {
        var context = factory.name(null);
        var template = getWordResource(Path.of("ProcessorDisplayIf.docx"));
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
                a|!===
                !Really
                !
                !===
                |===
                
                // runPr {color=0F4761, rFonts={asciiTheme=majorHAnsi, cs=majorBidi, eastAsia=majorEastAsia, hAnsi=majorHAnsi}, sz=32, szCs=32}
                
                
                
                <<<
                
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
                a|!===
                !Really'
                !===
                |===
                
                
                
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
                
                
                
                <<<
                
                === Words
                
                   None.
                
                   No Simpsons.
                
                // runPr {color=0F4761, rFonts={asciiTheme=majorHAnsi, cs=majorBidi, eastAsia=majorEastAsia, hAnsi=majorHAnsi}, sz=32, szCs=32}
                
                
                
                <<<
                
                === Doc Parts
                
                These 1^sts^ multiple paragraph block stays untouched.
                
                To show how comments spanning multiple paragraphs works.
                
                These 3^rd^ multiple paragraph block stays if “name” is not “Bart”.
                
                To show how comments spanning multiple paragraphs works.
                
                These 5^th^ multiple paragraph block stays if “name” is not “Bart”.
                
                To show how comments spanning multiple paragraphs works.
                
                These 7^th^ multiple paragraph block stays if “name” is “null”.
                
                To show how comments spanning multiple paragraphs works.
                
                // section {docGrid={charSpace=-6145, linePitch=240}, pgMar={bottom=1134, left=1134, right=1134, top=1134}, pgSz={h=16838, w=11906}, space=720}
                
                """;

        var config = standard();
        var stamper = docxPackageStamper(config);
        var stamped = stamper.stamp(template, context);
        var actual = toAsciidoc(stamped);
        assertEquals(expected, actual);
    }

    @DisplayName("Display Paragraph If Integration test (off case) + Inline processors Integration test")
    @ParameterizedTest
    @MethodSource("factories")
    void conditionalDisplayOfParagraphsTest_inlineProcessorExpressionsAreResolved(ContextFactory factory) {
        var context = factory.name("Homer");
        var template = getWordResource(Path.of("ProcessorDisplayIf_Inlined.docx"));
        var expected = """
                == Conditional Display of Paragraphs
                
                Paragraph 1 stays untouched.
                
                Paragraph 3 stays untouched.
                
                |===
                a|=== Conditional Display of paragraphs also works in tables
                |Paragraph 4 in cell 2,1 stays untouched.
                |
                a|!===
                a!=== Also works in nested tables
                !Paragraph 6 in cell 2,1 in cell 3,1 stays untouched.
                !===
                |===
                
                
                
                // section {docGrid={charSpace=-6145, linePitch=240}, pgMar={bottom=1134, left=1134, right=1134, top=1134}, pgSz={h=16838, w=11906}, space=720}
                
                """;

        var config = standard();
        var stamper = docxPackageStamper(config);
        var wordprocessingMLPackage = stamper.stamp(template, context);
        var actual = toAsciidoc(wordprocessingMLPackage);
        assertEquals(expected, actual);
    }

    @DisplayName("Display Paragraph If Integration test (on case) + Inline processors Integration test")
    @ParameterizedTest
    @MethodSource("factories")
    void conditionalDisplayOfParagraphsTest_unresolvedInlineProcessorExpressionsAreRemoved(ContextFactory factory) {
        var context = factory.name("Bart");
        var template = getWordResource(Path.of("ProcessorDisplayIf_Inlined.docx"));
        var expected = """
                == Conditional Display of Paragraphs
                
                Paragraph 1 stays untouched.
                
                Paragraph 2 is only included if the “name” is “Bart”.
                
                Paragraph 3 stays untouched.
                
                |===
                a|=== Conditional Display of paragraphs also works in tables
                |Paragraph 4 in cell 2,1 stays untouched.
                |Paragraph 5 in cell 2,2 is only included if the “name” is “Bart”.
                a|!===
                a!=== Also works in nested tables
                a!Paragraph 6 in cell 2,1 in cell 3,1 stays untouched.
                
                Paragraph 7  in cell 2,1 in cell 3,1 is only included if the “name” is “Bart”.
                !===
                |===
                
                
                
                // section {docGrid={charSpace=-6145, linePitch=240}, pgMar={bottom=1134, left=1134, right=1134, top=1134}, pgSz={h=16838, w=11906}, space=720}
                
                """;

        var config = standard();
        var stamper = docxPackageStamper(config);
        var stamped = stamper.stamp(template, context);
        var actual = toAsciidoc(stamped);
        assertEquals(expected, actual);
    }

    @DisplayName("Display Table If Bug32 Regression test")
    @ParameterizedTest
    @MethodSource("factories")
    void conditionalDisplayOfTableRowsTest(ContextFactory factory) {
        var context = factory.name("Homer");
        var template = getWordResource(Path.of("ProcessorDisplayIf_TableRows.docx"));
        var expected = """
                == Conditional Display of Table Rows
                
                This paragraph stays untouched.
                
                |===
                [rowStyle=32]
                |This row stays untouched.
                [rowStyle=32]
                |This row stays untouched.
                [rowStyle=32]
                a|!===
                [rowStyle=2048]
                !Also works on nested Tables
                [rowStyle=32]
                !This row stays untouched.
                !===
                |===
                
                
                
                // section {docGrid={charSpace=-6145, linePitch=240}, pgMar={bottom=1134, left=1134, right=1134, top=1134}, pgSz={h=16838, w=11906}, space=720}
                
                """;

        var config = standard();
        var stamper = docxPackageStamper(config);
        var wordprocessingMLPackage = stamper.stamp(template, context);
        var actual = toAsciidoc(wordprocessingMLPackage);
        assertEquals(expected, actual);
    }

    @DisplayName("Display Table If Bug32 Regression test")
    @ParameterizedTest
    @MethodSource("factories")
    void conditionalDisplayOfTableBug32Test(ContextFactory factory) {
        var context = factory.name("Homer");
        var template = getWordResource(Path.of("ProcessorDisplayIf_#32.docx"));
        var expected = """
                == Conditional Display of Tables
                
                This paragraph stays untouched.
                
                
                
                |===
                [rowStyle=2048]
                [style=512]
                |This table stays untouched.
                |
                [rowStyle=32]
                [style=512]
                |
                |
                |===
                
                
                
                |===
                [rowStyle=2048]
                [style=512]
                |Also works on nested tables
                [rowStyle=32]
                [style=512]
                |
                |===
                
                
                
                This paragraph stays untouched.
                
                // section {docGrid={linePitch=240}, pgMar={bottom=1134, left=1134, right=1134, top=1134}, pgSz={h=16838, w=11906}, space=720}
                
                """;

        var config = standard();
        var stamper = docxPackageStamper(config);
        var stamped = stamper.stamp(template, context);
        var actual = toAsciidoc(stamped);
        assertEquals(expected, actual);
    }

    @DisplayName("Display Table If Integration test")
    @ParameterizedTest
    @MethodSource("factories")
    void conditionalDisplayOfTableTest(ContextFactory factory) {
        var context = factory.name("Homer");
        var template = getWordResource(Path.of("ProcessorDisplayIf_Tables.docx"));
        var expected = """
                == Conditional Display of Tables
                
                This paragraph stays untouched.
                
                
                
                |===
                [rowStyle=32]
                |This table stays untouched.
                |
                |
                |
                |===
                
                
                
                |===
                [rowStyle=2048]
                |Also works on nested tables
                [rowStyle=32]
                |
                |===
                
                
                
                This paragraph stays untouched.
                
                // section {docGrid={charSpace=-6145, linePitch=240}, pgMar={bottom=1134, left=1134, right=1134, top=1134}, pgSz={h=16838, w=11906}, space=720}
                
                """;
        var config = standard();
        var stamper = docxPackageStamper(config);
        var stamped = stamper.stamp(template, context);
        var actual = toAsciidoc(stamped);
        assertEquals(expected, actual);
    }
}
