package pro.verron.officestamper.test;

import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.api.function.ThrowingSupplier;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import pro.verron.officestamper.api.ExceptionResolver;
import pro.verron.officestamper.api.OfficeStamperException;
import pro.verron.officestamper.api.StringResolver;
import pro.verron.officestamper.preset.ExceptionResolvers;
import pro.verron.officestamper.preset.OfficeStamperConfigurations;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static pro.verron.officestamper.test.TestUtils.getResource;

class ResolutionTest {

    public static final ContextFactory FACTORY = ContextFactory.objectContextFactory();

    /// This method is a unit test for the `testStaticResolution` method. It uses parameterized testing with a CSV
    /// source
    /// to test various scenarios.
    ///
    /// @param template         The template path
    /// @param shouldFail       A boolean indicating whether the test should fail on unresolved expression
    /// @param emptyOnError     A boolean indicating whether the test should leave empty on expression error
    /// @param shouldReplace    A boolean indicating whether the test should replace unresolved expressions
    /// @param replacementValue The replacement value for unresolved expressions
    /// @param expected         The expected result of the test
    @CsvSource({
            "system.exit.docx,false,false,false,Unresolved,${T(java.lang.System).exit(0)}",
            "system.exit.docx,false,false,true,Unresolved,Unresolved",
            "system.exit.docx,false,true,false,Unresolved,''",
            "system.exit.docx,false,true,true,Unresolved,''",
            "system.exit.docx,true,false,false,Unresolved,Should fail",
            "system.exit.docx,true,false,true,Unresolved,Should fail",
            "system.exit.docx,true,true,false,Unresolved,Should fail",
            "system.exit.docx,true,true,true,Unresolved,Should fail",
    })
    @ParameterizedTest
    void testStaticResolution(
            String template,
            boolean shouldFail,
            boolean emptyOnError,
            boolean shouldReplace,
            String replacementValue,
            String expected
    ) {
        var resource = getResource(Path.of(template));

        var configuration = OfficeStamperConfigurations.standard();
        configuration.setExceptionResolver(legacyBehavior(shouldFail, emptyOnError, shouldReplace, replacementValue));

        var stamper = new TestDocxStamper<>(configuration);
        if (shouldFail) {
            Executable executable = () -> stamper.stampAndLoadAndExtract(resource, FACTORY.empty());
            assertThrows(OfficeStamperException.class, executable);
        }
        else {
            ThrowingSupplier<String> supplier = () -> stamper.stampAndLoadAndExtract(resource, FACTORY.empty());
            assertEquals(expected + "\n", assertDoesNotThrow(supplier));
        }
    }

    public static ExceptionResolver legacyBehavior(
            boolean shouldFail,
            boolean emptyOnError,
            boolean shouldReplace,
            String replacementValue
    ) {
        if (shouldFail) return ExceptionResolvers.throwing();
        if (emptyOnError) return ExceptionResolvers.defaulting("");
        if (shouldReplace) return ExceptionResolvers.defaulting(replacementValue);
        return ExceptionResolvers.passing();
    }

