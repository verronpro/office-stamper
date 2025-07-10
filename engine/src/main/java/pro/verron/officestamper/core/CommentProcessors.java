package pro.verron.officestamper.core;

import pro.verron.officestamper.api.*;

import java.util.AbstractMap;
import java.util.Map;
import java.util.Set;

/**
 * The CommentProcessors class is a specialized extension of {@link AbstractMap} that serves as a registry
 * for associating {@link CommentProcessor} instances with their corresponding {@link Class} types.
 * This class facilitates the management and execution of multiple {@link CommentProcessor} objects by
 * coordinating their lifecycle operations, such as setting contexts and committing changes.
 */
public class CommentProcessors
        extends AbstractMap<Class<?>, CommentProcessor> {

    private final Map<Class<?>, CommentProcessor> processors;

    /**
     * Constructs a new CommentProcessors instance with the specified map of processors.
     *
     * @param processors a map associating {@link Class} types with their corresponding {@link CommentProcessor}
     *                   instances; this map serves as the registry for managing and executing the processors
     */
    public CommentProcessors(Map<Class<?>, CommentProcessor> processors) {
        this.processors = processors;
    }

    /**
     * Sets the context for all registered {@link CommentProcessor} instances.
     * This method delegates the provided {@link ProcessorContext} to each processor in the registry,
     * enabling them to operate within the specified context.
     *
     * @param context the context in which the processors will operate, containing details about
     *                the paragraph, run, comment, and placeholder being processed
     */
    void setContext(ProcessorContext context) {
        for (var processor : processors.values()) {
            processor.setProcessorContext(context);
        }
    }

    /**
     * Commits all changes made to the provided {@link DocxPart} across all registered {@link CommentProcessor} instances.
     * This method ensures that each processor finalizes its processing for the given document part and resets its
     * internal state to prepare for subsequent operations.
     *
     * @param source the {@link DocxPart} instance representing the part of the document that is being processed;
     *               used as input for committing changes and finalizing the processing tasks of the comment processors
     */
    void commitChanges(DocxPart source) {
        for (var processor : processors.values()) {
            processor.commitChanges(source);
            processor.reset();
        }
    }

    @Override public Set<Entry<Class<?>, CommentProcessor>> entrySet() {
        return processors.entrySet();
    }
}
