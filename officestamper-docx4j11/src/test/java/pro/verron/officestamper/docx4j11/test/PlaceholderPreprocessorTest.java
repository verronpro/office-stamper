package pro.verron.officestamper.docx4j11.test;

import org.junit.jupiter.api.Test;
import pro.verron.officestamper.docx4j11.DocxDocument;
import pro.verron.officestamper.docx4j11.preset.PlaceholderHooker;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static pro.verron.officestamper.docx4j11.test.utils.OfficeStamperTestBase.toAsciidoc;
import static pro.verron.officestamper.docx4j11.test.utils.DocxFactory.makeWordResource;

class PlaceholderPreprocessorTest {

    @Test
    void process() {
        var preparePlaceholders = new PlaceholderHooker("#{", "inlineProcessor");
        var document = makeWordResource("Hello, #{name}!");
        var before = toAsciidoc(document);
        assertEquals("""
                Hello, #{name}!
                
                """, before);
        preparePlaceholders.process(document);
        var actual = toAsciidoc(document);
        assertEquals("""
                Hello, tag:[start, element=officestamper, type=inlineProcessor]nametag:[end]!
                
                """, actual);
    }
}