    /// This method is a unit test for the `testCustomResolution` method. It uses parameterized testing with a CSV
    /// source
    /// to test various scenarios.
    ///
    /// @param template                   The template path
    /// @param shouldFail                 A boolean indicating whether the test should fail on unresolved expression
    /// @param emptyOnError               A boolean indicating whether the test should leave empty on expression error
    /// @param shouldReplace              A boolean indicating whether the test should replace unresolved expressions
    /// @param withResolver               A boolean indicating whether the custom type resolver is added to the
    ///                                                                     configuration
    /// @param withValue                  A boolean indicating whether a custom type value is set, or is null in the
    ///                                                                     context
    /// @param unresolvedReplacementValue The replacement value for unresolved expressions
    /// @param expectedFail               A boolean indicating whether the test is expected to fail
    /// @param expected                   The expected result of the test
    @CsvSource({
            // template, failOnUnresolved, emptyOnError, shouldReplaceUnresolved, withResolver, withValue,
            // unresolvedReplacementValue, expectedFail, expected
            /* 01 */"CustomType.docx,false,false,false,false,false,Unresolved,false,${value}",
            /* 02 */"CustomType.docx,false,false,false,false,true,Unresolved,false,${value}",
            /* 03 */"CustomType.docx,false,false,false,true,false,Unresolved,false,${value}",
            /* 04 */"CustomType.docx,false,false,false,true,true,Unresolved,false,Custom",
            /* 05 */"CustomType.docx,false,false,true,false,false,Unresolved,false,Unresolved",
            /* 06 */"CustomType.docx,false,false,true,false,true,Unresolved,false,Unresolved",
            /* 07 */"CustomType.docx,false,false,true,true,false,Unresolved,false,Unresolved",
            /* 08 */"CustomType.docx,false,false,true,true,true,Unresolved,false,Custom",
            /* 09 */"CustomType.docx,false,true,false,false,false,Unresolved,false,''",
            /* 10 */"CustomType.docx,false,true,false,false,true,Unresolved,false,''",
            /* 11 */"CustomType.docx,false,true,false,true,false,Unresolved,false,''",
            /* 12 */"CustomType.docx,false,true,false,true,true,Unresolved,false,Custom",
            /* 13 */"CustomType.docx,false,true,true,false,false,Unresolved,false,''",
            /* 14 */"CustomType.docx,false,true,true,false,true,Unresolved,false,''",
            /* 15 */"CustomType.docx,false,true,true,true,false,Unresolved,false,''",
            /* 16 */"CustomType.docx,false,true,true,true,true,Unresolved,false,Custom",
            /* 17 */"CustomType.docx,true,false,false,false,false,Unresolved,true,Should fail",
            /* 18 */"CustomType.docx,true,false,false,false,true,Unresolved,true,Should fail",
            /* 19 */"CustomType.docx,true,false,false,true,false,Unresolved,true,Should fail",
            /* 20 */"CustomType.docx,true,false,false,true,true,Unresolved,false,Custom",
            /* 21 */"CustomType.docx,true,false,true,false,false,Unresolved,true,Should fail",
            /* 22 */"CustomType.docx,true,false,true,false,true,Unresolved,true,Should fail",
            /* 23 */"CustomType.docx,true,false,true,true,false,Unresolved,true,Should fail",
            /* 24 */"CustomType.docx,true,false,true,true,true,Unresolved,false,Custom",
            /* 25 */"CustomType.docx,true,true,false,false,false,Unresolved,true,Should fail",
            /* 26 */"CustomType.docx,true,true,false,false,true,Unresolved,true,Should fail",
            /* 27 */"CustomType.docx,true,true,false,true,false,Unresolved,true,Should fail",
            /* 28 */"CustomType.docx,true,true,false,true,true,Unresolved,false,Custom",
            /* 29 */"CustomType.docx,true,true,true,false,false,Unresolved,true,Should fail",
            /* 30 */"CustomType.docx,true,true,true,false,true,Unresolved,true,Should fail",
            /* 31 */"CustomType.docx,true,true,true,true,false,Unresolved,true,Should fail",
            /* 32 */"CustomType.docx,true,true,true,true,true,Unresolved,false,Custom",
    })
    @ParameterizedTest
    void testCustomResolution(
            String template,
            boolean shouldFail,
            boolean emptyOnError,
            boolean shouldReplace,
            boolean withResolver,
            boolean withValue,
            String unresolvedReplacementValue,
            boolean expectedFail,
            String expected
    ) {
        var resource = getResource(Path.of(template));

        var configuration = OfficeStamperConfigurations.raw();
        configuration.setExceptionResolver(legacyBehavior(shouldFail,
                emptyOnError,
                shouldReplace,
                unresolvedReplacementValue));
        if (withResolver) configuration.addResolver(new CustomResolver());
        CustomContext customContext = new CustomContext(withValue ? new CustomValue() : null);

        var stamper = new TestDocxStamper<>(configuration);
        if (expectedFail) {
            Executable executable = () -> stamper.stampAndLoadAndExtract(resource, customContext);
            assertThrows(OfficeStamperException.class, executable);
        }
        else {
            ThrowingSupplier<String> supplier = () -> stamper.stampAndLoadAndExtract(resource, customContext);
            assertEquals(expected + "\n", assertDoesNotThrow(supplier));
        }
    }

    private static class CustomValue {}

    private record CustomContext(CustomValue value) {}

    private static class CustomResolver
            extends StringResolver<CustomValue> {

        protected CustomResolver() {
            super(CustomValue.class);
        }

        @Override
        public String resolve(CustomValue ignored) {
            return "Custom";
        }
    }
}
