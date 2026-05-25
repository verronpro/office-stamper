package pro.verron.officestamper.asciidoc.core;

import java.math.BigInteger;

/// A builder class for constructing instances of
/// [Comment].
///
/// This class allows setting properties step-by-step, providing a
/// flexible way to construct a complete comment before its creation.
public class CommentBuilder {
    private BigInteger id;
    private int blockStart;
    private int lineStart;
    private int blockEnd;
    private int lineEnd;

    public CommentBuilder(BigInteger id) {
        this.id = id;
    }

    /// Creates and returns a new instance of [Comment].
    /// The instance is constructed with the current state of the builder,
    /// using the configured values for ID, block start, line start, block end, and line end.
    ///
    /// @return a new [Comment] instance with the initialized properties.
    public Comment createComment() {
        return new Comment(id, blockStart, lineStart, blockEnd, lineEnd);
    }

    /// Retrieves the unique identifier associated with this builder instance.
    ///
    /// @return a [BigInteger] representing the current ID.
    public BigInteger getId() {
        return this.id;
    }

    /// Sets the unique identifier for the comment being built.
    ///
    /// @param id the unique identifier for the comment as a [BigInteger]
    /// @return the current instance of [CommentBuilder] for method chaining
    public CommentBuilder setId(BigInteger id) {
        this.id = id;
        return this;
    }

    /// Sets the block start position for the comment being built.
    ///
    /// @param blockStart the starting block position as an integer
    /// @return the current instance of [CommentBuilder] for method chaining
    public CommentBuilder setBlockStart(int blockStart) {
        this.blockStart = blockStart;
        return this;
    }

    /// Sets the starting line position for the comment being built.
    ///
    /// @param lineStart the starting line position as an integer
    /// @return the current instance of [CommentBuilder] for method chaining
    public CommentBuilder setLineStart(int lineStart) {
        this.lineStart = lineStart;
        return this;
    }

    /// Sets the block end position for the comment being built.
    ///
    /// @param blockEnd the ending block position as an integer
    /// @return the current instance of [CommentBuilder] for method chaining
    public CommentBuilder setBlockEnd(int blockEnd) {
        this.blockEnd = blockEnd;
        return this;
    }

    /// Sets the ending line position for the comment being built.
    ///
    /// @param lineEnd the ending line position as an integer
    /// @return the current instance of [CommentBuilder] for method chaining
    public CommentBuilder setLineEnd(int lineEnd) {
        this.lineEnd = lineEnd;
        return this;
    }
}
