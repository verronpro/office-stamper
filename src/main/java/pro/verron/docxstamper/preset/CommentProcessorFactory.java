package pro.verron.docxstamper.preset;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.wickedsource.docxstamper.DocxStamper;
import org.wickedsource.docxstamper.DocxStamperConfiguration;
import org.wickedsource.docxstamper.api.commentprocessor.ICommentProcessor;
import org.wickedsource.docxstamper.processor.displayif.DisplayIfProcessor;
import org.wickedsource.docxstamper.processor.repeat.ParagraphRepeatProcessor;
import org.wickedsource.docxstamper.processor.repeat.RepeatDocPartProcessor;
import org.wickedsource.docxstamper.processor.repeat.RepeatProcessor;
import org.wickedsource.docxstamper.processor.replaceExpression.ReplaceWithProcessor;
import org.wickedsource.docxstamper.processor.table.TableResolver;
import pro.verron.docxstamper.api.OpcStamper;
import pro.verron.docxstamper.api.ParagraphPlaceholderReplacer;
import pro.verron.docxstamper.core.PlaceholderReplacer;

/**
 * Factory class to create the correct comment processor for a given comment.
 *
 * @author Joseph Verron
 * @version ${version}
 * @since 1.6.4
 */
public class CommentProcessorFactory {
	private final DocxStamperConfiguration configuration;

	/**
	 * Creates a new CommentProcessorFactory.
	 *
	 * @param configuration the configuration to use for the created processors.
	 */
	public CommentProcessorFactory(DocxStamperConfiguration configuration) {
		this.configuration = configuration;
	}

	/**
	 * Creates a new CommentProcessorFactory with default configuration.
	 *
	 * @param pr a {@link PlaceholderReplacer} object
	 * @return a {@link ICommentProcessor} object
	 */
	public ICommentProcessor repeatParagraph(ParagraphPlaceholderReplacer pr) {
		return ParagraphRepeatProcessor.newInstance(pr);
	}

	/**
	 * Creates a new CommentProcessorFactory with default configuration.
	 *
	 * @param pr a {@link PlaceholderReplacer} object
	 * @return a {@link ICommentProcessor} object
	 */
	public ICommentProcessor repeatDocPart(ParagraphPlaceholderReplacer pr) {
		return RepeatDocPartProcessor.newInstance(pr, getStamper());
	}

	private OpcStamper<WordprocessingMLPackage> getStamper() {
		return (template, context, output) -> new DocxStamper<>(configuration).stamp(template, context, output);
	}

	/**
	 * Creates a new CommentProcessorFactory with default configuration.
	 *
	 * @param pr a {@link PlaceholderReplacer} object
	 * @return a {@link ICommentProcessor} object
	 */
	public ICommentProcessor repeat(ParagraphPlaceholderReplacer pr) {
		return RepeatProcessor.newInstance(pr);
	}

	/**
	 * Creates a new CommentProcessorFactory with default configuration.
	 *
	 * @param pr a {@link PlaceholderReplacer} object
	 * @return a {@link ICommentProcessor} object
	 */
	public ICommentProcessor tableResolver(ParagraphPlaceholderReplacer pr) {
		return TableResolver.newInstance(pr);
	}

	/**
	 * Creates a new CommentProcessorFactory with default configuration.
	 *
	 * @param pr a {@link PlaceholderReplacer} object
	 * @return a {@link ICommentProcessor} object
	 */
	public ICommentProcessor displayIf(ParagraphPlaceholderReplacer pr) {
		return DisplayIfProcessor.newInstance(pr);
	}

	/**
	 * Creates a new CommentProcessorFactory with default configuration.
	 *
	 * @param pr a {@link PlaceholderReplacer} object
	 * @return a {@link ICommentProcessor} object
	 */
	public ICommentProcessor replaceWith(ParagraphPlaceholderReplacer pr) {
		return ReplaceWithProcessor.newInstance(pr);
	}
}
