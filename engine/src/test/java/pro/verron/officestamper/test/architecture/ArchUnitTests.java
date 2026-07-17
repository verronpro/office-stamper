package pro.verron.officestamper.test.architecture;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClasses;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import org.docx4j.wml.ContentAccessor;
import pro.verron.officestamper.core.DocxStamper;
import pro.verron.officestamper.core.DocxStamperConfiguration;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

@AnalyzeClasses(packages = "pro.verron.officestamper", importOptions = ImportOption.DoNotIncludeTests.class)
class ArchUnitTests {

    @ArchTest
    void core_depend_on_utils_and_api(JavaClasses javaClasses) {
        var rule = classes().that()
                .resideInAPackage("pro.verron.officestamper.core..")
                .should()
                .onlyDependOnClassesThat()
                .resideInAnyPackage("pro.verron.officestamper.core..",
                        "pro.verron.officestamper.utils..",
                        "pro.verron.officestamper.api..",
                        "java..",
                        "org.docx4j..",
                        "org.jspecify..",
                        "org.jvnet.jaxb.lang..",
                        "jakarta.xml.bind..",
                        "org.slf4j..",
                        "org.springframework.."
                ).andShould().dependOnClassesThat().areNotAssignableFrom(ContentAccessor.class);
        rule.check(javaClasses);
    }

    @ArchTest
    void preset_depend_on_utils_and_api(JavaClasses javaClasses) {
        var rule = classes().that()
                .resideInAPackage("pro.verron.officestamper.preset..")
                .should()
                .onlyDependOnClassesThat()
                .resideInAnyPackage("pro.verron.officestamper.preset..",
                        "pro.verron.officestamper.utils..",
                        "pro.verron.officestamper.api..",
                        "java..",
                        "org.docx4j..",
                        "org.jspecify..",
                        "org.jvnet.jaxb.lang..",
                        "jakarta.xml.bind..",
                        "org.slf4j..",
                        "org.springframework.."
                )
                .orShould()
                .dependOnClassesThat()
                .belongToAnyOf(DocxStamper.class, DocxStamperConfiguration.class);
        rule.check(javaClasses);
    }
}
