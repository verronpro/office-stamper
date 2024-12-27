package pro.verron.officestamper.test;

import pro.verron.officestamper.api.CommentProcessor;

/// ICustomCommentProcessor interface.
///
/// @author Joseph Verron
/// @version ${version}
/// @since 1.6.6
public interface ICustomCommentProcessor
        extends CommentProcessor {

    void visitParagraph();
}
