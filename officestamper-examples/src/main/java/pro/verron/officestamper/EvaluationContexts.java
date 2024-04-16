package pro.verron.officestamper;

import org.springframework.context.expression.MapAccessor;
import org.wickedsource.docxstamper.api.EvaluationContextConfigurer;

public class EvaluationContexts {
    static EvaluationContextConfigurer enableMapAccess() {
        return ctx -> ctx.addPropertyAccessor(new MapAccessor());
    }
}
