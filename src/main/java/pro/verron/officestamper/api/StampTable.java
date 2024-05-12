package pro.verron.officestamper.api;

import org.springframework.lang.NonNull;

import java.util.AbstractSequentialList;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import static java.util.Collections.singletonList;

/**
 * Represents a table with several columns, a header line, and several lines of content
 *
 * @author Joseph Verron
 * @version ${version}
 * @since 1.6.2
 */
public class StampTable
        extends AbstractSequentialList<List<String>> {
    private final List<String> headers;
    private final List<List<String>> records;

    /**
     * Instantiate an empty table
     */
    public StampTable() {
        this.headers = new ArrayList<>();
        this.records = new ArrayList<>();
    }

    /**
     * Instantiate a table with headers and several lines
     *
     * @param headers the header lines
     * @param records the lines that the table should contain
     */
    public StampTable(
            @NonNull List<String> headers,
            @NonNull List<List<String>> records
    ) {
        this.headers = headers;
        this.records = records;
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

    /**
     * <p>empty.</p>
     *
     * @return a {@link StampTable} object
     */
    public static StampTable empty() {
        return new StampTable(
                singletonList("placeholder"),
                singletonList(singletonList("placeholder"))
        );
    }

    /**
     * <p>headers.</p>
     *
     * @return a {@link List} object
     */
    public List<String> headers() {
        return headers;
    }

}
