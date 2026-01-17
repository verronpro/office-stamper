package pro.verron.officestamper.asciidoc;

import jakarta.xml.bind.JAXBElement;
import org.docx4j.TextUtils;
import org.docx4j.dml.Graphic;
import org.docx4j.dml.wordprocessingDrawing.Anchor;
import org.docx4j.dml.wordprocessingDrawing.Inline;
import org.docx4j.model.structure.HeaderFooterPolicy;
import org.docx4j.model.structure.SectionWrapper;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.FooterPart;
import org.docx4j.openpackaging.parts.WordprocessingML.HeaderPart;
import org.docx4j.vml.CTTextbox;
import org.docx4j.wml.*;

import java.io.StringWriter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

import static pro.verron.officestamper.asciidoc.AsciiDocModel.*;
import static pro.verron.officestamper.asciidoc.AsciiDocModel.Text;
import static pro.verron.officestamper.asciidoc.AsciiDocToText.compileToText;

/// Minimal DOCX → AsciiDoc text extractor used by tests. This intentionally mirrors a subset of the legacy Stringifier
/// formatting for:
///  - Paragraphs
///  - Tables (|=== fences, each cell prefixed with '|')
///  - Basic inline text extraction More advanced features (headers/footers, breaks, styles) can be added incrementally
/// as needed by tests.
public final class DocxToAsciiDoc
        implements Function<WordprocessingMLPackage, AsciiDocModel> {
    private static List<Block> toBlocks(Collection<?> content) {
        List<Block> blocks = new ArrayList<>();
        for (Object o : content) {
            Object val = unwrap(o);
            switch (val) {
                case P p -> blocks.add(new Paragraph(toInlines(p)));
                case Tbl tbl -> blocks.add(toTableBlock(tbl));
                case CTFtnEdn f -> blocks.addAll(toBlocks(f.getContent()));
                default -> { /* DO NOTHING*/ }
            }
        }
        return blocks;
    }

    private static List<AsciiDocModel.Inline> toInlines(P p) {
        return List.of(new Text(extractText(p)));
    }

    private static Table toTableBlock(Tbl tbl) {
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
            rows.add(new Row(cells));
        }
        return new Table(rows);
    }

    private static List<AsciiDocModel.Inline> toInlines(Tc tc) {
        return List.of(new Text(extractText(tc).trim()));
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

    private static String extractText(Tc tc) {
        // Concatenate paragraphs text inside the cell
        var sb = new StringBuilder();
        for (Object o : tc.getContent()) {
            Object v = unwrap(o);
            if (v instanceof P p) {
                sb.append(extractText(p))
                  .append("\n\n");
            }
        }
        return sb.toString()
                 .trim();
    }

    private static String extractText(P p) {
        try {
            var writer = new StringWriter();
            TextUtils.extractText(p, writer);
            String text = writer.toString();

            // Check for textboxes in runs
            StringBuilder tbText = new StringBuilder();
            for (Object o : p.getContent()) {
                Object val = unwrap(o);
                if (!(Objects.requireNonNull(val) instanceof R r)) continue;
                for (Object rcO : r.getContent()) {
                    Object rcV = unwrap(rcO);
                    switch (rcV) {
                        case Pict pict -> {
                            for (Object pO : pict.getAnyAndAny()) {
                                Object pV = unwrap(pO);
                                if (pV instanceof CTTextbox t) {
                                    var txbxContent = t.getTxbxContent();
                                    var textContent = txbxContent.getContent();
                                    var blocks = toBlocks(textContent);
                                    var docModel = of(blocks);
                                    var compileToText = compileToText(docModel);
                                    var trimmed = compileToText.trim();
                                    tbText.append(trimmed);
                                }
                            }
                        }
                        case Drawing drawing -> {
                            for (Object dO : drawing.getAnchorOrInline()) {
                                Object dV = unwrap(dO);
                                switch (dV) {
                                    case Inline inline -> tbText.append(extractDmlText(inline.getGraphic()));
                                    case Anchor anchor -> tbText.append(extractDmlText(anchor.getGraphic()));
                                    default -> { /* DO NOTHING */ }
                                }
                            }
                        }
                        default -> { /* DO NOTHING */ }
                    }
                }
            }
            return text + tbText;
        } catch (Docx4JException e) {
            throw new IllegalStateException("Failed to extract text from paragraph", e);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to extract text from paragraph, before docx4j version 1.5.1", e);
        }
    }

    private static String extractDmlText(Graphic graphic) {
        if (graphic.getGraphicData() == null) return "";
        String sb = "";
        for (Object o : graphic.getGraphicData()
                               .getAny()) {
            Object val = unwrap(o);
            // Handling WML textboxes (Wordprocessing Shape)
            if (val instanceof CTObject obj) {
                // Textboxes in DML are often buried. For now, let's keep it simple.
            }
        }
        return sb;
    }

    public AsciiDocModel apply(WordprocessingMLPackage pkg) {
        List<Block> blocks = new ArrayList<>();

        getHeaderParts(pkg).forEach(part -> blocks.addAll(toBlocks(part.getContent())));

        var mdp = pkg.getMainDocumentPart();
        blocks.addAll(toBlocks(mdp.getContent()));

        try {
            var footnotesPart = mdp.getFootnotesPart();
            if (footnotesPart != null) {
                var contents = footnotesPart.getContents();
                var footnote = contents.getFootnote();
                blocks.addAll(toBlocks(footnote));
            }
            var endNotesPart = mdp.getEndNotesPart();
            if (endNotesPart != null) {
                var contents = endNotesPart.getContents();
                var endnote = contents.getEndnote();
                blocks.addAll(toBlocks(endnote));
            }
        } catch (Docx4JException e) {
            throw new RuntimeException(e);
        }
        getFooterParts(pkg).forEach(part -> blocks.addAll(toBlocks(part.getContent())));
        return AsciiDocModel.of(blocks);
    }
}
