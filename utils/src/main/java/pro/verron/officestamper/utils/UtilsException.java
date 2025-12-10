package pro.verron.officestamper.utils;

/// Exception thrown when an error occurs in the utils module.
public class UtilsException
        extends RuntimeException {

    /// Constructs a new UtilsException with the specified detail message.
    ///
    /// @param message the detail message
    public UtilsException(String message) {
        super(message);
    }


    /// Constructs a new UtilsException with the specified cause.
    ///
    /// @param t the cause of the exception
    public UtilsException(Throwable t) {
        super(t);
    }
}
