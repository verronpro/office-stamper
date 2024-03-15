package pro.verron.docxstamper.api;

public class DocxStamperException
        extends RuntimeException {
    public DocxStamperException(String message) {super(message);}

    public DocxStamperException(Throwable cause) {super(cause);}

    public DocxStamperException(String message, Throwable cause) {
        super(message,
              cause);
    }
}
