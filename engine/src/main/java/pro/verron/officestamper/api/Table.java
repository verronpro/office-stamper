package pro.verron.officestamper.api;

import org.docx4j.wml.Tr;

import java.util.List;

/// Represents a table in a document that can be manipulated by the office stamper. This interface provides methods for
/// table-level operations such as removal.
public interface Table {
    /// Removes the entire table from the document. This operation is irreversible and will delete all rows and content
    /// within the table.
    void remove();

    /// Returns the index of the given row within the table.
    ///
    /// @param row the row to find the index of
    ///
    /// @return the index of the row, or -1 if not found
    int indexOf(Row row);

    /// Adds all the given rows to the table starting at the specified index.
    ///
    /// @param index the index at which to start adding the rows
    /// @param rows the list of rows to add
    void addAll(int index, List<Row> rows);

    /// Adds a row to the table at the specified index.
    ///
    /// @param index the index at which to add the row
    /// @param row the row to add
    void add(int index, Row row);


    /// Represents a row within a table that can be manipulated by the office stamper. This interface provides methods
    /// for row-level operations such as removal.
    interface Row {
        /// Removes the entire row from the table. This operation is irreversible and will delete all content within the
        /// row.
        void remove();

        /// Returns the table that this row belongs to.
        ///
        /// @return the [Table] object.
        Table table();

        /// Creates a deep copy of this row.
        ///
        /// @return a copy of the [Row].
        Row copy();

        /// Removes a comment from this row.
        ///
        /// @param comment the [Comment] to remove.
        void removeComment(Comment comment);

        /// Returns the hooks associated with this row.
        ///
        /// @return an [Iterable] of [Hook] objects.
        Iterable<Hook> hooks();

        /// Returns the underlying docx4j [Tr] object.
        ///
        /// @return the [Tr] object.
        Tr asTr();
    }
}
