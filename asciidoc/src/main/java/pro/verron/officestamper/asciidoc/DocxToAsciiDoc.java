package pro.verron.officestamper.asciidoc;

import jakarta.xml.bind.JAXBElement;
import org.docx4j.TextUtils;
import org.docx4j.model.structure.HeaderFooterPolicy;
import org.docx4j.model.structure.SectionWrapper;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.FooterPart;
import org.docx4j.openpackaging.parts.WordprocessingML.HeaderPart;
import org.docx4j.wml.P;
import org.docx4j.wml.Tbl;
import org.docx4j.wml.Tc;
import org.docx4j.wml.Tr;

import java.io.StringWriter;
import java.util.LinkedHashSet;
import java.util.stream.Stream;

/// Minimal DOCX → AsciiDoc text extractor used by tests. This intentionally mirrors a subset of the legacy Stringifier
/// formatting for:
///  - Paragraphs
///  - Tables (|=== fences, each cell prefixed with '|')
///  - Basic inline text extraction More advanced features (headers/footers, breaks, styles) can be added incrementally
/// as needed by tests.
public final class DocxToAsciiDoc {
    private DocxToAsciiDoc() {}

    public static String compile(WordprocessingMLPackage pkg) {
        var sb = new StringBuilder();

        var header = stringifyHeaders(getHeaderParts(pkg));
        header.ifPresent(s -> sb.append(s)
                                .append("\n\n"));

        var mdp = pkg.getMainDocumentPart();
        sb.append(stringifyContent(mdp.getContent()));

        try {
            if (mdp.getFootnotesPart() != null) {
                sb.append(stringifyContent(mdp.getFootnotesPart()
                                              .getContents()
                                              .getFootnote()));
            }
            if (mdp.getEndNotesPart() != null) {
                sb.append(stringifyContent(mdp.getEndNotesPart()
                                              .getContents()
                                              .getEndnote()));
            }
        } catch (org.docx4j.openpackaging.exceptions.Docx4JException e) {
            throw new RuntimeException(e);
        }

        var footer = stringifyFooters(getFooterParts(pkg));
        footer.ifPresent(s -> sb.append("\n")
                                .append(s)
                                .append("\n"));

        return sb.toString();
    }

    private static String stringifyContent(java.util.Collection<?> content) {
        var sb = new StringBuilder();
        for (Object o : content) {
            Object val = unwrap(o);
            if (val instanceof P p) {
                String text = extractText(p);
                sb.append(text)
                  .append("\n\n");
            }
            else if (val instanceof Tbl tbl) {
                sb.append(stringifyTable(tbl));
            }
            else if (val instanceof org.docx4j.wml.CTFtnEdn f) {
                sb.append(stringifyContent(f.getContent()));
            }
        }
        return sb.toString();
    }

    private static java.util.Optional<String> stringifyHeaders(Stream<HeaderPart> headerParts) {
        return headerParts.map(part -> stringifyContent(part.getContent()).trim())
                          .filter(s -> !s.isEmpty())
                          .reduce((a, b) -> a + "\n\n" + b);
    }

    private static java.util.Optional<String> stringifyFooters(Stream<FooterPart> footerParts) {
        return footerParts.map(part -> stringifyContent(part.getContent()).trim())
                          .filter(s -> !s.isEmpty())
                          .reduce((a, b) -> a + "\n\n" + b);
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

    private static String extractText(P p) {
        try {
            var writer = new StringWriter();
            TextUtils.extractText(p, writer);
            String text = writer.toString();

            // Check for textboxes in runs
            StringBuilder tbText = new StringBuilder();
            for (Object o : p.getContent()) {
                Object val = unwrap(o);
                if (val instanceof org.docx4j.wml.R r) {
                    for (Object rcO : r.getContent()) {
                        Object rcV = unwrap(rcO);
                        if (rcV instanceof org.docx4j.wml.Pict pict) {
                            for (Object pO : pict.getAnyAndAny()) {
                                Object pV = unwrap(pO);
                                if (pV instanceof org.docx4j.vml.CTTextbox t) {
                                    tbText.append(stringifyContent(t.getTxbxContent()
                                                                    .getContent()).trim());
                                }
                            }
                        }
                        else if (rcV instanceof org.docx4j.wml.Drawing drawing) {
                            for (Object dO : drawing.getAnchorOrInline()) {
                                Object dV = unwrap(dO);
                                if (dV instanceof org.docx4j.dml.wordprocessingDrawing.Inline inline) {
                                    tbText.append(extractDmlText(inline.getGraphic()));
                                }
                                else if (dV instanceof org.docx4j.dml.wordprocessingDrawing.Anchor anchor) {
                                    tbText.append(extractDmlText(anchor.getGraphic()));
                                }
                            }
                        }
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

    private static String extractDmlText(org.docx4j.dml.Graphic graphic) {
        if (graphic == null || graphic.getGraphicData() == null) return "";
        String sb = "";
        for (Object o : graphic.getGraphicData()
                               .getAny()) {
            Object val = unwrap(o);
            // Handling WML textboxes (Wordprocessing Shape)
            if (val instanceof org.docx4j.wml.CTObject obj) {
                // Textboxes in DML are often buried. For now, let's keep it simple.
            }
        }
        return sb;
    }

    private static String stringifyTable(Tbl tbl) {
        var sb = new StringBuilder();
        sb.append("|===\n");
        for (Object trO : tbl.getContent()) {
            Object trV = unwrap(trO);
            if (!(trV instanceof Tr tr)) continue;
            for (Object tcO : tr.getContent()) {
                Object tcV = unwrap(tcO);
                if (!(tcV instanceof Tc tc)) continue;
                String cellText = extractText(tc).trim();
                sb.append("|")
                  .append(cellText)
                  .append("\n\n");
            }
        }
        sb.append("|===\n");
        return sb.toString();
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
}
