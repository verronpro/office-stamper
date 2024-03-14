package org.wickedsource.docxstamper.processor;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.P;
import org.docx4j.wml.R;
import org.wickedsource.docxstamper.DocxStamper;
import org.wickedsource.docxstamper.api.commentprocessor.ICommentProcessor;
import pro.verron.docxstamper.api.CommentWrapper;
import pro.verron.docxstamper.api.ParagraphPlaceholderReplacer;

import java.util.Objects;

/**
 * Base class for comment processors. The current run and paragraph are set by the {@link DocxStamper} class.
 *
 * @author Joseph Verron
 * @author Tom Hombergs
 * @version ${version}
 * @since 1.0.0
 */
public abstract class BaseCommentProcessor implements ICommentProcessor {

	/**
	 * PlaceholderReplacer used to replace expressions in the comment text.
	 */
	protected final ParagraphPlaceholderReplacer placeholderReplacer;

	private P paragraph;
	private R currentRun;
	private CommentWrapper currentCommentWrapper;
	private WordprocessingMLPackage document;

	/**
	 * <p>Constructor for BaseCommentProcessor.</p>
	 *
	 * @param placeholderReplacer PlaceholderReplacer used to replace placeholders in the comment text.
	 */
	protected BaseCommentProcessor(ParagraphPlaceholderReplacer placeholderReplacer) {
		this.placeholderReplacer = placeholderReplacer;
	}

	/** {@inheritDoc} */
	@Override
	public void setCurrentRun(R run) {
		this.currentRun = run;
	}

	/** {@inheritDoc} */
	@Override
	public void setParagraph(P paragraph) {
		this.paragraph = paragraph;
	}

	/** {@inheritDoc} */
	@Override
	public void setCurrentCommentWrapper(CommentWrapper currentCommentWrapper) {
		Objects.requireNonNull(currentCommentWrapper.getCommentRangeStart());
		Objects.requireNonNull(currentCommentWrapper.getCommentRangeEnd());
		this.currentCommentWrapper = currentCommentWrapper;
	}

	/**
	 * {@inheritDoc}
	 * @deprecated the document is passed to the processor through the commitChange method now,
	 * and will probably pe passed through the constructor in the future
	 */
	@Deprecated(since = "1.6.5", forRemoval = true)
	@Override
	public void setDocument(WordprocessingMLPackage document) {
		this.document = document;
	}

	/**
	 * <p>Getter for the field <code>currentCommentWrapper</code>.</p>
	 *
	 * @return a {@link CommentWrapper} object
	 */
	public CommentWrapper getCurrentCommentWrapper() {
		return currentCommentWrapper;
	}

	/**
	 * <p>Getter for the field <code>paragraph</code>.</p>
	 *
	 * @return a {@link org.docx4j.wml.P} object
	 */
	public P getParagraph() {
		return paragraph;
	}

	/**
	 * <p>Getter for the field <code>currentRun</code>.</p>
	 *
	 * @return a {@link org.docx4j.wml.R} object
	 */
	public R getCurrentRun() {
		return currentRun;
	}

	/**
	 * <p>Getter for the field <code>document</code>.</p>
	 *
	 * @return a {@link WordprocessingMLPackage} object
	 * @deprecated the document is passed to the processor through the commitChange method now
	 * and will probably pe passed through the constructor in the future
	 */
	@Deprecated(since = "1.6.5", forRemoval = true)
	public WordprocessingMLPackage getDocument() {
		return document;
	}
}
