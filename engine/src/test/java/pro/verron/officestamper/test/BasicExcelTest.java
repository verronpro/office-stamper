package pro.verron.officestamper.test;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;

import static org.docx4j.openpackaging.packages.SpreadsheetMLPackage.load;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static pro.verron.officestamper.preset.ExperimentalStampers.xlsxStamper;
import static pro.verron.officestamper.test.IOStreams.getInputStream;
import static pro.verron.officestamper.test.IOStreams.getOutputStream;
import static pro.verron.officestamper.test.Stringifier.stringifyExcel;

@DisplayName("Basic Excel Test") class BasicExcelTest {

    @Test
    @DisplayName("Should stamp an Excel document")
    void testStamper()
            throws IOException, Docx4JException {

        var stamper = xlsxStamper();
        var templateStream = TestUtils.getResource(Path.of("excel-base.xlsx"));

        var templatePackage = load(templateStream);
        var templateExpectedString = """
                A1: Hello
                B1: ${name}""";
        var templateActualString = stringifyExcel(templatePackage);
        assertEquals(templateExpectedString, templateActualString);
        var stampedOutputStream = getOutputStream();
        record Person(String name) {}
        var context = new Person("Bart");

        stamper.stamp(templatePackage, context, stampedOutputStream);

        var stampedReadStream = getInputStream(stampedOutputStream);
        var stampedPackage = load(stampedReadStream);
        var stampedExpectedString = """
                A1: Hello
                B1: Bart""";
        var stampedActualString = stringifyExcel(stampedPackage);
        assertEquals(stampedExpectedString, stampedActualString);
    }
}
