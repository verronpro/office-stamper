package pro.verron.officestamper.core;

import org.springframework.expression.EvaluationContext;
import pro.verron.officestamper.api.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static pro.verron.officestamper.core.Invokers.streamInvokersFromClass;
import static pro.verron.officestamper.core.Invokers.streamInvokersFromCustomFunction;

public final class OfficeStamperEvaluationContextFactory {
    private final List<CustomFunction> functions;
    private final Map<Class<?>, CommentProcessorFactory> configurationCommentProcessors;
    private final Map<Class<?>, Object> expressionFunctions;
    private final EvaluationContextFactory evaluationContextFactory;

    OfficeStamperEvaluationContextFactory(
            List<CustomFunction> functions,
            Map<Class<?>, CommentProcessorFactory> configurationCommentProcessors,
            Map<Class<?>, Object> expressionFunctions,
            EvaluationContextFactory evaluationContextFactory
    ) {
        this.functions = functions;
        this.configurationCommentProcessors = configurationCommentProcessors;
        this.expressionFunctions = expressionFunctions;
        this.evaluationContextFactory = evaluationContextFactory;
    }

    public EvaluationContext create(ProcessorContext processorContext, ContextBranch branch) {
        var ec = evaluationContextFactory.create(branch);
        var invokers = computeInvokers(functions,
                configurationCommentProcessors,
                processorContext,
                expressionFunctions);
        return new UnionEvaluationContext(ec, invokers);
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
