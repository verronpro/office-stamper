package pro.verron.docxstamper.api;

import org.docx4j.wml.R;

public interface Paragraph {
    void replace(Placeholder placeholder, R replacement);

    String asString();
}
