package pro.verron.officestamper.api;

import org.springframework.expression.EvaluationContext;

/// Factory interface for creating [EvaluationContext] instances.
@FunctionalInterface
public interface EvaluationContextFactory {

    /// Creates an evaluation context for the given context root.
    ///
    /// @param contextRoot the root object for evaluation.
    ///
    /// @return the evaluation context.
    EvaluationContext create(Object contextRoot);
}
