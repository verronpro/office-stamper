package pro.verron.officestamper.test;

import org.junit.jupiter.api.Test;
import pro.verron.officestamper.api.PlaceholderHooker;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static pro.verron.officestamper.test.TestUtils.makeWordResource;
import static pro.verron.officestamper.utils.wml.DocxRenderer.docxToString;

class PlaceholderPreprocessorTest {

    @Test
    void process() {
        var preparePlaceholders = new PlaceholderHooker("(#\\{([^{]+?)})", "inlineProcessor");
        var document = makeWordResource("Hello, #{name}!");
        var before = docxToString(document);
        assertEquals("""
                Hello, #{name}!
                
                """, before);
        preparePlaceholders.process(document);
        var actual = docxToString(document);
        assertEquals("""
                Hello, <tag element="officestamper" attr="type:inlineProcessor">name<\\tag>!
                
                """, actual);
    }
}
