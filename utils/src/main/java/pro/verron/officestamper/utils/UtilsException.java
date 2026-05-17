package pro.verron.officestamper.utils;

import java.util.function.Supplier;

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

    /// Constructs a new UtilsException with the specified detail message and cause.
    ///
    /// @param message the detail message
    /// @param t the cause of the exception
    public UtilsException(String message, Throwable t) {
        super(message, t);
    }

    public static Supplier<? extends RuntimeException> supply(String message, Object... args) {
        return () -> new UtilsException(message.formatted(args));
    }
}
