package pro.verron.officestamper.test;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import pro.verron.officestamper.preset.ExceptionResolvers;
import pro.verron.officestamper.preset.Resolvers;
import pro.verron.officestamper.test.utils.ContextFactory;
import pro.verron.officestamper.test.utils.DocxFactory;
import pro.verron.officestamper.test.utils.OfficeStamperTestBase;

import java.nio.file.Path;

import static pro.verron.officestamper.preset.OfficeStamperConfigurations.full;
import static pro.verron.officestamper.preset.OfficeStamperConfigurations.standard;
import static pro.verron.officestamper.test.utils.ResourceUtils.getWordResource;

@DisplayName("Default Features")
class DefaultTests extends OfficeStamperTestBase {

    @MethodSource("factories")
    @ParameterizedTest(name = "Expression replacement with formatting should keep original formatting")
    void expressionReplacementWithFormattingTest(ContextFactory factory) {
        testStamper(standard(),
                factory.name("Homer Simpson"),
                getWordResource(Path.of("ExpressionReplacementWithFormattingTest.docx")),
                """
                        == Expression Replacement with text format
                        
                        The text format should be kept intact when an expression is replaced.
                        
                        It should be bold: *Homer Simpson*.
                        
                        It should be italic: _Homer Simpson_.
                        
                        It should be superscript: ^Homer Simpson^.
                        
                        It should be subscript: ~Homer Simpson~.
                        
                        It should be striked: [strike]#Homer Simpson#.
                        
                        It should be underlined: [u_single]#Homer Simpson#.
                        
                        It should be doubly underlined: [u_double]#Homer Simpson#.
                        
                        It should be thickly underlined: [u_thick]#Homer Simpson#.
                        
                        It should be dot underlined: [u_dotted]#Homer Simpson#.
                        
                        It should be dash underlined: [u_dash]#Homer Simpson#.
                        
                        It should be dot and dash underlined: [u_dotDash]#Homer Simpson#.
                        
                        It should be dot, dot and dash underlined: [u_dotDotDash]#Homer Simpson#.
                        
                        It should be highlighted yellow: [highlight_yellow]#Homer Simpson#.
                        
                        It should be white over darkblue: [color_FFFFFF]#[highlight_darkBlue]#Homer Simpson##.
                        
                        It should be with header formatting: [rStyle_TitreCar]#Homer Simpson#.
                        
                        // section {docGrid={charSpace=-6145, linePitch=240}, pgMar={bottom=1134, left=1134, right=1134, top=1134}, pgSz={h=16838, w=11906}, space=720}
                        
                        """
        );
    }

