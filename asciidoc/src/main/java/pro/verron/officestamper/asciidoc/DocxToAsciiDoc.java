package pro.verron.officestamper.asciidoc;

import jakarta.xml.bind.JAXBElement;
import org.docx4j.TextUtils;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.StyleDefinitionsPart;
import org.docx4j.wml.*;

/// Minimal DOCX → AsciiDoc text extractor used by tests. This intentionally mirrors a subset of the legacy Stringifier
/// formatting for:
///  - Paragraphs
///  - Tables (|=== fences, each cell prefixed with '|')
///  - Basic inline text extraction More advanced features (headers/footers, breaks, styles) can be added incrementally
/// as needed by tests.
final class DocxToAsciiDoc {
    private DocxToAsciiDoc() {}

    static String compile(WordprocessingMLPackage pkg, AsciiDocDialect dialect) {
        var sb = new StringBuilder();
        var mdp = pkg.getMainDocumentPart();
        StyleDefinitionsPart styles = mdp.getStyleDefinitionsPart(false);
        for (Object o : mdp.getContent()) {
            Object val = unwrap(o);
            if (val instanceof P p) {
                sb.append(stringifyParagraph(p, styles, dialect))
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

    private static String stringifyParagraph(P p, StyleDefinitionsPart styles, AsciiDocDialect dialect) {
        if (dialect == AsciiDocDialect.COMPAT) {
            String runs = stringifyRuns(p);
            return applyParagraphStyle(runs, p.getPPr(), styles);
        }
        // ADOC (initial simple): just raw text for now
        return extractText(p);
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

    private static String stringifyRuns(P p) {
        StringBuilder sb = new StringBuilder();
        for (Object o : p.getContent()) {
            Object v = unwrap(o);
            if (v instanceof R r) {
                String inner = stringifyRunContent(r);
                if (inner.isEmpty()) continue;
                String rpr = stringifyRPr(r.getRPr());
                if (rpr != null) {
                    sb.append("❬")
                      .append(inner)
                      .append("❘")
                      .append(rpr)
                      .append("❭");
                }
                else {
                    sb.append(inner);
                }
            }
            else if (v instanceof Br br) {
                STBrType type = br.getType();
                if (type == STBrType.PAGE) sb.append("\n[page-break]\n<<<\n");
                else if (type == STBrType.COLUMN) sb.append("\n[col-break]\n<<<\n");
                else sb.append("<br/>\n");
            }
            else if (v instanceof JAXBElement<?> j) {
                Object x = j.getValue();
                if (x instanceof R.Tab) {
                    sb.append("\t");
                }
            }
        }
        return sb.toString();
    }

    private static String applyParagraphStyle(String text, PPr ppr, StyleDefinitionsPart styles) {
        String result = text;
        if (ppr != null && ppr.getPStyle() != null && ppr.getPStyle()
                                                         .getVal() != null && styles != null) {
            String styleName = styles.getNameForStyleID(ppr.getPStyle()
                                                           .getVal());
            if (styleName != null) {
                String decorated = decorateWithStyle(styleName, text);
                if (decorated != null) result = decorated;
            }
        }
        // Section break marker after paragraph content
        if (ppr != null && ppr.getSectPr() != null) {
            String sect = stringifySectPr(ppr.getSectPr());
            if (!sect.isEmpty()) {
                result = result + "\n[section-break, " + sect + "]\n<<<";
            }
        }
        return result;
    }

    private static String extractText(P p) {
        try {
            java.io.StringWriter writer = new java.io.StringWriter();
            TextUtils.extractText(p, writer);
            return writer.toString();
        } catch (org.docx4j.openpackaging.exceptions.Docx4JException e) {
            throw new IllegalStateException("Failed to extract text from paragraph", e);
        }
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

    private static String stringifyRunContent(R r) {
        StringBuilder sb = new StringBuilder();
        for (Object rc : r.getContent()) {
            Object rv = unwrap(rc);
            if (rv instanceof Text t) {
                sb.append(t.getValue());
            }
            else if (rv instanceof R.Tab) {
                sb.append("\t");
            }
            else if (rv instanceof Br br) {
                STBrType type = br.getType();
                switch (type) {
                    case STBrType.PAGE -> sb.append("\n[page-break]\n<<<\n");
                    case STBrType.COLUMN -> sb.append("\n[col-break]\n<<<\n");
                    default -> sb.append("<br/>\n");
                }
            }
        }
        return sb.toString();
    }

    private static String stringifyRPr(RPr rPr) {
        if (rPr == null) return null;
        java.util.TreeMap<String, String> map = new java.util.TreeMap<>();
        if (rPr.getB() != null && rPr.getB()
                                     .isVal()) {
            map.put("b", "true");
        }
        if (rPr.getI() != null && rPr.getI()
                                     .isVal()) {
            map.put("i", "true");
        }
        if (rPr.getVertAlign() != null && rPr.getVertAlign()
                                             .getVal() != null) {
            map.put("vertAlign",
                    rPr.getVertAlign()
                       .getVal()
                       .value());
        }
        if (map.isEmpty()) return null;
        return map.entrySet()
                  .stream()
                  .map(e -> e.getKey() + "=" + e.getValue())
                  .collect(java.util.stream.Collectors.joining(",", "{", "}"));
    }

    private static String decorateWithStyle(String styleName, String text) {
        String name = styleName == null ? "" : styleName.trim();
        if (name.equalsIgnoreCase("Title")) return "= " + text + "\n";
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("(?i)heading\\s*([1-6])")
                                                           .matcher(name);
        if (m.find()) {
            int lvl = Integer.parseInt(m.group(1));
            String prefix; // Stringifier maps heading 1 -> "== "
            // In Stringifier, "heading 1" => "== ", i.e., level + 1
            prefix = "=".repeat(Math.clamp(lvl, 1, 6));
            return prefix + " " + text + "\n";
        }
        return null;
    }

    private static String stringifySectPr(SectPr sectPr) {
        java.util.TreeMap<String, String> map = new java.util.TreeMap<>();
        // docGrid
        if (sectPr.getDocGrid() != null && sectPr.getDocGrid()
                                                 .getLinePitch() != null) {
            map.put("docGrid",
                    "{linePitch=" + sectPr.getDocGrid()
                                          .getLinePitch() + "}");
        }
        // pgMar
        SectPr.PgMar m = sectPr.getPgMar();
        if (m != null) {
            java.util.TreeMap<String, String> mm = new java.util.TreeMap<>();
            if (m.getBottom() != null) mm.put("bottom", String.valueOf(m.getBottom()));
            if (m.getFooter() != null) mm.put("footer", String.valueOf(m.getFooter()));
            if (m.getGutter() != null) mm.put("gutter", String.valueOf(m.getGutter()));
            if (m.getHeader() != null) mm.put("header", String.valueOf(m.getHeader()));
            if (m.getLeft() != null) mm.put("left", String.valueOf(m.getLeft()));
            if (m.getRight() != null) mm.put("right", String.valueOf(m.getRight()));
            if (m.getTop() != null) mm.put("top", String.valueOf(m.getTop()));
            if (!mm.isEmpty()) {
                String v = mm.entrySet()
                             .stream()
                             .map(e -> e.getKey() + "=" + e.getValue())
                             .collect(java.util.stream.Collectors.joining(",", "{", "}"));
                map.put("pgMar", v);
            }
        }
        // pgSz
        SectPr.PgSz s = sectPr.getPgSz();
        if (s != null) {
            java.util.TreeMap<String, String> sm = new java.util.TreeMap<>();
            if (s.getH() != null) sm.put("h", String.valueOf(s.getH()));
            if (s.getOrient() != null) sm.put("orient", String.valueOf(s.getOrient()));
            if (s.getW() != null) sm.put("w", String.valueOf(s.getW()));
            if (!sm.isEmpty()) {
                String v = sm.entrySet()
                             .stream()
                             .map(e -> e.getKey() + "=" + e.getValue())
                             .collect(java.util.stream.Collectors.joining(",", "{", "}"));
                map.put("pgSz", v);
            }
        }
        if (map.isEmpty()) return "";
        return map.entrySet()
                  .stream()
                  .map(e -> e.getKey() + "=" + e.getValue())
                  .collect(java.util.stream.Collectors.joining(",", "{", "}"));
    }
}
