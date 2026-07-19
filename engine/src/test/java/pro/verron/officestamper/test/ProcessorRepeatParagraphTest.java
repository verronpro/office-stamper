package pro.verron.officestamper.test;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.verron.officestamper.test.utils.ContextFactory;
import pro.verron.officestamper.test.utils.ObjectContextFactory;
import pro.verron.officestamper.test.utils.OfficeStamperTestBase;
import pro.verron.officestamper.test.utils.ResourceUtils;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import static pro.verron.officestamper.preset.OfficeStamperConfigurations.standard;
import static pro.verron.officestamper.test.utils.DocxFactory.makeWordResource;
import static pro.verron.officestamper.test.utils.ResourceUtils.getWordResource;

class ProcessorRepeatParagraphTest extends OfficeStamperTestBase {
    public static final ObjectContextFactory FACTORY = new ObjectContextFactory();
    private static final Logger log = LoggerFactory.getLogger(ProcessorRepeatParagraphTest.class);

    @MethodSource("factories")
    @ParameterizedTest(name = "In multiple layouts, keeps section orientations inside RepeatParagraph comments")
    public void shouldKeepSectionBreakOrientationWithSectionBreakInsideComment(ContextFactory factory) {
        var context = factory.coupleContext();
        var template = getWordResource(Path.of("ProcessorRepeatParagraph_InLayout.docx"));
        var expected = """
                First page is landscape.
                
                
                
                
                
                // section {docGrid={linePitch=360}, pgMar={bottom=1418, footer=709, header=709, left=1418, right=1418, top=1418}, pgSz={h=11906, orient=landscape, w=16838}, space=708}
                
                Second page is portrait, layout change should survive to repeatParagraph processor (Homer).
                
                
                
                
                
                // section {docGrid={linePitch=360}, pgMar={bottom=1418, footer=709, header=709, left=1418, right=1418, top=1418}, pgSz={h=16838, w=11906}, space=708}
                
                With a page break changing the layout in between.
                
                // section {docGrid={linePitch=360}, pgMar={bottom=1418, footer=709, header=709, left=1418, right=1418, top=1418}, pgSz={h=11906, orient=landscape, w=16838}, space=708}
                
                Second page is portrait, layout change should survive to repeatParagraph processor (Marge).
                
                
                
                
                
                // section {docGrid={linePitch=360}, pgMar={bottom=1418, footer=709, header=709, left=1418, right=1418, top=1418}, pgSz={h=16838, w=11906}, space=708}
                
                With a page break changing the layout in between.
                
                
                
                // section {docGrid={linePitch=360}, pgMar={bottom=1418, footer=709, header=709, left=1418, right=1418, top=1418}, pgSz={h=11906, orient=landscape, w=16838}, space=708}
                
                Fourth page is set to portrait again.
                
                // section {docGrid={linePitch=360}, pgMar={bottom=1418, footer=709, header=709, left=1418, right=1418, top=1418}, pgSz={h=16838, w=11906}, space=708}
                
                """;

        var config = standard();
        testStamper(config, context, template, expected);

    }

    @MethodSource("factories")
    @ParameterizedTest(name = "In multiple layouts, keeps section orientations outside RepeatParagraph comments")
    public void shouldKeepSectionBreakOrientationWithoutSectionBreakInsideComment(ContextFactory factory) {
        var expected = """
                First page is landscape.
                
                
                
                
                
                // section {docGrid={linePitch=360}, pgMar={bottom=1418, footer=709, header=709, left=1418, right=1418, top=1418}, pgSz={h=11906, orient=landscape, w=16838}, space=708}
                
                Second page is portrait, layout change should survive to repeatParagraph processor (Homer).
                
                
                
                Without a section break changing the layout in between, but a page break instead.
                
                <<<
                
                Second page is portrait, layout change should survive to repeatParagraph processor (Marge).
                
                
                
                Without a section break changing the layout in between, but a page break instead.
                
                <<<
                
                
                
                // section {docGrid={linePitch=360}, pgMar={bottom=1418, footer=709, header=709, left=1418, right=1418, top=1418}, pgSz={h=16838, w=11906}, space=708}
                
                Fourth page is set to landscape again.
                
                // section {docGrid={linePitch=360}, pgMar={bottom=1418, footer=709, header=709, left=1418, right=1418, top=1418}, pgSz={h=11906, orient=landscape, w=16838}, space=708}
                
                """;
        var context = Map.of("repeatValues", List.of(factory.name("Homer"), factory.name("Marge")));
        var template = getWordResource(Path.of("ProcessorRepeatParagraph_OutLayout.docx"));
        testStamper(standard(), context, template, expected);
    }

