package pro.verron.officestamper.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.verron.officestamper.api.CommentProcessor;
import pro.verron.officestamper.api.ExceptionResolver;

/// Allows registration of [CommentProcessor] objects.
/// Each registered [CommentProcessor] must implement an interface specified at registration time.
/// Provides several getter methods to access the registered [CommentProcessor].
///
/// @author Joseph Verron
/// @author Tom Hombergs
/// @version ${version}
/// @since 1.0.0
public class CommentProcessorRegistry {

    private static final Logger logger = LoggerFactory.getLogger(CommentProcessorRegistry.class);
    private final ExpressionResolver expressionResolver;
    private final ExceptionResolver exceptionResolver;

    /// Constructs a new CommentProcessorRegistry.
    ///
    /// @param expressionResolver the resolver for evaluating expressions.
    /// @param exceptionResolver  the resolver for handling exceptions during processing.
    public CommentProcessorRegistry(
            ExpressionResolver expressionResolver,
            ExceptionResolver exceptionResolver
    ) {
        this.expressionResolver = expressionResolver;
        this.exceptionResolver = exceptionResolver;
    }


}
