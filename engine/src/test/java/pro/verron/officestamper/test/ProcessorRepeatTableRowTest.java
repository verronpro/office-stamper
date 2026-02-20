package pro.verron.officestamper.test;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.verron.officestamper.api.OfficeStamperConfiguration;
import pro.verron.officestamper.test.utils.ContextFactory;
import pro.verron.officestamper.test.utils.ObjectContextFactory;

import java.nio.file.Path;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.of;
import static pro.verron.officestamper.asciidoc.AsciiDocCompiler.toAsciidoc;
import static pro.verron.officestamper.preset.OfficeStamperConfigurations.full;
import static pro.verron.officestamper.preset.OfficeStampers.docxPackageStamper;
import static pro.verron.officestamper.test.utils.ContextFactory.mapContextFactory;
import static pro.verron.officestamper.test.utils.ContextFactory.objectContextFactory;
import static pro.verron.officestamper.test.utils.DocxFactory.makeWordResource;
import static pro.verron.officestamper.test.utils.ResourceUtils.getWordResource;

class ProcessorRepeatTableRowTest {
    private static final ObjectContextFactory FACTORY = new ObjectContextFactory();
    private static final Logger log = LoggerFactory.getLogger(ProcessorRepeatTableRowTest.class);

    private static Stream<Arguments> tests() {
        return factories().mapMulti((factory, pipe) -> {
            pipe.accept(repeatingRows(factory));
            pipe.accept(repeatingRowsWithLineBreak(factory));
            pipe.accept(repeatTableRowKeepsFormatTest(factory));
        });
    }

    static Stream<ContextFactory> factories() {
        return Stream.of(objectContextFactory(), mapContextFactory());
    }

    private static Arguments repeatingRows(ContextFactory factory) {
        return of("Repeating table rows should be possible",
                full(),
                factory.roles("Homer Simpson",
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
                        "Dan Castellaneta"),
                getWordResource(Path.of("ProcessorRepeatTableRow.docx")),
                """
                        Repeating Table Rows
                        
                        List of Simpsons characters
                        
                        |===
                        [cnfStyle=100000000000]
                        |Character name<cnfStyle=001000000000>
                        |Voice Actor
                        [cnfStyle=000000100000]
                        |Homer Simpson<cnfStyle=001000000000>
                        |Dan Castellaneta
                        [cnfStyle=000000100000]
                        |Marge Simpson<cnfStyle=001000000000>
                        |Julie Kavner
                        [cnfStyle=000000100000]
                        |Bart Simpson<cnfStyle=001000000000>
                        |Nancy Cartwright
                        [cnfStyle=000000100000]
                        |Kent Brockman<cnfStyle=001000000000>
                        |Harry Shearer
                        [cnfStyle=000000100000]
                        |Disco Stu<cnfStyle=001000000000>
                        |Hank Azaria
                        [cnfStyle=000000100000]
                        |Krusty the Clown<cnfStyle=001000000000>
                        |Dan Castellaneta
                        |===
                        
                        
                        
                        There are 6 characters in the above table.
                        
                        """);
    }

    private static Arguments repeatingRowsWithLineBreak(ContextFactory factory) {
        return of("Repeating table rows should be possible while replacing various linebreaks",
                full(),
                factory.roles("Homer Simpson",
                        "Dan Castellaneta",
                        "Marge Simpson",
                        "Julie\nKavner",
                        "Bart Simpson",
                        "Nancy\n\nCartwright",
                        "Kent Brockman",
                        "Harry\n\n\nShearer",
                        "Disco Stu",
                        "Hank\n\nAzaria",
                        "Krusty the Clown",
                        "Dan\nCastellaneta"),
                getWordResource(Path.of("ProcessorRepeatTableRow.docx")),
                """
                        Repeating Table Rows
                        
                        List of Simpsons characters
                        
                        |===
                        [cnfStyle=100000000000]
                        |Character name<cnfStyle=001000000000>
                        |Voice Actor
                        [cnfStyle=000000100000]
                        |Homer Simpson<cnfStyle=001000000000>
                        |Dan Castellaneta
                        [cnfStyle=000000100000]
                        |Marge Simpson<cnfStyle=001000000000>
                        |Julie
                        Kavner
                        [cnfStyle=000000100000]
                        |Bart Simpson<cnfStyle=001000000000>
                        |Nancy
                        
                        Cartwright
                        [cnfStyle=000000100000]
                        |Kent Brockman<cnfStyle=001000000000>
                        |Harry
                        
                        
                        Shearer
                        [cnfStyle=000000100000]
                        |Disco Stu<cnfStyle=001000000000>
                        |Hank
                        
                        Azaria
                        [cnfStyle=000000100000]
                        |Krusty the Clown<cnfStyle=001000000000>
                        |Dan
                        Castellaneta
                        |===
                        
                        
                        
                        There are 6 characters in the above table.
                        
                        """);
    }

