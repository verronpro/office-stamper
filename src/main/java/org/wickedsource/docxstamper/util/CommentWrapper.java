package org.wickedsource.docxstamper.util;

import pro.verron.docxstamper.core.StandardComment;

/**
 * @deprecated since 1.6.8, This class has been deprecated in the effort
 * of the library modularization.
 * It is recommended to use the
 * {@link pro.verron.docxstamper.core.StandardComment} class and
 * {@link pro.verron.docxstamper.api.Comment} interface
 * instead.
 * This class will be moved to internals in the future releases of the module.
 */
@Deprecated(since = "1.6.8", forRemoval = true)
public class CommentWrapper
        extends StandardComment {
    public CommentWrapper() {
        super(null);
    }
}
