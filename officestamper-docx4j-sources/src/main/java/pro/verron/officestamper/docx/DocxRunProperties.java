package pro.verron.officestamper.docx;

import org.docx4j.wml.RPr;
import pro.verron.officestamper.api.RunProperties;

/// docx4j 11 implementation of [RunProperties], snapshotting a run's `RPr`.
record DocxRunProperties(RPr rPr) implements RunProperties {

    @Override
    public <T> T unwrap(Class<T> type) {
        if (type.isInstance(rPr)) return type.cast(rPr);
        throw new IllegalArgumentException("Cannot unwrap RunProperties as " + type.getName());
    }
}
