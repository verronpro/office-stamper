package pro.verron.docxstamper.core;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.SpreadsheetMLPackage;
import org.springframework.expression.spel.SpelParserConfiguration;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.xlsx4j.sml.CTRst;
import pro.verron.docxstamper.api.OfficeStamper;
import pro.verron.docxstamper.api.OfficeStamperException;

import java.io.OutputStream;

import static pro.verron.docxstamper.core.Placeholders.findVariables;

/**
 * TODO: javadoc
 */
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
            throw new RuntimeException(e);
        }
    }
}
