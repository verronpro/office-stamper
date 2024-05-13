package pro.verron.officestamper;

import org.springframework.context.expression.MapAccessor;
import pro.verron.officestamper.api.EvaluationContextConfigurer;

public class EvaluationContexts {
    static EvaluationContextConfigurer enableMapAccess() {
        return ctx -> ctx.addPropertyAccessor(new MapAccessor());
    }
}
