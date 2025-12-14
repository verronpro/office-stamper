package pro.verron.officestamper.test;

import org.junit.jupiter.api.Test;
import pro.verron.officestamper.preset.preprocessors.placeholders.PrepareInlinePlaceholders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static pro.verron.officestamper.test.TestUtils.makeWordResource;

class PrepareInlinePlaceholdersTest {

    @Test
    void process() {
        var preparePlaceholders = new PrepareInlinePlaceholders("(#\\{([^{]+?)})", "processor");
        var document = makeWordResource("Hello, #{name}!");
        var before = Stringifier.stringifyWord(document);
        assertEquals("""
                Hello, #{name}!
                
                """, before);
        preparePlaceholders.process(document);
        var actual = Stringifier.stringifyWord(document);
        assertEquals("""
                Hello, <tag element="officestamper" attr="type:processor">name<\\tag>!
                
                """, actual);
    }
}
