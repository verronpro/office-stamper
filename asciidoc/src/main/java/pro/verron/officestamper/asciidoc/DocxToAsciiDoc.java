package pro.verron.officestamper.asciidoc;

import jakarta.xml.bind.JAXBElement;
import org.docx4j.TextUtils;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.P;
import org.docx4j.wml.Tbl;
import org.docx4j.wml.Tc;
import org.docx4j.wml.Tr;

import java.io.StringWriter;

/// Minimal DOCX → AsciiDoc text extractor used by tests. This intentionally mirrors a subset of the legacy Stringifier
/// formatting for:
///  - Paragraphs
///  - Tables (|=== fences, each cell prefixed with '|')
///  - Basic inline text extraction More advanced features (headers/footers, breaks, styles) can be added incrementally
/// as needed by tests.
final class DocxToAsciiDoc {
    private DocxToAsciiDoc() {}

    static String compile(WordprocessingMLPackage pkg) {
        var sb = new StringBuilder();
        var mdp = pkg.getMainDocumentPart();
        for (Object o : mdp.getContent()) {
            Object val = unwrap(o);
            if (val instanceof P p) {
                sb.append(extractText(p))
                  .append("\n\n");
            }
            else if (val instanceof Tbl tbl) {
                sb.append(stringifyTable(tbl));
            }
        }
        return sb.toString();
    }

    private static Object unwrap(Object o) {
        return (o instanceof JAXBElement<?> j) ? j.getValue() : o;
    }

    private static String extractText(P p) {
        try {
            var writer = new StringWriter();
            TextUtils.extractText(p, writer);
            return writer.toString();
        } catch (Docx4JException e) {
            throw new IllegalStateException("Failed to extract text from paragraph", e);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to extract text from paragraph, before docx4j version 1.5.1", e);
        }
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
