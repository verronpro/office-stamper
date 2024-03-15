package pro.verron.docxstamper.preset;

import pro.verron.docxstamper.api.DocxStamperConfiguration;

public class Configurations {
    public static DocxStamperConfiguration standard() {
        return new org.wickedsource.docxstamper.DocxStamperConfiguration();
    }
}
