package pro.verron.officestamper;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TemplateKindTest {

    @Test
    void templateKind_detectsWordPptxAndDiagnostic() {
        var word = TemplateKind.templateKind("file.DOCX");
        assertEquals(TemplateKind.WORD, word);

        var pptx = TemplateKind.templateKind("slides.pptx");
        assertEquals(TemplateKind.POWERPOINT, pptx);
    }
}
