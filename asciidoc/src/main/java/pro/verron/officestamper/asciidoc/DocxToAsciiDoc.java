package pro.verron.officestamper.asciidoc;

import jakarta.xml.bind.JAXBElement;
import org.docx4j.dml.Graphic;
import org.docx4j.dml.wordprocessingDrawing.Anchor;
import org.docx4j.dml.wordprocessingDrawing.Inline;
import org.docx4j.model.structure.HeaderFooterPolicy;
import org.docx4j.model.structure.SectionWrapper;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.FooterPart;
import org.docx4j.openpackaging.parts.WordprocessingML.HeaderPart;
import org.docx4j.openpackaging.parts.WordprocessingML.StyleDefinitionsPart;
import org.docx4j.vml.CTTextbox;
import org.docx4j.wml.*;
import org.jspecify.annotations.Nullable;

import java.math.BigInteger;
import java.util.*;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import static java.util.Optional.ofNullable;
import static pro.verron.officestamper.asciidoc.AsciiDocModel.of;

/// Minimal DOCX → AsciiDoc text extractor used by tests. This intentionally mirrors a subset of the legacy Stringifier
/// formatting for:
///  - Paragraphs
///  - Tables (|=== fences, each cell prefixed with '|')
///  - Basic inline text extraction More advanced features (headers/footers, breaks, styles) can be added incrementally
/// as needed by tests.
public final class DocxToAsciiDoc
        implements Function<WordprocessingMLPackage, AsciiDocModel> {
    private final StyleDefinitionsPart styleDefinitionsPart;

    public DocxToAsciiDoc(WordprocessingMLPackage pkg) {
        var mdp = pkg.getMainDocumentPart();
        this.styleDefinitionsPart = mdp.getStyleDefinitionsPart(true);
    }

    private static Stream<HeaderPart> getHeaderParts(WordprocessingMLPackage document) {
        var sections = document.getDocumentModel()
                               .getSections();

        var set = new LinkedHashSet<HeaderPart>();
        for (SectionWrapper section : sections) {
            HeaderFooterPolicy hfp = section.getHeaderFooterPolicy();
            if (hfp != null) {
                if (hfp.getFirstHeader() != null) set.add(hfp.getFirstHeader());
                if (hfp.getDefaultHeader() != null) set.add(hfp.getDefaultHeader());
                if (hfp.getEvenHeader() != null) set.add(hfp.getEvenHeader());
            }
        }
        return set.stream();
    }

    private static Stream<FooterPart> getFooterParts(WordprocessingMLPackage document) {
        var sections = document.getDocumentModel()
                               .getSections();

        var set = new LinkedHashSet<FooterPart>();
        for (SectionWrapper section : sections) {
            HeaderFooterPolicy hfp = section.getHeaderFooterPolicy();
            if (hfp != null) {
                if (hfp.getFirstFooter() != null) set.add(hfp.getFirstFooter());
                if (hfp.getDefaultFooter() != null) set.add(hfp.getDefaultFooter());
                if (hfp.getEvenFooter() != null) set.add(hfp.getEvenFooter());
            }
        }
        return set.stream();
    }

    private static Object unwrap(Object o) {
        return (o instanceof JAXBElement<?> j) ? j.getValue() : o;
    }

    private static String extractDmlText(Graphic graphic) {
        if (graphic.getGraphicData() == null) return "";
        String sb = "";
        for (Object o : graphic.getGraphicData()
                               .getAny()) {
            Object val = unwrap(o);
            // Handling WML textboxes (Wordprocessing Shape)
            if (val instanceof CTObject _) {
                // Textboxes in DML are often buried. For now, let's keep it simple.
            }
        }
        return sb;
    }

    private static UnaryOperator<List<AsciiDocModel.Inline>> getWrapper(STVerticalAlignRun valign) {
        return switch (valign) {
            case BASELINE -> i -> i;
            case SUPERSCRIPT -> i -> List.of(new AsciiDocModel.Sup(i));
            case SUBSCRIPT -> i -> List.of(new AsciiDocModel.Sub(i));
        };
    }

    private static Function<List<AsciiDocModel.Inline>, List<AsciiDocModel.Inline>> boldwrapper(BooleanDefaultTrue w) {
        return is -> List.of(new AsciiDocModel.Bold(is));
    }

    private static Function<List<AsciiDocModel.Inline>, List<AsciiDocModel.Inline>> italicwrapper(BooleanDefaultTrue booleanDefaultTrue) {
        return is -> List.of(new AsciiDocModel.Italic(is));
    }

    private static Function<List<AsciiDocModel.Inline>, List<AsciiDocModel.Inline>> styledwrapper(String s) {
        return is -> List.of(new AsciiDocModel.Styled(s, is));
    }

    private List<AsciiDocModel.Inline> toInlines(P p, BreakRecorder breakRecorder) {
        var inlines = new ArrayList<AsciiDocModel.Inline>();
        for (Object o : p.getContent()) {
            Object val = unwrap(o);
            if (val instanceof R r) {
                var runInlines = extractInlines(r, breakRecorder);
                if (runInlines.isEmpty()) continue;
                var styled = significantPr(r.getRPr()).apply(runInlines);
                inlines.addAll(styled);
            }
            else if (val instanceof Br br) {
                System.err.println(br);
            }
        }
        return List.copyOf(inlines);
    }

    private List<AsciiDocModel.Inline> extractInlines(ContentAccessor r, BreakRecorder brecorder) {
        var inlines = new ArrayList<AsciiDocModel.Inline>();
        var content = r.getContent();
        var iterator = content.iterator();
        var sb = new StringBuilder();
        while (iterator.hasNext()) {
            var rc = unwrap(iterator.next());
            switch (rc) {
                case Text t -> sb.append(t.getValue());
                case Br br when br.getType() == null -> sb.append(" +\n");
                case Br br when br.getType() == STBrType.TEXT_WRAPPING -> sb.append(" +\n");
                case Br br when br.getType() == STBrType.COLUMN -> brecorder.set();
                case Br br when br.getType() == STBrType.PAGE -> brecorder.set();
                case R.Tab _ -> sb.append("\t");
                case CTFtnEdnRef n -> sb.append("footnote:%s[]".formatted(n.getId()));
                case Pict pict -> {
                    if (!sb.isEmpty()) {
                        inlines.add(new AsciiDocModel.Text(sb.toString()));
                        sb = new StringBuilder();
                    }
                    StringBuilder sb1 = new StringBuilder();
                    for (Object pO : pict.getAnyAndAny()) {
                        Object pV = unwrap(pO);
                        if (pV instanceof CTTextbox t) {
                            var txbxContent = t.getTxbxContent();
                            var textContent = txbxContent.getContent();
                            var blocks = toBlocks(textContent);
                            var docModel = of(blocks);
                            var compileToText = AsciiDocCompiler.toAsciidoc(docModel);
                            var trimmed = compileToText.trim();
                            sb1.append(trimmed);
                        }
                    }
                    sb.append(sb1);
                }
                case Drawing drawing -> {
                    if (!sb.isEmpty()) {
                        inlines.add(new AsciiDocModel.Text(sb.toString()));
                        sb = new StringBuilder();
                    }
                    StringBuilder sb1 = new StringBuilder();
                    for (Object dO : drawing.getAnchorOrInline()) {
                        Object dV = unwrap(dO);
                        switch (dV) {
                            case Inline inline -> sb1.append(extractDmlText(inline.getGraphic()));
                            case Anchor anchor -> sb1.append(extractDmlText(anchor.getGraphic()));
                            default -> { /* DO NOTHING */ }
                        }
                    }
                    sb.append(sb1);
                }
                default -> { /* DO NOTHING */ }
            }
        }
        if (!sb.isEmpty()) {
            inlines.add(new AsciiDocModel.Text(sb.toString()));
        }
        return inlines;
    }

    private Function<List<AsciiDocModel.Inline>, List<AsciiDocModel.Inline>> significantPr(@Nullable RPr rPr) {
        if (rPr == null) return UnaryOperator.identity();

        Function<List<AsciiDocModel.Inline>, List<AsciiDocModel.Inline>> unaryOperator =
                ofNullable(rPr.getVertAlign()).map(
                                                      CTVerticalAlignRun::getVal)
                                              .map(DocxToAsciiDoc::getWrapper)
                                              .orElse(UnaryOperator.identity());
        unaryOperator = unaryOperator.andThen(ofNullable(rPr.getB()).filter(BooleanDefaultTrue::isVal)
                                                                    .map(DocxToAsciiDoc::boldwrapper)
                                                                    .orElse(Function.identity()));


        unaryOperator = unaryOperator.andThen(ofNullable(rPr.getI()).filter(BooleanDefaultTrue::isVal)
                                                                    .map(DocxToAsciiDoc::italicwrapper)
                                                                    .orElse(Function.identity()));


        unaryOperator = unaryOperator.andThen(ofNullable(rPr.getU())
                .map(u -> DocxToAsciiDoc.styledwrapper("u_" + u.getVal()
                                                               .value()))
                .orElse(Function.identity()));

        unaryOperator = unaryOperator.andThen(ofNullable(rPr.getStrike())
                .map(u -> DocxToAsciiDoc.styledwrapper("strike"))
                .orElse(Function.identity()));


        unaryOperator = unaryOperator.andThen(ofNullable(rPr.getHighlight())
                .map(u -> DocxToAsciiDoc.styledwrapper("highlight_" + u.getVal()))
                .orElse(Function.identity()));


        unaryOperator = unaryOperator.andThen(ofNullable(rPr.getColor())
                .map(u -> DocxToAsciiDoc.styledwrapper("color_" + u.getVal()))
                .orElse(Function.identity()));


        unaryOperator = unaryOperator.andThen(ofNullable(rPr.getRStyle())
                .map(u -> DocxToAsciiDoc.styledwrapper("rStyle_" + u.getVal()))
                .orElse(Function.identity()));

        return unaryOperator;
    }

    private List<AsciiDocModel.Block> toBlocks(Collection<?> content) {
        var blocks = new ArrayList<AsciiDocModel.Block>();
        for (Object o : content) {
            Object val = unwrap(o);
            switch (val) {
                case P p -> {
                    var headerLevel = getHeaderLevel(p);
                    var breakRecorder = new BreakRecorder();
                    if (p.getPPr() instanceof PPr ppr && ppr.getRPr() instanceof ParaRPr paraRPr)
                        stringified(paraRPr).ifPresent(pr -> blocks.add(new AsciiDocModel.CommentLine(pr)));
                    if (headerLevel.isPresent()) {
                        blocks.add(new AsciiDocModel.Heading(headerLevel.get(), toInlines(p, breakRecorder)));
                    }
                    else if (isQuote(p)) {
                        blocks.add(new AsciiDocModel.Blockquote(toInlines(p, breakRecorder)));
                    }
                    else {
                        blocks.add(new AsciiDocModel.Paragraph(toInlines(p, breakRecorder)));
                    }
                    if (p.getPPr() instanceof PPr ppr && ppr.getSectPr() instanceof SectPr sectPr)
                        stringified(sectPr).ifPresent(s -> blocks.add(new AsciiDocModel.CommentLine(s)));
                    if (breakRecorder.isSet()) blocks.add(new AsciiDocModel.Break());

                }
                case Tbl tbl -> blocks.add(toTableBlock(tbl));
                default -> {System.out.println(val);}
            }
        }
        return blocks;
    }

    private Optional<String> stringified(ParaRPr paraRPr) {
        var map = new TreeMap<String, Object>();
        ofNullable(paraRPr.getHighlight()).ifPresent(h -> map.put("highlight", h.getVal()));
        ofNullable(paraRPr.getColor()).ifPresent(c -> map.put("color", c.getVal()));
        ofNullable(paraRPr.getRFonts()).ifPresent(r -> {
            var rFontMap = new TreeMap<String, Object>();
            ofNullable(r.getAscii()).ifPresent(a -> rFontMap.put("ascii", a));
            ofNullable(r.getHAnsi()).ifPresent(h -> rFontMap.put("hAnsi", h));
            ofNullable(r.getEastAsia()).ifPresent(e -> rFontMap.put("eastAsia", e));
            ofNullable(r.getCs()).ifPresent(c -> rFontMap.put("cs", c));
            ofNullable(r.getAsciiTheme()).ifPresent(a -> rFontMap.put("asciiTheme", a.value()));
            ofNullable(r.getHAnsiTheme()).ifPresent(h -> rFontMap.put("hAnsi", h.value()));
            ofNullable(r.getEastAsiaTheme()).ifPresent(e -> rFontMap.put("eastAsia", e.value()));
            ofNullable(r.getCstheme()).ifPresent(c -> rFontMap.put("cs", c.value()));
            if (!rFontMap.isEmpty()) map.put("rFonts", rFontMap);
        });
        ofNullable(paraRPr.getSz()).ifPresent(s -> map.put("sz", s.getVal()));
        ofNullable(paraRPr.getSzCs()).ifPresent(s -> map.put("szCs", s.getVal()));
        ofNullable(paraRPr.getU()).ifPresent(u -> map.put("u", u.getVal()));
        ofNullable(paraRPr.getHighlight()).ifPresent(h -> map.put("highlight", h.getVal()));
        ofNullable(paraRPr.getI()).ifPresent(i -> map.put("i", i.isVal()));
        return map.isEmpty() ? Optional.empty() : Optional.of("runPr %s".formatted(map));
    }

    private Optional<String> stringified(SectPr sectPr) {
        var map = new TreeMap<String, Object>();
        ofNullable(sectPr.getDocGrid()).ifPresent(d -> {
            var dgmap = new TreeMap<String, Object>();
            ofNullable(d.getLinePitch()).ifPresent(l -> dgmap.put("linePitch", l));
            ofNullable(d.getCharSpace()).ifPresent(c -> dgmap.put("charSpace", c));
            ofNullable(d.getType()).ifPresent(t -> dgmap.put("type", t.value()));
            map.put("docGrid", dgmap);
        });
        ofNullable(sectPr.getPgMar()).ifPresent(p -> {
            var pmmap = new TreeMap<String, Object>();
            ofNullable(p.getTop()).filter(t -> !BigInteger.ZERO.equals(t))
                                  .ifPresent(t -> pmmap.put("top", t));
            ofNullable(p.getBottom()).filter(t -> !BigInteger.ZERO.equals(t))
                                     .ifPresent(b -> pmmap.put("bottom", b));
            ofNullable(p.getLeft()).filter(t -> !BigInteger.ZERO.equals(t))
                                   .ifPresent(l -> pmmap.put("left", l));
            ofNullable(p.getRight()).filter(t -> !BigInteger.ZERO.equals(t))
                                    .ifPresent(r -> pmmap.put("right", r));
            ofNullable(p.getHeader()).filter(t -> !BigInteger.ZERO.equals(t))
                                     .ifPresent(h -> pmmap.put("header", h));
            ofNullable(p.getFooter()).filter(t -> !BigInteger.ZERO.equals(t))
                                     .ifPresent(f -> pmmap.put("footer", f));
            ofNullable(p.getGutter()).filter(t -> !BigInteger.ZERO.equals(t))
                                     .ifPresent(g -> pmmap.put("gutter", g));
            map.put("pgMar", pmmap);
        });
        ofNullable(sectPr.getPgSz()).ifPresent(p -> {
            var psmap = new TreeMap<String, Object>();
            ofNullable(p.getW()).filter(t -> !BigInteger.ZERO.equals(t))
                                .ifPresent(w -> psmap.put("w", w));
            ofNullable(p.getH()).filter(t -> !BigInteger.ZERO.equals(t))
                                .ifPresent(h -> psmap.put("h", h));
            ofNullable(p.getOrient()).ifPresent(o -> psmap.put("orient", o.value()));
            ofNullable(p.getCode()).filter(t -> !BigInteger.ZERO.equals(t))
                                   .ifPresent(c -> psmap.put("code", c));
            map.put("pgSz", psmap);
        });
        ofNullable(sectPr.getPgBorders()).ifPresent(p -> map.put("pgBorders", p));
        ofNullable(sectPr.getBidi()).ifPresent(b -> map.put("bidi", b.isVal()));
        ofNullable(sectPr.getCols()).ifPresent(c -> {
            var colMap = new TreeMap<String, Object>();
            ofNullable(c.getNum()).filter(t -> !BigInteger.ZERO.equals(t))
                                  .ifPresent(n -> map.put("num", n));
            ofNullable(c.getSpace()).filter(t -> !BigInteger.ZERO.equals(t))
                                    .ifPresent(s -> map.put("space", s));
            ofNullable(c.getCol()).ifPresent(c1 -> {
                var list = c1.stream()
                             .map(coli -> {
                                 var colim = new TreeMap<String, Object>();
                                 ofNullable(coli.getSpace()).ifPresent(s -> colim.put("space", s));
                                 ofNullable(coli.getW()).ifPresent(w -> colim.put("w", w));
                                 return colim;
                             })
                             .toList();
                if (!list.isEmpty()) colMap.put("col", list);
            });
            if (!colMap.isEmpty()) map.put("cols", colMap);
        });
        ofNullable(sectPr.getType()).ifPresent(t -> map.put("type", t));
        return map.isEmpty() ? Optional.empty() : Optional.of("section %s".formatted(map));
    }

    private boolean isQuote(P p) {
        var pPr = p.getPPr();
        if (pPr == null) return false;
        var pStyle = pPr.getPStyle();
        if (pStyle == null) return false;
        var styleId = pStyle.getVal();
        var styleName = styleDefinitionsPart.getNameForStyleID(styleId);
        if (styleName == null) styleName = styleId;
        var quote = List.of("quote", "citation");
        return quote.contains(styleName.toLowerCase());
    }

    private Optional<Integer> getHeaderLevel(P p) {
        if (p.getPPr() == null || p.getPPr()
                                   .getPStyle() == null) return Optional.empty();
        var styleId = p.getPPr()
                       .getPStyle()
                       .getVal();
        var styleName = styleDefinitionsPart.getNameForStyleID(styleId);
        if (styleName == null) styleName = styleId;

        if (styleName.equalsIgnoreCase("Title") || styleName.equalsIgnoreCase("Titre")) {
            return Optional.of(1);
        }
        if (styleName.toLowerCase()
                     .startsWith("heading") || styleName.toLowerCase()
                                                        .startsWith("titre")) {
            String levelStr = styleName.replaceAll("\\D", "");
            if (!levelStr.isEmpty()) {
                return Optional.of(Integer.parseInt(levelStr) + 1);
            }
        }
        return Optional.empty();
    }

    private AsciiDocModel.Table toTableBlock(Tbl tbl) {
        List<AsciiDocModel.Row> rows = new ArrayList<>();
        for (Object trO : tbl.getContent()) {
            Object trV = unwrap(trO);
            if (!(trV instanceof Tr tr)) continue;
            List<AsciiDocModel.Cell> cells = new ArrayList<>();
            for (Object tcO : tr.getContent()) {
                Object tcV = unwrap(tcO);
                if (!(tcV instanceof Tc tc)) continue;
                List<AsciiDocModel.Block> cellBlocks = toBlocks(tc.getContent());
                Optional<String> ccnfStyle = Optional.empty();
                if (tc.getTcPr() != null && tc.getTcPr()
                                              .getCnfStyle() != null) {
                    ccnfStyle = ofNullable(tc.getTcPr()).map(TcPrInner::getCnfStyle)
                                                        .map(s -> "style=" + Long.parseLong(s.getVal(), 2));

                }
                cells.add(new AsciiDocModel.Cell(cellBlocks, ccnfStyle));
            }
            Optional<String> cnfStyle = Optional.empty();
            if (tr.getTrPr() != null && tr.getTrPr()
                                          .getCnfStyleOrDivIdOrGridBefore() != null) {
                cnfStyle = tr.getTrPr()
                             .getCnfStyleOrDivIdOrGridBefore()
                             .stream()
                             .map(DocxToAsciiDoc::unwrap)
                             .filter(CTCnf.class::isInstance)
                             .map(CTCnf.class::cast)
                             .findFirst()
                             .map(s -> "rowStyle=" + Long.parseLong(s.getVal(), 2));

            }
            rows.add(new AsciiDocModel.Row(cells, cnfStyle));
        }
        return new AsciiDocModel.Table(rows);
    }

    public AsciiDocModel apply(WordprocessingMLPackage pkg) {
        List<AsciiDocModel.Block> blocks = new ArrayList<>();

        getHeaderParts(pkg).forEach(part -> blocks.addAll(toBlocks(part.getContent())));
        var mdp = pkg.getMainDocumentPart();

        {
            Document contents;
            try {
                contents = mdp.getContents();
            } catch (Docx4JException e) {
                throw new RuntimeException(e);
            }
            var body = contents.getBody();
            blocks.addAll(toBlocks(body.getContent()));
            if (body.getSectPr() instanceof SectPr sectPr)
                stringified(sectPr).ifPresent(s -> blocks.add(new AsciiDocModel.CommentLine(s)));
        }

        try {
            var footnotesPart = mdp.getFootnotesPart();
            if (footnotesPart != null) {
                var contents = footnotesPart.getContents();
                var footnote = contents.getFootnote();
                toNoteBlock("footnotes", footnote).ifPresent(blocks::add);
            }
            var endNotesPart = mdp.getEndNotesPart();
            if (endNotesPart != null) {
                var contents = endNotesPart.getContents();
                var endnote = contents.getEndnote();
                toNoteBlock("endnotes", endnote).ifPresent(blocks::add);
            }
        } catch (Docx4JException e) {
            throw new RuntimeException(e);
        }
        getFooterParts(pkg).forEach(part -> blocks.addAll(toBlocks(part.getContent())));
        return of(blocks);
    }

    private Optional<AsciiDocModel.Block> toNoteBlock(String role, List<CTFtnEdn> notes) {
        var content = new ArrayList<AsciiDocModel.Block>();
        for (CTFtnEdn note : notes) {
            var noteType = note.getType();
            if (noteType != null && List.of(STFtnEdn.SEPARATOR, STFtnEdn.CONTINUATION_SEPARATOR)
                                        .contains(noteType)) continue;
            var extractedBlocks = toBlocks(note.getContent());
            content.add(new AsciiDocModel.Paragraph(List.of(new AsciiDocModel.Text("%s::".formatted(note.getId())))));
            content.addAll(extractedBlocks);
        }
        return content.isEmpty() ? Optional.empty() : Optional.of(new AsciiDocModel.OpenBlock(role, content));
    }

    private static class BreakRecorder {
        private boolean set;

        public void set() {
            set = true;
        }

        public boolean isSet() {
            return set;
        }
    }
}
