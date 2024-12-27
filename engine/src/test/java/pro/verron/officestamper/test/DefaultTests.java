package pro.verron.officestamper.test;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.expression.spel.SpelParserConfiguration;
import pro.verron.officestamper.api.OfficeStamperConfiguration;
import pro.verron.officestamper.preset.*;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.junit.jupiter.params.provider.Arguments.of;
import static pro.verron.officestamper.preset.OfficeStamperConfigurations.standard;
import static pro.verron.officestamper.test.ContextFactory.mapContextFactory;
import static pro.verron.officestamper.test.ContextFactory.objectContextFactory;
import static pro.verron.officestamper.test.TestUtils.*;

/// DefaultTests class.
///
/// @author Joseph Verron
/// @version ${version}
/// @since 1.6.6
@DisplayName("Core Features") class DefaultTests {

    private static final Logger log = LoggerFactory.getLogger(DefaultTests.class);

    private static Stream<Arguments> tests() {
        return Stream.concat(factories().mapMulti((factory, pipe) -> {
            pipe.accept(ternary(factory));
            pipe.accept(replaceWordWithIntegrationTest(factory));
            pipe.accept(replaceNullExpressionTest(factory));
            pipe.accept(replaceNullExpressionTest2(factory));
            pipe.accept(customEvaluationContextConfigurerTest_customEvaluationContextConfigurerIsHonored(factory));
            pipe.accept(expressionReplacementInGlobalParagraphsTest(factory));
            pipe.accept(expressionReplacementInTablesTest(factory));
            pipe.accept(expressionReplacementWithFormattingTest(factory));
            pipe.accept(expressionWithSurroundingSpacesTest(factory));
            pipe.accept(expressionReplacementWithCommentTest(factory));
            pipe.accept(imageReplacementInGlobalParagraphsTest(factory));
            pipe.accept(imageReplacementInGlobalParagraphsTestWithMaxWidth(factory));
            pipe.accept(leaveEmptyOnExpressionErrorTest(factory));
            pipe.accept(lineBreakReplacementTest(factory));
            pipe.accept(mapAccessorAndReflectivePropertyAccessorTest_shouldResolveMapAndPropertyPlaceholders(factory));
            pipe.accept(nullPointerResolutionTest_testWithDefaultSpel(factory));
            pipe.accept(customCommentProcessor(factory));
            pipe.accept(controls(factory));
        }), Stream.of(nullPointerResolutionTest_testWithCustomSpel(ContextFactory.objectContextFactory())));
    }

    private static Stream<ContextFactory> factories() {
        return Stream.of(objectContextFactory(), mapContextFactory());
    }

    private static Arguments ternary(ContextFactory factory) {
        return arguments("Ternary operators should function",
                standard(),
                factory.name("Homer"),
                getResource(Path.of("TernaryOperatorTest.docx")),
                """
                        Expression Replacement with ternary operator
                        This paragraph is untouched.
                        Some replacement before the ternary operator: Homer.
                        Homer <-- this should read "Homer".
                         <-- this should be empty.
                        """);
    }

    private static Arguments replaceWordWithIntegrationTest(ContextFactory factory) {
        return of("Replace Word With integration test",
                OfficeStamperConfigurations.standard(),
                factory.name("Simpsons"),
                getResource(Path.of("ReplaceWordWithIntegrationTest.docx")),
                """
                        == ReplaceWordWith Integration
                        
                        This variable name should be resolved to the value Simpsons.
                        |===
                        |This variable name should be resolved to the value Simpsons.
                        
                        
                        |===
                        
                        """);
    }

    private static Arguments replaceNullExpressionTest(ContextFactory factory) {
        return of("Do not replace 'null' values",
                standard().addResolver(Resolvers.nullToPlaceholder()),
                factory.name(null),
                getResource(Path.of("ReplaceNullExpressionTest.docx")),
                """
                        I am ${name}.
                        """);
    }


    private static Arguments replaceNullExpressionTest2(ContextFactory factory) {
        return of("Do replace 'null' values",
                standard().addResolver(Resolvers.nullToEmpty()),
                factory.name(null),
                getResource(Path.of("ReplaceNullExpressionTest.docx")),
                """
                        I am .
                        """);
    }


