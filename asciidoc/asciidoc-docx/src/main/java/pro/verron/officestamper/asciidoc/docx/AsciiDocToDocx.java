package pro.verron.officestamper.asciidoc.docx;

import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.WordprocessingML.DocumentSettingsPart;
import org.docx4j.openpackaging.parts.WordprocessingML.FooterPart;
import org.docx4j.openpackaging.parts.WordprocessingML.HeaderPart;
import org.docx4j.relationships.Relationship;
import org.docx4j.wml.*;
import org.jspecify.annotations.Nullable;
import pro.verron.officestamper.asciidoc.core.AsciiDocModel;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static pro.verron.officestamper.asciidoc.core.AsciiDocModel.*;

/// Renders [AsciiDocModel] into a [WordprocessingMLPackage] using docx4j.
public final class AsciiDocToDocx
        implements Function<AsciiDocModel, WordprocessingMLPackage> {

    private int headerCount = 1;
    private int footerCount = 1;

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
                for (Block block : cell.blocks()) {
                    addBlock(factory, tc.getContent(), block);
                }
                tr.getContent()
                  .add(tc);
            }
            tbl.getContent()
               .add(tr);
        }
        return tbl;
    }

    private static void addBlock(ObjectFactory factory, List<Object> content, Block block)
            throws UnsupportedOperationException {
        switch (block) {
            case Heading h -> content.add(createHeading(factory, h));
            case Paragraph p -> content.add(createParagraph(factory, p));
            case Table t -> content.add(createTable(factory, t));
            case UnorderedList(List<ListItem> items1) -> {
                for (ListItem item : items1) {
                    content.add(createListItem(factory, item, "* "));
                }
            }
            case OrderedList(List<ListItem> items) -> {
                int i = 1;
                for (ListItem item : items) {
                    content.add(createListItem(factory, item, (i++) + ". "));
                }
            }
            case Blockquote b -> content.add(createBlockquote(factory, b));
            case CodeBlock cb -> content.add(createCodeBlock(factory, cb));
            case ImageBlock ib -> content.add(createImageBlock(factory, ib));
            case Break _ -> throw new java.lang.UnsupportedOperationException("Breaks are not supported");
            case CommentLine _ -> throw new UnsupportedOperationException("Comments are not supported");
            case OpenBlock ob -> {
                for (Block subBlock : ob.content()) {
                    addBlock(factory, content, subBlock);
                }
            }
            case MacroBlock macroBlock -> throw new UnsupportedOperationException("Macro blocks are not supported");
        }
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
            t.setValue("[Image: " + ii.path() + "]");
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
    @Override
    public WordprocessingMLPackage apply(AsciiDocModel model) {
        headerCount = 1;
        footerCount = 1;
        try {
            var pkg = WordprocessingMLPackage.createPackage();
            var factory = Context.getWmlObjectFactory();
            var mainContent = pkg.getMainDocumentPart()
                                 .getContent();
            mainContent.clear();

            for (Block block : model.getBlocks()) {
                if (block instanceof OpenBlock ob && isHeaderOrFooter(ob)) {
                    processHeaderOrFooter(pkg, factory, ob);
                }
                else {
                    addBlock(factory, mainContent, block);
                }
            }
            return pkg;
        } catch (Docx4JException e) {
            throw new IllegalStateException("Unable to create WordprocessingMLPackage", e);
        }
    }

    private static boolean isHeaderOrFooter(OpenBlock ob) {
        return ob.header()
                 .stream()
                 .anyMatch(h -> h.startsWith("header") || h.startsWith("footer"));
    }

    private void processHeaderOrFooter(
            WordprocessingMLPackage pkg,
            ObjectFactory factory,
            OpenBlock ob
    ) {
        String role = ob.header()
                        .get(0);
        try {
            if (role.startsWith("header")) {
                HeaderPart hp = new HeaderPart(new PartName("/word/header" + (headerCount++) + ".xml"));
                hp.setJaxbElement(factory.createHdr());
                for (Block subBlock : ob.content()) {
                    addBlock(factory, hp.getContent(), subBlock);
                }
                Relationship rel = pkg.getMainDocumentPart()
                                      .addTargetPart(hp);
                addReference(pkg, factory, rel.getId(), role, true);
            }
            else if (role.startsWith("footer")) {
                FooterPart fp = new FooterPart(new PartName("/word/footer" + (footerCount++) + ".xml"));
                fp.setJaxbElement(factory.createFtr());
                for (Block subBlock : ob.content()) {
                    addBlock(factory, fp.getContent(), subBlock);
                }
                Relationship rel = pkg.getMainDocumentPart()
                                      .addTargetPart(fp);
                addReference(pkg, factory, rel.getId(), role, false);
            }
        } catch (Docx4JException e) {
            throw new RuntimeException(e);
        }
    }

    private static void addReference(
            WordprocessingMLPackage pkg,
            ObjectFactory factory,
            String relId,
            String role,
            boolean isHeader
    ) {
        SectPr sectPr = pkg.getMainDocumentPart()
                           .getJaxbElement()
                           .getBody()
                           .getSectPr();
        if (sectPr == null) {
            sectPr = factory.createSectPr();
            pkg.getMainDocumentPart()
               .getJaxbElement()
               .getBody()
               .setSectPr(sectPr);
        }

        HdrFtrRef type = switch (role) {
            case "header-even", "footer-even" -> {
                enableEvenOddHeaders(pkg, factory);
                yield HdrFtrRef.EVEN;
            }
            case "header-first", "footer-first" -> {
                sectPr.setTitlePg(new BooleanDefaultTrue());
                yield HdrFtrRef.FIRST;
            }
            default -> HdrFtrRef.DEFAULT;
        };

        if (isHeader) {
            HeaderReference ref = factory.createHeaderReference();
            ref.setId(relId);
            ref.setType(type);
            sectPr.getEGHdrFtrReferences()
                  .add(ref);
        }
        else {
            FooterReference ref = factory.createFooterReference();
            ref.setId(relId);
            ref.setType(type);
            sectPr.getEGHdrFtrReferences()
                  .add(ref);
        }
    }

    private static void enableEvenOddHeaders(WordprocessingMLPackage pkg, ObjectFactory factory) {
        try {
            DocumentSettingsPart dsp = pkg.getMainDocumentPart()
                                          .getDocumentSettingsPart();
            if (dsp == null) {
                dsp = new DocumentSettingsPart();
                pkg.getMainDocumentPart()
                   .addTargetPart(dsp);
                dsp.setJaxbElement(factory.createCTSettings());
            }
            dsp.getContents()
               .setEvenAndOddHeaders(new BooleanDefaultTrue());
        } catch (Docx4JException e) {
            throw new RuntimeException(e);
        }
    }
}
