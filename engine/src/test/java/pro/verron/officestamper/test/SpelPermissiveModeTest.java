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
import static pro.verron.officestamper.asciidoc.AsciiDocCompiler.toAsciidoc;
import static pro.verron.officestamper.preset.OfficeStamperConfigurations.full;
import static pro.verron.officestamper.preset.OfficeStampers.docxPackageStamper;
import static pro.verron.officestamper.test.utils.ContextFactory.mapContextFactory;
import static pro.verron.officestamper.test.utils.ContextFactory.objectContextFactory;
import static pro.verron.officestamper.test.utils.ResourceUtils.getWordResource;

class SpelPermissiveModeTest {

    static Stream<Arguments> factories() {
        return Stream.of(argumentSet("obj", objectContextFactory()), argumentSet("map", mapContextFactory()));
    }

    @DisplayName("Permissive SpEL mode allows type access (e.g., T(...)) and constructor usage")
    @MethodSource("factories")
    @ParameterizedTest
    void permissiveMode_allowsTypeAndConstructorFeatures(ContextFactory factory) {
        var configuration = full().setSpelSecurityMode(SecurityMode.PERMISSIVE);
        var stamper = docxPackageStamper(configuration);
        var template = getWordResource(Path.of("date.docx"));
        var context = factory.empty();

        var wordprocessingMLPackage = stamper.stamp(template, context);
        var actual = toAsciidoc(wordprocessingMLPackage);

        // Same expected output as in SpelInstantiationTest (validating T(...) and date/constructor-like features)
        var expected = """
                01.01.1970
                
                2000-01-01
                
                12:00:00
                
                2000-01-01T12:00:00
                
                // section {docGrid={linePitch=360}, pgMar={bottom=1417, footer=708, header=708, left=1417, right=1417, top=1417}, pgSz={h=16838, w=11906}, space=708}
                
                """;
        assertEquals(expected, actual);
    }
}
