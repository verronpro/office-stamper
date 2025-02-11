package pro.verron.officestamper.preset;

import org.springframework.lang.NonNull;

import java.util.*;

import static java.util.Collections.singletonList;

/// Represents a table with several columns, a header line, and several lines of content
///
/// @author Joseph Verron
/// @version ${version}
/// @since 1.6.2
public class StampTable
        extends AbstractSequentialList<List<String>> {
    private final List<String> headers;
    private final List<List<String>> records;

    /// Instantiate an empty table
    public StampTable() {
        this.headers = new ArrayList<>();
        this.records = new ArrayList<>();
    }

    /// Instantiate a table with headers and several lines
    ///
    /// @param headers the header lines
    /// @param records the lines that the table should contain
    public StampTable(
            @NonNull List<String> headers,
            @NonNull List<List<String>> records
    ) {
        this.headers = headers;
        this.records = records;
    }

    /// @return a [StampTable] object
    public static StampTable empty() {
        return new StampTable(
                singletonList("placeholder"),
                singletonList(singletonList("placeholder"))
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        StampTable lists = (StampTable) o;
        return Objects.equals(headers, lists.headers) && Objects.equals(records, lists.records);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), headers, records);
    }

    @Override
    public int size() {
        return records.size();
    }

    @Override
    @NonNull
    public ListIterator<List<String>> listIterator(int index) {
        return records.listIterator(index);
    }

    /// @return a [List] object
    public List<String> headers() {
        return headers;
    }
}
