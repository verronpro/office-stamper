package pro.verron.officestamper.preset;


import org.wickedsource.docxstamper.el.NoOpEvaluationContextConfigurer;
import pro.verron.officestamper.api.EvaluationContextConfigurer;

public class EvaluationContextConfigurers {
    public static EvaluationContextConfigurer noopConfigurer() {
        return new NoOpEvaluationContextConfigurer();
    }
}
