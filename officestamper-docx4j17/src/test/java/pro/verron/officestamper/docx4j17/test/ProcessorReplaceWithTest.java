package pro.verron.officestamper.docx4j17.test;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static pro.verron.officestamper.docx4j17.test.utils.OfficeStamperTestBase.toAsciidoc;
import static pro.verron.officestamper.docx4j17.DocxOfficeStampers.full;
import static pro.verron.officestamper.preset.OfficeStampers.docxStamper;
import static pro.verron.officestamper.docx4j17.test.utils.ResourceUtils.getWordResource;

class ProcessorReplaceWithTest {
    @Test
    @DisplayName("#585_ReplaceWith")
    void notWorking1() {
        var configuration = full();
        var stamper = docxStamper(configuration);
        var template = getWordResource(Path.of("ProcessorReplaceWith_#585.docx"));
        var context = Map.of("name", "Homer Simpson");
        var stamped = stamper.stamp(template, context);
        var actual = toAsciidoc(stamped);
        var expected = """
                This variable name should be resolved to the value Homer Simpson.
                
                """;
        assertEquals(expected, actual);
    }
}