    @MethodSource("factories")
    @ParameterizedTest(name = "Expression with surrounding spaces should manage spaces correctly")
    void expressionWithSurroundingSpacesTest(ContextFactory factory) {
        testStamper(standard(),
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
                        
                        // section {docGrid={charSpace=-6145, linePitch=240}, pgMar={bottom=1134, left=1134, right=1134, top=1134}, pgSz={h=16838, w=11906}, space=720}
                        
                        """
        );
    }

    @MethodSource("factories")
    @ParameterizedTest(name = "Expression Replacement With Comments Test")
    void expressionReplacementWithCommentTest(ContextFactory factory) {
        testStamper(full().setExceptionResolver(ExceptionResolvers.passing()),
                factory.name("Homer Simpson"),
                getWordResource(Path.of("ExpressionReplacementWithCommentsTest.docx")),
                """
                        == Expression Replacement with comments
                        
                        This paragraph is untouched.
                        
                        In this paragraph, the variable name should be resolved to the value Homer Simpson.
                        
                        In this paragraph, the variable foo should not be resolved: unresolvedValueWithComment.
                        
                In this paragraph, we test that _only_ the text in the comment (but not other occurrences of the same text) is replaced (also, the format – red, bold – should be retained): name [color_FF0000]#*Homer Simpson*#
                
                        // section {docGrid={charSpace=-6145, linePitch=240}, pgMar={bottom=1134, left=1134, right=1134, top=1134}, pgSz={h=16838, w=11906}, space=720}
                        
                        """
        );
    }

    @MethodSource("factories")
    @ParameterizedTest(name = "Leave Empty On Expression Error Test")
    void leaveEmptyOnExpressionErrorTest(ContextFactory factory) {
        var config = standard().setExceptionResolver(ExceptionResolvers.defaulting());
        var context = factory.name("Homer Simpson");
        var template = getWordResource(Path.of("LeaveEmptyOnExpressionErrorTest.docx"));
        var expected = """
                Leave me empty .
                
                // section {docGrid={charSpace=-6145, linePitch=240}, pgMar={bottom=1134, left=1134, right=1134, top=1134}, pgSz={h=16838, w=11906}, space=708}
                
                """;
        testStamper(config, context, template, expected);
    }

    @MethodSource("factories")
    @ParameterizedTest(name = "Line Break Replacement Test")
    void lineBreakReplacementTest(ContextFactory factory) {
        var config = standard(Resolvers.fallback("#"));
        var context = factory.sentence("whatever # split in # three lines");
        var template = DocxFactory.makeWordResource("""
                This paragraph should not be # split.
                This paragraph should have a split input: ${sentence}.
                """);
        var expected = """
                This paragraph should not be # split. +
                This paragraph should have a split input: whatever  +
                 split in  +
                 three lines.
                
                // section {pgMar={bottom=1440, left=1440, right=1440, top=1440}, pgSz={code=9, h=16839, w=11907}}
                
                """;
        testStamper(config, context, template, expected);
    }

    @MethodSource("factories")
    @ParameterizedTest(name = "Map Accessor and Reflective Property Accessor should resolve map and property placeholders")
    void mapAccessorAndReflectivePropertyAccessorTest_shouldResolveMapAndPropertyPlaceholders(ContextFactory factory) {
        var config = standard().addResolver(Resolvers.nullToDefault("N/C"))
                .setExceptionResolver(ExceptionResolvers.defaulting("N/C"));
        var context = factory.mapAndReflectiveContext();
        var template = getWordResource(Path.of("MapAccessorAndReflectivePropertyAccessorTest.docx"));
        var expected = """
                Flat string : Flat string has been resolved
                
                
                
                |===
                |Values
                a|first value
                a|second value
                |===
                
                
                
                
                
                Paragraph start
                
                first value
                
                Paragraph end
                
                Paragraph start
                
                second value
                
                Paragraph end
                
                
                
                // section {docGrid={linePitch=360}, pgMar={bottom=1417, footer=708, header=708, left=1417, right=1417, top=1417}, pgSz={h=16838, w=11906}, space=708}
                
                """;
        testStamper(config, context, template, expected);
    }

    @MethodSource("factories")
    @ParameterizedTest(name = "Form controls should be replaced as well")
    void controls(ContextFactory factory) {
        var config = standard();
        var context = factory.name("Homer");
        var template = getWordResource(Path.of("form-controls.docx"));
        var expected = """
                == Expression Replacement in Form Controls
                
                [form, id=8a282f9]
                --
                Rich text control line Homer
                
                --
                
                
                Rich text control inlined form:df261932[Homer]
                
                [form, id=fe2b2bd9]
                --
                Raw text control line Homer
                
                --
                
                
                Raw text control inlined form:50007206[Homer]
                
                [form, id=a90c90aa]
                --
                Homer
                
                --
                
                
                In a table:
                
                |===
                |Homer
                |===
                
                
                
                In another table:
                
                |===
                |Raw text control inlined form:c6202292[Homer]
                |===
                
                
                
                In another table:
                
                |===
                a|[form, id=61b817b8]
                --
                Raw text control line Homer
                
                --
                |===
                
                
                
                
                
                // section {docGrid={linePitch=360}, pgMar={bottom=1418, footer=709, header=709, left=1418, right=1418, top=1418}, pgSz={h=16838, w=11906}, space=708}
                
                """;
        testStamper(config, context, template, expected);
    }

    @MethodSource("factories")
    @ParameterizedTest(name = "Expression replacement in tables")
    void expressionReplacementInTablesTest(ContextFactory factory) {
        var config = standard().setExceptionResolver(ExceptionResolvers.passing());
        var context = factory.name("Bart Simpson");
        var template = getWordResource(Path.of("ExpressionReplacementInTablesTest.docx"));
        var expected = """
                == Expression Replacement in Tables
                
                |===
                |This should resolve to a name:
                |Bart Simpson
                |This should not resolve:
                |${foo}
                a|Nested Table:
                
                !===
                !This should resolve to a name:
                !Bart Simpson
                !This should not resolve:
                !${foo}
                !===
                |===
                
                
                
                // section {docGrid={charSpace=-6145, linePitch=240}, pgMar={bottom=1134, left=1134, right=1134, top=1134}, pgSz={h=16838, w=11906}, space=720}
                
                """;
        testStamper(config, context, template, expected);
    }

    @MethodSource("factories")
    @ParameterizedTest(name = "Expression replacement in global paragraphs")
    void expressionReplacementInGlobalParagraphsTest(ContextFactory factory) {
        var config = standard().setExceptionResolver(ExceptionResolvers.passing());
        var context = factory.name("Homer Simpson");
        var template = DocxFactory.makeWordResource("""
                Expression Replacement in global paragraphs
                This paragraph is untouched.
                In this paragraph, the variable name should be resolved to the value ${name}.
                In this paragraph, the variable foo should not be resolved: ${foo}.""");
        var expected = """
                Expression Replacement in global paragraphs +
                This paragraph is untouched. +
                In this paragraph, the variable name should be resolved to the value Homer Simpson. +
                In this paragraph, the variable foo should not be resolved: ${foo}.
                
                // section {pgMar={bottom=1440, left=1440, right=1440, top=1440}, pgSz={code=9, h=16839, w=11907}}
                
                """;
        testStamper(config, context, template, expected);
    }

    @MethodSource("factories")
    @ParameterizedTest(name = "Custom EvaluationContext Configurer Test - Custom EvaluationContext Configurer Is Honored")
    void customEvaluationContextConfigurerTest_customEvaluationContextConfigurerIsHonored(ContextFactory factory) {
        var configuration = standard().setEvaluationContextFactory(evalContext -> {
            var evaluationContext = new StandardEvaluationContext(evalContext);
            evaluationContext.addPropertyAccessor(new SimpleGetter("foo", "bar"));
            return evaluationContext;
        });
        var context = factory.empty();
        var template = DocxFactory.makeWordResource("""
                Custom EvaluationContextConfigurer Test
                
                This paragraph stays untouched.
                
                The variable foo has the value ${foo}.
                """);
        var expected = """
                Custom EvaluationContextConfigurer Test
                
                This paragraph stays untouched.
                
                The variable foo has the value bar.
                
                // section {pgMar={bottom=1440, left=1440, right=1440, top=1440}, pgSz={code=9, h=16839, w=11907}}
                
                """;
        testStamper(configuration, context, template, expected);
    }

    @MethodSource("factories")
    @ParameterizedTest(name = "Do replace 'null' values with empty string")
    void replaceNullExpressionTest2(ContextFactory factory) {
        testStamper(standard().addResolver(Resolvers.nullToEmpty()),
                factory.name(null),
                getWordResource(Path.of("ReplaceNullExpressionTest.docx")),
                """
                        I am .
                        
                        // section {docGrid={charSpace=-6145, linePitch=240}, pgMar={bottom=1134, left=1134, right=1134, top=1134}, pgSz={h=16838, w=11906}, space=708}
                        
                        """
        );
    }

    @MethodSource("factories")
    @ParameterizedTest(name = "Do not replace 'null' values - keep placeholder")
    void replaceNullExpressionTest(ContextFactory factory) {
        testStamper(standard().addResolver(Resolvers.nullToPlaceholder()),
                factory.name(null),
                getWordResource(Path.of("ReplaceNullExpressionTest.docx")),
                """
                        I am ${name}.
                        
                        // section {docGrid={charSpace=-6145, linePitch=240}, pgMar={bottom=1134, left=1134, right=1134, top=1134}, pgSz={h=16838, w=11906}, space=708}
                        
                        """
        );
    }

    @MethodSource("factories")
    @ParameterizedTest(name = "Replace DocxDocument With Integration Test")
    void replaceWordWithIntegrationTest(ContextFactory factory) {
        var config = full();
        var context = factory.name("Simpsons");
        var template = getWordResource(Path.of("ProcessorReplaceWith.docx"));
        var expected = """
                == ReplaceWith Integration
                
                This variable name should be resolved to the value Simpsons.
                
                |===
                |This variable name should be resolved to the value Simpsons.
                |===
                
                
                
                
                
                // section {docGrid={charSpace=-6145, linePitch=240}, pgMar={bottom=1134, left=1134, right=1134, top=1134}, pgSz={h=16838, w=11906}, space=720}
                
                """;
        testStamper(config, context, template, expected);
    }

    @MethodSource("factories")
    @ParameterizedTest(name = "Ternary operator expressions should be evaluated correctly")
    void ternary(ContextFactory factory) {
        var config = standard();
        var context = factory.name("Homer");
        var template = getWordResource(Path.of("TernaryOperatorTest.docx"));
        var expected = """
                Expression Replacement with ternary operator
                
                This paragraph is untouched.
                
                Some replacement before the ternary operator: Homer.
                
                Homer <-- this should read "Homer".
                
                 <-- this should be empty.
                
                // section {docGrid={charSpace=-6145, linePitch=240}, pgMar={bottom=1134, left=1134, right=1134, top=1134}, pgSz={h=16838, w=11906}, space=720}
                
                """;
        testStamper(config, context, template, expected);
    }
}
