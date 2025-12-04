package pro.verron.officestamper.experimental;

import org.docx4j.dml.CTRegularTextRun;
import org.docx4j.dml.CTTextParagraph;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.PresentationMLPackage;
import org.springframework.expression.spel.SpelParserConfiguration;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import pro.verron.officestamper.api.Insert;
import pro.verron.officestamper.api.OfficeStamper;
import pro.verron.officestamper.api.OfficeStamperException;

import java.io.OutputStream;
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
    public void stamp(PresentationMLPackage template, Object context, OutputStream outputStream)
            throws OfficeStamperException {
        Class<CTTextParagraph> ctTextParagraphClass = CTTextParagraph.class;
        List<CTTextParagraph> ctTextParagraphs = PowerpointCollector.collect(template, ctTextParagraphClass);
        for (CTTextParagraph paragraph : ctTextParagraphs) {
            PowerpointParagraph paragraph1 = new PowerpointParagraph(new PptxPart(template), paragraph);
            String string = paragraph1.asString();
            for (var variable : Placeholders.findVariables(string)) {
                var replacement = new CTRegularTextRun();
                var evaluationContext = new StandardEvaluationContext(context);
                var parserConfiguration = new SpelParserConfiguration();
                var parser = new SpelExpressionParser(parserConfiguration);
                var expression = parser.parseExpression(variable.content());
                var value = expression.getValue(evaluationContext);

                replacement.setT((String) value);
                var expression1 = variable.expression();
                paragraph1.replace(expression1, new Insert(replacement));
            }

        }
        try {
            template.save(outputStream);
        } catch (Docx4JException e) {
            throw new OfficeStamperException(e);
        }
    }
}
