package pro.verron.officestamper.asciidoc.test;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import pro.verron.officestamper.asciidoc.AsciiDocModel;
import pro.verron.officestamper.asciidoc.AsciiDocModel.Heading;
import pro.verron.officestamper.asciidoc.AsciiDocModel.Paragraph;
import pro.verron.officestamper.asciidoc.AsciiDocParser;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/// Unit test class for the `AsciiDocParser`. This class contains a suite of test cases that verify the functionality
/// and correctness of the `AsciiDocParser.parse` method. Each test case isolates specific scenarios and checks the
/// parser's output against expected results. Responsibilities:
/// - Validate the behavior when parsing `null`, empty, or blank AsciiDoc input.
/// - Test parsing of singular and hierarchical heading structures within AsciiDoc documents.
/// - Verify the parsing of a single paragraph and handling of inline text.
/// - Check the construction and structure of AsciiDoc tables, including rows and columns. Purpose:
/// - Ensure the `AsciiDocParser` handles a variety of input cases correctly.
/// - Test the fidelity of the parsed `AsciiDocModel` against the structural elements from the input. Test Scenarios:
/// - Parsing `null` input should return an empty model.
/// - Parsing an empty or blank AsciiDoc string should result in an empty model.
/// - Verify correct parsing of a single heading and associated text.
/// - Validate the parsing of multiple headings with hierarchical levels.
/// - Test the correct parsing of paragraphs containing inline text.
/// - Confirm the correct parsing of a simple AsciiDoc table with header and data rows.
class AsciiDocParserTest {

    /// Tests for the parse method in [AsciiDocParser]. This method is responsible for converting an AsciiDoc string
    /// into an [AsciiDocModel].
    @NullSource
    @EmptySource
    @ValueSource(strings = {"   "})
    @ParameterizedTest
    void parse_shouldReturnEmptyModel_whenInputIsNull(String asciidoc) {
        // Act
        AsciiDocModel result = AsciiDocParser.parse(asciidoc);

        // Assert
        assertNotNull(result);
        var blocks = result.getBlocks();
        assertTrue(blocks.isEmpty());
    }

    @Test
    void parse_shouldParseSingleHeading() {
        // Arrange
        String asciidoc = """
                = Title
                
                == Heading Level 1
                
                """;

        // Act
        AsciiDocModel result = AsciiDocParser.parse(asciidoc);

        // Assert
        assertNotNull(result);
        var blocks = result.getBlocks();
        assertEquals(1, blocks.size());
        var heading = assertInstanceOf(Heading.class, blocks.getFirst());
        assertEquals(1, heading.level());
        var inlines = heading.inlines();
        assertEquals("Heading Level 1",
                inlines.getFirst()
                       .text());
    }

    @Test
    void parse_shouldParseMultipleHeadings() {
        // Arrange
        String asciidoc = """
                = Title
                
                == Heading Level 1
                
                === Heading Level 2
                
                ==== Heading Level 3
                
                """;

        // Act
        AsciiDocModel result = AsciiDocParser.parse(asciidoc);

        // Assert
        assertNotNull(result);
        var blocks = result.getBlocks();
        assertEquals(3, blocks.size());

        Heading heading1 = (Heading) blocks.getFirst();
        assertEquals(1, heading1.level());
        assertEquals("Heading Level 1",
                heading1.inlines()
                        .getFirst()
                        .text());

        Heading heading2 = (Heading) blocks.get(1);
        assertEquals(2, heading2.level());
        assertEquals("Heading Level 2",
                heading2.inlines()
                        .getFirst()
                        .text());

        Heading heading3 = (Heading) blocks.get(2);
        assertEquals(3, heading3.level());
        assertEquals("Heading Level 3",
                heading3.inlines()
                        .getFirst()
                        .text());
    }

    @Test
    void parse_shouldParseParagraph() {
        // Arrange
        String asciidoc = "This is a simple paragraph.";

        // Act
        AsciiDocModel result = AsciiDocParser.parse(asciidoc);

        // Assert
        assertNotNull(result);
        var blocks = result.getBlocks();
        assertEquals(1, blocks.size());

        var paragraph = assertInstanceOf(Paragraph.class, blocks.getFirst());
        var inlines = paragraph.inlines();
        var first = inlines.getFirst();
        assertEquals("This is a simple paragraph.", first.text());
    }


    @Test
    void parse_shouldParseTable() {
        // Arrange
        String asciidoc = """
                |===
                |Column 1 |Column 2
                |Value 1  |Value 2
                |===
                """;

        // Act
        AsciiDocModel result = AsciiDocParser.parse(asciidoc);

        // Assert
        assertNotNull(result);
        var blocks = result.getBlocks();
        assertEquals(1, blocks.size());

        AsciiDocModel.Table table = assertInstanceOf(AsciiDocModel.Table.class, blocks.getFirst());
        List<AsciiDocModel.Row> rows = table.rows();
        assertEquals(2, rows.size());

        AsciiDocModel.Row headerRow = rows.getFirst();
        var headerCells = headerRow.cells();
        assertEquals(2, headerCells.size());
        assertEquals("Column 1",
                headerCells.get(0)
                           .inlines()
                           .getFirst()
                           .text());
        assertEquals("Column 2",
                headerCells.get(1)
                           .inlines()
                           .getFirst()
                           .text());

        AsciiDocModel.Row dataRow = rows.get(1);
        var dataCells = dataRow.cells();
        assertEquals(2, dataCells.size());
        assertEquals("Value 1",
                dataCells.get(0)
                         .inlines()
                         .getFirst()
                         .text());
        assertEquals("Value 2",
                dataCells.get(1)
                         .inlines()
                         .getFirst()
                         .text());
    }
}
