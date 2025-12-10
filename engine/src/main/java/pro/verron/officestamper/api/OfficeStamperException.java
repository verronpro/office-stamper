package pro.verron.officestamper.api;

import org.springframework.util.function.ThrowingFunction;

import java.util.function.Function;
import java.util.function.Supplier;

/// OfficeStamperException is a subclass of RuntimeException that represents an exception that can be thrown during the
/// processing of an Office document using the OfficeStamper library. It provides additional constructors to handle
/// different scenarios.
public class OfficeStamperException
        extends RuntimeException {
    /// OfficeStamperException is a subclass of RuntimeException that represents an exception that can be thrown during
    /// the processing of an Office document using the OfficeStamper library.
    ///
    /// @param message a message describing the error
    public OfficeStamperException(String message) {
        super(message);
    }

    /// OfficeStamperException is a subclass of RuntimeException that represents an exception that can be thrown during
    /// the processing of an Office document using the OfficeStamper library.
    ///
    /// @param cause the cause of the exception
    public OfficeStamperException(Throwable cause) {
        super(cause);
    }

    /// OfficeStamperException is a subclass of RuntimeException that represents an exception that can be thrown during
    /// the processing of an Office document using the OfficeStamper library.
    public OfficeStamperException() {
        super("Unexpected exception");
    }

    /// Creates a supplier that returns a new instance of [OfficeStamperException] with the specified message.
    ///
    /// @param message the message describing the exception
    ///
    /// @return a supplier that provides a new [OfficeStamperException] instance
    public static Supplier<OfficeStamperException> throwing(String message) {
        return () -> new OfficeStamperException(message);
    }

    /// Wraps a ThrowingFunction into a standard Java Function, converting any checked exceptions thrown by the original
    /// function into an OfficeStamperException.
    ///
    /// @param <T> the type of the input to the function
    /// @param <U> the type of the result of the function
    /// @param function the throwing function to be wrapped
    ///
    /// @return a Function that wraps the specified ThrowingFunction and handles exceptions by throwing an
    ///         OfficeStamperException
    public static <T, U> Function<T, U> throwing(ThrowingFunction<T, U> function) {
        return ThrowingFunction.of(function, OfficeStamperException::new);
    }

    /// OfficeStamperException is a subclass of RuntimeException that represents an exception that can be thrown during
    /// the processing of an Office document using the OfficeStamper library.
    ///
    /// @param message a message describing the error
    /// @param cause the cause of the exception
    public OfficeStamperException(String message, Throwable cause) {
        super(message, cause);
    }
}
