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
import org.docx4j.wml.Text;
import org.jspecify.annotations.Nullable;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.Optional.ofNullable;
import static pro.verron.officestamper.asciidoc.AsciiDocModel.*;

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

    private static String extractText(Drawing drawing) {
        StringBuilder sb = new StringBuilder();
        for (Object dO : drawing.getAnchorOrInline()) {
            Object dV = unwrap(dO);
            switch (dV) {
                case Inline inline -> sb.append(extractDmlText(inline.getGraphic()));
                case Anchor anchor -> sb.append(extractDmlText(anchor.getGraphic()));
                default -> { /* DO NOTHING */ }
            }
        }
        return sb.toString();
    }

    private List<AsciiDocModel.Inline> toInlines(P p) {
        return List.of(new AsciiDocModel.Text(extractText(p)));
    }

    private List<AsciiDocModel.Inline> toInlines(Tc tc) {
        List<AsciiDocModel.Inline> inlines = new ArrayList<>();
        inlines.add(new AsciiDocModel.Text(extractText(tc).trim()));
        if (tc.getTcPr() != null && tc.getTcPr()
                                      .getCnfStyle() != null) {
            var cnfStyle = tc.getTcPr()
                             .getCnfStyle()
                             .getVal();
            inlines.add(new AsciiDocModel.Text("<cnfStyle=" + cnfStyle + ">"));
        }
        return inlines;
    }

    private String extractText(Tc tc) {
        // Concatenate paragraphs text inside the cell
        var sb = new StringBuilder();
        for (Object o : tc.getContent()) {
            Object v = unwrap(o);
            if (v instanceof P p) {
                sb.append(extractText(p))
                  .append("\n\n");
            }
        }
        return sb.toString();
    }

    private String extractText(P p) {
        StringBuilder sb = new StringBuilder();
        for (Object o : p.getContent()) {
            Object val = unwrap(o);
            if (val instanceof R r) {

                var t = extractText(r);
                if (t.isEmpty()) continue;
                var significantPr = significantPr(r.getRPr());
                significantPr.ifPresent(sb::append);
                sb.append(t);
                significantPr.ifPresent(sb::append);
            }
        }
        return sb.toString();
    }

    private String extractText(R r) {
        StringBuilder sb = new StringBuilder();
        for (Object rcO : r.getContent()) {
            Object rcV = unwrap(rcO);
            switch (rcV) {
                case Text t -> sb.append(t.getValue());
                case Br _ -> sb.append("\n");
                case R.Tab _ -> sb.append("\t");
                case Pict pict -> sb.append(extractText(pict));
                case Drawing drawing -> sb.append(extractText(drawing));
                default -> { /* DO NOTHING */ }
            }
        }
        return sb.toString();
    }

    private String extractText(Pict pict) {
        StringBuilder sb = new StringBuilder();
        for (Object pO : pict.getAnyAndAny()) {
            Object pV = unwrap(pO);
            if (pV instanceof CTTextbox t) {
                var txbxContent = t.getTxbxContent();
                var textContent = txbxContent.getContent();
                var blocks = new ArrayList<Block>();
                toBlocks(textContent, blocks);
                var docModel = of(blocks);
                var compileToText = AsciiDocCompiler.toAsciidoc(docModel);
                var trimmed = compileToText.trim();
                sb.append(trimmed);
            }
        }
        return sb.toString();
    }

    private Optional<String> significantPr(@Nullable RPr rPr) {
        if (rPr == null) return Optional.empty();
        StringBuilder sb = new StringBuilder();
        ofNullable(rPr.getVertAlign()).map(CTVerticalAlignRun::getVal)
                                      .ifPresent(obj -> sb.append(switch (obj) {
                                          case BASELINE -> "";
                                          case SUPERSCRIPT -> "^";
                                          case SUBSCRIPT -> "~";
                                      }));
        if (sb.isEmpty()) return Optional.empty();
        else return Optional.of(sb.toString());
    }

    private void toBlocks(Collection<?> content, List<Block> blocks) {
        for (Object o : content) {
            Object val = unwrap(o);
            switch (val) {
                case P p -> {
                    var headerLevel = getHeaderLevel(p);
                    if (headerLevel.isPresent()) {
                        blocks.add(new Heading(headerLevel.get(), toInlines(p)));
                    }
                    else {
                        blocks.add(new Paragraph(toInlines(p)));
                    }
                }
                case Tbl tbl -> blocks.add(toTableBlock(tbl));
                case CTFtnEdn f -> toBlocks(f.getContent(), blocks);
                default -> { /* DO NOTHING*/ }
            }
        }
    }

    private Optional<Integer> getHeaderLevel(P p) {
        if (styleDefinitionsPart == null) return Optional.empty();
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

    private Table toTableBlock(Tbl tbl) {
        List<Row> rows = new ArrayList<>();
        for (Object trO : tbl.getContent()) {
            Object trV = unwrap(trO);
            if (!(trV instanceof Tr tr)) continue;
            List<Cell> cells = new ArrayList<>();
            for (Object tcO : tr.getContent()) {
                Object tcV = unwrap(tcO);
                if (!(tcV instanceof Tc tc)) continue;
                cells.add(new Cell(toInlines(tc)));
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
                             .map(s -> "cnfStyle=" + s.getVal());

            }
            rows.add(new Row(cells, cnfStyle));
        }
        return new Table(rows);
    }

    public AsciiDocModel apply(WordprocessingMLPackage pkg) {
        List<Block> blocks = new ArrayList<>();

        getHeaderParts(pkg).forEach(part -> toBlocks(part.getContent(), blocks));

        var mdp = pkg.getMainDocumentPart();
        toBlocks(mdp.getContent(), blocks);

        try {
            var footnotesPart = mdp.getFootnotesPart();
            if (footnotesPart != null) {
                var contents = footnotesPart.getContents();
                var footnote = contents.getFootnote();
                toBlocks(footnote, blocks);
            }
            var endNotesPart = mdp.getEndNotesPart();
            if (endNotesPart != null) {
                var contents = endNotesPart.getContents();
                var endnote = contents.getEndnote();
                toBlocks(endnote, blocks);
            }
        } catch (Docx4JException e) {
            throw new RuntimeException(e);
        }
        getFooterParts(pkg).forEach(part -> toBlocks(part.getContent(), blocks));
        return of(blocks);
    }
}
