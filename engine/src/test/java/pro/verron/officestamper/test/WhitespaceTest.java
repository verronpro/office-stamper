package pro.verron.officestamper.test;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import pro.verron.officestamper.preset.OfficeStamperConfigurations;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static pro.verron.officestamper.test.TestUtils.makeResource;

@DisplayName("Whitespaces manipulations")
class WhitespaceTest {

    public static final ContextFactory FACTORY = new ContextFactory();

    @DisplayName("Should keep any number of spaces")
    @CsvSource(
            {
                    "Homer Simpson,Homer Simpson",
                    "Homer  Simpson,Homer  Simpson",
                    "Homer   Simpson,Homer   Simpson"
            })
    @ParameterizedTest
    void should_preserve_spaces(String in, String out)
            throws Docx4JException, IOException {
        var config = OfficeStamperConfigurations.standard();
        var template = makeResource("Space ${name}");
        var context = FACTORY.name(in);

        var stamper = new TestDocxStamper<>(config);
        var actual = stamper.stampAndLoadAndExtract(template, context);
        var expected = "Space %s\n".formatted(out);
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Should keep tabulations as tabulations")
    void should_preserve_tabulations()
            throws IOException, Docx4JException {
        var config = OfficeStamperConfigurations.standard();
        var template = makeResource("Tab|TAB|${name}");
        var context = FACTORY.name("Homer\tSimpson");

        var stamper = new TestDocxStamper<>(config);
        var actual = stamper.stampAndLoadAndExtract(template, context);
        var expected = "Tab\tHomer\tSimpson\n";
        assertEquals(expected, actual);
    }
}
