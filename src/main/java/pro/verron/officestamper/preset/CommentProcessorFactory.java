package pro.verron.officestamper.preset;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.wickedsource.docxstamper.DocxStamper;
import org.wickedsource.docxstamper.api.commentprocessor.ICommentProcessor;
import org.wickedsource.docxstamper.processor.displayif.DisplayIfProcessor;
import org.wickedsource.docxstamper.processor.repeat.ParagraphRepeatProcessor;
import org.wickedsource.docxstamper.processor.repeat.RepeatDocPartProcessor;
import org.wickedsource.docxstamper.processor.repeat.RepeatProcessor;
import org.wickedsource.docxstamper.processor.replaceExpression.ReplaceWithProcessor;
import org.wickedsource.docxstamper.processor.table.TableResolver;
import pro.verron.officestamper.api.CommentProcessor;
import pro.verron.officestamper.api.OfficeStamper;
import pro.verron.officestamper.api.OfficeStamperConfiguration;
import pro.verron.officestamper.api.ParagraphPlaceholderReplacer;
import pro.verron.officestamper.core.PlaceholderReplacer;

/**
 * Factory class to create the correct comment processor for a given comment.
 *
 * @author Joseph Verron
 * @version ${version}
 * @since 1.6.4
 */
public class CommentProcessorFactory {
	private final OfficeStamperConfiguration configuration;

	/**
	 * Creates a new CommentProcessorFactory.
	 *
	 * @param configuration the configuration to use for the created processors.
	 */
	public CommentProcessorFactory(OfficeStamperConfiguration configuration) {
		this.configuration = configuration;
	}

	/**
	 * Creates a new CommentProcessorFactory with default configuration.
	 *
	 * @param pr a {@link PlaceholderReplacer} object
	 * @return a {@link ICommentProcessor} object
	 */
	public CommentProcessor repeatParagraph(ParagraphPlaceholderReplacer pr) {
		return ParagraphRepeatProcessor.newInstance(pr);
	}

	/**
	 * Creates a new CommentProcessorFactory with default configuration.
	 *
	 * @param pr a {@link PlaceholderReplacer} object
	 * @return a {@link ICommentProcessor} object
	 */
	public CommentProcessor repeatDocPart(ParagraphPlaceholderReplacer pr) {
		return RepeatDocPartProcessor.newInstance(pr, getStamper());
	}

	private OfficeStamper<WordprocessingMLPackage> getStamper() {
		return (template, context, output) -> new DocxStamper<>(configuration).stamp(template, context, output);
	}

	/**
	 * Creates a new CommentProcessorFactory with default configuration.
	 *
	 * @param pr a {@link PlaceholderReplacer} object
	 * @return a {@link ICommentProcessor} object
	 */
	public CommentProcessor repeat(ParagraphPlaceholderReplacer pr) {
		return RepeatProcessor.newInstance(pr);
	}

	/**
	 * Creates a new CommentProcessorFactory with default configuration.
	 *
	 * @param pr a {@link PlaceholderReplacer} object
	 * @return a {@link ICommentProcessor} object
	 */
	public CommentProcessor tableResolver(ParagraphPlaceholderReplacer pr) {
		return TableResolver.newInstance(pr);
	}

	/**
	 * Creates a new CommentProcessorFactory with default configuration.
	 *
	 * @param pr a {@link PlaceholderReplacer} object
	 * @return a {@link ICommentProcessor} object
	 */
	public CommentProcessor displayIf(ParagraphPlaceholderReplacer pr) {
		return DisplayIfProcessor.newInstance(pr);
	}

	/**
	 * Creates a new CommentProcessorFactory with default configuration.
	 *
	 * @param pr a {@link PlaceholderReplacer} object
	 * @return a {@link ICommentProcessor} object
	 */
	public CommentProcessor replaceWith(ParagraphPlaceholderReplacer pr) {
		return ReplaceWithProcessor.newInstance(pr);
	}
}
