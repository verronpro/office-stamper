package pro.verron.officestamper.test;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pro.verron.officestamper.test.utils.ResourceUtils;

import java.nio.file.Path;

import static org.docx4j.openpackaging.packages.SpreadsheetMLPackage.load;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static pro.verron.officestamper.experimental.ExperimentalStampers.xlsxPackageStamper;
import static pro.verron.officestamper.utils.sml.XlsxRenderer.xlsxToString;

@DisplayName("Basic Excel Test") class BasicExcelTest {

    @Test
    @DisplayName("Should stamp an Excel document")
    void testStamper()
            throws Docx4JException {

        var stamper = xlsxPackageStamper();
        var templateStream = ResourceUtils.getResource(Path.of("excel-base.xlsx"));

        var templatePackage = load(templateStream);
        var templateExpectedString = """
                A1: Hello
                B1: ${name}""";
        var templateActualString = xlsxToString(templatePackage);
        assertEquals(templateExpectedString, templateActualString);

        record Person(String name) {}
        var context = new Person("Bart");

        var stamped = stamper.stamp(templatePackage, context);

        var stampedExpectedString = """
                A1: Hello
                B1: Bart""";
        var stampedActualString = xlsxToString(stamped);
        assertEquals(stampedExpectedString, stampedActualString);
    }
}
