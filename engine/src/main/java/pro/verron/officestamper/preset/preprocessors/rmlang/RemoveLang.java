package pro.verron.officestamper.preset.preprocessors.rmlang;

import pro.verron.officestamper.api.PreProcessor;
import pro.verron.officestamper.utils.wml.DocxDocument;

import static pro.verron.officestamper.utils.wml.WmlUtils.visitDocument;

/// The [RemoveLang] preprocessor removes language settings from paragraphs and runs within a Word document. This is
/// useful when working with templates where language-specific formatting might interfere with the stamping process.
///
/// This preprocessor specifically targets and removes `w:lang` elements from both run properties (`w:rPr`) and
/// paragraph properties (`w:pPr`) throughout the document.
///
/// @author Joseph Verron
public class RemoveLang
        implements PreProcessor {

    @Override
    public void process(DocxDocument document) {
        removeRprLang(document);
        removePprLang(document);
    }

    private static void removeRprLang(DocxDocument document) {
        var visitor = new RprLangVisitor();
        visitDocument(document, visitor);
        for (var rPr : visitor.getrPrs()) {
            rPr.setLang(null);
        }
    }

    private static void removePprLang(DocxDocument document) {
        var visitor2 = new PprLangVisitor();
        visitDocument(document, visitor2);
        for (var rPr : visitor2.getParaPrs()) {
            rPr.setLang(null);
        }
    }
}
