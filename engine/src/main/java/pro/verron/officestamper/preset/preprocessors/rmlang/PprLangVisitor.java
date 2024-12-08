package pro.verron.officestamper.preset.preprocessors.rmlang;

import org.docx4j.utils.TraversalUtilVisitor;
import org.docx4j.wml.P;
import org.docx4j.wml.ParaRPr;

import java.util.ArrayList;
import java.util.List;

public class PprLangVisitor
        extends TraversalUtilVisitor<P> {
    private final List<ParaRPr> rPrs = new ArrayList<>();

    @Override
    public void apply(P element, Object parent1, List<Object> siblings) {
        if (element.getPPr() != null && element.getPPr()
                                               .getRPr() != null && element.getPPr()
                                                                           .getRPr()
                                                                           .getLang() != null)
            rPrs.add(element.getPPr()
                            .getRPr());
    }

    public List<ParaRPr> getrPrs() {
        return rPrs;
    }
}
