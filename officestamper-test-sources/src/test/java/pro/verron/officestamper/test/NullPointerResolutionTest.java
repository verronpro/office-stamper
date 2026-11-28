package pro.verron.officestamper.test;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.expression.spel.SpelParserConfiguration;
import pro.verron.officestamper.api.OfficeStamperException;
import pro.verron.officestamper.api.SecurityMode;
import pro.verron.officestamper.preset.ExceptionResolvers;
import pro.verron.officestamper.preset.Resolvers;
import pro.verron.officestamper.test.utils.ContextFactory;
import pro.verron.officestamper.test.utils.ObjectContextFactory;
import pro.verron.officestamper.test.utils.OfficeStamperTestBase;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static pro.verron.officestamper.preset.OfficeStampers.docxStamper;
import static pro.verron.officestamper.test.utils.DocxFactory.makeWordResource;

/// @author Joseph Verron
class NullPointerResolutionTest
        extends OfficeStamperTestBase {

    @MethodSource("factories")
    @ParameterizedTest(name = "Null Pointer Resolution with Default SpEL Configuration")
    void nullPointerResolutionTest_testWithDefaultSpel(ContextFactory factory) {
        testStamper(Flavor.get().standard().setExceptionResolver(ExceptionResolvers.passing()),
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
        var configuration = Flavor.get().standard().setParserConfiguration(parserConfiguration)
                                      .setSpelSecurityMode(SecurityMode.PERMISSIVE)
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
        var configuration = Flavor.get().standard();
        var stamper = docxStamper(configuration);
        assertThrows(OfficeStamperException.class, () -> stamper.stamp(template, context));
    }
}
