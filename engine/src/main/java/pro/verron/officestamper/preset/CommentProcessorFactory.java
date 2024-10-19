package pro.verron.officestamper.preset;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.*;
import org.springframework.lang.Nullable;
import pro.verron.officestamper.api.*;
import pro.verron.officestamper.core.*;
import pro.verron.officestamper.preset.processors.displayif.DisplayIfProcessor;
import pro.verron.officestamper.preset.processors.repeat.RepeatProcessor;
import pro.verron.officestamper.preset.processors.repeatdocpart.RepeatDocPartProcessor;
import pro.verron.officestamper.preset.processors.repeatparagraph.ParagraphRepeatProcessor;
import pro.verron.officestamper.preset.processors.replacewith.ReplaceWithProcessor;
import pro.verron.officestamper.preset.processors.table.TableResolver;

import java.util.*;

import static java.lang.String.format;
import static org.docx4j.TextUtils.getText;

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

    public static Tbl assertTable(Object obj) {
        if (obj instanceof Tbl table) return table;
        throw new OfficeStamperException(format("Paragraph is not within a table! : %s", getText(obj)));
    }

    public static Tr assertTableRow(Object obj) {
        if (obj instanceof Tr row) return row;
        throw new OfficeStamperException(format("Paragraph is not within a row! : %s", getText(obj)));
    }

    public static Tc assertTableCell(Object obj) {
        if (obj instanceof Tc cell) return cell;
        throw new OfficeStamperException(format("Paragraph is not within a cell! : %s", getText(obj)));
    }

    /**
     * Creates a new CommentProcessorFactory with default configuration.
     *
     * @param pr a {@link PlaceholderReplacer} object
     *
     * @return a {@link CommentProcessor} object
     */
    public CommentProcessor repeatParagraph(ParagraphPlaceholderReplacer pr) {
        return ParagraphRepeatProcessor.newInstance(pr);
    }

    /**
     * Creates a new CommentProcessorFactory with default configuration.
     *
     * @param pr a {@link PlaceholderReplacer} object
     *
     * @return a {@link CommentProcessor} object
     */
    public CommentProcessor repeatDocPart(ParagraphPlaceholderReplacer pr) {
        return RepeatDocPartProcessor.newInstance(pr, getStamper());
    }

    private OfficeStamper<WordprocessingMLPackage> getStamper() {
        return (template, context, output) -> new DocxStamper(configuration).stamp(template, context, output);
    }

    /**
     * Creates a new CommentProcessorFactory with default configuration.
     *
     * @param pr a {@link PlaceholderReplacer} object
     *
     * @return a {@link CommentProcessor} object
     */
    public CommentProcessor repeat(ParagraphPlaceholderReplacer pr) {
        return RepeatProcessor.newInstance(pr);
    }

    /**
     * Creates a new CommentProcessorFactory with default configuration.
     *
     * @param pr a {@link PlaceholderReplacer} object
     *
     * @return a {@link CommentProcessor} object
     */
    public CommentProcessor tableResolver(ParagraphPlaceholderReplacer pr) {
        return TableResolver.newInstance(pr);
    }

    /**
     * Creates a new CommentProcessorFactory with default configuration.
     *
     * @param pr a {@link PlaceholderReplacer} object
     *
     * @return a {@link CommentProcessor} object
     */
    public CommentProcessor displayIf(ParagraphPlaceholderReplacer pr) {
        return DisplayIfProcessor.newInstance(pr);
    }

    /**
     * Creates a new CommentProcessorFactory with default configuration.
     *
     * @param pr a {@link PlaceholderReplacer} object
     *
     * @return a {@link CommentProcessor} object
     */
    public CommentProcessor replaceWith(ParagraphPlaceholderReplacer pr) {
        return ReplaceWithProcessor.newInstance(pr);
    }

    /**
     * This interface is used to resolve a table in the template document.
     * The table is passed to the resolveTable method and will be used to fill an existing Tbl object in the document.
     *
     * @author Joseph Verron
     * @version ${version}
     * @since 1.6.2
     */
    public interface ITableResolver {
        /**
         * Resolves the given table by manipulating the given table in the template
         *
         * @param table the table to resolve.
         */
        void resolveTable(StampTable table);
    }

    /**
     * Interface for processors that replace a single word with an expression defined
     * in a comment.
     *
     * @author Joseph Verron
     * @author Tom Hombergs
     * @version ${version}
     * @since 1.0.8
     */
    public interface IReplaceWithProcessor {

        /**
         * May be called to replace a single word inside a paragraph with an expression
         * defined in a comment. The comment must be applied to a single word for the
         * replacement to take effect!
         *
         * @param expression the expression to replace the text with
         */
        void replaceWordWith(@Nullable String expression);
    }

    /**
     * Implementations of this interface are responsible for processing the repeat paragraph instruction.
     * The repeat paragraph instruction is a comment that contains the following text:
     * <p>
     * <code>
     * repeatParagraph(...)
     * </code>
     * <p>
     * Where the three dots represent an expression that evaluates to a list of objects.
     * The processor then copies the paragraph once for each object in the list and evaluates all expressions
     * within each copy against the respective object.
     *
     * @author Joseph Verron
     * @author Romain Lamarche
     * @version ${version}
     * @since 1.0.0
     */
    public interface IParagraphRepeatProcessor {

        /**
         * May be called to mark a paragraph to be copied once for each element in the passed-in list.
         * Within each copy of the row, all expressions are evaluated against one of the objects in the list.
         *
         * @param objects the objects which serve as context root for expressions found in the template table row.
         */
        void repeatParagraph(@Nullable List<Object> objects);
    }

    /**
     * Interface for processors which may be called to mark a document part to be copied once for each element in the
     * passed-in list.
     * Within each copy of the row, all expressions are evaluated against one of the objects in the list.
     *
     * @author Joseph Verron
     * @author Artem Medvedev
     * @version ${version}
     * @since 1.0.0
     */
    public interface IRepeatDocPartProcessor {

        /**
         * May be called to mark a document part to be copied once for each element in the passed-in list.
         * Within each copy of the row, all expressions are evaluated against one of the objects in the list.
         *
         * @param objects the objects which serve as context root for expressions found in the template table row.
         */
        void repeatDocPart(@Nullable List<Object> objects);
    }

    /**
     * Interface for processors that can repeat a table row.
     *
     * @author Joseph Verron
     * @author Tom Hombergs
     * @version ${version}
     * @since 1.0.0
     */
    public interface IRepeatProcessor {

        /**
         * May be called to mark a table row to be copied once for each element in the passed-in list.
         * Within each copy of the row, all expressions are evaluated against one of the objects in the list.
         *
         * @param objects the objects which serve as context root for expressions found in the template table row.
         */
        void repeatTableRow(List<Object> objects);

    }

    /**
     * Interface for processors that may be used to delete commented paragraphs or tables from the document, depending
     * on a given condition.
     *
     * @author Joseph Verron
     * @author Tom Hombergs
     * @version ${version}
     * @since 1.0.0
     */
    public interface IDisplayIfProcessor {

        /**
         * May be called to delete the commented paragraph or not, depending on the given boolean condition.
         *
         * @param condition if true, the commented paragraph will remain in the document. If false, the commented
         *                  paragraph
         *                  will be deleted at stamping.
         */
        void displayParagraphIf(Boolean condition);

        /**
         * May be called to delete the commented paragraph or not, depending on the presence of the given data.
         *
         * @param condition if non-null, the commented paragraph will remain in
         *                  the document. If null, the commented paragraph
         *                  will be deleted at stamping.
         */
        void displayParagraphIfPresent(@Nullable Object condition);

        /**
         * May be called to delete the table surrounding the commented paragraph, depending on the given boolean
         * condition.
         *
         * @param condition if true, the table row surrounding the commented paragraph will remain in the document. If
         *                  false, the table row
         *                  will be deleted at stamping.
         */
        void displayTableRowIf(Boolean condition);

        /**
         * May be called to delete the table surrounding the commented paragraph, depending on the given boolean
         * condition.
         *
         * @param condition if true, the table surrounding the commented paragraph will remain in the document. If
         *                  false, the table
         *                  will be deleted at stamping.
         */
        void displayTableIf(Boolean condition);

    }

}
