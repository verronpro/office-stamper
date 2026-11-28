package pro.verron.officestamper.test;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import pro.verron.officestamper.api.SecurityMode;
import pro.verron.officestamper.test.utils.ContextFactory;

import java.nio.file.Path;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.argumentSet;
import static pro.verron.officestamper.test.utils.OfficeStamperTestBase.toAsciidoc;
import static pro.verron.officestamper.preset.OfficeStampers.docxStamper;
import static pro.verron.officestamper.test.utils.ContextFactory.mapContextFactory;
import static pro.verron.officestamper.test.utils.ContextFactory.objectContextFactory;
import static pro.verron.officestamper.test.utils.ResourceUtils.getWordResource;

class SpelInstantiationTest {

    static Stream<Arguments> factories() {
        return Stream.of(argumentSet("obj", objectContextFactory()), argumentSet("map", mapContextFactory()));
    }

    @DisplayName("Keep spel instantiation features")
    @MethodSource("factories")
    @ParameterizedTest
    void testDateInstantiationAndResolution(ContextFactory factory) {
        var configuration = Flavor.get().full().setSpelSecurityMode(SecurityMode.PERMISSIVE);
        var stamper = docxStamper(configuration);
        var template = getWordResource(Path.of("date.docx"));
        var context = factory.empty();
        var wordprocessingMLPackage = stamper.stamp(template, context);
        var actual = toAsciidoc(wordprocessingMLPackage);
        var expected = """
                01.01.1970
                
                2000-01-01
                
                12:00:00
                
                2000-01-01T12:00:00
                
                """;
        assertEquals(expected, actual);
    }
}
