package pro.verron.officestamper.core;

import pro.verron.officestamper.api.ProcessorContext;

/// A functional interface responsible for creating instances of the [Engine] class.
///
/// This interface acts as a factory for producing [Engine] objects in the context of the specified [ProcessorContext].
/// It abstracts the creation logic, allowing for flexible instantiation of [Engine] based on the provided context.
///
/// Implementations of this interface can define how the [Engine] is constructed using the given [ProcessorContext].
@FunctionalInterface public interface EngineFactory {

    /// Creates an instance of the [Engine] class based on the provided [ProcessorContext]. This method serves as a
    /// factory function to generate [Engine] objects tailored to the given context.
    ///
    /// @param processorContext the context containing the required components to configure the [Engine]
    ///         instance
    ///
    /// @return a newly created [Engine] instance configured according to the provided [ProcessorContext]
    Engine create(ProcessorContext processorContext);
}
