package pro.verron.officestamper.core;

import pro.verron.officestamper.api.*;

import java.math.BigInteger;
import java.util.AbstractMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import static pro.verron.officestamper.utils.WmlFactory.*;

public class CommentProcessors
        extends AbstractMap<Class<?>, CommentProcessor> {
    private static final Random RANDOM = new Random();
    private final Map<Class<?>, CommentProcessor> processors;

    public CommentProcessors(Map<Class<?>, CommentProcessor> processors) {
        this.processors = processors;
    }

    static StandardComment fakeComment(
            DocxPart source,
            Paragraph paragraph,
            Placeholder placeholder
    ) {
        var parent = paragraph.getP();
        var id = new BigInteger(16, RANDOM);
        var commentWrapper = new StandardComment(source.document());
        commentWrapper.setComment(newComment(id, placeholder.content()));
        commentWrapper.setCommentRangeStart(newCommentRangeStart(id, parent));
        commentWrapper.setCommentRangeEnd(newCommentRangeEnd(id, parent));
        commentWrapper.setCommentReference(newCommentReference(id, parent));
        return commentWrapper;
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
