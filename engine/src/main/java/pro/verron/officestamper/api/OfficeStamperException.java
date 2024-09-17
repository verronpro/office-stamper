package pro.verron.officestamper.api;

/**
 * OfficeStamperException is a subclass of RuntimeException that represents an exception that can be thrown during the
 * processing of an Office document using the OfficeStamper library.
 * It provides additional constructors to handle different scenarios.
 */
public class OfficeStamperException
        extends RuntimeException {
    /**
     * OfficeStamperException is a subclass of RuntimeException that represents an exception that can be thrown
     * during the processing of an Office document using the OfficeStamper
     * library.
     *
     * @param message a message describing the error
     */
    public OfficeStamperException(String message) {
        super(message);
    }

    /**
     * OfficeStamperException is a subclass of RuntimeException that represents an exception that can be thrown
     * during the processing of an Office document using the OfficeStamper
     * library.
     *
     * @param cause the cause of the exception
     */
    public OfficeStamperException(Throwable cause) {
        super(cause);
    }

    /**
     * OfficeStamperException is a subclass of RuntimeException that represents an exception that can be thrown
     * during the processing of an Office document using the OfficeStamper
     * library.
     *
     * @param message a message describing the error
     * @param cause   the cause of the exception
     */
    public OfficeStamperException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * OfficeStamperException is a subclass of RuntimeException
     * that represents an exception
     * that can be thrown during the processing of an Office document using the OfficeStamper
     * library.
     */
    public OfficeStamperException() {
        super("Unexpected exception");
    }

    /**
     * Throws an OfficeStamperException encapsulating the provided exception.
     *
     * @param exception the exception to be thrown
     * @return this method does not return a value; it always throws an exception
     * @throws OfficeStamperException always thrown to indicate an unexpected error.
     */
    public static <T> T sneakyThrow(Exception exception) {
        throw new OfficeStamperException("Unexpected error", exception);
    }
}
