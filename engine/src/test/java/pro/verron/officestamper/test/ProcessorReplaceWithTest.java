package pro.verron.officestamper.test;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static pro.verron.officestamper.preset.OfficeStamperConfigurations.full;
import static pro.verron.officestamper.preset.OfficeStampers.docxPackageStamper;
import static pro.verron.officestamper.test.TestUtils.getWordResource;

class ProcessorReplaceWithTest {
    @Test
    @DisplayName("#585_ReplaceWith")
    void notWorking1() {
        var configuration = full();
        var stamper = docxPackageStamper(configuration);
        var template = getWordResource(Path.of("ProcessorReplaceWith_#585.docx"));
        var context = Map.of("name", "Homer Simpson");
        var stamped = stamper.stamp(template, context);
        var actual = Stringifier.stringifyWord(stamped);
        var expected = "This variable name should be resolved to the value Homer Simpson.\n\n";
        assertEquals(expected, actual);
    }
}
