package pro.verron.officestamper.docx4j17.test.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import pro.verron.officestamper.core.DocxStamper;
import pro.verron.officestamper.core.DocxStamperConfiguration;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

/// Architecture tests using ArchUnit.
@AnalyzeClasses(packages = "pro.verron.officestamper", importOptions = ImportOption.DoNotIncludeTests.class)
public class ArchUnitTests {

    @ArchTest ArchRule presetDependOnUtilsAndApi = classes().that()
                                                                 .resideInAPackage("..preset..")
                                                                 .should()
                                                                 .onlyDependOnClassesThat()
                                                                 .resideInAnyPackage("..preset..",
                                                                         "..utils..",
                                                                         "..api..",
                                                                         "..spi..",
                                                                         "java..",
                                                                         "org.docx4j..",
                                                                         "org.jspecify..",
                                                                         "org.jvnet.jaxb2_commons..",
                                                                         // docx4j 17's Child lives in the
                                                                         // re-packaged org.jvnet.jaxb runtime.
                                                                         "org.jvnet.jaxb..",
                                                                         "jakarta.xml.bind..",
                                                                         "org.slf4j..",
                                                                         "org.springframework..",
                                                                         // The provider's preset is the
                                                                         // docx4j-coupled layer by design: it
                                                                         // may use this module's own adapters.
                                                                         "..docx4j17..",
                                                                         "..docx..")
                                                                 .orShould()
                                                                 .dependOnClassesThat()
                                                                 .belongToAnyOf(DocxStamper.class,
                                                                         DocxStamperConfiguration.class);
    @ArchTest ArchRule coreDependOnUtilsAndApi = classes().that()
                                                               .resideInAPackage("..core..")
                                                               .should()
                                                               .onlyDependOnClassesThat()
                                                               .resideInAnyPackage("..core..",
                                                                       "..utils..",
                                                                       "..api..",
                                                                       "..spi..",
                                                                       "java..",
                                                                       "org.docx4j..",
                                                                       "org.jspecify..",
                                                                       "org.jvnet.jaxb2_commons..",
                                                                       "org.jvnet.jaxb..",
                                                                       "jakarta.xml.bind..",
                                                                       "org.slf4j..",
                                                                       "org.springframework..");
}
