package pro.verron.officestamper;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class MainContextDirectoryTest {

    @Test
    void contextualiseDirectory_mergesSupportedFilesByBasename()
            throws Exception {
        // Arrange: temp directory with one json and one properties file
        Path dir = Files.createTempDirectory("os_cli_ctx_");
        try {
            Files.writeString(dir.resolve("a.json"), "{\n  \"x\": 1, \"y\": \"z\"\n}", StandardCharsets.UTF_8);
            Files.writeString(dir.resolve("b.properties"), "k=v\n", StandardCharsets.UTF_8);

            var main = new Main();
            Method m = Main.class.getDeclaredMethod("contextualiseDirectory", Path.class);
            m.setAccessible(true);

            // Act
            @SuppressWarnings("unchecked")
            Map<String, Object> ctx = (Map<String, Object>) m.invoke(main, dir);

            // Assert
            assertNotNull(ctx);
            assertTrue(ctx.containsKey("a"));
            assertTrue(ctx.containsKey("b"));

            @SuppressWarnings("unchecked")
            Map<String, Object> a = (Map<String, Object>) ctx.get("a");
            @SuppressWarnings("unchecked")
            Map<String, Object> b = (Map<String, Object>) ctx.get("b");

            assertEquals("z", a.get("y"));
            Object xv = a.get("x");
            assertInstanceOf(Number.class, xv);
            assertEquals(1, ((Number) xv).intValue());

            assertEquals("v", b.get("k"));
        } finally {
            // Cleanup
            Files.walk(dir)
                 .sorted((p1, p2) -> p2.getNameCount() - p1.getNameCount())
                 .forEach(path -> {
                     try {Files.deleteIfExists(path);} catch (Exception ignored) {}
                 });
        }
    }
}
