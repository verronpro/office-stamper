package pro.verron.officestamper.test;

import org.junit.jupiter.api.Test;
import pro.verron.officestamper.preset.preprocessors.placeholders.PreparePlaceholders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static pro.verron.officestamper.test.TestUtils.makeDocx;

class PreparePlaceholdersTest {

    @Test
    void process() {
        var preparePlaceholders = new PreparePlaceholders();
        var document = makeDocx("Hello, ${name}!");
        var stringifier = new Stringifier(() -> document);
        var before = stringifier.stringify(document);
        assertEquals("Hello, ${name}!\n", before);
        preparePlaceholders.process(document);
        var actual = stringifier.stringify(document);
        assertEquals("""
                Hello, <tag element="name">name<\\tag>!
                """, actual);
    }
}
