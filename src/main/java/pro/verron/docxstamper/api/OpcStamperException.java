package pro.verron.docxstamper.api;

public class OpcStamperException
        extends RuntimeException {
    public OpcStamperException(String message) {super(message);}

    public OpcStamperException(Throwable cause) {super(cause);}

    public OpcStamperException(String message, Throwable cause) {
        super(message,
              cause);
    }
}
