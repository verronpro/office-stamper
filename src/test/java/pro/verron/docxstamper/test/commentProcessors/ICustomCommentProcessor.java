package pro.verron.docxstamper.test.commentProcessors;

import org.wickedsource.docxstamper.api.commentprocessor.ICommentProcessor;

/**
 * <p>ICustomCommentProcessor interface.</p>
 *
 * @author Joseph Verron
 * @version ${version}
 * @since 1.6.6
 */
public interface ICustomCommentProcessor extends ICommentProcessor {
    /**
     * <p>visitParagraph.</p>
     */
    void visitParagraph();
}
