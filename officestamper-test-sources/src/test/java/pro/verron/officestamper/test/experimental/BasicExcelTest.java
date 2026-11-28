package pro.verron.officestamper.test.experimental;
import pro.verron.officestamper.test.Flavor;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pro.verron.officestamper.test.utils.ResourceUtils;

import java.nio.file.Path;

import static org.docx4j.openpackaging.packages.SpreadsheetMLPackage.load;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.docx4j.openpackaging.packages.SpreadsheetMLPackage;

@DisplayName("Basic Excel Test") class BasicExcelTest {

    @Test
    @DisplayName("Should stamp an Excel document")
    void testStamper()
            throws Docx4JException {

        var stamper = Flavor.get().xlsxPackageStamper();
        var templateStream = ResourceUtils.getResource(Path.of("excel-base.xlsx"));

        var templatePackage = load(templateStream);
        var templateExpectedString = """
                A1: Hello
                B1: ${name}""";
        var templateActualString = Flavor.get().xlsxToString(templatePackage);
        assertEquals(templateExpectedString, templateActualString);

        record Person(String name) {}
        var context = new Person("Bart");

        var stamped = stamper.stamp(Flavor.get().xlsx(templatePackage), context);

        var stampedExpectedString = """
                A1: Hello
                B1: Bart""";
        var stampedActualString = Flavor.get().xlsxToString(stamped.unwrap(SpreadsheetMLPackage.class));
        assertEquals(stampedExpectedString, stampedActualString);
    }
}
