package pro.verron.officestamper.asciidoc;

import java.math.BigInteger;

public class CommentBuilder {
    private BigInteger id;
    private int blockStart;
    private int lineStart;
    private int blockEnd;
    private int lineEnd;

    public DocxToAsciiDoc.CommentRecorder.Comment createComment() {
        return new DocxToAsciiDoc.CommentRecorder.Comment(id, blockStart, lineStart, blockEnd, lineEnd);
    }

    public BigInteger getId() {
        return this.id;
    }

    public CommentBuilder setId(BigInteger id) {
        this.id = id;
        return this;
    }

    public CommentBuilder setBlockStart(int blockStart) {
        this.blockStart = blockStart;
        return this;
    }

    public CommentBuilder setLineStart(int lineStart) {
        this.lineStart = lineStart;
        return this;
    }

    public CommentBuilder setBlockEnd(int blockEnd) {
        this.blockEnd = blockEnd;
        return this;
    }

    public CommentBuilder setLineEnd(int lineEnd) {
        this.lineEnd = lineEnd;
        return this;
    }
}
