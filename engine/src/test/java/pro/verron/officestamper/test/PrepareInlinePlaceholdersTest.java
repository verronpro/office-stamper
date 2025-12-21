package pro.verron.officestamper.test;

import org.junit.jupiter.api.Test;
import pro.verron.officestamper.preset.preprocessors.placeholders.PrepareInlinePlaceholders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static pro.verron.officestamper.test.TestUtils.makeWordResource;
import static pro.verron.officestamper.utils.wml.DocxRenderer.docxToString;

class PrepareInlinePlaceholdersTest {

    @Test
    void process() {
        var preparePlaceholders = new PrepareInlinePlaceholders("(#\\{([^{]+?)})", "inlineProcessor");
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
