package pro.verron.officestamper.test;

import jakarta.xml.bind.JAXBElement;
import org.docx4j.TextUtils;
import org.docx4j.dml.*;
import org.docx4j.dml.picture.Pic;
import org.docx4j.dml.wordprocessingDrawing.Inline;
import org.docx4j.mce.AlternateContent;
import org.docx4j.model.structure.HeaderFooterPolicy;
import org.docx4j.model.structure.SectionWrapper;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.PresentationMLPackage;
import org.docx4j.openpackaging.packages.SpreadsheetMLPackage;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.WordprocessingML.*;
import org.docx4j.vml.CTShape;
import org.docx4j.vml.CTShapetype;
import org.docx4j.vml.CTTextbox;
import org.docx4j.wml.*;
import org.docx4j.wml.Comments.Comment;
import org.xlsx4j.org.apache.poi.ss.usermodel.DataFormatter;
import org.xlsx4j.sml.Cell;
import pro.verron.officestamper.api.OfficeStamperException;
import pro.verron.officestamper.experimental.ExcelCollector;
import pro.verron.officestamper.experimental.PowerpointCollector;
import pro.verron.officestamper.experimental.PowerpointParagraph;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.joining;

/**
 * <p>Stringifier class.</p>
 *
 * @author Joseph Verron
 * @version ${version}
 * @since 1.6.5
 */
public class Stringifier {

    private final Supplier<WordprocessingMLPackage> documentSupplier;

    /**
     * <p>Constructor for Stringifier.</p>
     *
     * @since 1.6.6
     */
    public Stringifier(Supplier<WordprocessingMLPackage> documentSupplier) {
        this.documentSupplier = documentSupplier;
    }

    public static String stringifyPowerpoint(PresentationMLPackage presentation) {
        var collector = new PowerpointCollector<>(CTTextParagraph.class);
        collector.visit(presentation);
        var collected = collector.collect();

        var powerpoint = new StringBuilder();
        for (CTTextParagraph paragraph : collected) {
            powerpoint.append(new PowerpointParagraph(paragraph).asString());
        }
        return powerpoint.toString();
    }

    public static String stringifyExcel(SpreadsheetMLPackage presentation) {
        var collector = new ExcelCollector<>(Cell.class);
        collector.visit(presentation);
        var formatter = new DataFormatter();
        return collector.collect()
                        .stream()
                        .map(cell -> cell.getR() + ": " + formatter.formatCellValue(cell))
                        .collect(joining("\n"));
    }

    private static MessageDigest findDigest() {
        try {
            return MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            throw new OfficeStamperException(e);
        }
    }

    /**
     * Finds a comment with the given ID in the specified WordprocessingMLPackage document.
     *
     * @param document the WordprocessingMLPackage document to search for the comment
     * @param id       the ID of the comment to find
     *
     * @return an Optional containing the Comment if found, or an empty Optional if not found
     *
     * @throws Docx4JException if an error occurs while searching for the comment
     */
    public static Optional<Comment> findComment(
            WordprocessingMLPackage document, BigInteger id
    )
            throws Docx4JException {
        var name = new PartName("/word/comments.xml");
        var parts = document.getParts();
        var wordComments = (CommentsPart) parts.get(name);
        var comments = wordComments.getContents();
        return comments.getComment()
                       .stream()
                       .filter(idEqual(id))
                       .findFirst();
    }

    private static Predicate<Comment> idEqual(BigInteger id) {
        return comment -> {
            var commentId = comment.getId();
            return commentId.equals(id);
        };
    }

    private static void extract(
            Map<String, Object> map, String key, Object value
    ) {
        if (value != null) map.put(key, value);
    }

    private static Function<Entry<?, ?>, String> format(String format) {
        return entry -> format.formatted(entry.getKey(), entry.getValue());
    }

    private String stringify(Text text) {
        return TextUtils.getText(text);
    }

    private WordprocessingMLPackage document() {
        return documentSupplier.get();
    }

    private String stringify(Br br) {
        var type = br.getType();
        if (type == STBrType.PAGE) return "<break page>\n";
        else if (type == STBrType.COLUMN) return "<break column>\n";
        else if (type == STBrType.TEXT_WRAPPING) return "<break line>\n";
        else if (type == null) return "<break line>\n";
        else throw new OfficeStamperException("Unexpected type: " + type);
    }

