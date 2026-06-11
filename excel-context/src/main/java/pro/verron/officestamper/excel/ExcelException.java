package pro.verron.officestamper.excel;

/// Exception thrown when an error occurs during Excel processing.
public class ExcelException
        extends RuntimeException {
    /// Constructs an ExcelException with the specified cause.
    ///
    /// @param cause the underlying cause of this exception
    public ExcelException(Throwable cause) {
        super(cause);
    }
}
