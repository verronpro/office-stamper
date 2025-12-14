package pro.verron.officestamper.test;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.expression.spel.SpelParserConfiguration;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import pro.verron.officestamper.api.OfficeStamperConfiguration;
import pro.verron.officestamper.preset.ExceptionResolvers;
import pro.verron.officestamper.preset.Resolvers;

import java.nio.file.Path;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.ArgumentSet;
import static org.junit.jupiter.params.provider.Arguments.argumentSet;
import static pro.verron.officestamper.preset.EvaluationContextFactories.noopFactory;
import static pro.verron.officestamper.preset.OfficeStamperConfigurations.full;
import static pro.verron.officestamper.preset.OfficeStamperConfigurations.standard;
import static pro.verron.officestamper.preset.OfficeStampers.docxPackageStamper;
import static pro.verron.officestamper.test.ContextFactory.mapContextFactory;
import static pro.verron.officestamper.test.ContextFactory.objectContextFactory;
import static pro.verron.officestamper.test.TestUtils.*;
import static pro.verron.officestamper.utils.wml.DocxRenderer.docxToString;

@DisplayName("Core Features") class DefaultTests {

    private static Stream<ArgumentSet> tests() {
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
            pipe.accept(controls(factory));
        }), Stream.of(nullPointerResolutionTest_testWithCustomSpel(ContextFactory.objectContextFactory())));
    }

    private static Stream<ContextFactory> factories() {
        return Stream.of(objectContextFactory(), mapContextFactory());
    }

    private static ArgumentSet ternary(ContextFactory factory) {
        return argumentSet("Ternary operator expressions should be evaluated correctly",
                standard(),
                factory.name("Homer"),
                getWordResource(Path.of("TernaryOperatorTest.docx")),
                """
                        Expression Replacement with ternary operator
                        
                        This paragraph is untouched.
                        
                        Some replacement before the ternary operator: Homer.
                        
                        Homer <-- this should read "Homer".
                        
                         <-- this should be empty.
                        
                        """);
    }

    private static ArgumentSet replaceWordWithIntegrationTest(ContextFactory factory) {
        return argumentSet("Replace Word With Integration Test",
                full(),
                factory.name("Simpsons"),
                getWordResource(Path.of("ProcessorReplaceWith.docx")),
                """
                        == ReplaceWith Integration
                        
                        
                        This variable name should be resolved to the value Simpsons.
                        
                        |===
                        |This variable name should be resolved to the value Simpsons.
                        
                        
                        |===
                        
                        
                        
                        
                        """);
    }

    private static ArgumentSet replaceNullExpressionTest(ContextFactory factory) {
        return argumentSet("Do not replace 'null' values - keep placeholder",
                standard().addResolver(Resolvers.nullToPlaceholder()),
                factory.name(null),
                getWordResource(Path.of("ReplaceNullExpressionTest.docx")),
                """
                        I am ${name}.
                        
                        """);
    }


    private static ArgumentSet replaceNullExpressionTest2(ContextFactory factory) {
        return argumentSet("Do replace 'null' values with empty string",
                standard().addResolver(Resolvers.nullToEmpty()),
                factory.name(null),
                getWordResource(Path.of("ReplaceNullExpressionTest.docx")),
                """
                        I am .
                        
                        """);
    }


    private static ArgumentSet customEvaluationContextConfigurerTest_customEvaluationContextConfigurerIsHonored(
            ContextFactory factory
    ) {

        var name = "Custom EvaluationContext Configurer Test - Custom EvaluationContext Configurer Is Honored";
        return argumentSet(name, standard().setEvaluationContextFactory(evalContext -> {
            var evaluationContext = new StandardEvaluationContext(evalContext);
            evaluationContext.addPropertyAccessor(new SimpleGetter("foo", "bar"));
            return evaluationContext;
        }), factory.empty(), makeWordResource("""
                Custom EvaluationContextConfigurer Test
                
                This paragraph stays untouched.
                
                The variable foo has the value ${foo}.
                """), """
                Custom EvaluationContextConfigurer Test
                
                This paragraph stays untouched.
                
                The variable foo has the value bar.
                
                """);
    }

    private static ArgumentSet expressionReplacementInGlobalParagraphsTest(ContextFactory factory) {
        return argumentSet("Expression replacement in global paragraphs",
                standard().setExceptionResolver(ExceptionResolvers.passing()),
                factory.name("Homer Simpson"),
                makeWordResource("""
                        Expression Replacement in global paragraphs
                        This paragraph is untouched.
                        In this paragraph, the variable name should be resolved to the value ${name}.
                        In this paragraph, the variable foo should not be resolved: ${foo}."""),
                """
                        Expression Replacement in global paragraphs
                        This paragraph is untouched.
                        In this paragraph, the variable name should be resolved to the value Homer Simpson.
                        In this paragraph, the variable foo should not be resolved: ${foo}.
                        
                        """);
    }

    private static ArgumentSet expressionReplacementInTablesTest(ContextFactory factory) {
        return argumentSet("Expression replacement in tables",
                standard().setExceptionResolver(ExceptionResolvers.passing()),
                factory.name("Bart Simpson"),
                getWordResource(Path.of("ExpressionReplacementInTablesTest.docx")),
                """
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
                        
                        
                        """);
    }

    private static ArgumentSet expressionReplacementWithFormattingTest(ContextFactory factory) {
        return argumentSet("Expression replacement with formatting should keep original formatting",
                standard(),
                factory.name("Homer Simpson"),
                getWordResource(Path.of("ExpressionReplacementWithFormattingTest.docx")),
                """
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
                        
                        """);
    }

    private static ArgumentSet expressionWithSurroundingSpacesTest(ContextFactory factory) {
        return argumentSet("Expression with surrounding spaces should manage spaces correctly",
                standard(),
                factory.spacy(),
                getWordResource(Path.of("ExpressionWithSurroundingSpacesTest.docx")),
                """
                        == Expression Replacement when expression has leading and/or trailing spaces
                        
                        
                        When an expression within a paragraph is resolved, the spaces between the replacement and the surrounding text should be as expected. The following paragraphs should all look the same.
                        
                        Before Expression After.
                        
                        Before Expression After.
                        
                        Before Expression After.
                        
                        Before Expression After.
                        
                        Before Expression After.
                        
                        Before Expression After.
                        
                        Before Expression After.
                        
                        """);
    }

    private static ArgumentSet expressionReplacementWithCommentTest(ContextFactory factory) {
        return argumentSet("Expression Replacement With Comments Test",
                full().setExceptionResolver(ExceptionResolvers.passing()),
                factory.name("Homer Simpson"),
                getWordResource(Path.of("ExpressionReplacementWithCommentsTest.docx")),
                """
                        == Expression Replacement with comments
                        
                        
                        This paragraph is untouched.
                        
                        In this paragraph, the variable name should be resolved to the value Homer Simpson.
                        
                        In this paragraph, the variable foo should not be resolved: <1|unresolvedValueWithComment|1><1|replaceWith(foo)>.
                        
                        """);
    }

    private static ArgumentSet imageReplacementInGlobalParagraphsTest(ContextFactory factory) {
        return argumentSet("Image Replacement in global paragraphs",
                standard(),
                factory.image(getImage(Path.of("monalisa.jpg"))),
                getWordResource(Path.of("ImageReplacementInGlobalParagraphsTest.docx")),
                """
                        == Image Replacement in global paragraphs
                        
                        
                        This paragraph is untouched.
                        
                        In this paragraph, an image of Mona Lisa is inserted: /word/media/document_image_rId6.jpeg:rId6:image/jpeg:8.8 kB:sha1=XMpVtDbetKjZTkPhy598GdJQM/4=:cy=$d:1276350.
                        
                        This paragraph has the image /word/media/document_image_rId7.jpeg:rId7:image/jpeg:8.8 kB:sha1=XMpVtDbetKjZTkPhy598GdJQM/4=:cy=$d:1276350 in the middle.
                        
                        """);
    }

    private static ArgumentSet imageReplacementInGlobalParagraphsTestWithMaxWidth(ContextFactory factory) {
        return argumentSet("Image Replacement in global paragraphs with max width",
                standard(),
                factory.image(getImage(Path.of("monalisa.jpg"), 1000)),
                getWordResource(Path.of("ImageReplacementInGlobalParagraphsTest.docx")),
                """
                        == Image Replacement in global paragraphs
                        
                        
                        This paragraph is untouched.
                        
                        In this paragraph, an image of Mona Lisa is inserted: /word/media/document_image_rId6.jpeg:rId6:image/jpeg:8.8 kB:sha1=XMpVtDbetKjZTkPhy598GdJQM/4=:cy=$d:635000.
                        
                        This paragraph has the image /word/media/document_image_rId7.jpeg:rId7:image/jpeg:8.8 kB:sha1=XMpVtDbetKjZTkPhy598GdJQM/4=:cy=$d:635000 in the middle.
                        
                        """);
    }

    private static ArgumentSet leaveEmptyOnExpressionErrorTest(ContextFactory factory) {
        return argumentSet("Leave Empty On Expression Error Test",
                standard().setExceptionResolver(ExceptionResolvers.defaulting()),
                factory.name("Homer Simpson"),
                getWordResource(Path.of("LeaveEmptyOnExpressionErrorTest.docx")),
                """
                        Leave me empty .
                        
                        """);
    }

    private static ArgumentSet lineBreakReplacementTest(ContextFactory factory) {
        return argumentSet("Line Break Replacement Test",
                standard(Resolvers.fallback("#")),
                factory.sentence("whatever # split in # three lines"),
                makeWordResource("""
                        This paragraph should not be # split.
                        This paragraph should have a split input: ${sentence}.
                        """),
                """
                        This paragraph should not be # split.
                        This paragraph should have a split input: whatever <br/>
                         split in <br/>
                         three lines.
                        
                        """);
    }

    private static ArgumentSet mapAccessorAndReflectivePropertyAccessorTest_shouldResolveMapAndPropertyPlaceholders(
            ContextFactory factory
    ) {
        return argumentSet("Map Accessor and Reflective Property Accessor should resolve map and property placeholders",
                standard().addResolver(Resolvers.nullToDefault("N/C"))
                          .setExceptionResolver(ExceptionResolvers.defaulting("N/C")),
                factory.mapAndReflectiveContext(),
                getWordResource(Path.of("MapAccessorAndReflectivePropertyAccessorTest.docx")),
                """
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
                        
                        
                        
                        """);
    }

    private static ArgumentSet nullPointerResolutionTest_testWithDefaultSpel(ContextFactory factory) {
        return argumentSet("Null Pointer Resolution with Default SpEL Configuration",
                standard().setExceptionResolver(ExceptionResolvers.passing()),
                factory.nullishContext(),
                getWordResource(Path.of("NullPointerResolution.docx")),
                """
                        Deal with null references
                        
                        
                        
                        Deal with: Fullish1
                        
                        Deal with: Fullish2
                        
                        Deal with: Fullish3
                        
                        Deal with: Fullish5
                        
                        
                        
                        Deal with: Nullish value!!
                        
                        Deal with: ${nullish.value ?: "Nullish value!!"}
                        
                        Deal with: ${nullish.li[0] ?: "Nullish value!!"}
                        
                        Deal with: ${nullish.li[2] ?: "Nullish value!!"}
                        
                        
                        
                        """);
    }

    private static ArgumentSet controls(ContextFactory factory) {
        return argumentSet("Form controls should be replaced as well",
                standard(),
                factory.name("Homer"),
                getWordResource(Path.of("form-controls.docx")),
                """
                        == Expression Replacement in Form Controls
                        
                        
                        [Rich text control line Homer]
                        Rich text control inlined [Homer]
                        
                        [Raw text control line Homer]
                        Raw text control inlined [Homer]
                        
                        [Homer]
                        
                        
                        """);
    }

    private static ArgumentSet nullPointerResolutionTest_testWithCustomSpel(ContextFactory factory) {
        // Beware, this configuration only autogrows pojos and java beans,
        // so it will not work if your type has no default constructor and no setters.
        var expressionParser = new SpelExpressionParser(new SpelParserConfiguration(true, true));
        return argumentSet("Null Pointer Resolution with Custom SpEL Configuration",
                standard().setExpressionParser(expressionParser)
                          .setEvaluationContextFactory(noopFactory())
                          .addResolver(Resolvers.nullToDefault("Nullish value!!")),
                factory.nullishContext(),
                getWordResource(Path.of("NullPointerResolution.docx")),
                """
                        Deal with null references
                        
                        
                        
                        Deal with: Fullish1
                        
                        Deal with: Fullish2
                        
                        Deal with: Fullish3
                        
                        Deal with: Fullish5
                        
                        
                        
                        Deal with: Nullish value!!
                        
                        Deal with: Nullish value!!
                        
                        Deal with: Nullish value!!
                        
                        Deal with: Nullish value!!
                        
                        
                        
                        """);
    }

    @MethodSource("tests")
    @DisplayName("Core Features")
    @ParameterizedTest(name = "Core Features: {argumentSetName}")
    void features(
            OfficeStamperConfiguration config,
            Object context,
            WordprocessingMLPackage template,
            String expected
    ) {
        var stamper = docxPackageStamper(config);
        var wordprocessingMLPackage = stamper.stamp(template, context);
        var actual = docxToString(wordprocessingMLPackage);
        assertEquals(expected, actual);
    }
}
