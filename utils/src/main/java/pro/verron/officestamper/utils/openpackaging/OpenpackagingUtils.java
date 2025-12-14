package pro.verron.officestamper.utils.openpackaging;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.PresentationMLPackage;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import pro.verron.officestamper.utils.UtilsException;

import java.io.InputStream;
import java.io.OutputStream;

/// Utility class for working with Open Packaging documents. This class provides methods to load and export Word
/// documents using DOCX4J
public class OpenpackagingUtils {
    private OpenpackagingUtils() {
        throw new UtilsException("Utility class shouldn't be instantiated");
    }

    /// Loads a Word document from the provided input stream.
    ///
    /// @param is the input stream containing the Word document data
    ///
    /// @return a WordprocessingMLPackage representing the loaded document
    ///
    /// @throws UtilsException if there is an error loading the document
    public static WordprocessingMLPackage loadWord(InputStream is) {
        try {
            return WordprocessingMLPackage.load(is);
        } catch (Docx4JException e) {
            throw new UtilsException(e);
        }
    }

    /// Exports a Word document to the provided output stream.
    ///
    /// @param wordprocessingMLPackage the Word document to export
    /// @param os the output stream to write the document to
    ///
    /// @throws UtilsException if there is an error exporting the document
    public static void exportWord(WordprocessingMLPackage wordprocessingMLPackage, OutputStream os) {
        try {
            wordprocessingMLPackage.save(os);
        } catch (Docx4JException e) {
            throw new UtilsException(e);
        }
    }


    /// Loads a PowerPoint document from the provided input stream.
    ///
    /// @param is the input stream containing the PowerPoint document data
    ///
    /// @return a PresentationMLPackage representing the loaded document
    ///
    /// @throws UtilsException if there is an error loading the document
    public static PresentationMLPackage loadPowerPoint(InputStream is) {
        try {
            return PresentationMLPackage.load(is);
        } catch (Docx4JException e) {
            throw new UtilsException(e);
        }
    }


    /// Exports a PowerPoint document to the provided output stream.
    ///
    /// @param presentationMLPackage the PowerPoint document to export
    /// @param os the output stream to write the document to
    ///
    /// @throws UtilsException if there is an error exporting the document
    public static void exportPowerPoint(PresentationMLPackage presentationMLPackage, OutputStream os) {
        try {
            presentationMLPackage.save(os);
        } catch (Docx4JException e) {
            throw new UtilsException(e);
        }
    }
}
