package pro.verron.officestamper.test.utils;

import org.junit.jupiter.params.provider.Arguments;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.verron.officestamper.api.OfficeStamperConfiguration;
import pro.verron.officestamper.api.OfficeStamperException;
import pro.verron.officestamper.utils.wml.Document;
import pro.verron.officestamper.utils.wml.DocxDocument;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.argumentSet;
import static pro.verron.asciidoc.compiler.AsciiDocCompiler.toAsciidoc;
import static pro.verron.officestamper.preset.OfficeStampers.docxPackageStamper;
import static pro.verron.officestamper.test.utils.ContextFactory.mapContextFactory;
import static pro.verron.officestamper.test.utils.ContextFactory.objectContextFactory;

public class OfficeStamperTestBase {
    private static final Logger log = LoggerFactory.getLogger(OfficeStamperTestBase.class);

    protected static Stream<Arguments> factories() {
        var obj = argumentSet("obj", objectContextFactory());
        var map = argumentSet("map", mapContextFactory());
        return Stream.of(obj, map);
    }

    private static void writeOutputFile(Document opcPackage) {
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

    private static void writeFile(Document opcPackage, Path tempFile) {
        try {
            opcPackage.save(new FileOutputStream(tempFile.toFile()));
        } catch (FileNotFoundException e) {
            throw new OfficeStamperException(e);
        }
    }

    protected void testStamper(OfficeStamperConfiguration config, Object context, DocxDocument template, String expected, boolean skipComments) {
        var stamper = docxPackageStamper(config);
        var wordprocessingMLPackage = stamper.stamp(template, context);
        OfficeStamperTestBase.writeOutputFile(wordprocessingMLPackage);
        var actual = toAsciidoc(wordprocessingMLPackage.getPackage(), skipComments);
        assertEquals(expected.replace("\r\n", "\n"), actual.replace("\r\n", "\n"));
    }

    protected void testStamper(OfficeStamperConfiguration config, Object context, DocxDocument template, String expected) {
        testStamper(config, context, template, expected, false);
    }
}
