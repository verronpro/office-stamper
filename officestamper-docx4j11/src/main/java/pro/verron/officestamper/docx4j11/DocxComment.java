package pro.verron.officestamper.docx4j11;

import org.docx4j.wml.Comments;
import org.docx4j.wml.ContentAccessor;
import org.docx4j.wml.Text;
import pro.verron.officestamper.api.Comment;

/// docx4j 11 implementation of {@link Comment}.
public final class DocxComment implements Comment {

    private final Comments.Comment comment;

    DocxComment(Comments.Comment comment) {
        this.comment = comment;
    }

    @Override
    public String id() {
        return String.valueOf(comment.getId());
    }

    @Override
    public String author() {
        return comment.getAuthor();
    }

    @Override
    public String text() {
        return textOf(comment);
    }

    /// @return the textual content of a comment object
    static String textOf(Comments.Comment comment) {
        if (comment instanceof ContentAccessor ca) {
            var sb = new StringBuilder();
            collect(ca, sb);
            return sb.toString();
        }
        return "";
    }

    private static void collect(ContentAccessor ca, StringBuilder sb) {
        for (Object o : ca.getContent()) {
            if (o instanceof Text t) sb.append(t.getValue());
            else if (o instanceof ContentAccessor c) collect(c, sb);
        }
    }
}
