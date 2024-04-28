package pro.verron.officestamper;

import org.wickedsource.docxstamper.DocxStamper;
import org.wickedsource.docxstamper.DocxStamperConfiguration;

import java.io.OutputStream;
import java.util.Map;
import java.util.logging.Logger;

import static pro.verron.officestamper.EvaluationContexts.enableMapAccess;

public class Examples {

    public static final Logger logger = Utils.getLogger();

    public static void stampDiagnostic(OutputStream outputStream) {
        logger.info("Start of the diagnostic stamping procedure");

        logger.info("Setup a map-reading able docx-stamper instance");
        var configuration = new DocxStamperConfiguration()
                .setEvaluationContextConfigurer(enableMapAccess());
        var stamper = new DocxStamper<>(configuration);

        logger.info("Load the internally packaged 'Diagnostic.docx' template resource");
        var template = Utils.streamResource("Diagnostic.docx");

        logger.info("""
                Create a context with: \
                system environment variables, \
                jvm properties, \
                and user preferences""");

        var diagnosticMaker = new Diagnostic();
        var context = Map.of(
                "reportDate", diagnosticMaker.date(),
                "reportUser", diagnosticMaker.user(),
                "environment", diagnosticMaker.environmentVariables(),
                "properties", diagnosticMaker.jvmProperties(),
                "preferences", diagnosticMaker.userPreferences()
        );

        logger.info("Start stamping process");
        stamper.stamp(template, context, outputStream);

        logger.info("End of the diagnostic stamping procedure");
    }

}
