package pro.verron.officestamper.experimental;

import org.docx4j.dml.CTRegularTextRun;
import org.docx4j.openpackaging.packages.PresentationMLPackage;
import org.springframework.expression.spel.SpelParserConfiguration;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import pro.verron.officestamper.api.Insert;
import pro.verron.officestamper.api.OfficeStamper;
import pro.verron.officestamper.api.OfficeStamperException;
import pro.verron.officestamper.preset.Image;
import pro.verron.officestamper.utils.openpackaging.OpenPackage;
import pro.verron.officestamper.utils.pml.PptxDocument;
import pro.verron.officestamper.utils.pml.PptxDocument.Slide;
import pro.verron.officestamper.utils.pml.PptxDocument.Slide.Shape;

/// The PowerpointStamper class implements the OfficeStamper interface to provide capability for stamping PowerPoint
/// presentations with context and writing the result to an OutputStream.
public class PowerpointStamper implements OfficeStamper<PptxDocument> {
    /// Constructs a new instance of the PowerpointStamper class. This constructor initializes an instance of
    /// PowerpointStamper, which implements the OfficeStamper interface. The class provides functionality to apply
    /// variable-based stamping on PowerPoint templates and outputs the modified presentation.
    public PowerpointStamper() {
        // Explicit default constructor for Javadoc
    }

    @Override
    public PptxDocument stamp(PptxDocument document, Object context) throws OfficeStamperException {
        for (var slide : document.getSlides()) {
            for (Shape shape : slide.getShapes()) {
                processShape(document, slide, shape, context);
            }
        }
        return document;
    }

    private void processShape(PptxDocument document, Slide slide, Shape shape, Object context) {
        for (var paragraph : shape.getParagraphs()) {
            PowerpointParagraph smartParagraph = new PowerpointParagraph(document, paragraph);
            String string = smartParagraph.asString();
            for (var variable : Placeholders.findVariables(string)) {
                var evaluationContext = new StandardEvaluationContext(context);
                var parserConfiguration = new SpelParserConfiguration();
                var parser = new SpelExpressionParser(parserConfiguration);
                var expression = parser.parseExpression(variable.content());
                var value = expression.getValue(evaluationContext);

                if (value instanceof Image image) {
                    fillShapeWithImage(document, slide, shape, image);
                    return;
                } else {
                    var replacement = new CTRegularTextRun();
                    replacement.setT(String.valueOf(value));
                    var expression1 = variable.expression();
                    smartParagraph.replace(expression1, new Insert(replacement));
                }
            }
        }
    }

    private void fillShapeWithImage(PptxDocument document, Slide slide, Shape shape, Image image) {
        PresentationMLPackage presentationMLPackage = document.getPackage();
        var openPackage = OpenPackage.getOrCreate(presentationMLPackage, slide.getPart());
        var imgPart = openPackage.findOrCreateImgPart(image::getBytes, true);
        var relId = imgPart.relationship().getId();

        var factory = new org.docx4j.dml.ObjectFactory();
        var blipFill = factory.createCTBlipFillProperties();
        var blip = factory.createCTBlip();
        blip.setEmbed(relId);
        blipFill.setBlip(blip);

        var stretch = factory.createCTStretchInfoProperties();
        stretch.setFillRect(factory.createCTRelativeRect());
        blipFill.setStretch(stretch);

        shape.setBlipFill(blipFill);
        shape.setTxBody(null);
    }

}
