package pro.verron.officestamper.test;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.file.Path;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.argumentSet;
import static pro.verron.officestamper.preset.EvaluationContextConfigurers.noopConfigurer;
import static pro.verron.officestamper.preset.OfficeStamperConfigurations.standard;
import static pro.verron.officestamper.test.ContextFactory.mapContextFactory;
import static pro.verron.officestamper.test.ContextFactory.objectContextFactory;

class SpelInstantiationTest {

    static Stream<Arguments> factories() {
        return Stream.of(argumentSet("obj", objectContextFactory()), argumentSet("map", mapContextFactory()));
    }

    @DisplayName("Keep spel instantiation features")
    @MethodSource("factories")
    @ParameterizedTest
    void testDateInstantiationAndResolution(ContextFactory factory) {
        var stamperConfiguration = standard().setEvaluationContextConfigurer(noopConfigurer());
        var stamper = new TestDocxStamper<>(stamperConfiguration);
        var templateStream = TestUtils.getResource(Path.of("date.docx"));
        var context = factory.empty();
        var actual = stamper.stampAndLoadAndExtract(templateStream, context);
        var expected = """
                01.01.1970
                2000-01-01
                12:00:00
                2000-01-01T12:00:00
                """;
        assertEquals(expected, actual);
    }
}
