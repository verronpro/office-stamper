package pro.verron.officestamper;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MainTemplateKindTest {

    @Test
    void templateKind_detectsWordPptxAndDiagnostic()
            throws Exception {
        var main = new Main();
        Method m = Main.class.getDeclaredMethod("templateKind", String.class);
        m.setAccessible(true);

        Object word = m.invoke(main, "file.DOCX");
        Object pptx = m.invoke(main, "slides.pptx");
        Object diag = m.invoke(main, "diagnostic");

        // Enum.name() assertions via reflection
        assertEquals("WORD", ((Enum<?>) word).name());
        assertEquals("POWERPOINT", ((Enum<?>) pptx).name());
        assertEquals("WORD", ((Enum<?>) diag).name());
    }
}
