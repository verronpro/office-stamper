package pro.verron.officestamper.spi;

/// A comment anchored in a document part.
public interface Comment {

    /// @return the comment identifier
    String id();

    /// @return the author of the comment
    String author();

    /// @return the textual content of the comment
    String text();
}