    @MethodSource("factories")
    @ParameterizedTest(name = "Repeat Paragraph Integration test")
    public void repeatParagraphTest(ContextFactory factory) {
        var context = factory.roles("Homer Simpson",
                "Dan Castellaneta",
                "Marge Simpson",
                "Julie Kavner",
                "Bart Simpson",
                "Nancy Cartwright",
                "Kent Brockman",
                "Harry Shearer",
                "Disco Stu",
                "Hank Azaria",
                "Krusty the Clown",
                "Dan Castellaneta"
        );
        var template = getWordResource(Path.of("ProcessorRepeatParagraph.docx"));
        var expected = """
                == Characters 1 line
                
                Homer Simpson: Dan Castellaneta
                
                Marge Simpson: Julie Kavner
                
                Bart Simpson: Nancy Cartwright
                
                Kent Brockman: Harry Shearer
                
                Disco Stu: Hank Azaria
                
                Krusty the Clown: Dan Castellaneta
                
                There are 6 characters.
                
                == Characters multi-line
                
                === Homer Simpson
                
                Actor: Dan Castellaneta
                
                === Marge Simpson
                
                Actor: Julie Kavner
                
                === Bart Simpson
                
                Actor: Nancy Cartwright
                
                === Kent Brockman
                
                Actor: Harry Shearer
                
                === Disco Stu
                
                Actor: Hank Azaria
                
                === Krusty the Clown
                
                Actor: Dan Castellaneta
                
                There are 6 characters.
                
                // section {docGrid={charSpace=-6145, linePitch=240}, pgMar={bottom=1134, left=1134, right=1134, top=1134}, pgSz={h=16838, w=11906}, space=720}
                
                """;
        testStamper(standard(), context, template, expected);
    }

    @Test
    void shouldAcceptList() {
        var config = standard();
        var template = makeWordResource("""
                comment::1[start="0,0", end="0,7", value="repeatParagraph(names)"]
                ${name}
                """);
        var context = FACTORY.names(List.class, "Homer", "Marge", "Bart", "Lisa", "Maggie");
        var expected = """
                Homer
                
                Marge
                
                Bart
                
                Lisa
                
                Maggie
                
                // section {pgMar={bottom=1440, left=1440, right=1440, top=1440}, pgSz={code=9, h=16839, w=11907}}
                
                """;
        testStamper(config, context, template, expected);
    }

    @Test
    void shouldAcceptSet() {
        var config = standard();
        var template = makeWordResource("""
                comment::1[start="0,0", end="0,7", value="repeatParagraph(names)"]
                ${name}
                """);
        var context = FACTORY.names(Set.class, "Homer", "Marge", "Bart", "Lisa", "Maggie");
        var expected = """
                Marge
                
                Homer
                
                Maggie
                
                Bart
                
                Lisa
                
                // section {pgMar={bottom=1440, left=1440, right=1440, top=1440}, pgSz={code=9, h=16839, w=11907}}
                
                """;
        testStamper(config, context, template, expected);
    }

    @Test
    void shouldAcceptQueue() {
        var config = standard();
        var template = makeWordResource("""
                comment::1[start="0,0", end="0,7", value="repeatParagraph(names)"]
                ${name}
                """);
        var context = FACTORY.names(Queue.class, "Homer", "Marge", "Bart", "Lisa", "Maggie");
        var expected = """
                Homer
                
                Marge
                
                Bart
                
                Lisa
                
                Maggie
                
                // section {pgMar={bottom=1440, left=1440, right=1440, top=1440}, pgSz={code=9, h=16839, w=11907}}
                
                """;
        testStamper(config, context, template, expected);
    }
}
