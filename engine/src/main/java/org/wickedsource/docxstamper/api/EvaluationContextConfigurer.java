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
 * @deprecated since 1.6.8, This class has been deprecated in the effort
 * of the library modularization.
 * It is recommended to use the {@link pro.verron.officestamper.api.EvaluationContextConfigurer} class instead.
 * This class will not be exported in the future releases of the module.
 */
@Deprecated(since = "1.6.8", forRemoval = true)
public interface EvaluationContextConfigurer
        extends pro.verron.officestamper.api.EvaluationContextConfigurer {
}
