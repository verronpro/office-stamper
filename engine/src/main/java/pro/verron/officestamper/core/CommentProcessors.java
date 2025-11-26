package pro.verron.officestamper.core;

import pro.verron.officestamper.api.CommentProcessor;

import java.util.AbstractMap;

/// The CommentProcessors class is a specialized extension of [AbstractMap] that serves as a registry
/// for associating [CommentProcessor] instances with their corresponding [Class] types.
/// This class facilitates the management and execution of multiple [CommentProcessor] objects by
/// coordinating their lifecycle operations, such as setting contexts and committing changes.
public class CommentProcessors {

    /// Constructs a new CommentProcessors instance with the specified map of processors.
    ///
    public CommentProcessors() {
    }

}
