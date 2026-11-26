package pro.verron.officestamper.test;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static pro.verron.officestamper.test.utils.OfficeStamperTestBase.toAsciidoc;
import static pro.verron.officestamper.test.utils.DocxFactory.makeWordResource;

class PlaceholderPreprocessorTest {

    @Test
    void process() {
        var document = makeWordResource("Hello, #{name}!");
        var before = toAsciidoc(document);
        assertEquals("""
                Hello, #{name}!
                
                """, before);
        Flavor.get().hookPlaceholders(document, "#{", "inlineProcessor");
        var actual = toAsciidoc(document);
        assertEquals("""
                Hello, tag:[start, element=officestamper, type=inlineProcessor]nametag:[end]!
                
                """, actual);
    }
}

