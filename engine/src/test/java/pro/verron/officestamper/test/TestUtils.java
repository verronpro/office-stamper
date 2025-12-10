package pro.verron.officestamper.test;

import jakarta.xml.bind.JAXBElement;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.*;
import pro.verron.officestamper.api.OfficeStamperException;
import pro.verron.officestamper.asciidoc.AsciiDocCompiler;
import pro.verron.officestamper.preset.Image;
import pro.verron.officestamper.utils.wml.DocxIterator;
import pro.verron.officestamper.utils.wml.WmlFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static pro.verron.officestamper.utils.wml.WmlFactory.newRun;


/// A utility class for testing. Provides methods for retrieving InputStreams from specified resource paths. Typically
/// used for accessing test resources.
public class TestUtils {

    /// Retrieves an InputStream for the specified resource path.
    ///
    /// @param path the path of the resource
    ///
    /// @return an InputStream for the specified resource
    public static InputStream getResource(String path) {
        return getResource(Path.of(path));
    }


    /// Retrieves an InputStream for the specified resource path.
    ///
    /// @param path the path of the resource
    ///
    /// @return an InputStream for the specified resource
    public static InputStream getResource(Path path) {
        try {
            var testRoot = Path.of("..", "test", "sources");
            var resolve = testRoot.resolve(path);
            return Files.newInputStream(resolve);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public static InputStream makeAsciiDocResource(String asciidoc) {
        // Extract comment macros and strip them from the AsciiDoc before compilation
        var extraction = extractAndStripCommentMacros(asciidoc);
        var model = AsciiDocCompiler.parse(extraction.cleanedAsciiDoc());
        WordprocessingMLPackage aPackage = AsciiDocCompiler.toDocx(model);
        // Apply comments specified by macros onto the generated DOCX
        applyCommentMacros(aPackage, model, extraction.specs());
        OutputStream outputStream;
        try {
            outputStream = IOStreams.getOutputStream();
        } catch (IOException e) {
            throw new OfficeStamperException(e);
        }
        try {
            aPackage.save(outputStream);
        } catch (Docx4JException e) {
            throw new OfficeStamperException(e);
        }
        return IOStreams.getInputStream(outputStream);
    }

    private static MacroExtraction extractAndStripCommentMacros(String asciidoc) {
        // Regex: comment::ID[...]
        var pattern = Pattern.compile("(?m)\\s*comment::([0-9]+)\\[([^]]*)]\\s*\n?");
        var matcher = pattern.matcher(asciidoc);
        var specs = new java.util.ArrayList<CommentSpec>();
        var sb = new StringBuilder();
        while (matcher.find()) {
            var id = new BigInteger(matcher.group(1));
            var attrs = matcher.group(2);
            int startBlock = 0, startChar = 0, endBlock = 0, endChar = 0;
            String value = "";
            // parse attributes start="b,c", end="b,c", value="..."
            var attrPattern = Pattern.compile("(start|end|value)\\s*=\\s*\"([^\"]*)\"");
            var am = attrPattern.matcher(attrs);
            while (am.find()) {
                var key = am.group(1);
                var val = am.group(2);
                switch (key) {
                    case "start" -> {
                        var parts = val.split(",");
                        startBlock = Integer.parseInt(parts[0].trim());
                        startChar = Integer.parseInt(parts[1].trim());
                    }
                    case "end" -> {
                        var parts = val.split(",");
                        endBlock = Integer.parseInt(parts[0].trim());
                        endChar = Integer.parseInt(parts[1].trim());
                    }
                    case "value" -> value = val;
                }
            }
            specs.add(new CommentSpec(id, startBlock, startChar, endBlock, endChar, value));
            matcher.appendReplacement(sb, ""); // remove macro line
        }
        matcher.appendTail(sb);
        return new MacroExtraction(sb.toString(), specs);
    }

    private static void applyCommentMacros(
            WordprocessingMLPackage pkg,
            pro.verron.officestamper.asciidoc.AsciiDocModel model,
            List<CommentSpec> specs
    ) {
        if (specs.isEmpty()) return;
        ensureCommentsPart(pkg);
        // Build flattened paragraph list in document order as per render
        var mdp = pkg.getMainDocumentPart();
        var paragraphs = new ArrayList<P>();

        new DocxIterator(mdp).filter(P.class::isInstance)
                             .map(P.class::cast)
                             .forEachRemaining(paragraphs::add);

        for (var spec : specs) {
            // Add the comment to CommentsPart
            var comment = WmlFactory.newComment(spec.id, spec.value);
            try {
                pkg.getMainDocumentPart()
                   .getCommentsPart()
                   .getContents()
                   .getComment()
                   .add(comment);
            } catch (Docx4JException e) {
                throw new OfficeStamperException(e);
            }

            // Insert markers. If both positions are in the same paragraph, insert end first,
            // then start, so indices based on original text remain valid and reference stays inside range.
            if (spec.startBlock == spec.endBlock) {
                var para = paragraphs.get(spec.endBlock);
                insertAtCharIndex(para, spec.endChar, false, spec.id);
                insertAtCharIndex(para, spec.startChar, true, spec.id);
            }
            else {
                var endP = paragraphs.get(spec.endBlock);
                insertAtCharIndex(endP, spec.endChar, false, spec.id);
                var startP = paragraphs.get(spec.startBlock);
                insertAtCharIndex(startP, spec.startChar, true, spec.id);
            }
        }
    }

    private static void ensureCommentsPart(WordprocessingMLPackage pkg) {
        var mdp = pkg.getMainDocumentPart();
        try {
            if (mdp.getCommentsPart() == null) {
                var cp = new org.docx4j.openpackaging.parts.WordprocessingML.CommentsPart();
                mdp.addTargetPart(cp);
                var comments = new org.docx4j.wml.Comments();
                cp.setContents(comments);
            }
        } catch (Docx4JException e) {
            throw new OfficeStamperException(e);
        }
    }

    // ===== New comment macro processing for AsciiDoc =====

    private static void insertAtCharIndex(P p, int charIndex, boolean start, BigInteger id) {
        // Split runs so that we can insert markers exactly at charIndex
        int remaining = charIndex;
        var newContent = new java.util.ArrayList<Object>();
        for (Object o : p.getContent()) {
            var val = o instanceof JAXBElement<?> j ? j.getValue() : o;
            // If we reached the exact insertion boundary before this element, insert marker now
            if (remaining == 0) {
                if (start) {
                    var crs = new CommentRangeStart();
                    crs.setId(id);
                    newContent.add(crs);
                    var ref = new R.CommentReference();
                    ref.setId(id);
                    newContent.add(newRun(java.util.List.of(ref)));
                }
                else {
                    var cre = new CommentRangeEnd();
                    cre.setId(id);
                    newContent.add(cre);
                }
                // add current and rest unchanged
                newContent.add(o);
                int idx2 = p.getContent()
                            .indexOf(o);
                for (int i = idx2 + 1;
                     i < p.getContent()
                          .size();
                     i++)
                    newContent.add(p.getContent()
                                    .get(i));
                p.getContent()
                 .clear();
                p.getContent()
                 .addAll(newContent);
                return;
            }
            if (!(val instanceof R r)) {
                newContent.add(o);
                continue;
            }
            int runLen = 0;
            for (Object rc : r.getContent()) {
                var rcv = rc instanceof JAXBElement<?> jj ? jj.getValue() : rc;
                if (rcv instanceof Text t) {
                    runLen += t.getValue() != null ? t.getValue()
                                                      .length() : 0;
                }
            }
            if (remaining <= 0) {
                newContent.add(o);
                continue;
            }
            if (remaining >= runLen) {
                newContent.add(o);
                remaining -= runLen;
                continue;
            }
            // Need to split inside this run at position `remaining`
            // Build left text and right text
            int left = remaining;
            int consumed = 0;
            R leftRun = new R();
            R rightRun = new R();
            for (Object rc : r.getContent()) {
                var rcv = rc instanceof JAXBElement<?> jj ? jj.getValue() : rc;
                if (rcv instanceof Text t) {
                    String v = t.getValue();
                    int partLeft = Math.min(Math.max(left - consumed, 0), v.length());
                    String lv = v.substring(0, Math.min(partLeft, v.length()));
                    String rv = v.substring(Math.min(partLeft, v.length()));
                    if (!lv.isEmpty()) {
                        Text lt = new Text();
                        lt.setValue(lv);
                        lt.setSpace("preserve");
                        leftRun.getContent()
                               .add(lt);
                    }
                    if (!rv.isEmpty()) {
                        Text rt = new Text();
                        rt.setValue(rv);
                        rt.setSpace("preserve");
                        rightRun.getContent()
                                .add(rt);
                    }
                    consumed += v.length();
                }
                else {
                    // Copy other elements into both as needed; keep them on right to preserve formatting
                    rightRun.getContent()
                            .add(rc);
                }
            }
            // Replace original run with leftRun, marker, rightRun
            if (!leftRun.getContent()
                        .isEmpty()) newContent.add(leftRun);
            if (start) {
                var crs = new CommentRangeStart();
                crs.setId(id);
                newContent.add(crs);
            }
            else {
                var cre = new CommentRangeEnd();
                cre.setId(id);
                newContent.add(cre);
                var ref = new R.CommentReference();
                ref.setId(id);
                newContent.add(newRun(java.util.List.of(ref)));
            }
            if (!rightRun.getContent()
                         .isEmpty()) newContent.add(rightRun);
            // Add the rest of the original paragraph content after the split run
            int idx = p.getContent()
                       .indexOf(o);
            for (int i = idx + 1;
                 i < p.getContent()
                      .size();
                 i++)
                newContent.add(p.getContent()
                                .get(i));
            p.getContent()
             .clear();
            p.getContent()
             .addAll(newContent);
            return;
        }
        // If we didn't insert yet and we exhausted runs, append marker at end
        if (start) {
            var crs = new CommentRangeStart();
            crs.setId(id);
            p.getContent()
             .add(crs);
        }
        else {
            var cre = new CommentRangeEnd();
            cre.setId(id);
            p.getContent()
             .add(cre);
            var ref = new R.CommentReference();
            ref.setId(id);
            p.getContent()
             .add(newRun(java.util.List.of(ref)));
        }
    }

    public static WordprocessingMLPackage makeAsciiDocDocx(String asciidoc) {
        return AsciiDocCompiler.toDocx(asciidoc);
    }

    static Image getImage(Path path) {
        try {
            return new Image(getResource(path));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static Image getImage(Path path, int size) {
        try {
            return new Image(getResource(path), size);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private record CommentSpec(BigInteger id, int startBlock, int startChar, int endBlock, int endChar, String value) {}

    private record MacroExtraction(String cleanedAsciiDoc, List<CommentSpec> specs) {}
}
