package pro.verron.officestamper.test;

import org.junit.jupiter.api.Test;
import pro.verron.officestamper.api.PlaceholderHooker;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static pro.verron.officestamper.asciidoc.AsciiDocCompiler.toAsciidoc;
import static pro.verron.officestamper.test.utils.DocxFactory.makeWordResource;

class PlaceholderPreprocessorTest {

    @Test
    void process() {
        var preparePlaceholders = new PlaceholderHooker("(#\\{([^{]+?)})", "inlineProcessor");
        var document = makeWordResource("Hello, #{name}!");
        var before = toAsciidoc(document);
        assertEquals("""
                Hello, #{name}!
                
                // section {pgMar={bottom=1440, left=1440, right=1440, top=1440}, pgSz={code=9, h=16839, w=11907}}
                
                """, before);
        preparePlaceholders.process(document);
        var actual = toAsciidoc(document);
        assertEquals("""
                Hello, <tag element="officestamper" attr="type:inlineProcessor">name<\\tag>!
                
                """, actual);
    }
}
