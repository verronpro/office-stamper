package pro.verron.officestamper;


import org.springframework.context.expression.MapAccessor;
import pro.verron.officestamper.api.EvaluationContextConfigurer;
import pro.verron.officestamper.api.OfficeStamperException;

public class EvaluationContexts {

    private EvaluationContexts() {
        throw new OfficeStamperException("EvaluationContexts cannot be instantiated");
    }

    static EvaluationContextConfigurer enableMapAccess() {
        return ctx -> ctx.addPropertyAccessor(new MapAccessor());
    }
}
