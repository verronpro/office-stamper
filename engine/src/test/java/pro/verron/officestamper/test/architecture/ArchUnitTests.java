package pro.verron.officestamper.test.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import pro.verron.officestamper.core.DocxStamper;
import pro.verron.officestamper.core.DocxStamperConfiguration;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

@AnalyzeClasses(packages = "pro.verron.officestamper", importOptions = ImportOption.DoNotIncludeTests.class)
public class ArchUnitTests {

    @ArchTest ArchRule preset_depend_on_utils_and_api = classes().that()
                                                                 .resideInAPackage("..preset..")
                                                                 .should()
                                                                 .onlyDependOnClassesThat()
                                                                 .resideInAnyPackage("..preset..",
                                                                         "..utils..",
                                                                         "..api..",
                                                                         "java..",
                                                                         "org.docx4j..",
                                                                         "org.jspecify..",
                                                                         "org.jvnet.jaxb2_commons..",
                                                                         "jakarta.xml.bind..",
                                                                         "org.slf4j..",
                                                                         "org.springframework..")
                                                                 .orShould()
                                                                 .dependOnClassesThat()
                                                                 .belongToAnyOf(DocxStamper.class,
                                                                         DocxStamperConfiguration.class);

    @ArchTest ArchRule core_depend_on_utils_and_api = classes().that()
                                                               .resideInAPackage("..core..")
                                                               .should()
                                                               .onlyDependOnClassesThat()
                                                               .resideInAnyPackage("..core..",
                                                                       "..utils..",
                                                                       "..api..",
                                                                       "java..",
                                                                       "org.docx4j..",
                                                                       "org.jspecify..",
                                                                       "org.jvnet.jaxb2_commons..",
                                                                       "jakarta.xml.bind..",
                                                                       "org.slf4j..",
                                                                       "org.springframework..");
}
