package pro.verron.docxstamper.api;

public class OfficeStamperException
        extends RuntimeException {
    public OfficeStamperException(String message) {super(message);}

    public OfficeStamperException(Throwable cause) {super(cause);}

    public OfficeStamperException(String message, Throwable cause) {
        super(message,
              cause);
    }
}
