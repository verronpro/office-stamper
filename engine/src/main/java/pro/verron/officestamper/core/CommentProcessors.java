package pro.verron.officestamper.core;

import org.docx4j.wml.*;
import org.springframework.lang.Nullable;
import pro.verron.officestamper.api.*;
import pro.verron.officestamper.utils.WmlFactory;

import java.math.BigInteger;
import java.util.AbstractMap;
import java.util.Map;
import java.util.Set;

public class CommentProcessors
        extends AbstractMap<Class<?>, CommentProcessor> {
    private final Map<Class<?>, CommentProcessor> processors;

    public CommentProcessors(Map<Class<?>, CommentProcessor> processors) {
        this.processors = processors;
    }

    void setContext(
            DocxPart source,
            Paragraph paragraph,
            Placeholder placeholder
    ) {
        var commentWrapper = new StandardComment(source.document());
        commentWrapper.setComment(WmlFactory.newComment(placeholder.content()));
        var commentRangeStart = new CommentRangeStart();
        commentRangeStart.setId(BigInteger.TEN);
        commentRangeStart.setParent(paragraph.getP());
        commentWrapper.setCommentRangeStart(commentRangeStart);
        var commentRangeEnd = new CommentRangeEnd();
        commentRangeEnd.setId(BigInteger.TEN);
        commentRangeEnd.setParent(paragraph.getP());
        commentWrapper.setCommentRangeEnd(commentRangeEnd);
        var commentReference = new R.CommentReference();
        commentReference.setId(BigInteger.TEN);
        commentReference.setParent(paragraph.getP());
        commentWrapper.setCommentReference(commentReference);
        var run = (R) paragraph.paragraphContent()
                               .get(0);
        setContext(paragraph, run, commentWrapper);
    }

    public void setContext(Paragraph paragraph, @Nullable R run, Comment comment) {
        for (var processor : processors.values()) {
            processor.setProcessorContext(paragraph, run, comment);
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
