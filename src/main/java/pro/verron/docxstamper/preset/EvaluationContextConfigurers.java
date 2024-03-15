package pro.verron.docxstamper.preset;

import org.wickedsource.docxstamper.api.EvaluationContextConfigurer;
import org.wickedsource.docxstamper.el.NoOpEvaluationContextConfigurer;

public class EvaluationContextConfigurers {
    public static EvaluationContextConfigurer noopConfigurer() {
        return new NoOpEvaluationContextConfigurer();
    }
}
