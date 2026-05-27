package pro.verron.officestamper.asciidoc.docx;

import org.junit.jupiter.api.Test;
import pro.verron.officestamper.asciidoc.converters.AsciiDocToText;
import pro.verron.officestamper.asciidoc.core.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DocxToAsciiDocTest {

    @Test
    void shouldRenderNestedTable() {
        // Create a model with a nested table
        var textA = new Text("A");
        var textB = new Text("B");
        var cellA = Cell.ofInlines(List.of(textA));
        var cellB = Cell.ofInlines(List.of(textB));
        var row = new Row(List.of(cellA, cellB));
        var nestedTable = new Table(List.of(row));

        var rootCell = new Cell(List.of(new Paragraph(List.of(new Text("Nested:"))), nestedTable));
        var otherCell = Cell.ofInlines(List.of(new Text("Other")));
        var rootRow = new Row(List.of(rootCell, otherCell));
        var rootTable = new Table(List.of(rootRow));

        var model = AsciiDocModel.of(List.of(rootTable));

        // Convert to DOCX and back to AsciiDoc
        var pkg = new AsciiDocToDocx().apply(model);
        var result = new AsciiDocToText(false).apply(new DocxToAsciiDoc(pkg).apply(pkg));

        var expected = """
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
        var textP1 = new Text("P1");
        var textP2 = new Text("P2");
        var paragraphP1 = new Paragraph(List.of(textP1));
        var paragraphP2 = new Paragraph(List.of(textP2));
        var cell = new Cell(List.of(paragraphP1, paragraphP2));
        var row = new Row(List.of(cell));
        var rootTable = new Table(List.of(row));

        var model = AsciiDocModel.of(List.of(rootTable));
        var pkg = new AsciiDocToDocx().apply(model);
        var result = new AsciiDocToText(false).apply(new DocxToAsciiDoc(pkg).apply(pkg));

        var expected = """
                |===
                a|P1
                
                P2
                |===
                
                // section {pgMar={bottom=1440, left=1440, right=1440, top=1440}, pgSz={code=9, h=16839, w=11907}}
                
                """;

        assertEquals(expected, result);
    }
}
