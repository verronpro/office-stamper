package pro.verron.officestamper.api;

import java.util.List;
import java.util.Optional;

/// A table in a document, exposed without leaking the underlying OOXML library.
public interface Table {

    /// Removes the entire table from the document.
    void remove();

    /// @return the rows of this table, in order
    List<Row> rows();

    /// @param row the row to locate
    /// @return the index of the row, or -1 if it is not part of this table
    int indexOf(Row row);

    /// Adds all the given rows to the table starting at the specified index.
    void addAll(int index, List<Row> rows);

    /// Adds a row to the table at the specified index.
    void add(int index, Row row);

    /// A row within a {@link Table}.
    interface Row
            extends Element {

        /// Removes the entire row from the table.
        void remove();

        /// @return the table this row belongs to
        Table table();

        /// @return a deep copy of this row
        Row copy();

        /// @return the cells of this row, in order
        List<Cell> cells();
    }

    /// A cell within a [Row].
    interface Cell
            extends Element {

        /// @return a deep copy of this cell
        Cell copy();

        /// Replaces the content of this cell with the given elements.
        ///
        /// @param elements the new content of the cell
        void setContent(List<Element> elements);
    }
}
