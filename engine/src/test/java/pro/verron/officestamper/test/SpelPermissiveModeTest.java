package pro.verron.officestamper.test;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import pro.verron.officestamper.api.SecurityMode;
import pro.verron.officestamper.test.utils.ContextFactory;
import pro.verron.officestamper.test.utils.OfficeStamperTestBase;
import pro.verron.officestamper.test.utils.ResourceUtils;

import java.nio.file.Path;

import static pro.verron.officestamper.preset.OfficeStamperConfigurations.full;
import static pro.verron.officestamper.test.utils.ResourceUtils.getWordResource;

class SpelPermissiveModeTest extends OfficeStamperTestBase {

    @DisplayName("Permissive SpEL mode allows type access (e.g., T(...)) and constructor usage")
    @MethodSource("factories")
    @ParameterizedTest
    void permissiveMode_allowsTypeAndConstructorFeatures(ContextFactory factory) {
        var config = full().setSpelSecurityMode(SecurityMode.PERMISSIVE);
        var context = factory.empty();
        var template = getWordResource(Path.of("date.docx"));
        // Same expected output as in SpelInstantiationTest (validating T(...) and date/constructor-like features)
        var expected = """
                01.01.1970
                
                2000-01-01
                
                12:00:00
                
                2000-01-01T12:00:00
                
                // section {docGrid={linePitch=360}, pgMar={bottom=1417, footer=708, header=708, left=1417, right=1417, top=1417}, pgSz={h=16838, w=11906}, space=708}
                
                """;
        testStamper(config, context, template, expected);
    }
}
