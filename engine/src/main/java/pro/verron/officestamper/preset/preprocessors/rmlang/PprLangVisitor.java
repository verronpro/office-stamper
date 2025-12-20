package pro.verron.officestamper.preset.preprocessors.rmlang;

import org.docx4j.utils.TraversalUtilVisitor;
import org.docx4j.wml.P;
import org.docx4j.wml.ParaRPr;

import java.util.ArrayList;
import java.util.List;

public class PprLangVisitor
        extends TraversalUtilVisitor<P> {
    private final List<ParaRPr> paraRPrs = new ArrayList<>();

    @Override
    public void apply(P element, Object parent1, List<Object> siblings) {
        var elementPPr = element.getPPr();
        if (elementPPr == null) return;
        var elementRPr = elementPPr.getRPr();
        if (elementRPr == null) return;
        var elementLang = elementRPr.getLang();
        if (elementLang == null) return;
        paraRPrs.add(elementRPr);
    }

    public List<ParaRPr> getParaPrs() {
        return paraRPrs;
    }
}
