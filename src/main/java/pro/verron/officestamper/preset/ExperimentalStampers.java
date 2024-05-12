package pro.verron.officestamper.preset;

import org.docx4j.openpackaging.packages.PresentationMLPackage;
import org.docx4j.openpackaging.packages.SpreadsheetMLPackage;
import pro.verron.officestamper.api.OfficeStamper;
import pro.verron.officestamper.experimental.ExcelStamper;
import pro.verron.officestamper.experimental.PowerpointStamper;

public class ExperimentalStampers {
    /**
     * Returns a new instance of the OfficeStamper implementation
     * for stamping Powerpoint presentations with context and writing
     * the result to an OutputStream.
     *
     * @return a new OfficeStamper instance for Powerpoint presentations
     *
     * @since 1.6.8
     */
    public static OfficeStamper<PresentationMLPackage> pptxStamper() {
        return new PowerpointStamper();
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