    private static Arguments customEvaluationContextConfigurerTest_customEvaluationContextConfigurerIsHonored(
            ContextFactory factory
    ) {
        var context = factory.empty();
        var template = makeResource("""
                Custom EvaluationContextConfigurer Test
                This paragraph stays untouched.
                The variable foo has the value ${foo}.
                """);
        var expected = """
                Custom EvaluationContextConfigurer Test
                This paragraph stays untouched.
                The variable foo has the value bar.
                """;
        var config =
                standard().setEvaluationContextConfigurer(evalContext -> evalContext.addPropertyAccessor(new SimpleGetter(
                        "foo",
                        "bar")));

        return arguments("customEvaluationContextConfigurerTest_customEvaluationContextConfigurerIsHonored",
                config,
                context,
                template,
                expected);
    }

    private static Arguments expressionReplacementInGlobalParagraphsTest(ContextFactory factory) {
        var context = factory.name("Homer Simpson");
        var template = makeResource("""
                Expression Replacement in global paragraphs
                This paragraph is untouched.
                In this paragraph, the variable name should be resolved to the value ${name}.
                In this paragraph, the variable foo should not be resolved: ${foo}.""");
        var expected = """
                Expression Replacement in global paragraphs
                This paragraph is untouched.
                In this paragraph, the variable name should be resolved to the value Homer Simpson.
                In this paragraph, the variable foo should not be resolved: ${foo}.
                """;
        OfficeStamperConfiguration config = standard().setExceptionResolver(ExceptionResolvers.passing());
        return arguments("expressionReplacementInGlobalParagraphsTest", config, context, template, expected);
    }

    private static Arguments expressionReplacementInTablesTest(ContextFactory factory) {
        var context = factory.name("Bart Simpson");
        var template = getResource(Path.of("ExpressionReplacementInTablesTest.docx"));

        var expected = """
                == Expression Replacement in Tables
                
                |===
                |This should resolve to a name:
                |Bart Simpson
                
                |This should not resolve:
                |${foo}
                
                |Nested Table:
                |===
                |This should resolve to a name:
                |Bart Simpson
                
                |This should not resolve:
                |${foo}
                
                
                |===
                
                
                |===
                
                """;
        var config = standard().setExceptionResolver(ExceptionResolvers.passing());
        return arguments("Placeholder replacement in tables", config, context, template, expected);
    }

    private static Arguments expressionReplacementWithFormattingTest(ContextFactory factory) {
        var context = factory.name("Homer Simpson");
        var template = getResource(Path.of("ExpressionReplacementWithFormattingTest.docx"));
        var expected = """
                == Expression Replacement with text format
                
                The text format should be kept intact when an expression is replaced.
                It should be bold: ❬Homer Simpson❘{b=true}❭.
                It should be italic: ❬Homer Simpson❘{i=true}❭.
                It should be superscript: ❬Homer Simpson❘{vertAlign=superscript}❭.
                It should be subscript: ❬Homer Simpson❘{vertAlign=subscript}❭.
                It should be striked: ❬Homer Simpson❘{strike=true}❭.
                It should be underlined: ❬Homer Simpson❘{u=single}❭.
                It should be doubly underlined: ❬Homer Simpson❘{u=double}❭.
                It should be thickly underlined: ❬Homer Simpson❘{u=thick}❭.
                It should be dot underlined: ❬Homer Simpson❘{u=dotted}❭.
                It should be dash underlined: ❬Homer Simpson❘{u=dash}❭.
                It should be dot and dash underlined: ❬Homer Simpson❘{u=dotDash}❭.
                It should be dot, dot and dash underlined: ❬Homer Simpson❘{u=dotDotDash}❭.
                It should be highlighted yellow: ❬Homer Simpson❘{highlight=yellow}❭.
                It should be white over darkblue: ❬Homer Simpson❘{color=FFFFFF,highlight=darkBlue}❭.
                It should be with header formatting: ❬Homer Simpson❘{rStyle=TitreCar}❭.
                """;
        return arguments("Placeholder replacement integration test (keep formatting)",
                standard(),
                context,
                template,
                expected);
    }

