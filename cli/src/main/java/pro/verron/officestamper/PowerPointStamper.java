package pro.verron.officestamper;


import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Logger;

import static pro.verron.officestamper.preset.ExperimentalStampers.pptxStamper;

public class PowerPointStamper
        implements Stamper {
    public static final Logger logger = Utils.getLogger();

    public void stamp(Object context, InputStream templateStream, OutputStream outputStream) {
        logger.info("Start of the stamping procedure");

        var stamper = pptxStamper();

        logger.info("Start stamping process");
        stamper.stamp(templateStream, context, outputStream);

        logger.info("End of the stamping procedure");
    }
}
