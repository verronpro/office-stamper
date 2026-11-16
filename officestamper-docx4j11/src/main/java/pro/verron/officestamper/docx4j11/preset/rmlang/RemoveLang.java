package pro.verron.officestamper.docx4j11.preset.rmlang;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import pro.verron.officestamper.spi.OfficeDocument;
import pro.verron.officestamper.spi.PreProcessor;

import static pro.verron.officestamper.docx4j11.wml.WmlUtils.visitDocument;

/// The [RemoveLang] preprocessor removes language settings from paragraphs and runs within a Word pkg. This is
/// useful when working with templates where language-specific formatting might interfere with the stamping process.
///
/// This preprocessor specifically targets and removes `w:lang` elements from both run properties (`w:rPr`) and
/// paragraph properties (`w:pPr`) throughout the pkg.
///
/// @author Joseph Verron
public class RemoveLang
        implements PreProcessor {

    @Override
    public void process(OfficeDocument document) {
        var pkg = document.unwrap(WordprocessingMLPackage.class);
        removeRprLang(pkg);
        removePprLang(pkg);
    }

    private static void removeRprLang(WordprocessingMLPackage pkg) {
        var visitor = new RprLangVisitor();
        visitDocument(pkg, visitor);
        for (var rPr : visitor.getrPrs()) {
            rPr.setLang(null);
        }
    }

    private static void removePprLang(WordprocessingMLPackage pkg) {
        var visitor2 = new PprLangVisitor();
        visitDocument(pkg, visitor2);
        for (var rPr : visitor2.getParaPrs()) {
            rPr.setLang(null);
        }
    }
}




