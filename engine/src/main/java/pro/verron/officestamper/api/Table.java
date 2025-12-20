package pro.verron.officestamper.api;

/// Represents a table in a document that can be manipulated by the office stamper. This interface provides methods for
/// table-level operations such as removal.
public interface Table {
    /// Removes the entire table from the document. This operation is irreversible and will delete all rows and content
    /// within the table.
    void remove();


    /// Represents a row within a table that can be manipulated by the office stamper. This interface provides methods
    /// for row-level operations such as removal.
    interface Row {
        /// Removes the entire row from the table. This operation is irreversible and will delete all content within the
        /// row.
        void remove();
    }
}
