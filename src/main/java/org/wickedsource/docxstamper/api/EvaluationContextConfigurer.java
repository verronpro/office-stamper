package org.wickedsource.docxstamper.api;

import org.springframework.expression.EvaluationContext;
import org.springframework.expression.MethodResolver;
import org.springframework.expression.PropertyAccessor;
import org.springframework.expression.spel.support.StandardEvaluationContext;

/**
 * Allows for custom configuration of a spring expression language {@link EvaluationContext}.
 * This can, for example, be used to add custom {@link PropertyAccessor}s and {@link MethodResolver}s.
 *
 * @author Joseph Verron
 * @version ${version}
 * @since 1.0.13
 */
public interface EvaluationContextConfigurer {
    /**
     * Configure the context before it's used by docxstamper.
     *
     * @param context the SPEL eval context, not null
     */
    void configureEvaluationContext(StandardEvaluationContext context);
}
