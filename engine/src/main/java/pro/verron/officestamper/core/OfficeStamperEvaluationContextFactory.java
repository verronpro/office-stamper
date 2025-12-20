package pro.verron.officestamper.core;

import org.springframework.expression.EvaluationContext;
import pro.verron.officestamper.api.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.function.Function.identity;
import static pro.verron.officestamper.core.Invokers.streamInvokersFromClass;
import static pro.verron.officestamper.core.Invokers.streamInvokersFromCustomFunction;

public final class OfficeStamperEvaluationContextFactory {
    private final List<CustomFunction> customFunctions;
    private final Map<Class<?>, CommentProcessorFactory> commentProcessors;
    private final Map<Class<?>, Object> interfaceFunctions;
    private final EvaluationContextFactory contextFactory;

    OfficeStamperEvaluationContextFactory(
            List<CustomFunction> customFunctions,
            Map<Class<?>, CommentProcessorFactory> commentProcessors,
            Map<Class<?>, Object> interfaceFunctions,
            EvaluationContextFactory contextFactory
    ) {
        this.customFunctions = customFunctions;
        this.commentProcessors = commentProcessors;
        this.interfaceFunctions = interfaceFunctions;
        this.contextFactory = contextFactory;
    }

    public EvaluationContext create(ProcessorContext processorContext, ContextBranch branch) {
        var ec = contextFactory.create(branch);
        var processors = instantiate(commentProcessors, processorContext);
        var invokerStream = Stream.of(streamInvokersFromClass(processors),
                                          streamInvokersFromClass(interfaceFunctions),
                                          streamInvokersFromCustomFunction(customFunctions))
                                  .flatMap(identity());
        var invokers = new Invokers(invokerStream);
        return new UnionEvaluationContext(ec, invokers);
    }

    /// Returns a set view of the mappings contained in this map. Each entry in the set is a mapping between a
    /// [Class<?>] key and its associated `CommentProcessor` value.
    ///
    /// @return a map representing the associations between [Class<?>] keys and their corresponding [CommentProcessor]
    ///         values in this map.

    private static Map<Class<?>, CommentProcessor> instantiate(
            Map<Class<?>, CommentProcessorFactory> commentProcessorFactories,
            ProcessorContext processorContext
    ) {
        var map = new HashMap<Class<?>, CommentProcessor>();
        for (var entry : commentProcessorFactories.entrySet()) {
            var processorClass = entry.getKey();
            var processorFactory = entry.getValue();
            var processor = processorFactory.create(processorContext);
            map.put(processorClass, processor);
        }
        return map;
    }
}
