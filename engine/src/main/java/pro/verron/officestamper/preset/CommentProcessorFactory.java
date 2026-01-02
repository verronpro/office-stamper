package pro.verron.officestamper.preset;

import org.jspecify.annotations.Nullable;
import pro.verron.officestamper.api.OfficeStamperException;

/// Factory class to create the correct comment processor for a given comment.
///
/// @author Joseph Verron
/// @version ${version}
/// @since 1.6.4
public class CommentProcessorFactory {

    private CommentProcessorFactory() {
        throw new OfficeStamperException("CommentProcessorFactory cannot be instantiated");
    }

    /// Used to resolve a table in the template document. Take the table passed-in to fill an existing Tbl object in the
    /// document.
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

    /// Interface for processors that replace a single word with an expression defined in a comment.
    ///
    /// @author Joseph Verron
    /// @author Tom Hombergs
    /// @version ${version}
    /// @since 1.0.8
    public interface IReplaceWithProcessor {
        /// Replaces content with the specified expression.Works only in a single paragraph.
        ///
        /// @param expression the expression to replace the content with; it may be null.
        void replaceWith(@Nullable String expression);
    }

    /// An interface that defines a processor for repeating a paragraph for each element present in the given iterable
    /// collection of objects.
    ///
    /// @author Joseph Verron
    /// @author Romain Lamarche
    /// @version ${version}
    /// @since 1.0.0
    public interface IParagraphRepeatProcessor {
        /// Mark a paragraph to be copied once for each element in the passed-in iterable. Within each copy, the
        /// placeholder evaluation context is the next object in the iterable.
        ///
        /// @param objects objects serving as evaluation context seeding a new copy.
        void repeatParagraph(@Nullable Iterable<Object> objects);
    }

    public interface IRepeatProcessor {
        void repeat(@Nullable Iterable<Object> items);
    }

    /// An interface that defines a processor for repeating a document part for each element present in the given
    /// iterable collection of objects.
    ///
    /// @author Joseph Verron
    /// @author Artem Medvedev
    /// @version ${version}
    /// @since 1.0.0
    public interface IRepeatDocPartProcessor {
        /// Mark a document part to be copied once for each element in the passed-in iterable. Within each copy, the
        /// placeholder evaluation context is the next object in the iterable.
        ///
        /// @param objects objects serving as evaluation context seeding a new copy.
        void repeatDocPart(@Nullable Iterable<Object> objects);
    }

    /// An interface that defines a processor for repeating a table row for each element present in the given iterable
    /// collection of objects.
    ///
    /// @author Joseph Verron
    /// @author Tom Hombergs
    /// @version ${version}
    /// @since 1.0.0
    public interface IRepeatRowProcessor {
        /// Mark a table row to be copied once for each element in the passed-in iterable. Within each copy, the
        /// placeholder evaluation context is the next object in the iterable.
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

        /// Displays or removes the paragraph surrounding a specific comment in a document based on the given
        /// condition.
        ///
        /// @param condition if non-null, keep the paragraph surrounding the comment, else remove.
        void displayParagraphIf(@Nullable Boolean condition);

        /// Displays or removes the paragraph surrounding a specific comment in a document based on the given
        /// condition.
        ///
        /// @param condition if non-null, keep the paragraph surrounding the comment, else remove.
        void displayParagraphIfPresent(@Nullable Object condition);

        /// Displays or removes the paragraph surrounding a specific comment in a document based on the given
        /// condition.
        ///
        /// @param condition if null, keep the paragraph surrounding the comment, else remove.
        void displayParagraphIfAbsent(@Nullable Object condition);

        /// Displays or removes the table row surrounding a specific comment in a document based on the given
        /// condition.
        ///
        /// @param condition if true, keep the table row surrounding the comment, else remove.
        void displayTableRowIf(@Nullable Boolean condition);

        /// Displays or removes the table row surrounding a specific comment in a document based on the given
        /// condition.
        ///
        /// @param condition if non-null, keep the table row surrounding the comment, else remove.
        void displayTableRowIfPresent(@Nullable Object condition);

        /// Displays or removes the table row surrounding a specific comment in a document based on the given
        /// condition.
        ///
        /// @param condition if null, keep the table row surrounding the comment, else remove.
        void displayTableRowIfAbsent(@Nullable Object condition);

        /// Displays or removes the table surrounding a specific comment in a document based on the given condition.
        ///
        /// @param condition if true, keep the table surrounding the comment, else remove.
        void displayTableIf(@Nullable Boolean condition);

        /// Displays or removes the table surrounding a specific comment in a document based on the given condition.
        ///
        /// @param condition if non-null, keep the table surrounding the comment, else remove.
        void displayTableIfPresent(@Nullable Object condition);

        /// Displays or removes the table surrounding a specific comment in a document based on the given condition.
        ///
        /// @param condition if null, keep the table surrounding the comment, else remove.
        void displayTableIfAbsent(@Nullable Object condition);

        /// Displays or removes the selected words surrounding a specific comment in a document based on the given
        /// condition.
        ///
        /// @param condition if true, keep the selected words surrounding the comment, else remove.
        void displayWordsIf(@Nullable Boolean condition);

        /// Displays or removes the selected words surrounding a specific comment in a document based on the given
        /// condition.
        ///
        /// @param condition if non-null, keep the selected words surrounding the comment, else remove.
        void displayWordsIfPresent(@Nullable Object condition);

        /// Displays or removes the selected words surrounding a specific comment in a document based on the given
        /// condition.
        ///
        /// @param condition if null, keep the selected words surrounding the comment, else remove.
        void displayWordsIfAbsent(@Nullable Object condition);

        /// Displays or removes the selected elements surrounding a specific comment in a document based on the given
        /// condition.
        ///
        /// @param condition if true, keep the selected elements surrounding the comment, else remove.
        void displayDocPartIf(@Nullable Boolean condition);

        /// Displays or removes the selected elements surrounding a specific comment in a document based on the given
        /// condition.
        ///
        /// @param condition if non-null, keep the selected elements surrounding the comment, else remove.
        void displayDocPartIfPresent(@Nullable Object condition);

        /// Displays or removes the selected elements surrounding a specific comment in a document based on the given
        /// condition.
        ///
        /// @param condition if null, keep the selected elements surrounding the comment, else remove.
        void displayDocPartIfAbsent(@Nullable Object condition);
    }
}
