package pro.verron.officestamper.test;

import pro.verron.officestamper.api.CommentProcessor;

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
