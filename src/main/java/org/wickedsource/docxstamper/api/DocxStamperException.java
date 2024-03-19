package org.wickedsource.docxstamper.api;

import pro.verron.docxstamper.api.OfficeStamperException;

/**
 * This class represents an exception that can be thrown during the processing of a Docx file using the DocxStamper library.
 * It extends the RuntimeException class and provides additional constructors to handle different scenarios.
 *
 * @author Joseph Verron
 * @author Tom Hombergs
 * @version ${version}
 * @since 1.0.0
 * @deprecated since 1.6.8, This class has been deprecated in the effort
 * of the library modularization.
 * It is recommended to use the {@link OfficeStamperException} class instead.
 * This class will not be exported in the future releases of the module.
 */
@Deprecated(since = "1.6.8", forRemoval = true)
public class DocxStamperException
        extends OfficeStamperException {

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
