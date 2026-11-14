package pro.verron.officestamper.test.utils;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.OpcPackage;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.P;
import org.docx4j.wml.SectPr;
import org.docx4j.wml.STPageOrientation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.verron.asciidoc.compiler.AsciiDocCompiler;
import pro.verron.officestamper.api.OfficeStamperConfiguration;
import pro.verron.officestamper.api.OfficeStamperException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static pro.verron.officestamper.preset.OfficeStampers.docxPackageStamper;
import static pro.verron.officestamper.test.utils.ContextFactory.mapContextFactory;
import static pro.verron.officestamper.test.utils.ContextFactory.objectContextFactory;

public class OfficeStamperTestBase {
    private static final Logger log = LoggerFactory.getLogger(OfficeStamperTestBase.class);

    /// Renders the stamped document without section-layout noise: asciidoc
    /// 4.0.0 emits a `[section-properties]` block per section by default, and
    /// stamper tests do not verify page geometry.
    public static String toAsciidoc(WordprocessingMLPackage pkg) {
        return toAsciidoc(pkg, false);
    }

    /// Same as {@link #toAsciidoc(WordprocessingMLPackage)} with control over
    /// comment emission, mirroring the asciidoc compiler's 2-arg overload.
    public static String toAsciidoc(WordprocessingMLPackage pkg, boolean skipComments) {
        return AsciiDocCompiler.toAsciidoc(pkg, skipComments, false);
    }

    /// Collects the sectPr of every section of the main document part, in
    /// document order: each paragraph-level sectPr (a section break) first,
    /// then the body-level sectPr (the last section).
    public static List<SectPr> sectionProperties(WordprocessingMLPackage pkg) {
        var result = new ArrayList<SectPr>();
        for (var content : pkg.getMainDocumentPart()
                              .getJaxbElement()
                              .getBody()
                              .getContent()) {
            if (content instanceof P p && p.getPPr() != null && p.getPPr().getSectPr() != null)
                result.add(p.getPPr().getSectPr());
        }
        var bodySectPr = pkg.getMainDocumentPart().getJaxbElement().getBody().getSectPr();
        if (bodySectPr != null) result.add(bodySectPr);
        return result;
    }

    /// Page orientation of a section; PORTRAIT when the pgSz element or its
    /// w:orient attribute is absent, per the OOXML default.
    public static STPageOrientation orientationOf(SectPr sectPr) {
        var pgSz = sectPr.getPgSz();
        return pgSz == null || pgSz.getOrient() == null ? STPageOrientation.PORTRAIT : pgSz.getOrient();
    }

    /// Convenience: {@link #orientationOf(SectPr)} applied to every section
    /// collected by {@link #sectionProperties(WordprocessingMLPackage)}.
    public static List<STPageOrientation> sectionOrientations(WordprocessingMLPackage pkg) {
        return sectionProperties(pkg).stream()
                                     .map(OfficeStamperTestBase::orientationOf)
                                     .toList();
    }

    protected static Stream<ContextFactory> factories() {
        return Stream.of(objectContextFactory(), mapContextFactory());
    }

    protected void testStamper(
            OfficeStamperConfiguration config,
            Object context,
            WordprocessingMLPackage template,
            String expected
    ) {
        var stamper = docxPackageStamper(config);
        var wordprocessingMLPackage = stamper.stamp(template, context);
        OfficeStamperTestBase.writeOutputFile(wordprocessingMLPackage);
        var actual = toAsciidoc(wordprocessingMLPackage);
        assertEquals(expected.replace("\r\n", "\n"), actual.replace("\r\n", "\n"));
    }

    private static void writeOutputFile(OpcPackage opcPackage) {
        var keepOutputFile = System.getenv("keepOutputFile");
        var parsedBoolean = Boolean.parseBoolean(keepOutputFile);
        if (!parsedBoolean) return;
        var tempFile = createTempFile();
        log.info("Write to {}", tempFile.toString());
        writeFile(opcPackage, tempFile);
    }

    private static Path createTempFile() {
        try {
            return Files.createTempFile("stamper", ".docx");
        } catch (IOException e) {
            throw new OfficeStamperException(e);
        }
    }

    private static void writeFile(OpcPackage opcPackage, Path tempFile) {
        try {
            opcPackage.save(tempFile.toFile());
        } catch (Docx4JException e) {
            throw new OfficeStamperException(e);
        }
    }
}