    /**
     * <p>stringify.</p>
     *
     * @param blip a {@link CTBlip} object
     *
     * @return a {@link String} object
     *
     * @since 1.6.6
     */
    private String stringify(CTBlip blip) {
        var image = document().getParts()
                              .getParts()
                              .entrySet()
                              .stream()
                              .filter(e -> e.getKey()
                                            .getName()
                                            .contains(blip.getEmbed()))
                              .map(Entry::getValue)
                              .findFirst()
                              .map(BinaryPartAbstractImage.class::cast)
                              .orElseThrow();
        byte[] imageBytes = image.getBytes();
        return "%s:%s:%s:%s:sha1=%s:cy=$d".formatted(image.getPartName(),
                blip.getEmbed(),
                image.getContentType(),
                humanReadableByteCountSI(imageBytes.length),
                sha1b64(imageBytes));
    }

    /**
     * <p>humanReadableByteCountSI.</p>
     *
     * @param bytes a long
     *
     * @return a {@link String} object
     *
     * @since 1.6.6
     */
    private String humanReadableByteCountSI(long bytes) {
        if (-1000 < bytes && bytes < 1000) return bytes + "B";

        CharacterIterator ci = new StringCharacterIterator("kMGTPE");
        while (bytes <= -999_950 || bytes >= 999_950) {
            bytes /= 1000;
            ci.next();
        }
        return String.format(Locale.US, "%.1f%cB", bytes / 1000.0, ci.current());
    }

    private String sha1b64(byte[] imageBytes) {
        MessageDigest messageDigest = findDigest();
        Base64.Encoder encoder = Base64.getEncoder();
        byte[] digest = messageDigest.digest(imageBytes);
        return encoder.encodeToString(digest);
    }

    /**
     * <p>stringify.</p>
     *
     * @param o a {@link Object} object
     *
     * @return a {@link String} object
     *
     * @since 1.6.6
     */
    public String stringify(Object o) {
        if (o instanceof JAXBElement<?> jaxb) return stringify(jaxb.getValue());
        if (o instanceof WordprocessingMLPackage mlPackage) {
            var header = stringifyHeaders(getHeaderPart(mlPackage));
            var body = stringify(mlPackage.getMainDocumentPart());
            var footer = stringifyFooters(getFooterPart(mlPackage));
            var hStr = header.map(h -> h + "\n\n")
                             .orElse("");
            var fStr = footer.map(f -> "\n" + f + "\n")
                             .orElse("");
            return hStr + body + fStr;
        }
        if (o instanceof Tbl tbl) return stringify(tbl);
        if (o instanceof Tr tr) return stringify(tr);
        if (o instanceof Tc tc) return stringify(tc);
        if (o instanceof MainDocumentPart mainDocumentPart) return stringify(mainDocumentPart.getContent());
        if (o instanceof Body body) return stringify(body.getContent());
        if (o instanceof List<?> list) return stringify(list);
        if (o instanceof Text text) return stringify(text);
        if (o instanceof P p) return stringify(p) + "\n";
        if (o instanceof R r) return stringify(r);
        if (o instanceof Drawing drawing) return stringify(drawing);
        if (o instanceof Inline inline) return stringify(inline);
        if (o instanceof Graphic graphic) return getStringify(graphic);
        if (o instanceof GraphicData graphicData) return stringify(graphicData);
        if (o instanceof Pic pic) return stringify(pic);
        if (o instanceof CTBlipFillProperties bfp) return stringify(bfp);
        if (o instanceof CTBlip blip) return stringify(blip);
        if (o instanceof R.LastRenderedPageBreak) return ""; // do not render
        if (o instanceof Br br) return stringify(br);
        if (o instanceof R.Tab) return "\t";
        if (o instanceof R.Cr) return "<carriage return>\n";
        if (o instanceof R.CommentReference cr) return stringify(cr);
        if (o instanceof CTMarkupRange) return "";
        if (o instanceof ProofErr) return "";
        if (o instanceof CommentRangeStart) return "";
        if (o instanceof CommentRangeEnd) return "";
        if (o instanceof SdtBlock block) return stringify(block.getSdtContent()) + "\n";
        if (o instanceof AlternateContent) return "";
        if (o instanceof Pict pict) return stringify(pict.getAnyAndAny());
        if (o instanceof CTShapetype) return "";
        if (o instanceof CTShape ctShape) return "[" + stringify(ctShape.getEGShapeElements()).trim() + "]\n";
        if (o instanceof CTTextbox ctTextbox) return stringify(ctTextbox.getTxbxContent());
        if (o instanceof CTTxbxContent content) return stringify(content.getContent());
        if (o instanceof SdtRun run) return stringify(run.getSdtContent());
        if (o instanceof SdtContent content) return "[" + stringify(content.getContent()).trim() + "]";
        if (o == null) throw new RuntimeException("Unsupported content: NULL");
        throw new RuntimeException("Unsupported content: " + o.getClass());
    }

