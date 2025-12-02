package pro.verron.officestamper.api;

/// A factory for creating [CommentProcessor] instances.
///
/// This functional interface allows implementations to define how comment processors should be created based on the
/// provided context and placeholder replacer.
@FunctionalInterface public interface CommentProcessorFactory {


    /// Creates a new [CommentProcessor] instance.
    ///
    /// @param processorContext the context in which the processor will operate
    ///
    /// @return a configured [CommentProcessor] instance
    CommentProcessor create(ProcessorContext processorContext);
}
