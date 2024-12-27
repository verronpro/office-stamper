package pro.verron.officestamper.experimental;

import org.xlsx4j.sml.CTRst;
import pro.verron.officestamper.api.Placeholder;

/// The ExcelParagraph class represents a paragraph in an Excel document.
/// It provides methods to replace expressions and retrieve the aggregated text over all runs.
public class ExcelParagraph {
    private final CTRst paragraph;

    /// Constructs a new ParagraphWrapper for the given paragraph.
    ///
    /// @param paragraph the paragraph to wrap.
    public ExcelParagraph(CTRst paragraph) {
        this.paragraph = paragraph;
    }

    /// Replaces the given expression with the replacement object within
    /// the paragraph.
    /// The replacement object must be a valid DOCX4J Object.
    ///
    /// @param placeholder the expression to be replaced.
    /// @param replacement the object to replace the expression.
    public void replace(Placeholder placeholder, String replacement) {
        var ctXstringWhitespace = paragraph.getT();
        var string = ctXstringWhitespace.getValue();
        var start = string.indexOf(placeholder.expression());
        var end = start + placeholder.expression()
                                     .length();
        var next = string.substring(0, start) + replacement + string.substring(end);
        ctXstringWhitespace.setValue(next);
    }

    @Override
    public String toString() {
        return asString();
    }

    /// Returns the aggregated text over all runs.
    ///
    /// @return the text of all runs.
    public String asString() {
        return paragraph.getR() + ": " + paragraph.getT()
                                                  .getValue();
    }
}
