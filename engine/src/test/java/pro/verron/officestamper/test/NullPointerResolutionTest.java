package pro.verron.officestamper.test;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import pro.verron.officestamper.api.OfficeStamperException;
import pro.verron.officestamper.test.utils.ContextFactory;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.argumentSet;
import static pro.verron.officestamper.preset.OfficeStamperConfigurations.standard;
import static pro.verron.officestamper.preset.OfficeStampers.docxPackageStamper;
import static pro.verron.officestamper.test.utils.ContextFactory.mapContextFactory;
import static pro.verron.officestamper.test.utils.ContextFactory.objectContextFactory;
import static pro.verron.officestamper.test.utils.DocxFactory.makeWordResource;

/// @author Joseph Verron
class NullPointerResolutionTest {

    static Stream<Arguments> factories() {
        return Stream.of(argumentSet("obj", objectContextFactory()), argumentSet("map", mapContextFactory()));
    }

    @MethodSource("factories")
    @ParameterizedTest
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
