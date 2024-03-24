package pro.verron.docxstamper.api;

public interface Paragraph<T> {
    void replace(Placeholder placeholder, T replacement);

    String asString();
}