    private String stringify(Tc tc) {
        var content = stringify(tc.getContent());
        return """
                |%s
                """.formatted(content.trim());
    }

    private String stringify(Tr tr) {
        var content = stringify(tr.getContent());
        return """
                %s
                """.formatted(content);
    }

    private String stringify(Tbl tbl) {
        var content = stringify(tbl.getContent());
        return """
                |===
                %s
                |===
                """.formatted(content);
    }

    private Optional<String> stringifyFooters(Stream<FooterPart> footerPart) {
        return footerPart.map(this::stringify)
                         .flatMap(Optional::stream)
                         .reduce((a, b) -> a + "\n\n" + b);
    }

    private Optional<String> stringifyHeaders(Stream<HeaderPart> headerPart) {
        return headerPart.map(this::stringify)
                         .flatMap(Optional::stream)
                         .reduce((a, b) -> a + "\n\n" + b);
    }

    private Optional<String> stringify(HeaderPart part) {
        var content = stringify(part.getContent());
        if (content.isEmpty()) return Optional.empty();
        return Optional.of("""
                [header, name="%s"]
                ----
                %s
                ----""".formatted(part.getPartName(), content));
    }

    private Optional<String> stringify(FooterPart part) {
        var content = stringify(part.getContent());
        if (content.isEmpty()) return Optional.empty();
        return Optional.of("""
                [footer, name="%s"]
                ----
                %s
                ----""".formatted(part.getPartName(), content));
    }


    private Stream<HeaderPart> getHeaderPart(WordprocessingMLPackage document) {
        var sections = document.getDocumentModel()
                               .getSections();

        var set = new LinkedHashSet<HeaderPart>();
        set.addAll(sections.stream()
                           .map(SectionWrapper::getHeaderFooterPolicy)
                           .map(HeaderFooterPolicy::getFirstHeader)
                           .filter(Objects::nonNull)
                           .toList());
        set.addAll(sections.stream()
                           .map(SectionWrapper::getHeaderFooterPolicy)
                           .map(HeaderFooterPolicy::getDefaultHeader)
                           .filter(Objects::nonNull)
                           .toList());
        set.addAll(sections.stream()
                           .map(SectionWrapper::getHeaderFooterPolicy)
                           .map(HeaderFooterPolicy::getEvenHeader)
                           .filter(Objects::nonNull)
                           .toList());

        return set.stream();
    }

    private Stream<FooterPart> getFooterPart(WordprocessingMLPackage document) {
        var sections = document.getDocumentModel()
                               .getSections();

        var set = new LinkedHashSet<FooterPart>();
        set.addAll(sections.stream()
                           .map(SectionWrapper::getHeaderFooterPolicy)
                           .map(HeaderFooterPolicy::getFirstFooter)
                           .filter(Objects::nonNull)
                           .toList());
        set.addAll(sections.stream()
                           .map(SectionWrapper::getHeaderFooterPolicy)
                           .map(HeaderFooterPolicy::getDefaultFooter)
                           .filter(Objects::nonNull)
                           .toList());
        set.addAll(sections.stream()
                           .map(SectionWrapper::getHeaderFooterPolicy)
                           .map(HeaderFooterPolicy::getEvenFooter)
                           .filter(Objects::nonNull)
                           .toList());

        return set.stream();
    }

    private String stringify(Pic pic) {
        return stringify(pic.getBlipFill());
    }

