package pro.verron.officestamper.api;

import org.springframework.expression.EvaluationContext;

public interface PlaceholderReplacer {
    Insert resolve(DocxPart docxPart, String expression, EvaluationContext context);
}
