package pro.verron.docxstamper.preset;


import org.wickedsource.docxstamper.el.NoOpEvaluationContextConfigurer;
import pro.verron.docxstamper.api.EvaluationContextConfigurer;

public class EvaluationContextConfigurers {
    public static EvaluationContextConfigurer noopConfigurer() {
        return new NoOpEvaluationContextConfigurer();
    }
}
