package pro.verron.officestamper.docx4j11.test;

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
import pro.verron.officestamper.docx.DocxDocument;
import pro.verron.officestamper.docx.DocxOfficeStampers;
import pro.verron.officestamper.docx.experimental.ExperimentalStampers;
import pro.verron.officestamper.docx.experimental.PowerpointCollector;
import pro.verron.officestamper.docx.experimental.PptxDocument;
import pro.verron.officestamper.docx.experimental.XlsxDocument;
import pro.verron.officestamper.docx.openpackaging.OpenpackagingUtils;
import pro.verron.officestamper.docx.pml.PptxRenderer;
import pro.verron.officestamper.docx.preset.PlaceholderHooker;
import pro.verron.officestamper.docx.preset.resolvers.image.ImageResolver;
import pro.verron.officestamper.docx.sml.XlsxRenderer;
import pro.verron.officestamper.docx.wml.DocxIterator;
import pro.verron.officestamper.docx.wml.WmlFactory;
import pro.verron.officestamper.test.TestFlavor;

import java.io.InputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/// docx4j11 flavor of the shared docx-stamping test suite.
public class TestFlavorImpl implements TestFlavor {
    @Override public OfficeStamperConfiguration standard() { return DocxOfficeStampers.standard(); }
    @Override public OfficeStamperConfiguration standard(ObjectResolver fallback) { return DocxOfficeStampers.standard(fallback); }
    @Override public OfficeStamperConfiguration full() { return DocxOfficeStampers.full(); }
    @Override public OfficeStamperConfiguration minimal() { return DocxOfficeStampers.minimal(); }
    @Override public void hookPlaceholders(OfficeDocument document, String placeholder, String processorName) {
        new PlaceholderHooker(placeholder, processorName).process(document);
    }

    @Override public ObjectResolver imageResolver(boolean cache) { return new ImageResolver(cache); }
    @Override public R newRun(List<Object> content) { return WmlFactory.newRun(content); }
    @Override public Comments.Comment newComment(BigInteger id, String value) { return WmlFactory.newComment(id, value); }
    @Override public List<P> paragraphs(MainDocumentPart mainDocumentPart) {
        var paragraphs = new ArrayList<P>();
        new DocxIterator(mainDocumentPart).filter(P.class::isInstance)
                                          .map(P.class::cast)
                                          .forEachRemaining(paragraphs::add);
        return paragraphs;
    }
    @Override public OfficeDocument docx(WordprocessingMLPackage pkg) { return new DocxDocument(pkg); }
    @Override public OfficeDocument loadWord(InputStream in) { return new DocxDocument(OpenpackagingUtils.loadWord(in)); }
    @Override public OfficeDocument pptx(PresentationMLPackage pkg) { return new PptxDocument(pkg); }
    @Override public OfficeDocument loadPowerPoint(InputStream in) { return new PptxDocument(OpenpackagingUtils.loadPowerPoint(in)); }
    @Override public OfficeDocument xlsx(SpreadsheetMLPackage pkg) { return new XlsxDocument(pkg); }
    @Override public StreamStamper pptxStamper() { return ExperimentalStampers.pptxStamper(); }
    @Override public OfficeStamper pptxPackageStamper() { return ExperimentalStampers.pptxPackageStamper(); }
    @Override public OfficeStamper xlsxPackageStamper() { return ExperimentalStampers.xlsxPackageStamper(); }
    @Override public <T> List<T> collectShapes(OfficeDocument stamped, Class<T> shapeType) {
        return PowerpointCollector.collect(stamped.unwrap(PresentationMLPackage.class), shapeType);
    }
    @Override public String xlsxToString(SpreadsheetMLPackage pkg) { return XlsxRenderer.xlsxToString(pkg); }
    @Override public String pptxToString(PresentationMLPackage pkg) { return PptxRenderer.pptxToString(pkg); }
}