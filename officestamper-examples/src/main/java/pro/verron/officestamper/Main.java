package pro.verron.officestamper;


import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.logging.Level;
import java.util.logging.Logger;

import static pro.verron.officestamper.Examples.legacyStampDiagnostic;
import static pro.verron.officestamper.Examples.stampDiagnostic;

public class Main {

    private static final Logger logger = Utils.getLogger();

    public static void main(String[] args)
            throws Exception {
        // Use the way prior to docx-stamper 1.6.8
        legacyStampDiagnostic(createOutStream("LegacyDiagnostic-"));

        // Use the way from docx-stamper 1.6.8 and with office-stamper
        stampDiagnostic(createOutStream("Diagnostic-"));
    }

    private static OutputStream createOutStream(String prefix)
            throws IOException {
        var outputPath = Files.createTempFile(prefix, ".docx");
        logger.log(Level.INFO, "Stamping to file: " + outputPath);
        return Files.newOutputStream(outputPath);
    }

}
