package pro.verron.officestamper.asciidoc;

import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.*;

import java.math.BigInteger;
import java.util.List;

import static pro.verron.officestamper.asciidoc.AsciiDocModel.*;

/// Renders [AsciiDocModel] into a [WordprocessingMLPackage] using docx4j.
public final class AsciiDocToDocx {
    private AsciiDocToDocx() {}

    /// Creates a new WordprocessingMLPackage and fills it with content from the model.
    ///
    /// @param model parsed AsciiDoc model
    ///
    /// @return package containing the rendered document
    public static WordprocessingMLPackage compileToPackage(AsciiDocModel model) {
        try {
            var pkg = WordprocessingMLPackage.createPackage();
            var factory = Context.getWmlObjectFactory();

            for (Block block : model.getBlocks()) {
                if (block instanceof Heading h) {
                    pkg.getMainDocumentPart()
                       .addObject(createHeading(factory, h));
                }
                else if (block instanceof Paragraph p) {
                    pkg.getMainDocumentPart()
                       .addObject(createParagraph(factory, p));
                }
            }
            return pkg;
        } catch (Docx4JException e) {
            throw new IllegalStateException("Unable to create WordprocessingMLPackage", e);
        }
    }

    private static P createHeading(ObjectFactory factory, Heading heading) {
        P p = factory.createP();
        PPr ppr = factory.createPPr();
        PPrBase.PStyle pStyle = factory.createPPrBasePStyle();
        pStyle.setVal("Heading" + heading.level());
        ppr.setPStyle(pStyle);
        p.setPPr(ppr);

        RPr headingRunPr = factory.createRPr();
        // Increase size a bit relative to level if heading styles are missing
        HpsMeasure sz = factory.createHpsMeasure();
        int base = switch (heading.level()) {
            case 1 -> 32; // 16pt
            case 2 -> 28;
            case 3 -> 26;
            case 4 -> 24;
            case 5 -> 22;
            default -> 20;
        };
        sz.setVal(BigInteger.valueOf(base));
        headingRunPr.setSz(sz);
        headingRunPr.setSzCs(sz);

        addInlines(factory, p, heading.inlines(), headingRunPr);
        return p;
    }

    private static P createParagraph(ObjectFactory factory, Paragraph paragraph) {
        P p = factory.createP();
        addInlines(factory, p, paragraph.inlines(), null);
        return p;
    }

    private static void addInlines(ObjectFactory factory, P p, List<Inline> inlines, RPr base) {
        for (Inline inline : inlines) {
            emitInline(factory, p, inline, base);
        }
    }

    private static void emitInline(ObjectFactory factory, P p, Inline inline, RPr base) {
        if (inline instanceof AsciiDocModel.Text(String text)) {
            R r = factory.createR();
            RPr rpr = base != null ? deepCopy(factory, base) : factory.createRPr();
            org.docx4j.wml.Text tx = factory.createText();
            tx.setValue(text);
            r.getContent()
             .add(tx);
            r.setRPr(rpr);
            p.getContent()
             .add(r);
            return;
        }

        if (inline instanceof Bold(List<Inline> children)) {
            RPr next = base != null ? deepCopy(factory, base) : factory.createRPr();
            next.setB(new BooleanDefaultTrue());
            for (Inline child : children) {
                emitInline(factory, p, child, next);
            }
            return;
        }

        if (inline instanceof Italic(List<Inline> children)) {
            RPr next = base != null ? deepCopy(factory, base) : factory.createRPr();
            next.setI(new BooleanDefaultTrue());
            for (Inline child : children) {
                emitInline(factory, p, child, next);
            }
        }
    }

    private static RPr deepCopy(ObjectFactory factory, RPr src) {
        // Minimal copy of relevant props; docx4j doesn't offer a trivial clone here.
        RPr c = factory.createRPr();
        if (src.getB() != null) {
            BooleanDefaultTrue b = new BooleanDefaultTrue();
            c.setB(b);
        }
        if (src.getI() != null) {
            BooleanDefaultTrue i = new BooleanDefaultTrue();
            c.setI(i);
        }
        if (src.getSz() != null) {
            HpsMeasure sz = factory.createHpsMeasure();
            sz.setVal(src.getSz()
                         .getVal());
            c.setSz(sz);
            HpsMeasure szCs = factory.createHpsMeasure();
            szCs.setVal(src.getSz()
                           .getVal());
            c.setSzCs(szCs);
        }
        return c;
    }
}
