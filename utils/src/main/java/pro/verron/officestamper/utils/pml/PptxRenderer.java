package pro.verron.officestamper.utils.pml;

import org.docx4j.dml.CTRegularTextRun;
import org.docx4j.dml.CTTextParagraph;
import org.docx4j.openpackaging.packages.PresentationMLPackage;

import static java.util.stream.Collectors.joining;

/// Utility class for rendering PowerPoint presentations to string representations.
///
/// This class provides methods to extract text content from PowerPoint files and convert them into formatted strings.
/// It is designed as a utility class with only static methods and cannot be instantiated.
///
/// @author Joseph Verron
/// @since 3.0
public class PptxRenderer {

    private PptxRenderer() {
        throw new IllegalStateException("Utility class");
    }

    /// Converts the content of a PowerPoint presentation into a string by extracting text paragraphs from the
    /// presentation and formatting them as strings.
    ///
    /// @param presentation the PowerPoint presentation represented as a [PresentationMLPackage].
    ///
    /// @return a string representation of the text content within the PowerPoint presentation.
    public static String pptxToString(PresentationMLPackage presentation) {
        return new PptxIterator(presentation).filter(CTTextParagraph.class::isInstance)
                                             .map(CTTextParagraph.class::cast)
                                             .map(CTTextParagraph::getEGTextRun)
                                             .map(l -> l.stream()
                                                        .filter(CTRegularTextRun.class::isInstance)
                                                        .map(CTRegularTextRun.class::cast)
                                                        .map(CTRegularTextRun::getT)
                                                        .collect(joining()))
                                             .collect(joining("\n", "", "\n"));
    }
}
