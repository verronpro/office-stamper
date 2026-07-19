package pro.verron.officestamper.test;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pro.verron.officestamper.test.utils.OfficeStamperTestBase;

import java.nio.file.Path;
import java.util.Map;

import static pro.verron.officestamper.preset.OfficeStamperConfigurations.full;
import static pro.verron.officestamper.test.utils.ResourceUtils.getWordResource;

class ProcessorReplaceWithTest extends OfficeStamperTestBase {
    @Test
    @DisplayName("#585_ReplaceWith")
    void notWorking1() {
        var configuration = full();
        var template = getWordResource(Path.of("ProcessorReplaceWith_#585.docx"));
        var context = Map.of("name", "Homer Simpson");
        var expected = """
                This variable name should be resolved to the value Homer Simpson.
                
                // section {pgMar={bottom=1134, left=1134, right=1134, top=1134}, pgSz={h=16838, w=11906}, space=720}
                
                """;
        testStamper(configuration, context, template, expected);
    }
}
