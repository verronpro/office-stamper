package pro.verron.officestamper;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pro.verron.officestamper.excel.ExcelMergeStrategy;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class MainContextDirectoryTest {

    private Path dir;

    @BeforeEach
    void setUp() throws Exception {
        dir = Files.createTempDirectory("os_cli_ctx_");
    }

    @Test
    void contextualiseDirectory_mergesSupportedFilesByBasename() throws IOException {
        // Arrange: temp directory with one json and one properties file
        Files.writeString(dir.resolve("a.json"), "{\n  \"x\": 1, \"y\": \"z\"\n}", StandardCharsets.UTF_8);
        Files.writeString(dir.resolve("b.properties"), "k=v\n", StandardCharsets.UTF_8);

        var excelStrategy = new ExcelStrategy(ExcelMergeStrategy.MAP, null);

        // Act
        Map<String, Object> ctx = Contextualizer.contextualiseDirectory(dir, excelStrategy);

        // Assert
        assertNotNull(ctx);
        assertTrue(ctx.containsKey("a"));
        assertTrue(ctx.containsKey("b"));

        Map<String, Object> a = (Map<String, Object>) ctx.get("a");
        Map<String, Object> b = (Map<String, Object>) ctx.get("b");

        assertEquals("z", a.get("y"));
        Object xv = a.get("x");
        assertInstanceOf(Number.class, xv);
        assertEquals(1, ((Number) xv).intValue());

        assertEquals("v", b.get("k"));
    }

    @AfterEach
    void tearDown() throws Exception {
        Files.walkFileTree(dir, new FileDeleteVisitor());
    }

}
