package org.wickedsource.docxstamper.processor;

import org.wickedsource.docxstamper.DocxStamper;
import pro.verron.docxstamper.api.AbstractCommentProcessor;
import pro.verron.docxstamper.api.ParagraphPlaceholderReplacer;

/**
 * Base class for comment processors. The current run and paragraph are set by the {@link DocxStamper} class.
 *
 * @author Joseph Verron
 * @author Tom Hombergs
 * @version ${version}
 * @since 1.0.0
 */
public abstract class BaseCommentProcessor
		extends AbstractCommentProcessor {

	/**
	 * <p>Constructor for BaseCommentProcessor.</p>
	 *
	 * @param placeholderReplacer PlaceholderReplacer used to replace placeholders in the comment text.
	 */
	protected BaseCommentProcessor(ParagraphPlaceholderReplacer placeholderReplacer) {
		super(placeholderReplacer);
	}

}
