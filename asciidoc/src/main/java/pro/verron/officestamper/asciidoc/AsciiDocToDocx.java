package pro.verron.officestamper.asciidoc;

import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.*;
import org.jspecify.annotations.Nullable;

import java.math.BigInteger;
import java.util.List;
import java.util.function.Function;

import static pro.verron.officestamper.asciidoc.AsciiDocModel.*;

/// Renders [AsciiDocModel] into a [WordprocessingMLPackage] using docx4j.
public final class AsciiDocToDocx
        implements Function<AsciiDocModel, WordprocessingMLPackage> {

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

    private static void addInlines(ObjectFactory factory, P p, List<Inline> inlines, @Nullable RPr base) {
        for (Inline inline : inlines) {
            emitInline(factory, p, inline, base);
        }
    }

    private static Tbl createTable(ObjectFactory factory, Table table) {
        Tbl tbl = factory.createTbl();
        // Minimal table without explicit grid; Word will auto-fit columns
        for (Row row : table.rows()) {
            Tr tr = factory.createTr();
            for (Cell cell : row.cells()) {
                Tc tc = factory.createTc();
                // Add a single paragraph per cell for now
                P p = factory.createP();
                addInlines(factory, p, cell.inlines(), null);
                tc.getContent()
                  .add(p);
                tr.getContent()
                  .add(tc);
            }
            tbl.getContent()
               .add(tr);
        }
        return tbl;
    }

    private static P createListItem(ObjectFactory factory, ListItem item, String prefix) {
        P p = factory.createP();
        R r = factory.createR();
        org.docx4j.wml.Text t = factory.createText();
        t.setValue(prefix);
        r.getContent()
         .add(t);
        p.getContent()
         .add(r);
        addInlines(factory, p, item.inlines(), null);
        return p;
    }

    private static P createBlockquote(ObjectFactory factory, Blockquote blockquote) {
        P p = factory.createP();
        PPr ppr = factory.createPPr();
        PPrBase.Ind ind = factory.createPPrBaseInd();
        ind.setLeft(BigInteger.valueOf(720)); // 0.5 inch
        ppr.setInd(ind);
        p.setPPr(ppr);
        addInlines(factory, p, blockquote.inlines(), null);
        return p;
    }

    private static P createCodeBlock(ObjectFactory factory, CodeBlock codeBlock) {
        P p = factory.createP();
        RPr rpr = factory.createRPr();
        RFonts fonts = factory.createRFonts();
        fonts.setAscii("Courier New");
        fonts.setHAnsi("Courier New");
        rpr.setRFonts(fonts);

        String[] lines = codeBlock.content()
                                  .split("\n");
        for (int i = 0; i < lines.length; i++) {
            R r = factory.createR();
            r.setRPr(rpr);
            org.docx4j.wml.Text t = factory.createText();
            t.setValue(lines[i]);
            t.setSpace("preserve");
            r.getContent()
             .add(t);
            if (i < lines.length - 1) {
                r.getContent()
                 .add(factory.createBr());
            }
            p.getContent()
             .add(r);
        }
        return p;
    }

    private static P createImageBlock(ObjectFactory factory, ImageBlock imageBlock) {
        P p = factory.createP();
        R r = factory.createR();
        org.docx4j.wml.Text t = factory.createText();
        t.setValue("[Image: " + imageBlock.url() + " - " + imageBlock.altText() + "]");
        r.getContent()
         .add(t);
        p.getContent()
         .add(r);
        return p;
    }

    private static void emitInline(ObjectFactory factory, P p, Inline inline, @Nullable RPr base) {
        switch (inline) {
            case AsciiDocModel.Text(String text) -> {
                RPr rpr = base != null ? deepCopy(factory, base) : factory.createRPr();
                String[] lines = text.split("\n", -1);
                for (int i = 0; i < lines.length; i++) {
                    if (!lines[i].isEmpty()) {
                        R r = factory.createR();
                        r.setRPr(rpr);
                        org.docx4j.wml.Text tx = factory.createText();
                        tx.setValue(lines[i]);
                        tx.setSpace("preserve");
                        r.getContent()
                         .add(tx);
                        p.getContent()
                         .add(r);
                    }
                    if (i < lines.length - 1) {
                        R r = factory.createR();
                        r.setRPr(rpr);
                        r.getContent()
                         .add(factory.createBr());
                        p.getContent()
                         .add(r);
                    }
                }
                return;
            }
            case Bold(List<Inline> children) -> {
                RPr next = base != null ? deepCopy(factory, base) : factory.createRPr();
                next.setB(new BooleanDefaultTrue());
                for (Inline child : children) {
                    emitInline(factory, p, child, next);
                }
                return;
            }
            case Italic(List<Inline> children) -> {
                RPr next = base != null ? deepCopy(factory, base) : factory.createRPr();
                next.setI(new BooleanDefaultTrue());
                for (Inline child : children) {
                    emitInline(factory, p, child, next);
                }
                return;
            }
            case Tab _ -> {
                R r = factory.createR();
                R.Tab tab = factory.createRTab();
                r.getContent()
                 .add(tab);
                p.getContent()
                 .add(r);
            }
            default -> { /* DO NOTHING */ }
        }

        if (inline instanceof Link link) {
            R r = factory.createR();
            RPr rpr = base != null ? deepCopy(factory, base) : factory.createRPr();
            Color color = factory.createColor();
            color.setVal("0000FF");
            rpr.setColor(color);
            U u = factory.createU();
            u.setVal(UnderlineEnumeration.SINGLE);
            rpr.setU(u);
            r.setRPr(rpr);
            org.docx4j.wml.Text t = factory.createText();
            t.setValue(link.text());
            r.getContent()
             .add(t);
            p.getContent()
             .add(r);
        }

        if (inline instanceof InlineImage ii) {
            R r = factory.createR();
            org.docx4j.wml.Text t = factory.createText();
            t.setValue("[Image: " + ii.url() + "]");
            r.getContent()
             .add(t);
            p.getContent()
             .add(r);
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

    /// Creates a new WordprocessingMLPackage and fills it with content from the model.
    ///
    /// @param model parsed AsciiDoc model
    ///
    /// @return package containing the rendered document
    public WordprocessingMLPackage apply(AsciiDocModel model) {
        try {
            var pkg = WordprocessingMLPackage.createPackage();
            var factory = Context.getWmlObjectFactory();
            pkg.getMainDocumentPart()
               .getContent()
               .clear();

            for (Block block : model.getBlocks()) {
                final var mainDocumentPart = pkg.getMainDocumentPart();
                switch (block) {
                    case Heading h -> mainDocumentPart.addObject(createHeading(factory, h));
                    case Paragraph p -> mainDocumentPart.addObject(createParagraph(factory, p));
                    case Table t -> mainDocumentPart.addObject(createTable(factory, t));
                    case UnorderedList(List<ListItem> items1) -> {
                        for (ListItem item : items1) {
                            mainDocumentPart.addObject(createListItem(factory, item, "* "));
                        }
                    }
                    case OrderedList(List<ListItem> items) -> {
                        int i = 1;
                        for (ListItem item : items) {
                            mainDocumentPart.addObject(createListItem(factory, item, (i++) + ". "));
                        }
                    }
                    case Blockquote b -> mainDocumentPart.addObject(createBlockquote(factory, b));
                    case CodeBlock cb -> mainDocumentPart.addObject(createCodeBlock(factory, cb));
                    case ImageBlock ib -> mainDocumentPart.addObject(createImageBlock(factory, ib));
                    default -> { /* DO NOTHING */ }
                }
            }
            return pkg;
        } catch (Docx4JException e) {
            throw new IllegalStateException("Unable to create WordprocessingMLPackage", e);
        }
    }
}