    private String stringify(CTBlipFillProperties blipFillProperties) {
        return stringify(blipFillProperties.getBlip());
    }

    private String stringify(R.CommentReference commentReference) {
        try {
            return findComment(document(), commentReference.getId()).map(c -> stringify(c.getContent()))
                                                                    .orElseThrow();
        } catch (Docx4JException e) {
            throw new RuntimeException(e);
        }
    }

    private String stringify(GraphicData graphicData) {
        return stringify(graphicData.getPic());
    }

    private String getStringify(Graphic graphic) {
        return stringify(graphic.getGraphicData());
    }

    private String stringify(Inline inline) {
        var graphic = inline.getGraphic();
        var extent = inline.getExtent();
        return "%s:%d".formatted(stringify(graphic), extent.getCx());
    }

    private String stringify(Drawing drawing) {
        return stringify(drawing.getAnchorOrInline());
    }

    private String stringify(List<?> list) {
        return list.stream()
                   .map(this::stringify)
                   .collect(joining());
    }

    /**
     * <p>stringify.</p>
     *
     * @param spacing a {@link PPrBase.Spacing} object
     *
     * @return a {@link Optional} object
     *
     * @since 1.6.6
     */
    private Optional<String> stringify(PPrBase.Spacing spacing) {
        if (spacing == null) return Optional.empty();
        SortedMap<String, Object> map = new TreeMap<>();
        extract(map, "after", spacing.getAfter());
        extract(map, "before", spacing.getBefore());
        extract(map, "beforeLines", spacing.getBeforeLines());
        extract(map, "afterLines", spacing.getAfterLines());
        extract(map, "line", spacing.getLine());
        extract(map, "lineRule", spacing.getLineRule());
        return map.isEmpty()
                ? Optional.empty()
                : Optional.of(map.entrySet()
                                 .stream()
                                 .map(format("%s=%s"))
                                 .collect(joining(",", "{", "}")));
    }

    /**
     * <p>stringify.</p>
     *
     * @param p a {@link P} object
     *
     * @return a {@link String} object
     *
     * @since 1.6.6
     */
    private String stringify(P p) {
        var runs = stringify(p.getContent());
        return stringify(p.getPPr()).map(ppr -> "❬%s❘%s❭".formatted(runs, ppr))
                                    .orElse(runs);
    }

    private Optional<String> stringify(PPr pPr) {
        if (pPr == null) return Optional.empty();
        var set = new TreeSet<String>();
        if (pPr.getJc() != null) set.add("jc=" + pPr.getJc()
                                                    .getVal()
                                                    .value());
        if (pPr.getInd() != null) set.add("ind=" + pPr.getInd()
                                                      .getLeft()
                                                      .intValue());
        if (pPr.getKeepLines() != null) set.add("keepLines=" + pPr.getKeepLines()
                                                                  .isVal());
        if (pPr.getKeepNext() != null) set.add("keepNext=" + pPr.getKeepNext()
                                                                .isVal());
        if (pPr.getOutlineLvl() != null) set.add("outlineLvl=" + pPr.getOutlineLvl()
                                                                    .getVal()
                                                                    .intValue());
        if (pPr.getPageBreakBefore() != null) set.add("pageBreakBefore=" + pPr.getPageBreakBefore()
                                                                              .isVal());
        if (pPr.getPBdr() != null) set.add("pBdr=xxx");
        if (pPr.getPPrChange() != null) set.add("pPrChange=xxx");
        stringify(pPr.getRPr()).ifPresent(set::add);
        stringify(pPr.getSectPr()).ifPresent(set::add);
        if (pPr.getShd() != null) set.add("shd=xxx");
        stringify(pPr.getSpacing()).ifPresent(spacing -> set.add("spacing=" + spacing));
        if (pPr.getSuppressAutoHyphens() != null) set.add("suppressAutoHyphens=xxx");
        if (pPr.getSuppressLineNumbers() != null) set.add("suppressLineNumbers=xxx");
        if (pPr.getSuppressOverlap() != null) set.add("suppressOverlap=xxx");
        if (pPr.getTabs() != null) set.add("tabs=xxx");
        if (pPr.getTextAlignment() != null) set.add("textAlignment=xxx");
        if (pPr.getTextDirection() != null) set.add("textDirection=xxx");
        if (pPr.getTopLinePunct() != null) set.add("topLinePunct=xxx");
        if (pPr.getWidowControl() != null) set.add("widowControl=xxx");
        if (pPr.getWordWrap() != null) set.add("wordWrap=xxx");
        if (pPr.getFramePr() != null) set.add("framePr=xxx");
        if (pPr.getDivId() != null) set.add("divId=xxx");
        ofNullable(pPr.getCnfStyle()).ifPresent(style -> set.add("cnfStyle=" + style.getVal()));
        if (set.isEmpty()) return Optional.empty();
        return Optional.of(String.join(",", set));
    }

