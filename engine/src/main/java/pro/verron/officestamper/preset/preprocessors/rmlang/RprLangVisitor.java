package pro.verron.officestamper.preset.preprocessors.rmlang;

import org.docx4j.utils.TraversalUtilVisitor;
import org.docx4j.wml.R;
import org.docx4j.wml.RPr;

import java.util.ArrayList;
import java.util.List;

/// A visitor implementation for processing Run Properties (RPr) language elements in DOCX documents. This class extends
/// TraversalUtilVisitor to traverse and collect RPr elements that contain language information.
///
/// This visitor is specifically designed to identify and store RPr objects that have non-null language settings, which
/// can be used for further processing or analysis of document formatting.
public class RprLangVisitor
        extends TraversalUtilVisitor<R> {
    private final List<RPr> rPrs = new ArrayList<>();

    @Override
    public void apply(R element, Object parent1, List<Object> siblings) {
        var elementRPr = element.getRPr();
        if (elementRPr == null) return;
        var elementLang = elementRPr.getLang();
        if (elementLang == null) return;
        rPrs.add(elementRPr);
    }


    /// Returns the list of RPr elements that have been collected during the traversal. These RPr elements contain
    /// language information and were identified as having non-null language settings.
    ///
    /// @return a list of RPr elements with language information
    public List<RPr> getrPrs() {
        return rPrs;
    }
}
