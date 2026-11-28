package pro.verron.officestamper.test;

import org.docx4j.openpackaging.packages.PresentationMLPackage;
import org.docx4j.openpackaging.packages.SpreadsheetMLPackage;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.wml.Comments;
import org.docx4j.wml.P;
import org.docx4j.wml.R;
import pro.verron.officestamper.api.ObjectResolver;
import pro.verron.officestamper.api.OfficeDocument;
import pro.verron.officestamper.api.OfficeStamper;
import pro.verron.officestamper.api.OfficeStamperConfiguration;
import pro.verron.officestamper.api.StreamStamper;

import java.io.InputStream;
import java.math.BigInteger;
import java.util.List;

/// Operations that differ between docx4j flavors in the shared docx-stamping
/// test suite. Each provider test module registers exactly one implementation
/// via [java.util.ServiceLoader], so the identical shared sources run
/// once per flavor.
public interface TestFlavor {

    /// The provider's equivalent of the legacy standard preset.
    OfficeStamperConfiguration standard();

    /// The provider's equivalent of the legacy standard preset with a custom
    /// fallback resolver.
    OfficeStamperConfiguration standard(ObjectResolver fallback);

    /// The provider's equivalent of the legacy full preset.
    OfficeStamperConfiguration full();

    /// The provider's equivalent of the legacy minimal preset.
    OfficeStamperConfiguration minimal();

    /// Runs the provider's placeholder-hooking preprocessor on a document.
    void hookPlaceholders(OfficeDocument document, String placeholder, String processorName);

    /// The provider's image resolver.
    ObjectResolver imageResolver(boolean cache);

    /// Builds a run with the given content.
    R newRun(List<Object> content);

    /// Builds a comment with the given id and value.
    Comments.Comment newComment(BigInteger id, String value);

    /// Flattens the paragraphs of a main document part, in document order.
    List<P> paragraphs(MainDocumentPart mainDocumentPart);

    /// Wraps a native WordprocessingMLPackage into a docx-free document.
    OfficeDocument docx(WordprocessingMLPackage pkg);

    /// Loads a Word document from a stream.
    OfficeDocument loadWord(InputStream in);

    /// Wraps a native PresentationMLPackage into a docx-free document.
    OfficeDocument pptx(PresentationMLPackage pkg);

    /// Loads a PowerPoint document from a stream.
    OfficeDocument loadPowerPoint(InputStream in);

    /// Wraps a native SpreadsheetMLPackage into a docx-free document.
    OfficeDocument xlsx(SpreadsheetMLPackage pkg);

    /// The provider's experimental stream stamper (pptx).
    StreamStamper pptxStamper();

    /// The provider's experimental package stamper (pptx).
    OfficeStamper pptxPackageStamper();

    /// The provider's experimental package stamper (xlsx).
    OfficeStamper xlsxPackageStamper();

    /// Collects shapes of the given type from a stamped presentation.
    <T> List<T> collectShapes(OfficeDocument stamped, Class<T> shapeType);

    /// Renders a spreadsheet package to its xlsx text form.
    String xlsxToString(SpreadsheetMLPackage pkg);

    /// Renders a presentation package to its pptx text form.
    String pptxToString(PresentationMLPackage pkg);
}
