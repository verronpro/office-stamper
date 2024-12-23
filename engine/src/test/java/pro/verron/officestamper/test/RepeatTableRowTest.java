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
import java.util.Queue;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.of;
import static pro.verron.officestamper.preset.OfficeStamperConfigurations.standard;
import static pro.verron.officestamper.test.ContextFactory.mapContextFactory;
import static pro.verron.officestamper.test.ContextFactory.objectContextFactory;
import static pro.verron.officestamper.test.TestUtils.getResource;
import static pro.verron.officestamper.test.TestUtils.makeResource;

class RepeatTableRowTest {
    private static final ObjectContextFactory FACTORY = new ObjectContextFactory();
    private static final Logger log = LoggerFactory.getLogger(RepeatTableRowTest.class);

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
                standard(),
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
                getResource(Path.of("RepeatTableRowTest.docx")),
                """
                        Repeating Table Rows
                        List of Simpsons characters
                        |===
                        |Character name
                        |Voice Actor<cnfStyle=100000000000>
                        
                        |Homer Simpson
                        |Dan Castellaneta<cnfStyle=000000100000>
                        
                        |Marge Simpson
                        |Julie Kavner<cnfStyle=000000100000>
                        
                        |Bart Simpson
                        |Nancy Cartwright<cnfStyle=000000100000>
                        
                        |Kent Brockman
                        |Harry Shearer<cnfStyle=000000100000>
                        
                        |Disco Stu
                        |Hank Azaria<cnfStyle=000000100000>
                        
                        |Krusty the Clown
                        |Dan Castellaneta<cnfStyle=000000100000>
                        
                        
                        |===
                        
                        There are 6 characters in the above table.
                        """);
    }

    private static Arguments repeatingRowsWithLineBreak(ContextFactory factory) {
        return of("Repeating table rows should be possible while replacing various linebreaks",
                standard(),
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
                getResource(Path.of("RepeatTableRowTest.docx")),
                """
                        Repeating Table Rows
                        List of Simpsons characters
                        |===
                        |Character name
                        |Voice Actor<cnfStyle=100000000000>
                        
                        |Homer Simpson
                        |Dan Castellaneta<cnfStyle=000000100000>
                        
                        |Marge Simpson
                        |Julie<br/>
                        Kavner<cnfStyle=000000100000>
                        
                        |Bart Simpson
                        |Nancy<br/>
                        <br/>
                        Cartwright<cnfStyle=000000100000>
                        
                        |Kent Brockman
                        |Harry<br/>
                        <br/>
                        <br/>
                        Shearer<cnfStyle=000000100000>
                        
                        |Disco Stu
                        |Hank<br/>
                        <br/>
                        Azaria<cnfStyle=000000100000>
                        
                        |Krusty the Clown
                        |Dan<br/>
                        Castellaneta<cnfStyle=000000100000>
                        
                        
                        |===
                        
                        There are 6 characters in the above table.
                        """);
    }

    static Arguments repeatTableRowKeepsFormatTest(ContextFactory factory) {
        return of("Repeat Table row Integration test (keeps formatting)",
                standard(),
                factory.show(),
                getResource(Path.of("RepeatTableRowKeepsFormatTest.docx")),
                """
                        |===
                        |1❬st❘{vertAlign=superscript}❭ Homer Simpson-❬Dan Castellaneta❘{b=true}❭
                        
                        |2❬nd❘{vertAlign=superscript}❭ Marge Simpson-❬Julie Kavner❘{b=true}❭
                        
                        |3❬rd❘{vertAlign=superscript}❭ Bart Simpson-❬Nancy Cartwright❘{b=true}❭
                        
                        |4❬th❘{vertAlign=superscript}❭ Lisa Simpson-❬Yeardley Smith❘{b=true}❭
                        
                        |5❬th❘{vertAlign=superscript}❭ Maggie Simpson-❬Julie Kavner❘{b=true}❭
                        
                        
                        |===
                        
                        """);
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
        var template = makeResource("""
                |===
                |<1|>${name}<|1><1|repeatTableRow(names)>
                |===
                """);
        var context = FACTORY.names(List.class, "Homer", "Marge", "Bart", "Lisa", "Maggie");
        var actual = stamper.stampAndLoadAndExtract(template, context);
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
        var config = standard();
        var stamper = new TestDocxStamper<>(config);
        var template = makeResource("""
                |===
                |<1|>${name}<|1><1|repeatTableRow(names)>
                |===
                """);
        var context = FACTORY.names(Set.class, "Homer", "Marge", "Bart", "Lisa", "Maggie");
        var actual = stamper.stampAndLoadAndExtract(template, context);
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
        var config = standard();
        var stamper = new TestDocxStamper<>(config);
        var template = makeResource("""
                |===
                |<1|>${name}<|1><1|repeatTableRow(names)>
                |===
                """);
        var context = FACTORY.names(Queue.class, "Homer", "Marge", "Bart", "Lisa", "Maggie");
        var actual = stamper.stampAndLoadAndExtract(template, context);
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
