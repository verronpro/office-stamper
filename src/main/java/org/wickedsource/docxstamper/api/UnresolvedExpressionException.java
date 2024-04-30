package org.wickedsource.docxstamper.api;

import pro.verron.docxstamper.api.OfficeStamperException;

/**
 * This exception is thrown if an expression could not be processed by any comment processor.
 *
 * @author Joseph Verron
 * @author Tom Hombergs
 * @version ${version}
 * @since 1.0.3
 */
public class UnresolvedExpressionException
        extends OfficeStamperException {
    /**
     * <p>Constructor for UnresolvedExpressionException.</p>
     *
     * @param expression the expression that could not be processed.
     * @param cause      the root cause for this exception
     */
    public UnresolvedExpressionException(String expression, Throwable cause) {
        super(String.format("The following expression could not be processed by any comment processor: %s", expression),
                cause);
    }
}
