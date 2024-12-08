package pro.verron.officestamper.preset.preprocessors.rmlang;

import org.docx4j.utils.TraversalUtilVisitor;
import org.docx4j.wml.R;
import org.docx4j.wml.RPr;

import java.util.ArrayList;
import java.util.List;

public class RprLangVisitor
        extends TraversalUtilVisitor<R> {
    private final List<RPr> rPrs = new ArrayList<>();

    @Override
    public void apply(R element, Object parent1, List<Object> siblings) {
        if (element.getRPr() != null && element.getRPr()
                                               .getLang() != null)
            rPrs.add(element.getRPr());
    }

    public List<RPr> getrPrs() {
        return rPrs;
    }
}
