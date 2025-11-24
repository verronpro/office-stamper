package pro.verron.officestamper.utils;

import org.docx4j.dml.CTRegularTextRun;
import org.docx4j.wml.CTSmartTagRun;
import org.docx4j.wml.R;
import org.docx4j.wml.RPr;
import pro.verron.officestamper.api.Insert;

import java.util.List;
import java.util.SequencedCollection;

public class Inserts {
    private Inserts() {
        throw new AssertionError("No instances");
    }

    public static Insert of(CTSmartTagRun ctSmartTagRun) {
        return () -> List.of(ctSmartTagRun);
    }

    public static Insert of(R r) {
        return new Insert() {
            @Override
            public SequencedCollection<Object> getElements() {
                return List.of(r);
            }

            @Override
            public void setRPr(RPr rPr) {
                r.setRPr(rPr);
            }
        };
    }

    public static Insert of(CTRegularTextRun ctRegularTextRun) {
        return () -> List.of(ctRegularTextRun);
    }

    public static Insert of(SequencedCollection<Object> elements) {
        return new Insert() {

            @Override
            public SequencedCollection<Object> getElements() {
                return elements;
            }

            @Override
            public void setRPr(RPr rPr) {
                for (Object element : elements) {
                    if (element instanceof R r) {
                        r.setRPr(rPr);
                    }
                }
            }
        };
    }
}
