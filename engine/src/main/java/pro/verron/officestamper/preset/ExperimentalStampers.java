package pro.verron.officestamper.preset;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.PresentationMLPackage;
import org.docx4j.openpackaging.packages.SpreadsheetMLPackage;
import pro.verron.officestamper.api.OfficeStamper;
import pro.verron.officestamper.api.OfficeStamperException;
import pro.verron.officestamper.api.StreamStamper;
import pro.verron.officestamper.experimental.ExcelStamper;
import pro.verron.officestamper.experimental.PowerpointStamper;

import java.io.InputStream;

/**
 * ExperimentalStampers is a class that provides static methods for obtaining instances of OfficeStamper
 * implementations for stamping PowerPoint presentations and Excel templates with context and writing
 * the result to an OutputStream.
 *
 * @since 1.6.8
 */
public class ExperimentalStampers {

    private ExperimentalStampers() {
        throw new OfficeStamperException("ExperimentalStampers cannot be instantiated");
    }

    /**
     * Returns a new instance of the OfficeStamper implementation
     * for stamping PowerPoint presentations with context and writing
     * the result to an OutputStream.
     *
     * @return a new OfficeStamper instance for PowerPoint presentations
     *
     * @since 1.6.8
     */
    public static StreamStamper<PresentationMLPackage> pptxStamper() {
        var stamper = new PowerpointStamper();
        return new StreamStamper<>(ExperimentalStampers::loadPowerPoint, stamper);
    }

    private static PresentationMLPackage loadPowerPoint(InputStream inputStream) {
        try {
            return PresentationMLPackage.load(inputStream);
        } catch (Docx4JException e) {
            throw new OfficeStamperException(e);
        }
    }

    /**
     * Returns a new instance of the OfficeStamper implementation
     * for stamping Excel templates with context and writing the result to an OutputStream.
     *
     * @return a new OfficeStamper instance for Excel templates
     *
     * @since 1.6.8
     */
    public static OfficeStamper<SpreadsheetMLPackage> xlsxStamper() {
        return new ExcelStamper();
    }
}
