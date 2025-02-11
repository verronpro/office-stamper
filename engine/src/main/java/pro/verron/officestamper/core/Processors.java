package pro.verron.officestamper.core;

import pro.verron.officestamper.api.DocxPart;
import pro.verron.officestamper.api.Processor;
import pro.verron.officestamper.api.ProcessorContext;

import java.util.AbstractMap;
import java.util.Map;
import java.util.Set;

/// The Processors class serves as a container for managing and interacting with
/// multiple instances of Processor implementations. It provides functionality
/// to set processing context, apply changes to a DocxPart, and reset states for reusability.
/// This class extends AbstractMap to provide map-like behavior, where the key is the
/// class of the Processor and the value is the actual processor instance.
public class Processors
        extends AbstractMap<Class<?>, Processor> {

    private final Map<Class<?>, Processor> processors;

    public Processors(Map<Class<?>, Processor> processors) {
        this.processors = processors;
    }

    public void setContext(ProcessorContext context) {
        for (var processor : processors.values()) {
            processor.setProcessorContext(context);
        }
    }

    void commitChanges(DocxPart source) {
        for (var processor : processors.values()) {
            processor.commitChanges(source);
            processor.reset();
        }
    }

    /// Applies all comment processors to the provided DocxPart using the given processing context.
    /// Each processor in the collection will execute its defined behavior on the specified document part.
    ///
    /// @param context The processing context that contains details such as paragraph, run, comment, and placeholder
    ///                               being processed.
    /// @param source  The part of the .docx document to which the processors' changes are applied.
    void apply(ProcessorContext context, DocxPart source) {
        for (var processor : processors.values()) {
            processor.apply(context, source);
        }
    }

    @Override
    public Set<Entry<Class<?>, Processor>> entrySet() {
        return processors.entrySet();
    }
}
