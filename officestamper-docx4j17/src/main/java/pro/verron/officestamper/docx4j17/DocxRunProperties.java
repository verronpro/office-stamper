package pro.verron.officestamper.docx4j17;

import org.docx4j.wml.RPr;
import pro.verron.officestamper.api.RunProperties;

/// docx4j 11 implementation of {@link RunProperties}, snapshotting a run's {@code RPr}.
final class DocxRunProperties
        implements RunProperties {

    private final RPr rPr;

    DocxRunProperties(RPr rPr) {
        this.rPr = rPr;
    }

    RPr rPr() {
        return rPr;
    }

    @Override
    public <T> T unwrap(Class<T> type) {
        if (type.isInstance(rPr)) return type.cast(rPr);
        throw new IllegalArgumentException("Cannot unwrap RunProperties as " + type.getName());
    }
}
