package pro.verron.officestamper.asciidoc.docx;

import org.junit.jupiter.api.Test;
import pro.verron.officestamper.asciidoc.converters.AsciiDocToText;
import pro.verron.officestamper.asciidoc.core.AsciiDocModel;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static pro.verron.officestamper.asciidoc.core.AsciiDocModel.*;

class DocxToAsciiDocTest {

    @Test
    void shouldRenderNestedTable() {
        // Create a model with a nested table
        Table nestedTable = new Table(Row.listOf());

        Table rootTable = new Table(List.of(
                new Row(List.of(
                        new Cell(List.of(new Paragraph(List.of(new Text("Nested:"))), nestedTable)),
                        Cell.ofInlines(List.of(new Text("Other")))
                ))
        ));

        AsciiDocModel model = AsciiDocModel.of(List.of(rootTable));

        // Convert to DOCX and back to AsciiDoc
        var pkg = new AsciiDocToDocx().apply(model);
        String result = new AsciiDocToText(false).apply(new DocxToAsciiDoc(pkg).apply(pkg));

        String expected = """
                |===
                a|Nested:
                
                !===
                !A
                !B
                !===
                |Other
                |===
                
                // section {pgMar={bottom=1440, left=1440, right=1440, top=1440}, pgSz={code=9, h=16839, w=11907}}
                
                """;

        assertEquals(expected, result);
    }

    @Test
    void shouldRenderAdvancedCellWithMultipleParagraphs() {
        Table rootTable = new Table(List.of(
                new Row(List.of(
                        new Cell(List.of(
                                new Paragraph(List.of(new Text("P1"))),
                                new Paragraph(List.of(new Text("P2")))
                        ))
                ))
        ));

        AsciiDocModel model = AsciiDocModel.of(List.of(rootTable));
        var pkg = new AsciiDocToDocx().apply(model);
        String result = new AsciiDocToText(false).apply(new DocxToAsciiDoc(pkg).apply(pkg));

        String expected = """
                |===
                a|P1
                
                P2
                |===
                
                // section {pgMar={bottom=1440, left=1440, right=1440, top=1440}, pgSz={code=9, h=16839, w=11907}}
                
                """;

        assertEquals(expected, result);
    }
}
