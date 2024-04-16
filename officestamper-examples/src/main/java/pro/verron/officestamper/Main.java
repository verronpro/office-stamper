package pro.verron.officestamper;


import java.nio.file.Files;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {

    private static final Logger logger = Utils.getLogger();

    public static void main(String[] args)
            throws Exception {
        var outputPath = Files.createTempFile("Diagnostic-", ".docx");
        var outputStream = Files.newOutputStream(outputPath);
        logger.log(Level.INFO, "Stamping to file: " + outputPath);
        Examples.stampDiagnostic(outputStream);
    }
}
