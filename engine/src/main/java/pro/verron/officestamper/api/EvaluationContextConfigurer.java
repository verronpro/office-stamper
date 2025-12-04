package pro.verron.officestamper.api;

import org.springframework.expression.EvaluationContext;
import org.springframework.expression.MethodResolver;
import org.springframework.expression.PropertyAccessor;
import org.springframework.expression.spel.support.StandardEvaluationContext;

/// The [EvaluationContextConfigurer] interface allows for custom configuration of a Spring Expression Language (SpEL)
/// [EvaluationContext].
///
///  Implementations of this interface can be used to add custom [PropertyAccessor] and [MethodResolver] to the
/// [EvaluationContext].
public interface EvaluationContextConfigurer {

    /// Configure the context before it is used by office-stamper.
    ///
    /// @param context the SpEL eval context, not null
    void configureEvaluationContext(StandardEvaluationContext context);
}
