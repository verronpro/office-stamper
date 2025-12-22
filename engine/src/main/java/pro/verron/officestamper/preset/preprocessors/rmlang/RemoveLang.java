package pro.verron.officestamper.preset.preprocessors.rmlang;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import pro.verron.officestamper.api.PreProcessor;

import static pro.verron.officestamper.core.DocumentUtil.visitDocument;

/// The [RemoveLang] preprocessor removes language settings from paragraphs and runs within a Word document. This is
/// useful when working with templates where language-specific formatting might interfere with the stamping process.
///
/// This preprocessor specifically targets and removes `w:lang` elements from both run properties (`w:rPr`) and
/// paragraph properties (`w:pPr`) throughout the document.
///
/// @author Joseph Verron
/// @version ${version}
public class RemoveLang
        implements PreProcessor {

    @Override
    public void process(WordprocessingMLPackage document) {
        removeRprLang(document);
        removePprLang(document);
    }

    private static void removeRprLang(WordprocessingMLPackage document) {
        var visitor = new RprLangVisitor();
        visitDocument(document, visitor);
        for (var rPr : visitor.getrPrs()) {
            rPr.setLang(null);
        }
    }

    private static void removePprLang(WordprocessingMLPackage document) {
        var visitor2 = new PprLangVisitor();
        visitDocument(document, visitor2);
        for (var rPr : visitor2.getParaPrs()) {
            rPr.setLang(null);
        }
    }
}
