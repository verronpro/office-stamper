package pro.verron.officestamper;

import pro.verron.officestamper.preset.OfficeStampers;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Logger;

import static pro.verron.officestamper.preset.OfficeStamperConfigurations.standard;

public class WordStamper
        implements Stamper {

    public static final Logger logger = Utils.getLogger();

    @Override
    public void stamp(Object context, InputStream templateStream, OutputStream outputStream) {
        logger.info("Start of the stamping procedure");

        logger.info("Setup a map-reading able docx-stamper instance");

        var configuration = standard();
        var stamper = OfficeStampers.docxStamper(configuration);

        logger.info("Start stamping process");
        stamper.stamp(templateStream, context, outputStream);

        logger.info("End of the stamping procedure");
    }
}
