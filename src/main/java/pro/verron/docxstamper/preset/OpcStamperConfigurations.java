package pro.verron.docxstamper.preset;

import org.wickedsource.docxstamper.preprocessor.MergeSameStyleRuns;
import org.wickedsource.docxstamper.preprocessor.RemoveProofErrors;
import pro.verron.docxstamper.api.OpcStamperConfiguration;

public class OpcStamperConfigurations {
    public static OpcStamperConfiguration standard() {
        return new org.wickedsource.docxstamper.DocxStamperConfiguration();
    }

    static OpcStamperConfiguration standardWithPreprocessing() {
        var configuration = standard();
        configuration.addPreprocessor(new RemoveProofErrors());
        configuration.addPreprocessor(new MergeSameStyleRuns());
        return configuration;
    }
}
