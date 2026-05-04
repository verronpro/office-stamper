package pro.verron.officestamper.imageio.emf.utils;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.OpcPackage;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.verron.officestamper.api.OfficeStamperConfiguration;
import pro.verron.officestamper.api.OfficeStamperException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static pro.verron.officestamper.asciidoc.AsciiDocCompiler.toAsciidoc;
import static pro.verron.officestamper.imageio.emf.utils.ContextFactory.mapContextFactory;
import static pro.verron.officestamper.imageio.emf.utils.ContextFactory.objectContextFactory;
import static pro.verron.officestamper.preset.OfficeStampers.docxPackageStamper;

public class OfficeStamperTest {
    private static final Logger log = LoggerFactory.getLogger(OfficeStamperTest.class);

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
        OfficeStamperTest.writeOutputFile(wordprocessingMLPackage);
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
