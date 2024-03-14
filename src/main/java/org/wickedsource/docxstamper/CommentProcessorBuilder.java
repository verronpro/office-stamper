package org.wickedsource.docxstamper;

import org.wickedsource.docxstamper.api.commentprocessor.ICommentProcessor;
import pro.verron.docxstamper.core.PlaceholderReplacer;

/**
 * Factory interface for creating {@link ICommentProcessor} instances.
 *
 * @author Joseph Verron
 * @version ${version}
 * @since 1.6.5
 */
public interface CommentProcessorBuilder {
    /**
     * Creates a {@link ICommentProcessor} instance.
     *
     * @param placeholderReplacer the placeholder replacer that should be used by the comment processor.
     * @return a {@link ICommentProcessor} instance.
     */
    Object create(PlaceholderReplacer placeholderReplacer);
}
