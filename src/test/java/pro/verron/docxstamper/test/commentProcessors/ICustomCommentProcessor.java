package pro.verron.docxstamper.test.commentProcessors;

import pro.verron.docxstamper.api.CommentProcessor;

/**
 * <p>ICustomCommentProcessor interface.</p>
 *
 * @author Joseph Verron
 * @version ${version}
 * @since 1.6.6
 */
public interface ICustomCommentProcessor
        extends CommentProcessor {
    /**
     * <p>visitParagraph.</p>
     */
    void visitParagraph();
}
