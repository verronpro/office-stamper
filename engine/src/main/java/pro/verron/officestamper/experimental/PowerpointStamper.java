package pro.verron.officestamper.experimental;

import org.docx4j.dml.CTBlip;
import org.docx4j.dml.CTBlip;
import org.docx4j.dml.CTBlipFillProperties;
import org.docx4j.dml.CTRegularTextRun;
import org.docx4j.dml.CTShapeProperties;
import org.docx4j.dml.CTTextParagraph;
import org.docx4j.openpackaging.packages.PresentationMLPackage;
import org.docx4j.openpackaging.parts.PresentationML.SlidePart;
import org.pptx4j.Pptx4jException;
import org.pptx4j.pml.Shape;
import org.springframework.expression.spel.SpelParserConfiguration;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import pro.verron.officestamper.api.Insert;
import pro.verron.officestamper.api.OfficeStamper;
import pro.verron.officestamper.api.OfficeStamperException;
import pro.verron.officestamper.preset.Image;
import pro.verron.officestamper.utils.openpackaging.OpenPackage;

import java.util.ArrayList;
import java.util.List;

/// The PowerpointStamper class implements the OfficeStamper interface to provide capability for stamping PowerPoint
/// presentations with context and writing the result to an OutputStream.
public class PowerpointStamper
        implements OfficeStamper<PresentationMLPackage> {
    /// Constructs a new instance of the PowerpointStamper class. This constructor initializes an instance of
    /// PowerpointStamper, which implements the OfficeStamper interface. The class provides functionality to apply
    /// variable-based stamping on PowerPoint templates and outputs the modified presentation.
    public PowerpointStamper() {
        // Explicit default constructor for Javadoc
    }

    @Override
    public PresentationMLPackage stamp(PresentationMLPackage template, Object context)
            throws OfficeStamperException {
        try {
            List<SlidePart> slideParts = template.getMainPresentationPart()
                                                 .getSlideParts();
            for (SlidePart slide : slideParts) {
                List<Shape> shapes = PowerpointCollector.collect(slide, Shape.class);
                for (Shape shape : shapes) {
                    processShape(slide, shape, context);
                }
            }
            return template;
        } catch (Pptx4jException e) {
            throw new OfficeStamperException(e);
        }
    }

    private void processShape(SlidePart slide, Shape shape, Object context) {
        if (shape.getTxBody() == null) return;
        List<CTTextParagraph> paragraphs = new ArrayList<>(shape.getTxBody()
                                                                .getP());
        for (CTTextParagraph paragraph : paragraphs) {
            PowerpointParagraph paragraph1 = new PowerpointParagraph(new PptxPart((PresentationMLPackage) slide.getPackage()),
                    paragraph);
            String string = paragraph1.asString();
            for (var variable : Placeholders.findVariables(string)) {
                var evaluationContext = new StandardEvaluationContext(context);
                var parserConfiguration = new SpelParserConfiguration();
                var parser = new SpelExpressionParser(parserConfiguration);
                var expression = parser.parseExpression(variable.content());
                var value = expression.getValue(evaluationContext);

                if (value instanceof Image image) {
                    fillShapeWithImage(slide, shape, image);
                    return;
                }
                else {
                    var replacement = new CTRegularTextRun();
                    replacement.setT(String.valueOf(value));
                    var expression1 = variable.expression();
                    paragraph1.replace(expression1, new Insert(replacement));
                }
            }
        }
    }

    private void fillShapeWithImage(SlidePart slide, Shape shape, Image image) {
        PresentationMLPackage presentationMLPackage = (PresentationMLPackage) slide.getPackage();
        var openPackage = OpenPackage.getOrCreate(presentationMLPackage, slide);
        var imgPart = openPackage.findOrCreateImgPart(image::getBytes, true);
        var relId = imgPart.relationship()
                           .getId();

        var factory = new org.docx4j.dml.ObjectFactory();
        var blipFill = factory.createCTBlipFillProperties();
        var blip = factory.createCTBlip();
        blip.setEmbed(relId);
        blipFill.setBlip(blip);

        var stretch = factory.createCTStretchInfoProperties();
        stretch.setFillRect(factory.createCTRelativeRect());
        blipFill.setStretch(stretch);

        if (shape.getSpPr() == null) shape.setSpPr(factory.createCTShapeProperties());
        shape.getSpPr()
             .setBlipFill(blipFill);

        shape.setTxBody(null);
    }
}