    static Arguments repeatTableRowKeepsFormatTest(ContextFactory factory) {
        return of("Repeat Table row Integration test (keeps formatting)",
                full(),
                factory.show(),
                getWordResource(Path.of("ProcessorRepeatTableRow_KeepsFormatTest.docx")),
                """
                        |===
                        |1^st^ Homer Simpson-Dan Castellaneta
                        |2^nd^ Marge Simpson-Julie Kavner
                        |3^rd^ Bart Simpson-Nancy Cartwright
                        |4^th^ Lisa Simpson-Yeardley Smith
                        |5^th^ Maggie Simpson-Julie Kavner
                        |===
                        
                        
                        
                        """);
    }

    @MethodSource("tests")
    @ParameterizedTest(name = "{0}")
    void features(
            String name,
            OfficeStamperConfiguration config,
            Object context,
            WordprocessingMLPackage template,
            String expected
    ) {
        log.info(name);
        var stamper = docxPackageStamper(config);
        var wordprocessingMLPackage = stamper.stamp(template, context);
        var actual = toAsciidoc(wordprocessingMLPackage);
        assertEquals(expected, actual);
    }

    @Test
    void shouldAcceptList() {
        var config = full();
        var stamper = docxPackageStamper(config);
        var template = makeWordResource("""
                comment::1[start="0,0", end="0,7", value="repeatTableRow(names)"]
                |===
                |${name}
                |===
                """);
        var context = FACTORY.names(List.class, "Homer", "Marge", "Bart", "Lisa", "Maggie");
        var wordprocessingMLPackage = stamper.stamp(template, context);
        var actual = toAsciidoc(wordprocessingMLPackage);
        var expected = """
                |===
                |Homer
                |Marge
                |Bart
                |Lisa
                |Maggie
                |===
                
                """;
        assertEquals(expected, actual);
    }

    @Test
    void shouldAcceptSet() {
        var config = full();
        var stamper = docxPackageStamper(config);
        var template = makeWordResource("""
                comment::1[start="0,0", end="0,7", value="repeatTableRow(names)"]
                |===
                |${name}
                |===
                """);
        var context = FACTORY.names(Set.class, "Homer", "Marge", "Bart", "Lisa", "Maggie");
        var stamped = stamper.stamp(template, context);
        var actual = toAsciidoc(stamped);
        var expected = """
                |===
                |Marge
                |Homer
                |Maggie
                |Bart
                |Lisa
                |===
                
                """;
        assertEquals(expected, actual);
    }

    @Test
    void shouldAcceptQueue() {
        var config = full();
        var stamper = docxPackageStamper(config);
        var template = makeWordResource("""
                comment::1[start="0,0", end="0,7", value="repeatTableRow(names)"]
                |===
                |${name}
                
                |===
                """);
        var context = FACTORY.names(Queue.class, "Homer", "Marge", "Bart", "Lisa", "Maggie");
        var stamped = stamper.stamp(template, context);
        var actual = toAsciidoc(stamped);
        var expected = """
                |===
                |Homer
                |Marge
                |Bart
                |Lisa
                |Maggie
                |===
                
                """;
        assertEquals(expected, actual);
    }
}
