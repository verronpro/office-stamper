package pro.verron.officestamper;


import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.logging.Level;
import java.util.logging.Logger;

import static pro.verron.officestamper.Examples.stampDiagnostic;

public class Main {

    private static final Logger logger = Utils.getLogger();

    public static void main(String[] args)
            throws Exception {
        stampDiagnostic(createOutStream("Diagnostic-"));
    }

    private static OutputStream createOutStream(String prefix)
            throws IOException {
        var outputPath = Files.createTempFile(prefix, ".docx");
        logger.log(Level.INFO, "Stamping to file: ", outputPath);
        return Files.newOutputStream(outputPath);
    }

}
