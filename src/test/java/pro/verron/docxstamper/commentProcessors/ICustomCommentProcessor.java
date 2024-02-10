package pro.verron.docxstamper.commentProcessors;

import org.wickedsource.docxstamper.api.commentprocessor.ICommentProcessor;

/**
 * <p>ICustomCommentProcessor interface.</p>
 *
 * @author Joseph Verron
 * @version 1.6.6
 * @since 1.6.6
 */
public interface ICustomCommentProcessor extends ICommentProcessor {
    /**
     * <p>visitParagraph.</p>
     */
    void visitParagraph();
}
