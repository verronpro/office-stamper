package pro.verron.officestamper.asciidoc.core;

import java.math.BigInteger;

/// Represents a comment with positional information, identified by a unique ID.
///
/// This class is an immutable record storing metadata about a comment,
/// including its unique identifier, starting and ending block positions,
/// as well as starting and ending line positions.
///
/// @param id the unique identifier for the comment
/// @param blockStart the starting block position of the comment
/// @param lineStart the starting line position of the comment
/// @param blockEnd the ending block position of the comment
/// @param lineEnd the ending line position of the comment
public record Comment(
        BigInteger id,
        int blockStart,
        int lineStart,
        int blockEnd,
        int lineEnd
) {
}
