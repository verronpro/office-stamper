package pro.verron.officestamper.test;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.verron.officestamper.api.OfficeStamperConfiguration;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static pro.verron.officestamper.preset.OfficeStamperConfigurations.standard;
import static pro.verron.officestamper.test.ContextFactory.mapContextFactory;
import static pro.verron.officestamper.test.ContextFactory.objectContextFactory;
import static pro.verron.officestamper.test.TestUtils.getResource;
import static pro.verron.officestamper.test.TestUtils.makeResource;

class ProcessorRepeatParagraphTest {
    public static final ObjectContextFactory FACTORY = new ObjectContextFactory();
    private static final Logger log = LoggerFactory.getLogger(ProcessorRepeatParagraphTest.class);

    private static Stream<Arguments> tests() {
        return factories().mapMulti((factory, pipe) -> {
            pipe.accept(repeatParagraphTest(factory));
            pipe.accept(shouldKeepSectionBreakOrientationWithoutSectionBreakInsideComment(factory));
            pipe.accept(shouldKeepSectionBreakOrientationWithSectionBreakInsideComment(factory));
        });
    }

    static Stream<ContextFactory> factories() {
        return Stream.of(objectContextFactory(), mapContextFactory());
    }

    private static Arguments repeatParagraphTest(ContextFactory factory) {
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
                "Dan Castellaneta");
        var template = getResource(Path.of("ProcessorRepeatParagraph.docx"));
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
                """;

        return arguments("Repeat Paragraph Integration test", standard(), context, template, expected);
    }

    private static Arguments shouldKeepSectionBreakOrientationWithoutSectionBreakInsideComment(
            ContextFactory factory
    ) {
        return arguments("In multiple layouts, keeps section orientations outside RepeatParagraph comments",
                standard(),
                Map.of("repeatValues", List.of(factory.name("Homer"), factory.name("Marge"))),
                getResource(Path.of("ProcessorRepeatParagraph_OutLayout.docx")),
                """
                        First page is landscape.
                        
                        
                        [section-break, {docGrid={linePitch=360},pgMar={bottom=1418,footer=709,gutter=0,header=709,left=1418,right=1418,top=1418},pgSz={h=11906,orient=LANDSCAPE,w=16838}}]
                        <<<
                        Second page is portrait, layout change should survive to repeatParagraph processor (Homer).
                        
                        Without a section break changing the layout in between, but a page break instead.
                        [page-break]
                        <<<
                        
                        Second page is portrait, layout change should survive to repeatParagraph processor (Marge).
                        
                        Without a section break changing the layout in between, but a page break instead.
                        [page-break]
                        <<<
                        
                        
                        [section-break, {docGrid={linePitch=360},pgMar={bottom=1418,footer=709,gutter=0,header=709,left=1418,right=1418,top=1418},pgSz={h=16838,w=11906}}]
                        <<<
                        Fourth page is set to landscape again.
                        """);
    }

    private static Arguments shouldKeepSectionBreakOrientationWithSectionBreakInsideComment(
            ContextFactory factory
    ) {
        var context = factory.coupleContext();
        var template = getResource(Path.of("ProcessorRepeatParagraph_InLayout.docx"));
        var expected = """
                First page is landscape.
                
                
                [section-break, {docGrid={linePitch=360},pgMar={bottom=1418,footer=709,gutter=0,header=709,left=1418,right=1418,top=1418},pgSz={h=11906,orient=LANDSCAPE,w=16838}}]
                <<<
                Second page is portrait, layout change should survive to repeatParagraph processor (Homer).
                
                
                [section-break, {docGrid={linePitch=360},pgMar={bottom=1418,footer=709,gutter=0,header=709,left=1418,right=1418,top=1418},pgSz={h=16838,w=11906}}]
                <<<
                With a page break changing the layout in between.
                [section-break, {docGrid={linePitch=360},pgMar={bottom=1418,footer=709,gutter=0,header=709,left=1418,right=1418,top=1418},pgSz={h=11906,orient=LANDSCAPE,w=16838}}]
                <<<
                Second page is portrait, layout change should survive to repeatParagraph processor (Marge).
                
                
                [section-break, {docGrid={linePitch=360},pgMar={bottom=1418,footer=709,gutter=0,header=709,left=1418,right=1418,top=1418},pgSz={h=16838,w=11906}}]
                <<<
                With a page break changing the layout in between.
                
                [section-break, {docGrid={linePitch=360},pgMar={bottom=1418,footer=709,gutter=0,header=709,left=1418,right=1418,top=1418},pgSz={h=11906,orient=LANDSCAPE,w=16838}}]
                <<<
                Fourth page is set to portrait again.
                """;

        var config = standard();
        return arguments("In multiple layouts, keeps section orientations inside RepeatParagraph comments",
                config,
                context,
                template,
                expected);
    }

    @MethodSource("tests")
    @ParameterizedTest(name = "{0}")
    void features(
            String name,
            OfficeStamperConfiguration config,
            Object context,
            InputStream template,
            String expected
    ) {
        log.info(name);
        var stamper = new TestDocxStamper<>(config);
        var actual = stamper.stampAndLoadAndExtract(template, context);
        assertEquals(expected, actual);
    }

    @Test
    void shouldAcceptList() {
        var config = standard();
        var stamper = new TestDocxStamper<>(config);
        var template = makeResource("<1|>${name}<|1><1|repeatParagraph(names)>");
        var context = FACTORY.names(List.class, "Homer", "Marge", "Bart", "Lisa", "Maggie");
        var actual = stamper.stampAndLoadAndExtract(template, context);
        var expected = """
                Homer
                Marge
                Bart
                Lisa
                Maggie
                """;
        assertEquals(expected, actual);
    }

    @Test
    void shouldAcceptSet() {
        var config = standard();
        var stamper = new TestDocxStamper<>(config);
        var template = makeResource("<1|>${name}<|1><1|repeatParagraph(names)>");
        var context = FACTORY.names(Set.class, "Homer", "Marge", "Bart", "Lisa", "Maggie");
        var actual = stamper.stampAndLoadAndExtract(template, context);
        var expected = """
                Marge
                Homer
                Maggie
                Bart
                Lisa
                """;
        assertEquals(expected, actual);
    }

    @Test
    void shouldAcceptQueue() {
        var config = standard();
        var stamper = new TestDocxStamper<>(config);
        var template = makeResource("<1|>${name}<|1><1|repeatParagraph(names)>");
        var context = FACTORY.names(Queue.class, "Homer", "Marge", "Bart", "Lisa", "Maggie");
        var actual = stamper.stampAndLoadAndExtract(template, context);
        var expected = """
                Homer
                Marge
                Bart
                Lisa
                Maggie
                """;
        assertEquals(expected, actual);
    }
}
