package org.wickedsource.docxstamper.el;

import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.wickedsource.docxstamper.api.EvaluationContextConfigurer;

/**
 * {@link org.wickedsource.docxstamper.api.EvaluationContextConfigurer} that does no customization.
 *
 * @author Joseph Verron
 * @version ${version}
 */
public class NoOpEvaluationContextConfigurer implements EvaluationContextConfigurer {
    /**
     * {@inheritDoc}
     */
    @Override
    public void configureEvaluationContext(StandardEvaluationContext context) {
        // DO NOTHING
    }
}
