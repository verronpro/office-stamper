package org.wickedsource.docxstamper.el;

import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.wickedsource.docxstamper.api.EvaluationContextConfigurer;

/**
 * {@link EvaluationContextConfigurer} that does no customization.
 * <p>
 * The NoOpEvaluationContextConfigurer is a configuration placeholder used to indicate the
 * intention to keep the standard powerful features provided by the
 * Spring framework's StandardEvaluationContext class.
 * <p>
 * StandardEvaluationContext is a powerful class by default, which can lead to potential security risks
 * if not properly managed. This might include potential dangerous injections in the template.
 * <p>
 * This configurer does nothing to the StandardEvaluationContext class, and therefore all the
 * unfiltered features are accessible. It should be used when there is a need to use the
 * powerful features of the aforementioned class, and there is a trust that the template won't
 * contain any dangerous injections.

 * @author Joseph Verron
 * @version ${version}
 */
public class NoOpEvaluationContextConfigurer implements EvaluationContextConfigurer {
    /**
     * Configures the provided StandardEvaluationContext.
     *
     * @param context the StandardEvaluationContext to be configured, not null
     */
    @Override
    public void configureEvaluationContext(StandardEvaluationContext context) {
        // DO NOTHING
    }
}
