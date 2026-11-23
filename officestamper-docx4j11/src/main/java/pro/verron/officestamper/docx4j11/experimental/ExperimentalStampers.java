package pro.verron.officestamper.docx4j11.experimental;

import org.docx4j.openpackaging.packages.OpcPackage;
import org.docx4j.openpackaging.packages.PresentationMLPackage;
import org.docx4j.openpackaging.packages.SpreadsheetMLPackage;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import pro.verron.officestamper.api.OfficeDocument;
import pro.verron.officestamper.api.OfficeStamper;
import pro.verron.officestamper.api.OfficeStamperException;
import pro.verron.officestamper.api.StreamStamper;
import pro.verron.officestamper.docx4j11.DocxDocument;
import pro.verron.officestamper.docx4j11.openpackaging.OpenpackagingUtils;
import pro.verron.officestamper.preset.OfficeStamperConfigurations;
import pro.verron.officestamper.preset.OfficeStampers;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/// Experimental stampers for PowerPoint presentations and Excel templates, plus a
/// format-detecting entry point covering all supported OOXML formats.
///
/// The stamper types are expressed over the docx-free [OfficeDocument] abstraction,
/// the same contract the engine uses for docx: provider-specific implementations
/// ([PptxDocument], [XlsxDocument]) wrap the native package, and native access
/// remains available through [OfficeDocument#unwrap(Class)].
///
/// @since 1.6.8
public class ExperimentalStampers {

    private static final OfficeStamper<OfficeDocument> DOCX_STAMPER =
            OfficeStampers.docxStamper(OfficeStamperConfigurations.full());

    protected ExperimentalStampers() {
        throw new OfficeStamperException("ExperimentalStampers cannot be instantiated");
    }

    /// Returns an [OfficeStamper] for stamping Excel templates.
    /// Use [OfficeDocument#unwrap(Class)] with [SpreadsheetMLPackage] for
    /// native access to the stamped workbook.
    ///
    /// @return a new OfficeStamper instance for Excel templates
    /// @since 3.0
    public static OfficeStamper<OfficeDocument> xlsxPackageStamper() {
        return (doc, context) -> {
            new ExcelStamper().stamp(doc.unwrap(SpreadsheetMLPackage.class), context);
            return doc;
        };
    }

    /// Returns a [StreamStamper] for stamping PowerPoint presentations from streams.
    ///
    /// @return a new StreamStamper instance for PowerPoint presentations
    /// @since 1.6.8
    public static StreamStamper<OfficeDocument> pptxStamper() {
        return new StreamStamper<>(
                in -> new PptxDocument(OpenpackagingUtils.loadPowerPoint(in)),
                pptxPackageStamper(),
                ExperimentalStampers::save);
    }

    /// Returns an [OfficeStamper] for stamping PowerPoint presentations.
    /// Use [OfficeDocument#unwrap(Class)] with [PresentationMLPackage] for
    /// native access to the stamped presentation.
    ///
    /// @return a new OfficeStamper instance for PowerPoint presentations
    /// @since 3.0
    public static OfficeStamper<OfficeDocument> pptxPackageStamper() {
        return (doc, context) -> {
            new PowerpointStamper().stamp(doc.unwrap(PresentationMLPackage.class), context);
            return doc;
        };
    }

    /// Returns a format-detecting [StreamStamper]: the template stream is loaded as a
    /// generic OOXML package and dispatched to the docx, pptx or xlsx stamper according
    /// to its package type.
    ///
    /// @return a new StreamStamper handling docx, pptx and xlsx templates
    public static StreamStamper<OfficeDocument> openStamper() {
        return new StreamStamper<>(ExperimentalStampers::load,
                                   ExperimentalStampers::stampAny,
                                   ExperimentalStampers::save);
    }

    private static OfficeDocument load(InputStream in) {
        try {
            var pkg = OpcPackage.load(in);
            if (pkg instanceof PresentationMLPackage p) return new PptxDocument(p);
            if (pkg instanceof SpreadsheetMLPackage s) return new XlsxDocument(s);
            if (pkg instanceof WordprocessingMLPackage w) return new DocxDocument(w);
            throw new OfficeStamperException(
                    "Unsupported OOXML package type: " + pkg.getClass().getName());
        } catch (OfficeStamperException e) {
            throw e;
        } catch (Exception e) {
            throw new OfficeStamperException("Failed to load document", e);
        }
    }

    private static OfficeDocument stampAny(OfficeDocument doc, Object context) {
        var pkg = doc.unwrap(OpcPackage.class);
        if (pkg instanceof PresentationMLPackage) return pptxPackageStamper().stamp(doc, context);
        if (pkg instanceof SpreadsheetMLPackage) return xlsxPackageStamper().stamp(doc, context);
        if (pkg instanceof WordprocessingMLPackage) return DOCX_STAMPER.stamp(doc, context);
        throw new OfficeStamperException(
                "Unsupported OOXML package type: " + pkg.getClass().getName());
    }

    private static void save(OfficeDocument doc, OutputStream out) {
        try {
            doc.save(out);
        } catch (IOException e) {
            throw new OfficeStamperException(e);
        }
    }
}
