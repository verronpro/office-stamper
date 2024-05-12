package org.wickedsource.docxstamper.util;

import pro.verron.officestamper.api.Comment;
import pro.verron.officestamper.core.StandardComment;

/**
 * @deprecated since 1.6.8, This class has been deprecated in the effort
 * of the library modularization.
 * It is recommended to use the
 * {@link StandardComment} class and
 * {@link Comment} interface
 * instead.
 * This class will be moved to internals in the future releases of the module.
 */
@Deprecated(since = "1.6.8", forRemoval = true)
public class CommentWrapper
        extends StandardComment {

    /**
     * The CommentWrapper class is a subclass of StandardComment that represents a comment wrapper in a Word document.
     * This class extends StandardComment and does not accommodate the feature needing direct document access.
     */
    public CommentWrapper() {
        super(null);
    }
}
