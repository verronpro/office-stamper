package pro.verron.officestamper.api;

/// A factory for creating [CommentProcessor] instances.
///
/// This functional interface allows implementations to define how comment processors
/// should be created based on the provided context and placeholder replacer.
@FunctionalInterface public interface CommentProcessorFactory {

    /// Creates a new [CommentProcessor] instance.
    ///
    /// @param processorContext the context in which the processor will operate
    /// @param replacer         the replacer used for handling placeholders within paragraphs
    ///
    /// @return a configured [CommentProcessor] instance
    CommentProcessor create(ProcessorContext processorContext, PlaceholderReplacer replacer);
}
