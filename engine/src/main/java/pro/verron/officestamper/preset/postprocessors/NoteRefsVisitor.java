package pro.verron.officestamper.preset.postprocessors;

import org.docx4j.utils.TraversalUtilVisitor;
import org.docx4j.wml.CTFtnEdnRef;

import java.math.BigInteger;
import java.util.SortedSet;
import java.util.TreeSet;

public class NoteRefsVisitor
        extends TraversalUtilVisitor<CTFtnEdnRef> {
    private final SortedSet<BigInteger> ids = new TreeSet<>();

    @Override
    public void apply(CTFtnEdnRef element) {
        ids.add(element.getId());
    }

    public SortedSet<BigInteger> referencedNoteIds() {
        return ids;
    }
}
