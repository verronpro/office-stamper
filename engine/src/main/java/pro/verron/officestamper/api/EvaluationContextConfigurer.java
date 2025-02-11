package pro.verron.officestamper.api;

import org.springframework.expression.spel.support.StandardEvaluationContext;

/// The EvaluationContextConfigurer interface allows for custom configuration of a Spring Expression Language
/// (SPEL) EvaluationContext.
/// Implementations of this interface can be used
/// to add custom PropertyAccessors and MethodResolvers to the EvaluationContext.
public interface EvaluationContextConfigurer {

    /// Configure the context before it's used by docxstamper.
    ///
    /// @param context the SPEL eval context, not null
    void configureEvaluationContext(StandardEvaluationContext context);
}