    /**
     * <p>stringify.</p>
     *
     * @param run a {@link R} object
     *
     * @return a {@link String} object
     *
     * @since 1.6.6
     */
    private String stringify(R run) {
        String serialized = stringify(run.getContent());
        if (serialized.isEmpty()) return "";
        return ofNullable(run.getRPr())
                .flatMap(this::stringify)
                .map(rPr -> "❬%s❘%s❭".formatted(serialized, rPr))
                .orElse(serialized);
    }

    /**
     * <p>stringify.</p>
     *
     * @param rPr a {@link RPrAbstract} object
     *
     * @return a {@link String} object
     *
     * @since 1.6.6
     */
    private Optional<String> stringify(RPrAbstract rPr) {
        if (rPr == null) return Optional.empty();
        var set = new TreeSet<String>();
        if (rPr.getB() != null) set.add("b=" + rPr.getB()
                                                  .isVal());
        if (rPr.getBdr() != null) set.add("bdr=xxx");
        if (rPr.getCaps() != null) set.add("caps=" + rPr.getCaps()
                                                        .isVal());
        if (rPr.getColor() != null) set.add("color=" + rPr.getColor()
                                                          .getVal());
        if (rPr.getDstrike() != null) set.add("dstrike=" + rPr.getDstrike()
                                                              .isVal());
        if (rPr.getI() != null) set.add("i=" + rPr.getI()
                                                  .isVal());
        if (rPr.getKern() != null) set.add("kern=" + rPr.getKern()
                                                        .getVal()
                                                        .intValue());
        if (rPr.getLang() != null) set.add("lang=" + rPr.getLang()
                                                        .getVal());
        if (rPr.getRFonts() != null) {/* DO NOTHING */}
        if (rPr.getRPrChange() != null) set.add("rPrChange=xxx");
        if (rPr.getRStyle() != null) set.add("rStyle=" + rPr.getRStyle()
                                                            .getVal());
        if (rPr.getRtl() != null) set.add("rtl=" + rPr.getRtl()
                                                      .isVal());
        if (rPr.getShadow() != null) set.add("shadow=" + rPr.getShadow()
                                                            .isVal());
        if (rPr.getShd() != null) set.add("shd=" + rPr.getShd()
                                                      .getColor());
        if (rPr.getSmallCaps() != null) set.add("smallCaps=" + rPr.getSmallCaps()
                                                                  .isVal());
        if (rPr.getVertAlign() != null) set.add("vertAlign=" + rPr.getVertAlign()
                                                                  .getVal()
                                                                  .value());
        if (rPr.getSpacing() != null) set.add("spacing=" + rPr.getSpacing()
                                                              .getVal()
                                                              .intValue());
        if (rPr.getStrike() != null) set.add("strike=" + rPr.getStrike()
                                                            .isVal());
        if (rPr.getOutline() != null) set.add("outline=" + rPr.getOutline()
                                                              .isVal());
        if (rPr.getEmboss() != null) set.add("emboss=" + rPr.getEmboss()
                                                            .isVal());
        if (rPr.getImprint() != null) set.add("imprint=" + rPr.getImprint()
                                                              .isVal());
        if (rPr.getNoProof() != null) set.add("noProof=" + rPr.getNoProof()
                                                              .isVal());
        if (rPr.getSpecVanish() != null) set.add("specVanish=" + rPr.getSpecVanish()
                                                                    .isVal());
        if (rPr.getU() != null) set.add("u=" + rPr.getU()
                                                  .getVal()
                                                  .value());
        if (rPr.getVanish() != null) set.add("vanish=" + rPr.getVanish()
                                                            .isVal());
        if (rPr.getW() != null) set.add("w=" + rPr.getW()
                                                  .getVal());
        if (rPr.getWebHidden() != null) set.add("webHidden=" + rPr.getWebHidden()
                                                                  .isVal());
        if (rPr.getHighlight() != null) set.add("highlight=" + rPr.getHighlight()
                                                                  .getVal());
        if (rPr.getEffect() != null) set.add("effect=" + rPr.getEffect()
                                                            .getVal()
                                                            .value());
        if (set.isEmpty()) return Optional.empty();
        return Optional.of(String.join(",", set));
    }

