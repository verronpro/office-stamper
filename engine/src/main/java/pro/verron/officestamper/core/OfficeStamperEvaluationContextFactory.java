package pro.verron.officestamper.core;

import org.springframework.expression.EvaluationContext;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import pro.verron.officestamper.api.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static pro.verron.officestamper.core.Invokers.streamInvokersFromClass;
import static pro.verron.officestamper.core.Invokers.streamInvokersFromCustomFunction;

record OfficeStamperEvaluationContextFactory(
        List<CustomFunction> functions,
        Map<Class<?>, CommentProcessorFactory> configurationCommentProcessors,
        Map<Class<?>, Object> expressionFunctions,
        EvaluationContextConfigurer evaluationContextConfigurer
)
        implements EvaluationContextFactory {

    @Override
    public EvaluationContext create(ProcessorContext processorContext, ContextBranch branch) {
        var ec = prepare(processorContext, branch);
        return new UnionEvaluationContext(ec);
    }

    private EvaluationContext prepare(ProcessorContext processorContext, ContextBranch branch) {
        var invokers = computeInvokers(functions,
                configurationCommentProcessors,
                processorContext,
                expressionFunctions);
        var evaluationContext = createEvaluationContext(branch, evaluationContextConfigurer);
        evaluationContext.addMethodResolver(invokers);
        return evaluationContext;
    }

    private static Invokers computeInvokers(
            List<CustomFunction> functions,
            Map<Class<?>, CommentProcessorFactory> configurationCommentProcessors,
            ProcessorContext processorContext,
            Map<Class<?>, Object> expressionFunctions1
    ) {
        var processors = instantiate(configurationCommentProcessors, processorContext);
        var invokerStream = Stream.of(streamInvokersFromClass(processors),
                                          streamInvokersFromClass(expressionFunctions1),
                                          streamInvokersFromCustomFunction(functions))
                                  .flatMap(s -> s);
        return new Invokers(invokerStream);
    }

    private static StandardEvaluationContext createEvaluationContext(
            ContextBranch contextBranch,
            EvaluationContextConfigurer contextConfigurer
    ) {
        var evaluationContext = new StandardEvaluationContext(contextBranch);
        contextConfigurer.configureEvaluationContext(evaluationContext);
        return evaluationContext;
    }

    /// Returns a set view of the mappings contained in this map. Each entry in the set is a mapping between a
    /// [Class<?>] key and its associated `CommentProcessor` value.
    ///
    /// @return a map representing the associations between [Class<?>] keys and their corresponding [CommentProcessor]
    ///         values in this map.

    private static Map<Class<?>, CommentProcessor> instantiate(
            Map<Class<?>, CommentProcessorFactory> processorFactoryMap,
            ProcessorContext processorContext
    ) {
        Map<Class<?>, CommentProcessor> map = new HashMap<>();
        for (Map.Entry<Class<?>, CommentProcessorFactory> entry : processorFactoryMap.entrySet()) {
            var processorClass = entry.getKey();
            var processorFactory = entry.getValue();
            var processor = processorFactory.create(processorContext);
            map.put(processorClass, processor);
        }
        return map;
    }
}