    private static Arguments expressionWithSurroundingSpacesTest(ContextFactory factory) {
        var spacyContext = factory.spacy();
        var template = getResource(Path.of("ExpressionWithSurroundingSpacesTest.docx"));
        var expected = """
                == Expression Replacement when expression has leading and/or trailing spaces
                
                When an expression within a paragraph is resolved, the spaces between the replacement and the surrounding text should be as expected. The following paragraphs should all look the same.
                Before Expression After.
                Before Expression After.
                Before Expression After.
                Before Expression After.
                Before Expression After.
                Before Expression After.
                Before Expression After.
                """;
        return arguments("Placeholder replacement test, spaces management",
                standard(),
                spacyContext,
                template,
                expected);
    }

    private static Arguments expressionReplacementWithCommentTest(ContextFactory factory) {
        var context = factory.name("Homer Simpson");
        var template = getResource(Path.of("ExpressionReplacementWithCommentsTest.docx"));
        var expected = """
                == Expression Replacement with comments
                
                This paragraph is untouched.
                In this paragraph, the variable name should be resolved to the value Homer Simpson.
                In this paragraph, the variable foo should not be resolved: <1|unresolvedValueWithComment|1><1|replaceWordWith(foo)>.
                """;
        var config = standard().setExceptionResolver(ExceptionResolvers.passing());
        return arguments("Replace Word With Integration test", config, context, template, expected);
    }

    /// testDateInstantiationAndResolution.
    private static Arguments imageReplacementInGlobalParagraphsTest(ContextFactory factory) {
        var context = factory.image(getImage(Path.of("monalisa.jpg")));
        var template = getResource(Path.of("ImageReplacementInGlobalParagraphsTest.docx"));
        var expected = """
                == Image Replacement in global paragraphs
                
                This paragraph is untouched.
                In this paragraph, an image of Mona Lisa is inserted: /word/media/document_image_rId6.jpeg:rId6:image/jpeg:8.8kB:sha1=XMpVtDbetKjZTkPhy598GdJQM/4=:cy=$d:1276350.
                This paragraph has the image /word/media/document_image_rId7.jpeg:rId7:image/jpeg:8.8kB:sha1=XMpVtDbetKjZTkPhy598GdJQM/4=:cy=$d:1276350 in the middle.
                """;
        return arguments("Image Type resolver integration test", standard(), context, template, expected);
    }

    private static Arguments imageReplacementInGlobalParagraphsTestWithMaxWidth(ContextFactory factory) {
        var context = factory.image(getImage(Path.of("monalisa.jpg"), 1000));
        var template = getResource(Path.of("ImageReplacementInGlobalParagraphsTest.docx"));
        var expected = """
                == Image Replacement in global paragraphs
                
                This paragraph is untouched.
                In this paragraph, an image of Mona Lisa is inserted: /word/media/document_image_rId6.jpeg:rId6:image/jpeg:8.8kB:sha1=XMpVtDbetKjZTkPhy598GdJQM/4=:cy=$d:635000.
                This paragraph has the image /word/media/document_image_rId7.jpeg:rId7:image/jpeg:8.8kB:sha1=XMpVtDbetKjZTkPhy598GdJQM/4=:cy=$d:635000 in the middle.
                """;
        return arguments("Image Type resolver integration test (with max width)",
                standard(),
                context,
                template,
                expected);
    }

    private static Arguments leaveEmptyOnExpressionErrorTest(ContextFactory factory) {
        var context = factory.name("Homer Simpson");
        var template = getResource(Path.of("LeaveEmptyOnExpressionErrorTest.docx"));
        var expected = "Leave me empty .\n";
        var config = standard().setExceptionResolver(ExceptionResolvers.defaulting());
        return arguments("Default Exception Resolver Integration test, with empty value",
                config,
                context,
                template,
                expected);
    }

    private static Arguments lineBreakReplacementTest(ContextFactory factory) {
        var config = standard().addPostprocessor(Postprocessors.linebreaker("#"));
        var context = factory.name(null);
        var template = getResource(Path.of("LineBreakReplacementTest.docx"));
        var expected = """
                Line Break Replacement
                This paragraph is untouched.
                This paragraph should be <br/>
                 split in <br/>
                 three lines.
                This paragraph is untouched.
                """;
        return arguments("lineBreakReplacementTest", config, context, template, expected);
    }

