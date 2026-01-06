package pro.verron.officestamper.preset.preprocessors.malformedcomments;

import org.docx4j.utils.TraversalUtilVisitor;
import org.docx4j.wml.CommentRangeStart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class CommentRangeStartRemoverVisitor
        extends TraversalUtilVisitor<CommentRangeStart> {
    private static final Logger log = LoggerFactory.getLogger(CommentRangeStartRemoverVisitor.class);
    private final List<BigInteger> ids;
    private final Map<Object, List<Object>> toRemove = new HashMap<>();

    CommentRangeStartRemoverVisitor(List<BigInteger> ids) {
        this.ids = ids;
    }

    @Override
    public void apply(CommentRangeStart element, Object parent, List<Object> siblings) {
        if (ids.contains(element.getId())) toRemove.put(element, siblings);
    }

    void run() {
        if (!toRemove.isEmpty()) log.debug("Removed Comment Range Starts: {}", toRemove);
        toRemove.forEach((object, siblings) -> siblings.remove(object));
    }
}
