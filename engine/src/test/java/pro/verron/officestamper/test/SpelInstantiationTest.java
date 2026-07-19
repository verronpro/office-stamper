package pro.verron.officestamper.test;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import pro.verron.officestamper.test.utils.ContextFactory;
import pro.verron.officestamper.test.utils.OfficeStamperTestBase;

import java.nio.file.Path;

import static pro.verron.officestamper.preset.EvaluationContextFactories.noopFactory;
import static pro.verron.officestamper.preset.OfficeStamperConfigurations.full;
import static pro.verron.officestamper.test.utils.ResourceUtils.getWordResource;

class SpelInstantiationTest extends OfficeStamperTestBase {

    @DisplayName("Keep spel instantiation features")
    @MethodSource("factories")
    @ParameterizedTest
    void testDateInstantiationAndResolution(ContextFactory factory) {
        var configuration = full().setEvaluationContextFactory(noopFactory());
        var template = getWordResource(Path.of("date.docx"));
        var context = factory.empty();
        var expected = """
                01.01.1970
                
                2000-01-01
                
                12:00:00
                
                2000-01-01T12:00:00
                
                // section {docGrid={linePitch=360}, pgMar={bottom=1417, footer=708, header=708, left=1417, right=1417, top=1417}, pgSz={h=16838, w=11906}, space=708}
                
                """;
        testStamper(configuration, context, template, expected);
    }
}
