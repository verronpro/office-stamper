package pro.verron.officestamper.preset;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.springframework.lang.Nullable;
import pro.verron.officestamper.api.CommentProcessor;
import pro.verron.officestamper.api.OfficeStamper;
import pro.verron.officestamper.api.OfficeStamperConfiguration;
import pro.verron.officestamper.api.ParagraphPlaceholderReplacer;
import pro.verron.officestamper.core.DocxStamper;
import pro.verron.officestamper.preset.processors.displayif.DisplayIfProcessor;
import pro.verron.officestamper.preset.processors.repeat.RepeatProcessor;
import pro.verron.officestamper.preset.processors.repeatdocpart.RepeatDocPartProcessor;
import pro.verron.officestamper.preset.processors.repeatparagraph.ParagraphRepeatProcessor;
import pro.verron.officestamper.preset.processors.replacewith.ReplaceWithProcessor;
import pro.verron.officestamper.preset.processors.table.TableResolver;

/// Factory class to create the correct comment processor for a given comment.
///
/// @author Joseph Verron
/// @version ${version}
/// @since 1.6.4
public class CommentProcessorFactory {
    private final OfficeStamperConfiguration configuration;

    /// Creates a new CommentProcessorFactory.
    ///
    /// @param configuration the configuration to use for the created processors.
    public CommentProcessorFactory(OfficeStamperConfiguration configuration) {
        this.configuration = configuration;
    }

    /// Creates new repeatParagraph [CommentProcessor] with default configuration.
    ///
    /// @param pr a [ParagraphPlaceholderReplacer] object
    ///
    /// @return a [CommentProcessor] object
    public CommentProcessor repeatParagraph(ParagraphPlaceholderReplacer pr) {
        return ParagraphRepeatProcessor.newInstance(pr);
    }

    /// Creates new repeatDocPart [CommentProcessor] with default configuration.
    ///
    /// @param pr a [ParagraphPlaceholderReplacer] object
    ///
    /// @return a [CommentProcessor] object
    public CommentProcessor repeatDocPart(ParagraphPlaceholderReplacer pr) {
        return RepeatDocPartProcessor.newInstance(pr, getStamper());
    }

    private OfficeStamper<WordprocessingMLPackage> getStamper() {
        return (template, context, output) -> new DocxStamper(configuration).stamp(template, context, output);
    }

    /// Creates new repeating [CommentProcessor] with default configuration.
    ///
    /// @param pr a [ParagraphPlaceholderReplacer] object
    ///
    /// @return a [CommentProcessor] object
    public CommentProcessor repeat(ParagraphPlaceholderReplacer pr) {
        return RepeatProcessor.newInstance(pr);
    }

    /// Creates new tableResolver [CommentProcessor] with default configuration.
    ///
    /// @param pr a [ParagraphPlaceholderReplacer] object
    ///
    /// @return a [CommentProcessor] object
    public CommentProcessor tableResolver(ParagraphPlaceholderReplacer pr) {
        return TableResolver.newInstance(pr);
    }

    /// Creates new displayIf [CommentProcessor] with default configuration.
    ///
    /// @param pr a [ParagraphPlaceholderReplacer] object
    ///
    /// @return a [CommentProcessor] object
    public CommentProcessor displayIf(ParagraphPlaceholderReplacer pr) {
        return DisplayIfProcessor.newInstance(pr);
    }

    /// Creates new replaceWith [CommentProcessor] with default configuration.
    ///
    /// @param pr a [ParagraphPlaceholderReplacer] object
    ///
    /// @return a [CommentProcessor] object
    public CommentProcessor replaceWith(ParagraphPlaceholderReplacer pr) {
        return ReplaceWithProcessor.newInstance(pr);
    }

    /// Used to resolve a table in the template document.
    /// Take the table passed-in to fill an existing Tbl object in the document.
    ///
    /// @author Joseph Verron
    /// @version ${version}
    /// @since 1.6.2
    public interface ITableResolver {
        /// Resolves the given table by manipulating the given table in the template.
        ///
        /// @param table the table to resolve.
        void resolveTable(@Nullable StampTable table);
    }

    /// Interface for processors that replace a single word with an expression defined
    /// in a comment.
    ///
    /// @author Joseph Verron
    /// @author Tom Hombergs
    /// @version ${version}
    /// @since 1.0.8
    public interface IReplaceWithProcessor {

        /// Replace a single word inside a paragraph with an expression defined in the comment.
        /// The comment should apply to a single word for the replacement to take effect.
        ///
        /// @param expression the expression to replace the text with
        void replaceWordWith(@Nullable String expression);
    }

    /// An interface that defines a processor for repeating a paragraph
    /// for each element present in the given iterable collection of objects.
    ///
    /// @author Joseph Verron
    /// @author Romain Lamarche
    /// @version ${version}
    /// @since 1.0.0
    public interface IParagraphRepeatProcessor {
        /// Mark a paragraph to be copied once for each element in the passed-in iterable.
        /// Within each copy, placeholder evaluation context is the next object in the iterable.
        ///
        /// @param objects objects serving as evaluation context seeding a new copy.
        void repeatParagraph(@Nullable Iterable<Object> objects);
    }

    /// An interface that defines a processor for repeating a document part
    /// for each element present in the given iterable collection of objects.
    ///
    /// @author Joseph Verron
    /// @author Artem Medvedev
    /// @version ${version}
    /// @since 1.0.0
    public interface IRepeatDocPartProcessor {
        /// Mark a document part to be copied once for each element in the passed-in iterable.
        /// Within each copy, placeholder evaluation context is the next object in the iterable.
        ///
        /// @param objects objects serving as evaluation context seeding a new copy.
        void repeatDocPart(@Nullable Iterable<Object> objects);
    }

    /// An interface that defines a processor for repeating a table row
    /// for each element present in the given iterable collection of objects.
    ///
    /// @author Joseph Verron
    /// @author Tom Hombergs
    /// @version ${version}
    /// @since 1.0.0
    public interface IRepeatProcessor {
        /// Mark a table row to be copied once for each element in the passed-in iterable.
        /// Within each copy, placeholder evaluation context is the next object in the iterable.
        ///
        /// @param objects objects serving as evaluation context seeding a new copy.
        void repeatTableRow(@Nullable Iterable<Object> objects);
    }

    /// Interface for processors used to delete paragraphs or tables from the document, depending on condition.
    ///
    /// @author Joseph Verron
    /// @author Tom Hombergs
    /// @version ${version}
    /// @since 1.0.0
    public interface IDisplayIfProcessor {

        /// @param condition if true, keep the paragraph surrounding the comment, else remove.
        void displayParagraphIf(@Nullable Boolean condition);

        /// @param condition if non-null, keep the paragraph surrounding the comment, else remove.
        void displayParagraphIfPresent(@Nullable Object condition);

        /// @param condition if true, keep the table row surrounding the comment, else remove.
        void displayTableRowIf(@Nullable Boolean condition);

        /// @param condition if non-null, keep the table row surrounding the comment, else remove.
        void displayTableRowIfPresent(@Nullable Object condition);

        /// @param condition if true, keep the table surrounding the comment, else remove.
        void displayTableIf(@Nullable Boolean condition);

        /// @param condition if non-null, keep the table surrounding the comment, else remove.
        void displayTableIfPresent(@Nullable Object condition);
    }
}
