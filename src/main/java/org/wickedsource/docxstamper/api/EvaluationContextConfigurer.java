package org.wickedsource.docxstamper.api;

import org.springframework.expression.EvaluationContext;
import org.springframework.expression.MethodResolver;
import org.springframework.expression.PropertyAccessor;

/**
 * Allows for custom configuration of a spring expression language {@link EvaluationContext}.
 * This can, for example, be used to add custom {@link PropertyAccessor}s and {@link MethodResolver}s.
 *
 * @author Joseph Verron
 * @author Mario Siegenthaler
 * @version ${version}
 * @since 1.0.13
 */
public interface EvaluationContextConfigurer
        extends pro.verron.docxstamper.api.EvaluationContextConfigurer {
}
