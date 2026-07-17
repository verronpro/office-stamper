package pro.verron.officestamper.utils.wml;

import org.docx4j.wml.CTSdtContentRun;

import java.util.List;

public class SdtContent implements Parent {
    private final CTSdtContentRun paragraph;

    public SdtContent(CTSdtContentRun paragraph) {
        this.paragraph = paragraph;
    }

    @Override
    public List<Object> getContent() {
        return paragraph.getContent();
    }
}
