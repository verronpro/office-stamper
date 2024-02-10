package org.wickedsource.docxstamper.api;

/**
 * This class represents an exception that can be thrown during the processing of a Docx file using the DocxStamper library.
 * It extends the RuntimeException class and provides additional constructors to handle different scenarios.
 *
 * @author Joseph Verron
 * @version 1.6.6
 */
public class DocxStamperException extends RuntimeException {

    /**
     * <p>Constructor for DocxStamperException.</p>
     *
     * @param message a message describing the error
     */
    public DocxStamperException(String message) {
        super(message);
    }

    /**
     * <p>Constructor for DocxStamperException.</p>
     *
     * @param message a message describing the error
     * @param cause   the cause of the error
     */
    public DocxStamperException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * <p>Constructor for DocxStamperException.</p>
     *
     * @param cause the cause of the error
     */
    public DocxStamperException(Throwable cause) {
        super(cause);
    }
}
