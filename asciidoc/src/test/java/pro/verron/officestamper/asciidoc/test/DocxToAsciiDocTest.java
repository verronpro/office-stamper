package pro.verron.officestamper.asciidoc.test;

import org.junit.jupiter.api.Test;
import pro.verron.officestamper.asciidoc.AsciiDocCompiler;
import pro.verron.officestamper.asciidoc.AsciiDocModel;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static pro.verron.officestamper.asciidoc.AsciiDocModel.*;

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
        var pkg = AsciiDocCompiler.toDocx(model);
        String result = AsciiDocCompiler.toAsciidoc(pkg);

        String expected = """
                |===
                a|Nested:
                
                |===
                |A
                |B
                |===
                |Other
                |===
                
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
        var pkg = AsciiDocCompiler.toDocx(model);
        String result = AsciiDocCompiler.toAsciidoc(pkg);

        String expected = """
                |===
                a|P1
                
                P2
                |===
                
                """;

        assertEquals(expected, result);
    }
}
