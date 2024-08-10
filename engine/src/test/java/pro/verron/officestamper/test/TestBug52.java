package pro.verron.officestamper.test;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static pro.verron.officestamper.preset.OfficeStamperConfigurations.standardWithPreprocessing;

class TestBug52 {

    @Test
    void test_empty() {
        var stamperConfiguration = standardWithPreprocessing();
        var stamper = new TestDocxStamper<>(stamperConfiguration);
        var templateStream = TestUtils.getResource(Path.of("#52.docx"));

        record Element(boolean condition) {}
        record Context(List<Element> elements) {}
        var context = new Context(Collections.emptyList());
        var actual = stamper.stampAndLoadAndExtract(templateStream, context);
        assertEquals("", actual);
    }


    @Test
    void test_repeat_once_true() {
        var stamperConfiguration = standardWithPreprocessing();
        var stamper = new TestDocxStamper<>(stamperConfiguration);
        var templateStream = TestUtils.getResource(Path.of("#52.docx"));

        record Element(boolean condition) {}
        record Context(List<Element> elements) {}
        var context = new Context(List.of(new Element(true)));
        var actual = stamper.stampAndLoadAndExtract(templateStream, context);
        var expected = """
                Start
                Hello, World!
                End
                """;
        assertEquals(expected, actual);
    }


    @Test
    void test_repeat_once_false() {
        var stamperConfiguration = standardWithPreprocessing();
        var stamper = new TestDocxStamper<>(stamperConfiguration);
        var templateStream = TestUtils.getResource(Path.of("#52.docx"));
        var expected = """
                Start
                End
                """;
        record Element(boolean condition) {}
        record Context(List<Element> elements) {}
        var context = new Context(List.of(new Element(false)));
        var actual = stamper.stampAndLoadAndExtract(templateStream, context);
        assertEquals(expected, actual);
    }

    @Test
    void test_repeat_twice_true_true() {
        var stamperConfiguration = standardWithPreprocessing();
        var stamper = new TestDocxStamper<>(stamperConfiguration);
        var templateStream = TestUtils.getResource(Path.of("#52.docx"));

        var expected = """
                Start
                Hello, World!
                End
                Start
                Hello, World!
                End
                """;
        record Element(boolean condition) {}
        record Context(List<Element> elements) {}
        var context = new Context(List.of(new Element(true), new Element(true)));
        var actual = stamper.stampAndLoadAndExtract(templateStream, context);
        assertEquals(expected, actual);
    }

    @Test
    void test_repeat_twice_true_false() {
        var stamperConfiguration = standardWithPreprocessing();
        var stamper = new TestDocxStamper<>(stamperConfiguration);
        var templateStream = TestUtils.getResource(Path.of("#52.docx"));

        var expected = """
                Start
                Hello, World!
                End
                Start
                End
                """;
        record Element(boolean condition) {}
        record Context(List<Element> elements) {}
        var context = new Context(List.of(new Element(true), new Element(false)));
        var actual = stamper.stampAndLoadAndExtract(templateStream, context);
        assertEquals(expected, actual);
    }

    @Test
    void test_repeat_twice_false_true() {
        var stamperConfiguration = standardWithPreprocessing();
        var stamper = new TestDocxStamper<>(stamperConfiguration);
        var templateStream = TestUtils.getResource(Path.of("#52.docx"));

        var expected = """
                Start
                End
                Start
                Hello, World!
                End
                """;
        record Element(boolean condition) {}
        record Context(List<Element> elements) {}
        var context = new Context(List.of(new Element(false), new Element(true)));
        var actual = stamper.stampAndLoadAndExtract(templateStream, context);
        assertEquals(expected, actual);
    }

    @Test
    void test_repeat_twice_false_false() {
        var stamperConfiguration = standardWithPreprocessing();
        var stamper = new TestDocxStamper<>(stamperConfiguration);
        var templateStream = TestUtils.getResource(Path.of("#52.docx"));

        var expected = """
                Start
                End
                Start
                End
                """;
        record Element(boolean condition) {}
        record Context(List<Element> elements) {}
        var context = new Context(List.of(new Element(false), new Element(false)));
        var actual = stamper.stampAndLoadAndExtract(templateStream, context);
        assertEquals(expected, actual);
    }
}
