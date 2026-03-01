package pro.verron.officestamper.asciidoc;

import jakarta.xml.bind.JAXBElement;
import org.docx4j.TextUtils;
import org.docx4j.dml.Graphic;
import org.docx4j.dml.picture.Pic;
import org.docx4j.dml.wordprocessingDrawing.Anchor;
import org.docx4j.dml.wordprocessingDrawing.Inline;
import org.docx4j.model.structure.HeaderFooterPolicy;
import org.docx4j.model.structure.SectionWrapper;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.CommentsPart;
import org.docx4j.openpackaging.parts.WordprocessingML.FooterPart;
import org.docx4j.openpackaging.parts.WordprocessingML.HeaderPart;
import org.docx4j.openpackaging.parts.WordprocessingML.StyleDefinitionsPart;
import org.docx4j.vml.CTTextbox;
import org.docx4j.wml.*;
import org.docx4j.wml.PPrBase.PStyle;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
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
    private static final Logger log = LoggerFactory.getLogger(DocxToAsciiDoc.class);
    private final StyleDefinitionsPart styleDefinitionsPart;
    private final CommentRecorder commentRecorder;
    private final BlockRecorder blocks;
    private final WordprocessingMLPackage wordprocessingMLPackage;
    private final CommentsPart commentsPart;

    public DocxToAsciiDoc(WordprocessingMLPackage pkg) {
        this.wordprocessingMLPackage = pkg;
        var mdp = wordprocessingMLPackage.getMainDocumentPart();
        this.styleDefinitionsPart = mdp.getStyleDefinitionsPart(true);
        this.commentsPart = mdp.getCommentsPart();
        this.commentRecorder = new CommentRecorder(commentsPart);
        this.blocks = new BlockRecorder();
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

    private static Optional<AsciiDocModel.InlineImage> extractGraphic(Graphic graphic) {
        if (graphic.getGraphicData() == null) return Optional.empty();
        for (Object o : graphic.getGraphicData()
                               .getAny()) {
            Object val = unwrap(o);
            // Handling WML textboxes (Wordprocessing Shape)
            if (val instanceof Pic pic) {
                var inlineImage = getInlineImage(pic);
                return Optional.of(inlineImage);
            }
        }
        return Optional.empty();
    }

    private static AsciiDocModel.@NonNull InlineImage getInlineImage(Pic pic) {
        var blipFill = pic.getBlipFill();
        var blip = blipFill.getBlip();
        var ctShapeProperties = pic.getSpPr();
        var ctShapePropertiesXfrm = ctShapeProperties.getXfrm();
        var ctShapePropertiesXfrmExt = ctShapePropertiesXfrm.getExt();
        var cx = ctShapePropertiesXfrmExt.getCx();
        var cy = ctShapePropertiesXfrmExt.getCy();
        var embed = blip.getEmbed();
        var map = Map.of("cx", String.valueOf(cx), "cy", String.valueOf(cy));
        return new AsciiDocModel.InlineImage(embed, map);
    }

    private static Object unwrap(Object o) {
        return (o instanceof JAXBElement<?> j) ? j.getValue() : o;
    }

    private static Function<List<AsciiDocModel.Inline>, List<AsciiDocModel.Inline>> getWrapper(STVerticalAlignRun valign) {
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

    private List<AsciiDocModel.Inline> toInlines(ContentAccessor accessor, BreakRecorder breakRecorder) {
        var inlines = new ArrayList<AsciiDocModel.Inline>();
        for (Object o : accessor.getContent()) {
            Object val = unwrap(o);
            switch (val) {
                case R r -> inlines.addAll(getInlines(r, breakRecorder));
                case ProofErr _ -> {/* NOOP */}
                case CommentRangeStart crs -> commentRecorder.open(crs.getId(), blocks.size(), 0);
                case CommentRangeEnd cre -> commentRecorder.close(cre.getId(), blocks.size(), 0);
                case SdtRun sdtRun -> {
                    List<String> list = new ArrayList<>();
                    var id = ofNullable(sdtRun.getSdtPr()).map(SdtPr::getId)
                                                          .map(Id::getVal)
                                                          .map(BigInteger::intValueExact)
                                                          .map(Integer::toHexString)
                                                          .orElse("");

                    ofNullable(sdtRun.getSdtPr()).map(SdtPr::getTag)
                                                 .map(Tag::getVal)
                                                 .map(s -> "tag=" + s)
                                                 .ifPresent(list::add);
                    list.add(toInlines(sdtRun.getSdtContent()).stream()
                                                              .map(AsciiDocModel.Inline::text)
                                                              .collect(Collectors.joining()));
                    inlines.add(new AsciiDocModel.InlineMacro("form", id, list));
                }
                case CTSmartTagRun tag -> {
                    var list = new ArrayList<String>();
                    list.add("start");
                    Optional.ofNullable(tag.getElement())
                            .ifPresent(e -> list.add("element=" + e));
                    Optional.ofNullable(tag.getSmartTagPr())
                            .stream()
                            .map(CTSmartTagPr::getAttr)
                            .flatMap(Collection::stream)
                            .forEach(a -> list.add("%s=%s".formatted(a.getName(), a.getVal())));
                    inlines.add(new AsciiDocModel.InlineMacro("tag", "", list));
                    inlines.addAll(toInlines(tag));
                    inlines.add(new AsciiDocModel.InlineMacro("tag", "", List.of("end")));
                }
                default -> log.debug("Unexpected inline: {}", val);
            }
        }
        return List.copyOf(inlines);
    }

    private List<AsciiDocModel.Inline> getInlines(R r, BreakRecorder breakRecorder) {
        var runInlines = extractInlines(r, breakRecorder);
        List<AsciiDocModel.Inline> styled = runInlines;
        if (!runInlines.isEmpty()) styled = significantPr(r.getRPr()).apply(runInlines);
        return styled;
    }

    private List<AsciiDocModel.Inline> toInlines(ContentAccessor accessor) {
        return toInlines(accessor, new BreakRecorder());
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
                case CommentRangeStart crs -> commentRecorder.open(crs.getId(), blocks.size(), inlines.size());
                case CommentRangeEnd cre -> commentRecorder.close(cre.getId(), blocks.size(), inlines.size());
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
                            var toAsciiDoc = new DocxToAsciiDoc(wordprocessingMLPackage);
                            var docModel = toAsciiDoc.apply(txbxContent);
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
                    for (Object dO : drawing.getAnchorOrInline()) {
                        Object dV = unwrap(dO);
                        switch (dV) {
                            case Inline inline -> extractGraphic(inline.getGraphic()).ifPresent(inlines::add);
                            case Anchor anchor -> extractGraphic(anchor.getGraphic()).ifPresent(inlines::add);
                            default -> { /* DO NOTHING */ }
                        }
                    }
                }
                default -> { /* DO NOTHING */ }
            }
        }
        if (!sb.isEmpty()) {
            inlines.add(new AsciiDocModel.Text(sb.toString()));
        }
        return inlines;
    }

    private AsciiDocModel apply(ContentAccessor accessor) {
        readBlocks(accessor);
        return of(blocks.blocks);
    }

    private Function<List<AsciiDocModel.Inline>, List<AsciiDocModel.Inline>> significantPr(@Nullable RPr rPr) {
        if (rPr == null) return Function.identity();

        List<Function<List<AsciiDocModel.Inline>, List<AsciiDocModel.Inline>>> wrappers = new ArrayList<>();
        ofNullable(rPr.getVertAlign()).map(CTVerticalAlignRun::getVal)
                                      .map(DocxToAsciiDoc::getWrapper)
                                      .ifPresent(wrappers::add);

        ofNullable(rPr.getB()).filter(BooleanDefaultTrue::isVal)
                              .map(DocxToAsciiDoc::boldwrapper)
                              .ifPresent(wrappers::add);

        ofNullable(rPr.getI()).filter(BooleanDefaultTrue::isVal)
                              .map(DocxToAsciiDoc::italicwrapper)
                              .ifPresent(wrappers::add);

        ofNullable(rPr.getU()).map(U::getVal)
                              .map(u -> DocxToAsciiDoc.styledwrapper("u_" + u.value()))
                              .ifPresent(wrappers::add);

        ofNullable(rPr.getStrike()).map(_ -> DocxToAsciiDoc.styledwrapper("strike"))
                                   .ifPresent(wrappers::add);

        ofNullable(rPr.getHighlight()).map(h -> DocxToAsciiDoc.styledwrapper("highlight_" + h.getVal()))
                                      .ifPresent(wrappers::add);

        ofNullable(rPr.getColor()).map(c -> DocxToAsciiDoc.styledwrapper("color_" + c.getVal()))
                                  .ifPresent(wrappers::add);

        ofNullable(rPr.getRStyle()).map(s -> DocxToAsciiDoc.styledwrapper("rStyle_" + s.getVal()))
                                   .ifPresent(wrappers::add);

        return wrappers.stream()
                       .reduce(Function::andThen)
                       .orElse(Function.identity());
    }

    private void readBlocks(ContentAccessor accessor) {
        for (Object o : accessor.getContent()) {
            Object val = unwrap(o);
            switch (val) {
                case P p -> {
                    var headerLevel = getHeaderLevel(p);
                    var breakRecorder = new BreakRecorder();

                    ofNullable(p.getPPr()).map(PPr::getRPr)
                                          .flatMap(this::stringified)
                                          .ifPresent(pr -> blocks.add(new AsciiDocModel.CommentLine(pr)));

                    var style = style(p).stream()
                                        .toList();

                    if (headerLevel.isPresent())
                        blocks.add(new AsciiDocModel.Heading(style, headerLevel.get(), toInlines(p, breakRecorder)));
                    else blocks.add(new AsciiDocModel.Paragraph(style, toInlines(p, breakRecorder)));

                    ofNullable(p.getPPr()).map(PPr::getSectPr)
                                          .flatMap(this::stringified)
                                          .ifPresent(s -> blocks.add(new AsciiDocModel.CommentLine(s)));

                    if (breakRecorder.isSet()) blocks.add(new AsciiDocModel.Break());
                }
                case Tbl tbl -> blocks.add(toTableBlock(tbl));
                case SdtBlock sdtBlock -> blocks.add(toSdtBlock(sdtBlock));
                default -> log.debug("Unexpected block: {}", val);
            }
        }
    }

    private AsciiDocModel.Block toSdtBlock(SdtBlock sdtBlock) {
        var form = "form";
        var id = ofNullable(sdtBlock.getSdtPr()).map(SdtPr::getId)
                                                .map(Id::getVal)
                                                .map(BigInteger::intValueExact)
                                                .map(Integer::toHexString);
        var tag = ofNullable(sdtBlock.getSdtPr()).map(SdtPr::getTag)
                                                 .map(Tag::getVal);
        var header = new ArrayList<String>();
        header.add(form);
        id.ifPresent(e -> header.add("id=" + e));
        tag.ifPresent(e -> header.add("tag=" + e));
        var toAsciiDoc = new DocxToAsciiDoc(wordprocessingMLPackage);
        var docModel = toAsciiDoc.apply(() -> sdtBlock.getSdtContent()
                                                      .getContent());
        return new AsciiDocModel.OpenBlock(header, docModel.getBlocks());
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

    private Optional<String> style(P p) {
        return ofNullable(p.getPPr()).map(PPr::getPStyle)
                                     .map(PStyle::getVal)
                                     .map(styleDefinitionsPart::getNameForStyleID);
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
                var toAsciiDoc = new DocxToAsciiDoc(wordprocessingMLPackage);
                List<AsciiDocModel.Block> cellBlocks = toAsciiDoc.apply(tc)
                                                                 .getBlocks();
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
        getHeaderParts(pkg).map(this::toHeaderBlock)
                           .flatMap(Optional::stream)
                           .forEach(blocks::add);
        var mdp = pkg.getMainDocumentPart();

        {
            Document contents;
            try {
                contents = mdp.getContents();
            } catch (Docx4JException e) {
                throw new RuntimeException(e);
            }
            var body = contents.getBody();
            readBlocks(body);
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
        getFooterParts(pkg).map(this::toFooterBlock)
                           .flatMap(Optional::stream)
                           .forEach(blocks::add);
        var list = new ArrayList<AsciiDocModel.Block>();
        list.addAll(commentRecorder.all());
        list.addAll(blocks.all());
        return of(list);
    }

    private Optional<AsciiDocModel.Block> toFooterBlock(FooterPart footerPart) {
        var toAsciiDoc = new DocxToAsciiDoc(wordprocessingMLPackage);
        var extractedBlocks = toAsciiDoc.apply(footerPart)
                                        .getBlocks();
        return extractedBlocks.isEmpty()
                ? Optional.empty()
                : Optional.of(new AsciiDocModel.OpenBlock(List.of("footer"), extractedBlocks));

    }

    private Optional<AsciiDocModel.Block> toHeaderBlock(HeaderPart headerPart) {
        var toAsciiDoc = new DocxToAsciiDoc(wordprocessingMLPackage);
        var extractedBlocks = toAsciiDoc.apply(headerPart)
                                        .getBlocks();
        return extractedBlocks.isEmpty()
                ? Optional.empty()
                : Optional.of(new AsciiDocModel.OpenBlock(List.of("header"), extractedBlocks));
    }

    private Optional<AsciiDocModel.Block> toNoteBlock(String role, List<CTFtnEdn> notes) {
        var content = new ArrayList<AsciiDocModel.Block>();
        for (CTFtnEdn note : notes) {
            var noteType = note.getType();
            if (noteType != null && List.of(STFtnEdn.SEPARATOR, STFtnEdn.CONTINUATION_SEPARATOR)
                                        .contains(noteType)) continue;
            var toAsciiDoc = new DocxToAsciiDoc(wordprocessingMLPackage);
            var extractedBlocks = toAsciiDoc.apply(note::getContent)
                                            .getBlocks();
            content.add(new AsciiDocModel.Paragraph(List.of(new AsciiDocModel.Text("%s::".formatted(note.getId())))));
            content.addAll(extractedBlocks);
        }
        return content.isEmpty() ? Optional.empty() : Optional.of(new AsciiDocModel.OpenBlock(List.of(role), content));
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

    public static class CommentRecorder {
        private final Deque<CommentBuilder> comments;
        private final List<BigInteger> ids;
        private final Map<BigInteger, Comment> map;
        private final CommentsPart commentsPart;

        CommentRecorder(CommentsPart commentsPart) {
            this.commentsPart = commentsPart;
            comments = new ArrayDeque<>();
            ids = new ArrayList<>();
            map = new HashMap<>();
        }

        public void open(BigInteger id, int blockStart, int lineStart) {
            var commentBuilder = new CommentBuilder().setId(id)
                                                     .setBlockStart(blockStart)
                                                     .setLineStart(lineStart);
            comments.addLast(commentBuilder);
            ids.add(id);
        }

        public void close(BigInteger id, int blockEnd, int lineEnd) {
            var lastComment = comments.removeLast();
            var lastCommentId = lastComment.getId();
            assertThat(lastCommentId.equals(id),
                    "Closing comment %s but last comment open is %s".formatted(id, lastCommentId));
            lastComment.setBlockEnd(blockEnd);
            lastComment.setLineEnd(lineEnd);
            map.put(id, lastComment.createComment());
        }

        private void assertThat(boolean bool, String msg) {
            if (!bool) throw new IllegalStateException(msg);
        }

        public Collection<AsciiDocModel.MacroBlock> all() {
            return ids.stream()
                      .map(map::get)
                      .map(comment -> new AsciiDocModel.MacroBlock("comment",
                              comment.id()
                                     .toString(),
                              List.of("start=\"%d,%d\"".formatted(comment.blockStart(), comment.lineStart()),
                                      "end=\"%d,%d\"".formatted(comment.blockEnd(), comment.lineEnd()),
                                      "value=\"%s\"".formatted(getCom(comment.id())))))
                      .toList();
        }

        private String getCom(BigInteger id) {
            try {
                return this.commentsPart.getContents()
                                        .getComment()
                                        .stream()
                                        .filter(c -> Objects.equals(c.getId(), id))
                                        .findFirst()
                                        .map((Comments.Comment comment) -> str(comment))
                                        .orElseThrow();
            } catch (Docx4JException e) {
                throw new RuntimeException(e);
            }
        }

        private String str(Comments.Comment comment) {

            var first = (P) comment.getContent()
                                   .getFirst();
            return TextUtils.getText(first);
        }

        public record Comment(BigInteger id, int blockStart, int lineStart, int blockEnd, int lineEnd) {}
    }

    private static class BlockRecorder {
        private final List<AsciiDocModel.Block> blocks = new ArrayList<>();
        private int size = 0;

        public int size() {
            return size;
        }

        public void add(AsciiDocModel.Block block) {
            blocks.add(block);
            size += block.size();
        }

        public List<AsciiDocModel.Block> all() {
            return blocks;
        }
    }

    private static class InlineRecorder {
        private final List<AsciiDocModel.Inline> inlines = new ArrayList<>();
        private int size = 0;

        public void add(AsciiDocModel.Inline inline) {
            inlines.add(inline);
            size += inline.text()
                          .length();
        }

        public int size() {
            return size;
        }
    }
}
