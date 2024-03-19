package pro.verron.docxstamper.preset;

import org.wickedsource.docxstamper.preprocessor.MergeSameStyleRuns;
import org.wickedsource.docxstamper.preprocessor.RemoveProofErrors;
import pro.verron.docxstamper.api.OfficeStamperConfiguration;

public class OfficeStamperConfigurations {
    public static OfficeStamperConfiguration standard() {
        return new org.wickedsource.docxstamper.DocxStamperConfiguration();
    }

    static OfficeStamperConfiguration standardWithPreprocessing() {
        var configuration = standard();
        configuration.addPreprocessor(new RemoveProofErrors());
        configuration.addPreprocessor(new MergeSameStyleRuns());
        return configuration;
    }
}
