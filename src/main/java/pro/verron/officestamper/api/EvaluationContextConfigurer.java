package pro.verron.officestamper.api;

import org.springframework.expression.spel.support.StandardEvaluationContext;

public interface EvaluationContextConfigurer {
    /**
     * Configure the context before it's used by docxstamper.
     *
     * @param context the SPEL eval context, not null
     */
    void configureEvaluationContext(StandardEvaluationContext context);
}
