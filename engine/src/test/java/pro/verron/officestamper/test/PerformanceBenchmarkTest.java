package pro.verron.officestamper.test;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pro.verron.officestamper.core.DocxStamper;
import pro.verron.officestamper.preset.OfficeStamperConfigurations;
import pro.verron.officestamper.test.utils.ResourceUtils;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Performance Benchmark Tests")
class PerformanceBenchmarkTest {

    @Test
    @DisplayName("Should process a large complex document in less than 20 seconds")
    void testLargeDocumentPerformance() {
        // 1. Create a large context
        int rows = 2000;
        List<Row> data = new ArrayList<>();
        for (int i = 0; i < rows; i++) {
            data.add(new Row("Item " + i, "Value " + i, i));
        }

        record Context(List<Row> characters) {}
        var context = new Context(data);

        // 2. Load a template with repeated rows
        var stamper = new DocxStamper(OfficeStamperConfigurations.standard());
        var template = ResourceUtils.getWordResource(Path.of("ProcessorRepeatTableRow.docx"));

        long start = System.currentTimeMillis();
        stamper.stamp(template, context);
        long end = System.currentTimeMillis();

        long duration = end - start;
        System.out.println("[DEBUG_LOG] Stamping duration for " + rows + " rows: " + duration + "ms");

        assertTrue(duration < 20000, "Stamping should take less than 20s, but took " + duration + "ms");
    }

    public record Row(String name, String actor, int index) {}
}
