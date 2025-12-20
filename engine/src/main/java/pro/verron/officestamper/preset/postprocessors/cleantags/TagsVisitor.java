package pro.verron.officestamper.preset.postprocessors.cleantags;

import org.docx4j.utils.TraversalUtilVisitor;
import org.docx4j.wml.CTSmartTagRun;

import java.util.ArrayList;
import java.util.List;

public class TagsVisitor
        extends TraversalUtilVisitor<CTSmartTagRun> {
    private final String element;
    private final List<CTSmartTagRun> results = new ArrayList<>();

    public TagsVisitor(String element) {this.element = element;}

    @Override
    public void apply(CTSmartTagRun tag) {
        if (element.equals(tag.getElement())) results.add(tag);
    }

    public List<CTSmartTagRun> getTags() {
        return results;
    }
}
