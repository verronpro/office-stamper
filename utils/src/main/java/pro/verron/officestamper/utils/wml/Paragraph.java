package pro.verron.officestamper.utils.wml;

import org.docx4j.wml.P;

import java.util.List;

public class Paragraph implements Parent {
    private final P paragraph;

    public Paragraph(P paragraph) {
        this.paragraph = paragraph;
    }

    @Override
    public List<Object> getContent() {
        return paragraph.getContent();
    }
}
