package pro.verron.officestamper.test;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.expression.spel.SpelParserConfiguration;
import pro.verron.officestamper.api.OfficeStamperException;
import pro.verron.officestamper.preset.ExceptionResolvers;
import pro.verron.officestamper.preset.Resolvers;
import pro.verron.officestamper.test.utils.ContextFactory;
import pro.verron.officestamper.test.utils.ObjectContextFactory;
import pro.verron.officestamper.test.utils.OfficeStamperTest;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static pro.verron.officestamper.preset.EvaluationContextFactories.noopFactory;
import static pro.verron.officestamper.preset.OfficeStamperConfigurations.standard;
import static pro.verron.officestamper.preset.OfficeStampers.docxPackageStamper;
import static pro.verron.officestamper.test.utils.DocxFactory.makeWordResource;

/// @author Joseph Verron
class NullPointerResolutionTest
        extends OfficeStamperTest {

    @MethodSource("factories")
    @ParameterizedTest(name = "Null Pointer Resolution with Default SpEL Configuration")
    void nullPointerResolutionTest_testWithDefaultSpel(ContextFactory factory) {
        testStamper(standard().setExceptionResolver(ExceptionResolvers.passing()),
                factory.nullishContext(),
                makeWordResource("""
                        Deal with null references
                        
                        Deal with: ${fullish_value ?: "Fullish value?!"}
                        
                        Deal with: ${fullish.value ?: "Fullish value?!"}
                        
                        Deal with: ${fullish.li[0] ?: "Fullish value?!"}
                        
                        Deal with: ${fullish.li[2] ?: "Fullish value?!"}
                        
                        Deal with: ${nullish_value ?: "Nullish value!!"}
                        
                        Deal with: ${nullish.value ?: "Nullish value!!"}
                        
                        Deal with: ${nullish.li[0] ?: "Nullish value!!"}
                        
                        Deal with: ${nullish.li[2] ?: "Nullish value!!"}
                        """),
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
                        
                        // section {pgMar={bottom=1440, left=1440, right=1440, top=1440}, pgSz={code=9, h=16839, w=11907}}
                        
                        """);
    }

    @MethodSource("factories")
    @ParameterizedTest(name = "Null Pointer Resolution with Custom SpEL Parser Configuration")
    void nullPointerResolutionTest_testWithCustomSpel() {
        var contextFactory = new ObjectContextFactory();
        // Beware, this configuration only autogrows pojos and java beans,
        // so it will not work if your type has no default constructor and no setters.
        // It does not work with arrays, lists, maps, etc. since it cannot guess the types to auto grow at runtime
        var parserConfiguration = new SpelParserConfiguration(true, true);
        var configuration = standard().setParserConfiguration(parserConfiguration)
                                      .setEvaluationContextFactory(noopFactory())
                                      .addResolver(Resolvers.nullToDefault("Nullish value!!"));
        var context = contextFactory.nullishContext();
        var template = makeWordResource("""
                Deal with null references
                
                
                Deal with: ${fullish_value ?: "Fullish value?!"}
                
                Deal with: ${fullish.value ?: "Fullish value?!"}
                
                Deal with: ${fullish.li[0] ?: "Fullish value?!"}
                
                Deal with: ${fullish.li[2] ?: "Fullish value?!"}
                
                
                Deal with: ${nullish_value ?: "Nullish value!!"}
                
                Deal with: ${nullish.value ?: "Nullish value!!"}
                
                Deal with: ${nullish.li[0] ?: "Nullish value!!"}
                
                Deal with: ${nullish.li[2] ?: "Nullish value!!"}
                
                """);
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
                
                // section {pgMar={bottom=1440, left=1440, right=1440, top=1440}, pgSz={code=9, h=16839, w=11907}}
                
                """;
        testStamper(configuration, context, template, expected);
    }

    @MethodSource("factories")
    @ParameterizedTest(name = "Null Pointer Resolution with Throwing Case")
    void nullPointerResolutionTest_testThrowingCase(ContextFactory factory) {
        var context = factory.nullishContext();
        var template = makeWordResource("""
                Deal with null references
                
                
                Deal with: ${fullish_value ?: "Fullish value?!"}
                
                Deal with: ${fullish.value ?: "Fullish value?!"}
                
                Deal with: ${fullish.li[0] ?: "Fullish value?!"}
                
                Deal with: ${fullish.li[2] ?: "Fullish value?!"}
                
                
                Deal with: ${nullish_value ?: "Nullish value!!"}
                
                Deal with: ${nullish.value ?: "Nullish value!!"}
                
                Deal with: ${nullish.li[0] ?: "Nullish value!!"}
                
                Deal with: ${nullish.li[2] ?: "Nullish value!!"}
                
                """);
        var configuration = standard();
        var stamper = docxPackageStamper(configuration);
        assertThrows(OfficeStamperException.class, () -> stamper.stamp(template, context));
    }
}
