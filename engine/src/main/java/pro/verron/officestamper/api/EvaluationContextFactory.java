package pro.verron.officestamper.api;

import org.springframework.expression.EvaluationContext;

@FunctionalInterface public interface EvaluationContextFactory {
    EvaluationContext create(Object contextRoot);
}
