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
import org.docx4j.openpackaging.parts.WordprocessingML.*;
import org.docx4j.vml.CTShadow;
import org.docx4j.vml.CTShapetype;
import org.docx4j.vml.CTTextbox;
import org.docx4j.vml.VmlShapeElements;
import org.docx4j.wml.*;
import org.xlsx4j.org.apache.poi.ss.usermodel.DataFormatter;
import org.xlsx4j.sml.Cell;
import pro.verron.officestamper.api.OfficeStamperException;
import pro.verron.officestamper.experimental.ExcelCollector;
import pro.verron.officestamper.experimental.PowerpointCollector;
import pro.verron.officestamper.experimental.PowerpointParagraph;
import pro.verron.officestamper.experimental.PptxPart;
import pro.verron.officestamper.utils.WmlUtils;

import java.math.BigInteger;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.util.Optional.*;
import static java.util.stream.Collectors.joining;
import static pro.verron.officestamper.utils.ByteUtils.humanReadableByteCountSI;
import static pro.verron.officestamper.utils.ByteUtils.sha1b64;

/// @author Joseph Verron
/// @version ${version}
/// @since 1.6.5
public class Stringifier {

    private final Supplier<WordprocessingMLPackage> documentSupplier;
    private final Supplier<StyleDefinitionsPart> styleDefinitionsPartSupplier;

    /// @since 1.6.6
    public Stringifier(Supplier<WordprocessingMLPackage> documentSupplier) {
        this.documentSupplier = documentSupplier;
        this.styleDefinitionsPartSupplier = () -> documentSupplier.get()
                                                                  .getMainDocumentPart()
                                                                  .getStyleDefinitionsPart(true);
    }

