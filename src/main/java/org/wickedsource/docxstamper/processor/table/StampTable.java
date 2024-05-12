package org.wickedsource.docxstamper.processor.table;

import org.springframework.lang.NonNull;

import java.util.List;

/**
 * @deprecated since 1.6.8, This class has been deprecated in the effort
 * of the library modularization.
 * It is recommended to use the
 * {@link pro.verron.officestamper.preset.StampTable} class instead.
 * This class will be removed in the future releases of the module.
 */
@Deprecated(since = "1.6.8", forRemoval = true)
public class StampTable
        extends pro.verron.officestamper.preset.StampTable {
    /**
     * {@inheritDoc}
     */
    public StampTable() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    public StampTable(
            @NonNull List<String> headers,
            @NonNull List<List<String>> records
    ) {
        super(headers, records);
    }

}
