package pro.verron.officestamper.core;

import pro.verron.officestamper.api.*;

import java.util.AbstractMap;
import java.util.Map;
import java.util.Set;

public class CommentProcessors
        extends AbstractMap<Class<?>, CommentProcessor> {

    private final Map<Class<?>, CommentProcessor> processors;

    public CommentProcessors(Map<Class<?>, CommentProcessor> processors) {
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

    @Override public Set<Entry<Class<?>, CommentProcessor>> entrySet() {
        return processors.entrySet();
    }
}