    private Optional<String> stringify(SectPr sectPr) {
        if (sectPr == null) return Optional.empty();
        var set = new TreeSet<String>();
        if (sectPr.getEGHdrFtrReferences() != null && !sectPr.getEGHdrFtrReferences()
                                                             .isEmpty())
            set.add("eGHdrFtrReferences=%s".formatted(sectPr.getEGHdrFtrReferences()
                                                            .stream()
                                                            .map(this::stringify)
                                                            .collect(joining(",",
                                                                    "[",
                                                                    "]"))));
        if (sectPr.getPgSz() != null) set.add("pgSz={" + stringify(sectPr.getPgSz()) + "}");
        if (sectPr.getPgMar() != null) set.add("pgMar={" + stringify(sectPr.getPgMar()) + "}");
        if (sectPr.getPaperSrc() != null) set.add("paperSrc=xxx");
        if (sectPr.getBidi() != null) set.add("bidi=xxx");
        if (sectPr.getRtlGutter() != null) set.add("rtlGutter=xxx");
        if (sectPr.getDocGrid() != null) set.add("docGrid={"
                                                 + stringify(sectPr.getDocGrid()) + "}");
        if (sectPr.getFormProt() != null) set.add("formProt=xxx");
        if (sectPr.getVAlign() != null) set.add("vAlign=xxx");
        if (sectPr.getNoEndnote() != null) set.add("noEndnote=xxx");
        if (sectPr.getTitlePg() != null) set.add("titlePg=xxx");
        if (sectPr.getTextDirection() != null) set.add("textDirection=xxx");
        if (sectPr.getRtlGutter() != null) set.add("rtlGutter=xxx");
        if (set.isEmpty()) return Optional.empty();
        return Optional.of(String.join(",", set));
    }

    private String stringify(CTDocGrid ctDocGrid) {
        var set = new TreeSet<String>();
        if (ctDocGrid.getCharSpace() != null) set.add("charSpace=" + ctDocGrid.getCharSpace());
        if (ctDocGrid.getLinePitch() != null) set.add("linePitch=" + ctDocGrid.getLinePitch()
                                                                              .intValue());
        if (ctDocGrid.getType() != null) set.add("type=" + ctDocGrid.getType());
        return String.join(",", set);
    }

    private String stringify(CTRel ctRel) {
        var set = new TreeSet<String>();
        if (ctRel.getId() != null) set.add("id=" + ctRel.getId());
        return String.join(",", set);
    }

    private String stringify(SectPr.PgMar pgMar) {
        var set = new TreeSet<String>();
        if (pgMar.getHeader() != null) set.add("header=" + pgMar.getHeader());
        if (pgMar.getFooter() != null) set.add("footer=" + pgMar.getFooter());
        if (pgMar.getGutter() != null) set.add("gutter=" + pgMar.getGutter());
        if (pgMar.getTop() != null) set.add("top=" + pgMar.getTop());
        if (pgMar.getLeft() != null) set.add("left=" + pgMar.getLeft());
        if (pgMar.getBottom() != null) set.add("bottom=" + pgMar.getBottom());
        if (pgMar.getRight() != null) set.add("right=" + pgMar.getRight());
        return String.join(",", set);
    }

    private String stringify(SectPr.PgSz pgSz) {
        var set = new TreeSet<String>();
        if (pgSz.getOrient() != null) set.add("orient=" + pgSz.getOrient());
        if (pgSz.getW() != null) set.add("w=" + pgSz.getW());
        if (pgSz.getH() != null) set.add("h=" + pgSz.getH());
        if (pgSz.getCode() != null) set.add("code=" + pgSz.getCode());
        return String.join(",", set);
    }
}
