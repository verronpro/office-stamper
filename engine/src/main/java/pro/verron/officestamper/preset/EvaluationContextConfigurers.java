package pro.verron.officestamper.preset;


import org.springframework.expression.EvaluationContext;
import org.wickedsource.docxstamper.el.DefaultEvaluationContextConfigurer;
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

    /**
     * Returns a default {@link EvaluationContextConfigurer} instance.
     * <p>
     * The default configurer provides better default security for the
     * {@link EvaluationContext} used by office stamper.
     * It sets up the context with enhanced security measures, such as
     * limited property accessors, constructor resolvers, and method resolvers.
     * It also sets a type locator, type converter, type comparator, and operator overloader.
     * This configurer is recommended to be used when there is a need for improved security
     * and protection against potential dangerous injections in the template.
     *
     * @return a {@link EvaluationContextConfigurer} instance with enhanced security features
     */
    public static EvaluationContextConfigurer defaultConfigurer() {
        return new DefaultEvaluationContextConfigurer();
    }
}
