package pro.verron.officestamper.preset;


import org.wickedsource.docxstamper.el.NoOpEvaluationContextConfigurer;
import pro.verron.officestamper.api.EvaluationContextConfigurer;

/**
 * Utility class for configuring the EvaluationContext used by officestamper.
 */
public class EvaluationContextConfigurers {

    /**
     * Returns a {@link EvaluationContextConfigurer} instance that does no customization.
     * <p>
     * This configurer does nothing to the StandardEvaluationContext class, and therefore all the
     * unfiltered features are accessible.
     * It should be used when there is a need to use the
     * powerful features of the aforementioned class, and there is a trust that the template won't
     * contain any dangerous injections.
     *
     * @return a {@link EvaluationContextConfigurer} instance
     */
    public static EvaluationContextConfigurer noopConfigurer() {
        return new NoOpEvaluationContextConfigurer();
    }
}
