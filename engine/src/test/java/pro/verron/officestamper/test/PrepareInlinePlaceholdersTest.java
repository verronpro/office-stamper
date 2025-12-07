package pro.verron.officestamper.test;

import org.junit.jupiter.api.Test;
import pro.verron.officestamper.preset.preprocessors.placeholders.PrepareInlinePlaceholders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static pro.verron.officestamper.test.TestUtils.makeDocx;

class PrepareInlinePlaceholdersTest {

    @Test
    void process() {
        var preparePlaceholders = new PrepareInlinePlaceholders("(#\\{([^{]+?)})", "processor");
        var document = makeDocx("Hello, #{name}!");
        var stringifier = new Stringifier(() -> document);
        var before = stringifier.stringify(document);
        assertEquals("""
                Hello, #{name}!
                
                """, before);
        preparePlaceholders.process(document);
        var actual = stringifier.stringify(document);
        assertEquals("""
                Hello, <tag element="officestamper" attr="type:processor">name<\\tag>!
                
                """, actual);
    }
}
