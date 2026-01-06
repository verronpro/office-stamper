package pro.verron.officestamper.preset.preprocessors.malformedcomments;

import org.docx4j.utils.TraversalUtilVisitor;
import org.docx4j.wml.R;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class CommentReferenceRemoverVisitor
        extends TraversalUtilVisitor<R.CommentReference> {
    private static final Logger log = LoggerFactory.getLogger(CommentReferenceRemoverVisitor.class);

    private final List<BigInteger> ids;
    private final Map<Object, List<Object>> toRemove = new HashMap<>();

    CommentReferenceRemoverVisitor(List<BigInteger> ids) {
        this.ids = ids;
    }

    @Override
    public void apply(R.CommentReference element, Object parent, List<Object> siblings) {
        if (ids.contains(element.getId())) toRemove.put(element, siblings);
    }

    void run() {
        if (!toRemove.isEmpty()) log.debug("Removed Comment References: {}", toRemove);
        toRemove.forEach((object, siblings) -> siblings.remove(object));
    }
}
