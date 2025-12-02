package pro.verron.officestamper.core;

import org.springframework.expression.EvaluationContext;
import pro.verron.officestamper.api.ProcessorContext;

@FunctionalInterface public interface EvaluationContextFactory {
    EvaluationContext create(ProcessorContext processorContext, ContextBranch branch);
}