    private static Arguments mapAccessorAndReflectivePropertyAccessorTest_shouldResolveMapAndPropertyPlaceholders(
            ContextFactory factory
    ) {
        var context = factory.mapAndReflectiveContext();
        var template = getResource(Path.of("MapAccessorAndReflectivePropertyAccessorTest.docx"));
        var expected = """
                Flat string : Flat string has been resolved
                
                |===
                |Values
                
                |first value
                
                |second value
                
                
                |===
                
                
                Paragraph start
                first value
                Paragraph end
                Paragraph start
                second value
                Paragraph end
                
                """;

        var defaultValue = "N/C";
        var config = standard().addResolver(Resolvers.nullToDefault(defaultValue))
                               .setExceptionResolver(ExceptionResolvers.defaulting(defaultValue));

        return arguments("Should be able to stamp from a Map<String, Object> context",
                config,
                context,
                template,
                expected);
    }

    private static Arguments nullPointerResolutionTest_testWithDefaultSpel(ContextFactory factory) {
        var context = factory.nullishContext();
        var template = getResource(Path.of("NullPointerResolution.docx"));
        var expected = """
                Deal with null references
                
                Deal with: Fullish1
                Deal with: Fullish2
                Deal with: Fullish3
                Deal with: Fullish5
                
                Deal with: Nullish value!!
                Deal with: ${nullish.value ?: "Nullish value!!"}
                Deal with: ${nullish.li[0] ?: "Nullish value!!"}
                Deal with: ${nullish.li[2] ?: "Nullish value!!"}
                
                """;

        var config = standard().setExceptionResolver(ExceptionResolvers.passing());

        return arguments("nullPointerResolutionTest_testWithDefaultSpel", config, context, template, expected);
    }

    private static Arguments customCommentProcessor(ContextFactory factory) {
        return arguments("Custom processor Integration test",
                standard().addCommentProcessor(ICustomCommentProcessor.class, CustomCommentProcessor::new),
                factory.empty(),
                getResource(Path.of("CustomCommentProcessorTest.docx")),
                """     
                        == Custom Comment Processor Test
                        
                        Visited
                        This paragraph is untouched.
                        Visited
                        """);
    }

    private static Arguments controls(ContextFactory factory) {
        return of("Form controls should be replaced as well",
                standard(),
                factory.name("Homer"),
                getResource(Path.of("form-controls.docx")),
                """
                        == Expression Replacement in Form Controls
                        
                        [Rich text control line Homer]
                        Rich text control inlined [Homer]
                        [Raw text control line Homer]
                        Raw text control inlined [Homer]
                        [Homer]
                        
                        """);
    }

    private static Arguments nullPointerResolutionTest_testWithCustomSpel(ContextFactory factory) {
        var context = factory.nullishContext();
        var template = getResource(Path.of("NullPointerResolution.docx"));
        var expected = """
                Deal with null references
                
                Deal with: Fullish1
                Deal with: Fullish2
                Deal with: Fullish3
                Deal with: Fullish5
                
                Deal with: Nullish value!!
                Deal with: Nullish value!!
                Deal with: Nullish value!!
                Deal with: Nullish value!!
                
                """;

        // Beware, this configuration only autogrows pojos and java beans,
        // so it will not work if your type has no default constructor and no setters.
        var config = standard().setSpelParserConfiguration(new SpelParserConfiguration(true, true))
                               .setEvaluationContextConfigurer(EvaluationContextConfigurers.noopConfigurer())
                               .addResolver(Resolvers.nullToDefault("Nullish value!!"));

        return arguments("nullPointerResolutionTest_testWithCustomSpel", config, context, template, expected);
    }

    @MethodSource("tests")
    @ParameterizedTest(name = "{0}")
    void features(
            String name,
            OfficeStamperConfiguration config,
            Object context,
            InputStream template,
            String expected
    ) {
        log.info(name);
        var stamper = new TestDocxStamper<>(config);
        var actual = stamper.stampAndLoadAndExtract(template, context);
        assertEquals(expected, actual);
    }
}
