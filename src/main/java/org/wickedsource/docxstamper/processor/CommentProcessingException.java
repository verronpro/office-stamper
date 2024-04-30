package org.wickedsource.docxstamper.processor;

import org.docx4j.wml.P;
import pro.verron.docxstamper.api.OfficeStamperException;

import static java.lang.String.format;
import static org.docx4j.TextUtils.getText;

/**
 * Thrown when an error occurs while processing a comment in the docx template.
 *
 * @author Joseph Verron
 * @author Tom Hombergs
 * @version ${version}
 * @since 1.0.0
 */
public class CommentProcessingException
        extends OfficeStamperException {

    /**
     * <p>Constructor for CommentProcessingException.</p>
     *
     * @param message   the error message
     * @param paragraph the paragraph containing the comment that caused the error
     */
    public CommentProcessingException(String message, P paragraph) {
        super(format("%s : %s", message, getText(paragraph)));
    }
}
