package pro.verron.officestamper.test;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import pro.verron.officestamper.api.OfficeStamperException;
import pro.verron.officestamper.preset.ExceptionResolvers;
import pro.verron.officestamper.preset.OfficeStamperConfigurations;
import pro.verron.officestamper.test.utils.ContextFactory;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static pro.verron.asciidoc.compiler.AsciiDocCompiler.toAsciidoc;
import static pro.verron.officestamper.preset.OfficeStampers.docxPackageStamper;
import static pro.verron.officestamper.test.utils.ContextFactory.mapContextFactory;
import static pro.verron.officestamper.test.utils.DocxFactory.makeWordResource;
import static pro.verron.officestamper.test.utils.ResourceUtils.getWordResource;


/// @author Joseph Verron
class SpelInjectionTest {

    static Stream<Arguments> factories() {
        return Stream.of(
                org.junit.jupiter.params.provider.Arguments.argumentSet("obj", ContextFactory.objectContextFactory()),
                org.junit.jupiter.params.provider.Arguments.argumentSet("map", mapContextFactory()));
    }

    @DisplayName("Ensure dangerous SpEL injection throws an error, and not execute directly")
    @MethodSource("factories")
    @ParameterizedTest
    void spelInjectionFromBinaryResourceThrows(ContextFactory factory) {
        var context = factory.empty();
        var template = getWordResource("SpelInjectionTest.docx");
        var configuration = OfficeStamperConfigurations.standard();
        var stamper = docxPackageStamper(configuration);
        assertThrows(OfficeStamperException.class, () -> stamper.stamp(template, context));
        assertDoesNotThrow(() -> "Does not throw", "Since VM is still up.");
    }

    @DisplayName("Issue #796: a malformed placeholder whose opening brace is never balanced is handed to the exception "
            + "resolver instead of being left as a stray literal")
    @Test
    void malformedPlaceholderIsResolvedByResolver() {
        var context = mapContextFactory().empty();
        var template = makeWordResource("""
                ${#{T(java.lang.System).exit(0)}

                The rest of the document keeps being processed.
                """);
        var configuration = OfficeStamperConfigurations.minimal();
        configuration.setExceptionResolver(ExceptionResolvers.defaulting("__________"));
        var stamper = docxPackageStamper(configuration);
        var result = stamper.stamp(template, context);
        var actual = toAsciidoc(result);
        assertTrue(actual.contains("__________"),
                "The malformed placeholder should be replaced by the resolver's default value");
        assertTrue(actual.contains("The rest of the document keeps being processed."),
                "The rest of the document should keep being processed");
        assertFalse(actual.contains("exit"), "The dangerous expression must never be evaluated");
        assertFalse(actual.contains("${"), "No stray '${' should be left behind");
    }

    @DisplayName("The two-braces injection attempt is still blocked and throws under the throwing resolver")
    @Test
    void wellFormedDangerousPlaceholderStillThrows() {
        var context = mapContextFactory().empty();
        var template = makeWordResource("${#{T(java.lang.System).exit(0)}}");
        var configuration = OfficeStamperConfigurations.standard();
        var stamper = docxPackageStamper(configuration);
        assertThrows(OfficeStamperException.class, () -> stamper.stamp(template, context));
        assertDoesNotThrow(() -> "Does not throw", "Since VM is still up.");
    }

    @DisplayName("SpEL inline lists can be used as placeholders (brace balancing)")
    @Test
    void inlineListPlaceholderResolves() {
        var context = mapContextFactory().empty();
        var template = makeWordResource("Numbers: ${ {1, 2, 3} }");
        var configuration = OfficeStamperConfigurations.minimal();
        var stamper = docxPackageStamper(configuration);
        var result = stamper.stamp(template, context);
        var actual = toAsciidoc(result);
        assertTrue(actual.contains("[1, 2, 3]"), "The inline list should be resolved to its string form");
        assertFalse(actual.contains("${"), "The placeholder must have been consumed");
    }

    @DisplayName("SpEL inline maps can be used as placeholders (brace balancing)")
    @Test
    void inlineMapPlaceholderResolves() {
        var context = mapContextFactory().empty();
        var template = makeWordResource("Map: ${ {'a': 1, 'b': 2} }");
        var configuration = OfficeStamperConfigurations.minimal();
        var stamper = docxPackageStamper(configuration);
        var result = stamper.stamp(template, context);
        var actual = toAsciidoc(result);
        assertTrue(actual.contains("1"), "The map value should be resolved");
        assertTrue(actual.contains("2"), "The map value should be resolved");
        assertFalse(actual.contains("${"), "The placeholder must have been consumed");
    }
}
