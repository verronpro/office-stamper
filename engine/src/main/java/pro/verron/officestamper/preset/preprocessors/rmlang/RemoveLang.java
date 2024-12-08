package pro.verron.officestamper.preset.preprocessors.rmlang;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.ParaRPr;
import org.docx4j.wml.RPr;
import pro.verron.officestamper.api.PreProcessor;

import static pro.verron.officestamper.core.DocumentUtil.visitDocument;

public class RemoveLang
        implements PreProcessor {

    @Override
    public void process(WordprocessingMLPackage document) {
        var visitor = new RprLangVisitor();
        visitDocument(document, visitor);
        for (RPr rPr : visitor.getrPrs()) {
            rPr.setLang(null);
        }
        var visitor2 = new PprLangVisitor();
        visitDocument(document, visitor2);
        for (ParaRPr rPr : visitor2.getrPrs()) {
            rPr.setLang(null);
        }
    }
}
