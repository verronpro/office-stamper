package pro.verron.officestamper.asciidoc.test;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import pro.verron.officestamper.asciidoc.AsciiDocModel;
import pro.verron.officestamper.asciidoc.AsciiDocModel.*;
import pro.verron.officestamper.asciidoc.AsciiDocParser;

import static org.junit.jupiter.api.Assertions.*;

class AsciiDocParserTest {

    @NullSource
    @EmptySource
    @ValueSource(strings = {"   "})
    @ParameterizedTest
    void parse_shouldReturnEmptyModel_whenInputIsNull(String asciidoc) {
        AsciiDocModel result = AsciiDocParser.parse(asciidoc);
        assertNotNull(result);
        assertTrue(result.getBlocks()
                         .isEmpty());
    }

    @Test
    void parse_shouldParseParagraph() {
        String asciidoc = "This is a simple paragraph.";
        AsciiDocModel result = AsciiDocParser.parse(asciidoc);
        var blocks = result.getBlocks();
        assertEquals(1, blocks.size());
        var paragraph = assertInstanceOf(Paragraph.class, blocks.getFirst());
        assertEquals("This is a simple paragraph.",
                paragraph.inlines()
                         .getFirst()
                         .text());
    }

    @Test
    void parse_shouldParseHeading() {
        String asciidoc = """
                = Heading 1
                
                == Heading 2""";
        AsciiDocModel result = AsciiDocParser.parse(asciidoc);
        var blocks = result.getBlocks();
        assertEquals(2, blocks.size());

        var h1 = assertInstanceOf(Heading.class, blocks.get(0));
        assertEquals(1, h1.level());
        assertEquals("Heading 1",
                h1.inlines()
                  .getFirst()
                  .text());

        var h2 = assertInstanceOf(Heading.class, blocks.get(1));
        assertEquals(2, h2.level());
        assertEquals("Heading 2",
                h2.inlines()
                  .getFirst()
                  .text());
    }

    @Test
    void parse_shouldParseUnorderedList() {
        String asciidoc = """
                * Item 1
                * Item 2""";
        AsciiDocModel result = AsciiDocParser.parse(asciidoc);
        var blocks = result.getBlocks();
        assertEquals(1, blocks.size());

        var list = assertInstanceOf(UnorderedList.class, blocks.getFirst());
        assertEquals(2,
                list.items()
                    .size());
        assertEquals("Item 1",
                list.items()
                    .get(0)
                    .inlines()
                    .getFirst()
                    .text());
        assertEquals("Item 2",
                list.items()
                    .get(1)
                    .inlines()
                    .getFirst()
                    .text());
    }

    @Test
    void parse_shouldParseOrderedList() {
        String asciidoc = """
                . Item 1
                . Item 2""";
        AsciiDocModel result = AsciiDocParser.parse(asciidoc);
        var blocks = result.getBlocks();
        assertEquals(1, blocks.size());

        var list = assertInstanceOf(OrderedList.class, blocks.getFirst());
        assertEquals(2,
                list.items()
                    .size());
        assertEquals("Item 1",
                list.items()
                    .get(0)
                    .inlines()
                    .getFirst()
                    .text());
        assertEquals("Item 2",
                list.items()
                    .get(1)
                    .inlines()
                    .getFirst()
                    .text());
    }

    @Test
    void parse_shouldParseTable() {
        String asciidoc = """
                |===
                |Column 1 |Column 2
                |Value 1  |Value 2
                |===
                """;
        AsciiDocModel result = AsciiDocParser.parse(asciidoc);
        var blocks = result.getBlocks();
        assertEquals(1, blocks.size());

        var table = assertInstanceOf(Table.class, blocks.getFirst());
        var rows = table.rows();
        assertEquals(2, rows.size());

        assertEquals("Column 1",
                ((Paragraph) rows.get(0)
                                 .cells()
                                 .get(0)
                                 .blocks()
                                 .getFirst()).inlines()
                                             .getFirst()
                                             .text());
        assertEquals("Column 2",
                ((Paragraph) rows.get(0)
                                 .cells()
                                 .get(1)
                                 .blocks()
                                 .getFirst()).inlines()
                                             .getFirst()
                                             .text());
        assertEquals("Value 1",
                ((Paragraph) rows.get(1)
                                 .cells()
                                 .get(0)
                                 .blocks()
                                 .getFirst()).inlines()
                                             .getFirst()
                                             .text());
        assertEquals("Value 2",
                ((Paragraph) rows.get(1)
                                 .cells()
                                 .get(1)
                                 .blocks()
                                 .getFirst()).inlines()
                                             .getFirst()
                                             .text());
    }

    @Test
    void parse_shouldParseInlines() {
        String asciidoc = "Text with *bold*, _italic_, and a http://example.com[Link].";
        AsciiDocModel result = AsciiDocParser.parse(asciidoc);
        var blocks = result.getBlocks();
        var paragraph = assertInstanceOf(Paragraph.class, blocks.getFirst());
        var inlines = paragraph.inlines();

        assertEquals("Text with ",
                inlines.get(0)
                       .text());
        assertInstanceOf(Bold.class, inlines.get(1));
        assertEquals("bold",
                inlines.get(1)
                       .text());
        assertEquals(", ",
                inlines.get(2)
                       .text());
        assertInstanceOf(Italic.class, inlines.get(3));
        assertEquals("italic",
                inlines.get(3)
                       .text());
        assertEquals(", and a ",
                inlines.get(4)
                       .text());
        var link = assertInstanceOf(Link.class, inlines.get(5));
        assertEquals("http://example.com", link.url());
        assertEquals("Link", link.text());
        assertEquals(".",
                inlines.get(6)
                       .text());
    }

    @Test
    void parse_shouldParseBlockquote() {
        String asciidoc = """
                ____
                This is a quote.
                ____
                """;
        AsciiDocModel result = AsciiDocParser.parse(asciidoc);
        var blocks = result.getBlocks();
        assertEquals(1, blocks.size());
        var quote = assertInstanceOf(Blockquote.class, blocks.getFirst());
        assertEquals("This is a quote.",
                quote.inlines()
                     .getFirst()
                     .text());
    }

    @Test
    void parse_shouldParseCodeBlock() {
        String asciidoc = """
                [source,java]
                ----
                public class Main {}
                ----
                """;
        AsciiDocModel result = AsciiDocParser.parse(asciidoc);
        var blocks = result.getBlocks();
        assertEquals(1, blocks.size());
        var code = assertInstanceOf(CodeBlock.class, blocks.getFirst());
        assertEquals("java", code.language());
        assertEquals("public class Main {}", code.content());
    }

    @Test
    void parse_shouldParseImageBlock() {
        String asciidoc = "image::path/to/img.png[Alt Text]";
        AsciiDocModel result = AsciiDocParser.parse(asciidoc);
        var blocks = result.getBlocks();
        assertEquals(1, blocks.size());
        var image = assertInstanceOf(ImageBlock.class, blocks.getFirst());
        assertEquals("path/to/img.png", image.url());
        assertEquals("Alt Text", image.altText());
    }
}
