package pro.verron.officestamper.experimental;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.SpreadsheetMLPackage;
import org.springframework.expression.spel.SpelParserConfiguration;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.xlsx4j.sml.CTRst;
import pro.verron.officestamper.api.OfficeStamper;
import pro.verron.officestamper.api.OfficeStamperException;

import java.io.OutputStream;

import static pro.verron.officestamper.core.Placeholders.findVariables;

/// The ExcelStamper class is an implementation of the OfficeStamper interface for stamping Excel templates.
/// It uses the DOCX4J library to manipulate the template and replace variable expressions with values from the context.
public class ExcelStamper
        implements OfficeStamper<SpreadsheetMLPackage> {

    @Override
    public void stamp(
            SpreadsheetMLPackage template,
            Object context,
            OutputStream outputStream
    )
            throws OfficeStamperException {
        var paragraphs = ExcelCollector.collect(template, CTRst.class);
        for (CTRst cell : paragraphs) {
            var paragraph = new ExcelParagraph(cell);
            var string = paragraph.asString();
            for (var variable : findVariables(string)) {
                var evaluationContext = new StandardEvaluationContext(context);
                var parserConfiguration = new SpelParserConfiguration();
                var parser = new SpelExpressionParser(parserConfiguration);
                var expression = parser.parseExpression(variable.content());
                var value = expression.getValue(evaluationContext);
                var stringValue = String.valueOf(value);
                paragraph.replace(variable, stringValue);
            }
        }
        try {
            template.save(outputStream);
        } catch (Docx4JException e) {
            throw new OfficeStamperException(e);
        }
    }
}