    public static String stringifyPowerpoint(PresentationMLPackage presentation) {
        var collector = new PowerpointCollector<>(CTTextParagraph.class);
        collector.visit(presentation);
        var collected = collector.collect();

        var powerpoint = new StringBuilder();
        for (CTTextParagraph paragraph : collected) {
            powerpoint.append(new PowerpointParagraph(new PptxPart(), paragraph).asString());
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

    private static String stringify(Map<String, String> map) {
        return map.entrySet()
                  .stream()
                  .map(e -> "%s=%s".formatted(e.getKey(), e.getValue()))
                  .collect(joining(",", "{", "}"));
    }

    private static <T> Optional<String> stringify(List<T> list, Function<T, Optional<String>> stringify) {
        if (list == null) return empty();
        if (list.isEmpty()) return empty();
        return of(list.stream()
                      .map(stringify)
                      .flatMap(Optional::stream)
                      .collect(joining(",", "[", "]")));
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
        if (spacing == null) return empty();
        var map = new TreeMap<String, String>();
        ofNullable(spacing.getAfter()).ifPresent(value -> map.put("after", String.valueOf(value)));
        ofNullable(spacing.getBefore()).ifPresent(value -> map.put("before", String.valueOf(value)));
        ofNullable(spacing.getBeforeLines()).ifPresent(value -> map.put("beforeLines", String.valueOf(value)));
        ofNullable(spacing.getAfterLines()).ifPresent(value -> map.put("afterLines", String.valueOf(value)));
        ofNullable(spacing.getLine()).ifPresent(value -> map.put("line", String.valueOf(value)));
        ofNullable(spacing.getLineRule()).ifPresent(value -> map.put("lineRule", value.value()));
        return map.isEmpty() ? empty() : of(stringify(map));
    }

    private String stringify(Text text) {
        return TextUtils.getText(text);
    }

    private WordprocessingMLPackage document() {
        return documentSupplier.get();
    }

    private String styleID(String name) {
        return styleDefinitionsPartSupplier.get()
                                           .getNameForStyleID(name);
    }

    private String stringify(Br br) {
        var type = br.getType();
        if (type == STBrType.PAGE) return "\n[page-break]\n<<<\n";
        else if (type == STBrType.COLUMN) return "\n[col-break]\n<<<\n";
        else if (type == STBrType.TEXT_WRAPPING) return "<br/>\n";
        else if (type == null) return "<br/>\n";
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
        if (o instanceof WordprocessingMLPackage mlPackage) return stringify(mlPackage);
        if (o instanceof Tbl tbl) return stringify(tbl);
        if (o instanceof Tr tr) return stringify(tr);
        if (o instanceof Tc tc) return stringify(tc);
        if (o instanceof MainDocumentPart mainDocumentPart) return stringify(mainDocumentPart.getContent());
        if (o instanceof Body body) return stringify(body.getContent());
        if (o instanceof List<?> list) return stringify(list);
        if (o instanceof Text text) return stringify(text);
        if (o instanceof P p) return stringify(p);
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
        if (o instanceof CommentRangeStart crs) return stringify(crs);
        if (o instanceof CommentRangeEnd cre) return stringify(cre);
        if (o instanceof SdtBlock block) return stringify(block);
        if (o instanceof AlternateContent) return "";
        if (o instanceof Pict pict) return stringify(pict.getAnyAndAny());
        if (o instanceof CTShapetype) return "";
        if (o instanceof VmlShapeElements vmlShapeElements) return stringify(vmlShapeElements);
        if (o instanceof CTTextbox ctTextbox) return stringify(ctTextbox.getTxbxContent());
        if (o instanceof CTTxbxContent content) return stringify(content.getContent());
        if (o instanceof CTShadow) return "";
        if (o instanceof SdtRun run) return stringify(run.getSdtContent());
        if (o instanceof SdtContent content) return stringify(content);
        if (o instanceof R.AnnotationRef) return "";
        if (o instanceof CTFtnEdnRef ref) return "[" + ref.getId() + "]";
        if (o instanceof FootnotesPart footnotesPart) return stringify(footnotesPart).orElse("");
        if (o instanceof EndnotesPart endnotesPart) return stringify(endnotesPart).orElse("");
        if (o instanceof R.Separator) return "\n";
        if (o instanceof R.ContinuationSeparator) return "\n";
        if (o instanceof R.FootnoteRef) return "";
        if (o instanceof R.EndnoteRef) return "";
        if (o instanceof FldChar fldChar) return stringify(fldChar).orElse("");
        if (o instanceof P.Hyperlink hyperlink) return stringify(hyperlink).orElse("");
        if (o == null) throw new RuntimeException("Unsupported content: NULL");
        throw new RuntimeException("Unsupported content: " + o.getClass());
    }

    private Optional<String> stringify(EndnotesPart endnotesPart) {
        if (endnotesPart == null) return Optional.empty();
        try {
            var list = endnotesPart.getContents()
                                   .getEndnote()
                                   .stream()
                                   .map(this::stringify)
                                   .flatMap(Optional::stream)
                                   .toList();
            if (list.isEmpty()) return Optional.empty();
            return Optional.of(list.stream()
                                   .collect(joining("\n", "[endnotes]\n---\n", "\n---\n")));

        } catch (Docx4JException e) {
            throw new OfficeStamperException("Error processing footnotes", e);
        }
    }

    private Optional<String> stringify(FootnotesPart footnotesPart) {
        if (footnotesPart == null) return Optional.empty();
        try {
            var list = footnotesPart.getContents()
                                    .getFootnote()
                                    .stream()
                                    .map(this::stringify)
                                    .flatMap(Optional::stream)
                                    .toList();
            if (list.isEmpty()) return Optional.empty();
            return Optional.of(list.stream()
                                   .collect(joining("\n", "[footnotes]\n---\n", "\n---\n")));

        } catch (Docx4JException e) {
            throw new OfficeStamperException("Error processing footnotes", e);
        }
    }

    private Optional<String> stringify(FldChar fldChar) {
        var fldData = fldChar.getFldData();
        return ofNullable(fldData).map(Text::getValue)
                                  .map("[fldchar data=%s]"::formatted);
    }

    private Optional<String> stringify(P.Hyperlink hyperlink) {
        return of("[link data=%s]".formatted(stringify(hyperlink.getContent())));
    }

    private Optional<String> stringify(CTFtnEdn c) {
        var type = ofNullable(c.getType()).orElse(STFtnEdn.NORMAL);
        if (STFtnEdn.NORMAL != type) return Optional.empty();
        return Optional.of("[%s]%s".formatted(c.getId(), stringify(c.getContent())));
    }

    private String stringify(SdtBlock block) {
        return stringify(block.getSdtContent()) + "\n";
    }

    private String stringify(SdtContent content) {
        return "[" + stringify(content.getContent()).trim() + "]";
    }

    private String stringify(VmlShapeElements vmlShapeElements) {
        return "[" + stringify(vmlShapeElements.getEGShapeElements()).trim() + "]\n";
    }

    private String stringify(CommentRangeStart crs) {
        return "<" + crs.getId() + "|";
    }

    private String stringify(CommentRangeEnd cre) {
        return "|" + cre.getId() + ">";
    }

    private String stringify(WordprocessingMLPackage mlPackage) {
        var header = stringifyHeaders(getHeaderPart(mlPackage));
        var mainDocumentPart = mlPackage.getMainDocumentPart();
        var body = stringify(mainDocumentPart);

        var footer = stringifyFooters(getFooterPart(mlPackage));
        var hStr = header.map("%s\n\n"::formatted)
                         .orElse("");
        var fStr = footer.map("\n%s\n"::formatted)
                         .orElse("");
        var footnotesPart = mainDocumentPart.getFootnotesPart();
        var endnotesPart = mainDocumentPart.getEndNotesPart();
        return hStr + body + stringify(footnotesPart).orElse("") + stringify(endnotesPart).orElse("") + fStr;
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
        if (content.isEmpty()) return empty();
        return of("""
                [header, name="%s"]
                ----
                %s
                ----""".formatted(part.getPartName(), content));
    }

    private Optional<String> stringify(FooterPart part) {
        var content = stringify(part.getContent());
        if (content.isEmpty()) return empty();
        return of("""
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
        var id = commentReference.getId();
        var stringifiedComment = stringifyComment(id);
        return "<%s|%s>".formatted(id, stringifiedComment);
    }

    private String stringifyComment(BigInteger id) {
        var document = document();
        return WmlUtils.findComment(document, id)
                       .map(Comments.Comment::getContent)
                       .map(this::stringify)
                       .orElseThrow()
                       .strip();
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
     * @param rPr a {@link RPrAbstract} object
     *
     * @return a {@link String} object
     *
     * @since 1.6.6
     */
    private Optional<String> stringify(RPrAbstract rPr) {
        if (rPr == null) return empty();
        var map = new TreeMap<String, String>();
        ofNullable(rPr.getB()).ifPresent(value -> map.put("b", String.valueOf(value.isVal())));
        ofNullable(rPr.getBdr()).ifPresent(value -> map.put("bdr", "xxx"));
        ofNullable(rPr.getCaps()).ifPresent(value -> map.put("caps", String.valueOf(value.isVal())));
        ofNullable(rPr.getColor()).ifPresent(value -> map.put("color", value.getVal()));
        ofNullable(rPr.getDstrike()).ifPresent(value -> map.put("dstrike", String.valueOf(value.isVal())));
        ofNullable(rPr.getI()).ifPresent(value -> map.put("i", String.valueOf(value.isVal())));
        ofNullable(rPr.getKern()).ifPresent(value -> map.put("kern", String.valueOf(value.getVal())));
        ofNullable(rPr.getLang()).ifPresent(value -> map.put("lang", value.getVal()));
        stringify(rPr.getRFonts()).ifPresent(e -> map.put("rFont", e));
        ofNullable(rPr.getRStyle()).ifPresent(value -> map.put("rStyle", value.getVal()));
        ofNullable(rPr.getRtl()).ifPresent(value -> map.put("rtl", String.valueOf(value.isVal())));
        ofNullable(rPr.getShadow()).ifPresent(value -> map.put("shadow", String.valueOf(value.isVal())));
        ofNullable(rPr.getShd()).ifPresent(value -> map.put("shd", value.getColor()));
        ofNullable(rPr.getSmallCaps()).ifPresent(value -> map.put("smallCaps", String.valueOf(value.isVal())));
        ofNullable(rPr.getVertAlign()).ifPresent(value -> map.put("vertAlign",
                value.getVal()
                     .value()));
        ofNullable(rPr.getSpacing()).ifPresent(value -> map.put("spacing", String.valueOf(value.getVal())));
        ofNullable(rPr.getStrike()).ifPresent(value -> map.put("strike", String.valueOf(value.isVal())));
        ofNullable(rPr.getOutline()).ifPresent(value -> map.put("outline", String.valueOf(value.isVal())));
        ofNullable(rPr.getEmboss()).ifPresent(value -> map.put("emboss", String.valueOf(value.isVal())));
        ofNullable(rPr.getImprint()).ifPresent(value -> map.put("imprint", String.valueOf(value.isVal())));
        ofNullable(rPr.getNoProof()).ifPresent(value -> map.put("noProof", String.valueOf(value.isVal())));
        ofNullable(rPr.getSpecVanish()).ifPresent(value -> map.put("specVanish", String.valueOf(value.isVal())));
        ofNullable(rPr.getU()).ifPresent(value -> map.put("u",
                value.getVal()
                     .value()));
        ofNullable(rPr.getVanish()).ifPresent(value -> map.put("vanish", String.valueOf(value.isVal())));
        ofNullable(rPr.getW()).ifPresent(value -> map.put("w", String.valueOf(value.getVal())));
        ofNullable(rPr.getWebHidden()).ifPresent(value -> map.put("webHidden", String.valueOf(value.isVal())));
        ofNullable(rPr.getHighlight()).ifPresent(value -> map.put("highlight", value.getVal()));
        ofNullable(rPr.getEffect()).ifPresent(value -> map.put("effect",
                value.getVal()
                     .value()));
        return map.isEmpty() ? empty() : of(stringify(map));
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
        var ppr = stringify(p.getPPr());
        return ppr.apply(runs) + "\n";
    }

    private Function<String, String> stringify(PPr pPr) {
        if (pPr == null) return Function.identity();
        var set = new TreeMap<String, String>();
        ofNullable(pPr.getPStyle()).ifPresent(element -> set.put("pStyle", styleID(element.getVal())));
        ofNullable(pPr.getJc()).ifPresent(element -> set.put("jc",
                element.getVal()
                       .toString()));
        ofNullable(pPr.getInd()).ifPresent(element -> set.put("ind",
                element.getLeft()
                       .toString()));
        ofNullable(pPr.getKeepLines()).ifPresent(element -> set.put("keepLines", String.valueOf(element.isVal())));
        ofNullable(pPr.getKeepNext()).ifPresent(element -> set.put("keepNext", String.valueOf(element.isVal())));
        ofNullable(pPr.getOutlineLvl()).ifPresent(element -> set.put("outlineLvl",
                element.getVal()
                       .toString()));
        ofNullable(pPr.getPageBreakBefore()).ifPresent(element -> set.put("pageBreakBefore",
                String.valueOf(element.isVal())));
        ofNullable(pPr.getPBdr()).ifPresent(element -> set.put("pBdr", "xxx"));
        ofNullable(pPr.getPPrChange()).ifPresent(element -> set.put("pPrChange", "xxx"));
        stringify(pPr.getRPr()).ifPresent(key -> set.put("rPr", key));
        stringify(pPr.getSectPr()).ifPresent(key -> set.put("sectPr", key));
        ofNullable(pPr.getShd()).ifPresent(element -> set.put("shd", "xxx"));
        stringify(pPr.getSpacing()).ifPresent(spacing -> set.put("spacing", spacing));
        ofNullable(pPr.getSuppressAutoHyphens()).ifPresent(element -> set.put("suppressAutoHyphens", "xxx"));
        ofNullable(pPr.getSuppressLineNumbers()).ifPresent(element -> set.put("suppressLineNumbers", "xxx"));
        ofNullable(pPr.getSuppressOverlap()).ifPresent(element -> set.put("suppressOverlap", "xxx"));
        ofNullable(pPr.getTabs()).ifPresent(element -> set.put("tabs", "xxx"));
        ofNullable(pPr.getTextAlignment()).ifPresent(element -> set.put("textAlignment", "xxx"));
        ofNullable(pPr.getTextDirection()).ifPresent(element -> set.put("textDirection", "xxx"));
        ofNullable(pPr.getTopLinePunct()).ifPresent(element -> set.put("topLinePunct", "xxx"));
        ofNullable(pPr.getWidowControl()).ifPresent(element -> set.put("widowControl", "xxx"));
        ofNullable(pPr.getFramePr()).ifPresent(element -> set.put("framePr", "xxx"));
        ofNullable(pPr.getWordWrap()).ifPresent(element -> set.put("wordWrap", "xxx"));
        ofNullable(pPr.getDivId()).ifPresent(element -> set.put("divId", "xxx"));
        ofNullable(pPr.getCnfStyle()).ifPresent(style -> set.put("cnfStyle", style.getVal()));
        return set.entrySet()
                  .stream()
                  .reduce(Function.identity(), (f, entry) -> switch (entry.getKey()) {
                      case "pStyle" -> f.compose(decorateWithStyle(entry.getValue()));
                      case "sectPr" -> f.compose(str -> str + "\n[section-break, " + entry.getValue() + "]\n<<<");
                      default -> f.andThen(s -> s + "<%s=%s>".formatted(entry.getKey(), entry.getValue()));
                  }, Function::andThen);
    }

    private Function<? super String, String> decorateWithStyle(String value) {
        return switch (value) {
            case "Title" -> "= %s\n"::formatted;
            case "heading 1" -> "== %s\n"::formatted;
            case "heading 2" -> "=== %s\n"::formatted;
            case "heading 3" -> "==== %s\n"::formatted;
            case "heading 4" -> "===== %s\n"::formatted;
            case "heading 5" -> "====== %s\n"::formatted;
            case "heading 6" -> "======= %s\n"::formatted;
            case "caption" -> ".%s"::formatted;
            case "annotation text", "footnote text", "endnote text" -> string -> string;
            default -> "[%s] %%s".formatted(value)::formatted;
        };
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
        return ofNullable(run.getRPr()).flatMap(this::stringify)
                                       .map(rPr -> "❬%s❘%s❭".formatted(serialized, rPr))
                                       .orElse(serialized);
    }

    private Optional<String> stringify(RFonts rFonts) {
        if (rFonts == null) return empty();
        var map = new TreeMap<String, String>();
        ofNullable(rFonts.getAscii()).ifPresent(value -> map.put("ascii", value));
        ofNullable(rFonts.getHAnsi()).ifPresent(value -> map.put("hAnsi", value));
        ofNullable(rFonts.getCs()).ifPresent(value -> map.put("cs", value));
        ofNullable(rFonts.getEastAsia()).ifPresent(value -> map.put("eastAsia", value));
        ofNullable(rFonts.getAsciiTheme()).ifPresent(value -> map.put("asciiTheme", value.value()));
        ofNullable(rFonts.getHAnsiTheme()).ifPresent(value -> map.put("hAnsiTheme", value.value()));
        ofNullable(rFonts.getCstheme()).ifPresent(value -> map.put("cstheme", value.value()));
        ofNullable(rFonts.getEastAsiaTheme()).ifPresent(value -> map.put("eastAsiaTheme", value.value()));
        return map.isEmpty() ? empty() : of(stringify(map));
    }

    private Optional<String> stringify(SectPr sectPr) {
        if (sectPr == null) return empty();
        var map = new TreeMap<String, String>();
        stringify(sectPr.getEGHdrFtrReferences(), this::stringify).ifPresent(value -> map.put("eGHdrFtrReferences",
                value));
        stringify(sectPr.getPgSz()).ifPresent(value -> map.put("pgSz", value));
        stringify(sectPr.getPgMar()).ifPresent(value -> map.put("pgMar", value));
        ofNullable(sectPr.getPaperSrc()).ifPresent(value -> map.put("paperSrc", "xxx"));
        ofNullable(sectPr.getBidi()).ifPresent(value -> map.put("bidi", "xxx"));
        ofNullable(sectPr.getRtlGutter()).ifPresent(value -> map.put("rtlGutter", "xxx"));
        stringify(sectPr.getDocGrid()).ifPresent(value -> map.put("docGrid", value));
        ofNullable(sectPr.getFormProt()).ifPresent(value -> map.put("formProt", "xxx"));
        ofNullable(sectPr.getVAlign()).ifPresent(value -> map.put("vAlign", "xxx"));
        ofNullable(sectPr.getNoEndnote()).ifPresent(value -> map.put("noEndnote", "xxx"));
        ofNullable(sectPr.getTitlePg()).ifPresent(value -> map.put("titlePg", "xxx"));
        ofNullable(sectPr.getTextDirection()).ifPresent(value -> map.put("textDirection", "xxx"));
        ofNullable(sectPr.getRtlGutter()).ifPresent(value -> map.put("rtlGutter", "xxx"));
        return map.isEmpty() ? empty() : of(stringify(map));
    }

    private Optional<String> stringify(CTRel ctRel) {
        if (ctRel == null) return empty();
        var map = new TreeMap<String, String>();
        ofNullable(ctRel.getId()).ifPresent(value -> map.put("id", value));
        return map.isEmpty() ? empty() : of(stringify(map));
    }

    private Optional<String> stringify(SectPr.PgSz pgSz) {
        if (pgSz == null) return empty();
        var map = new TreeMap<String, String>();
        ofNullable(pgSz.getOrient()).ifPresent(value -> map.put("orient", String.valueOf(value)));
        ofNullable(pgSz.getW()).ifPresent(value -> map.put("w", String.valueOf(value)));
        ofNullable(pgSz.getH()).ifPresent(value -> map.put("h", String.valueOf(value)));
        ofNullable(pgSz.getCode()).ifPresent(value -> map.put("code", String.valueOf(value)));
        return map.isEmpty() ? empty() : of(stringify(map));
    }

    private Optional<String> stringify(SectPr.PgMar pgMar) {
        if (pgMar == null) return empty();
        var map = new TreeMap<String, String>();
        ofNullable(pgMar.getHeader()).ifPresent(value -> map.put("header", String.valueOf(value)));
        ofNullable(pgMar.getFooter()).ifPresent(value -> map.put("footer", String.valueOf(value)));
        ofNullable(pgMar.getGutter()).ifPresent(value -> map.put("gutter", String.valueOf(value)));
        ofNullable(pgMar.getTop()).ifPresent(value -> map.put("top", String.valueOf(value)));
        ofNullable(pgMar.getLeft()).ifPresent(value -> map.put("left", String.valueOf(value)));
        ofNullable(pgMar.getBottom()).ifPresent(value -> map.put("bottom", String.valueOf(value)));
        ofNullable(pgMar.getRight()).ifPresent(value -> map.put("right", String.valueOf(value)));
        return map.isEmpty() ? empty() : of(stringify(map));
    }

    private Optional<String> stringify(CTDocGrid ctDocGrid) {
        if (ctDocGrid == null) return empty();
        var map = new TreeMap<String, String>();
        ofNullable(ctDocGrid.getCharSpace()).ifPresent(value -> map.put("charSpace", String.valueOf(value)));
        ofNullable(ctDocGrid.getLinePitch()).ifPresent(value -> map.put("linePitch", String.valueOf(value)));
        ofNullable(ctDocGrid.getType()).ifPresent(value -> map.put("type", String.valueOf(value)));
        return map.isEmpty() ? empty() : of(stringify(map));
    }
}
