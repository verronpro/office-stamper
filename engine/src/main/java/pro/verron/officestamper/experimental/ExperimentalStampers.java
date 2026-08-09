package pro.verron.officestamper.experimental;

import pro.verron.officestamper.api.OfficeStamper;
import pro.verron.officestamper.api.OfficeStamperException;
import pro.verron.officestamper.api.StreamStamper;
import pro.verron.officestamper.utils.pml.PptxDocument;
import pro.verron.officestamper.utils.sml.XlsxDocument;

import java.io.OutputStream;

/// ExperimentalStampers is a class that provides static methods for obtaining instances of OfficeStamper
/// implementations for stamping PowerPoint presentations and Excel templates with context and writing the result to an
/// [OutputStream].
///
/// @since 1.6.8
public class ExperimentalStampers {

    private ExperimentalStampers() {
        throw new OfficeStamperException("ExperimentalStampers cannot be instantiated");
    }

    /// Returns a new instance of the OfficeStamper implementation for stamping Excel templates with context and writing
    /// the result to an OutputStream.
    ///
    /// @return a new OfficeStamper instance for Excel templates
    /// @since 3.0
    public static OfficeStamper<XlsxDocument> xlsxPackageStamper() {
        return new ExcelStamper();
    }


    /// Returns a new instance of the StreamStamper implementation for stamping PowerPoint presentations with context
    /// and writing the result to an OutputStream.
    ///
    /// @return a new StreamStamper instance for PowerPoint presentations
    /// @since 1.6.8
    public static StreamStamper<PptxDocument> pptxStamper() {
        return new StreamStamper<>(PptxDocument::load, pptxPackageStamper(), PptxDocument::save);
    }

    /// Returns a new instance of the OfficeStamper implementation for stamping PowerPoint presentations with context
    /// and writing the result to an OutputStream.
    ///
    /// @return a new OfficeStamper instance for PowerPoint presentations
    /// @since 3.0
    public static OfficeStamper<PptxDocument> pptxPackageStamper() {
        return new PowerpointStamper();
    }
}
