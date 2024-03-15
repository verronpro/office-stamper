package pro.verron.docxstamper.test;

import org.junit.jupiter.api.Test;
import pro.verron.docxstamper.preset.Configurations;
import pro.verron.docxstamper.test.utils.TestDocxStamper;
import pro.verron.docxstamper.test.utils.context.Contexts;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.wickedsource.docxstamper.test.DefaultTests.getResource;

/**
 * A test class that verifies that stampTable feature works correctly
 */
class StampTableTest {

    @Test
    void stampTableTest() {

        var testDocx = getResource(Path.of("StampTableTest.docx"));

        var configuration = Configurations.standard();
        var stamper = new TestDocxStamper<>(configuration);

        String string = stamper.stampAndLoadAndExtract(
                testDocx,
                Contexts.characterTable(
                        List.of("Character", "Actor"),
                        List.of(
                                List.of("Homer Simpson", "Dan Castellaneta"),
                                List.of("Marge Simpson", "Julie Kavner"),
                                List.of("Bart Simpson", "Nancy Cartwright"),
                                List.of("Kent Brockman", "Harry Shearer"),
                                List.of("Disco Stu", "Hank Azaria"),
                                List.of("Krusty the Clown", "Dan Castellaneta")
                        )
                )
        );
        assertEquals("""
                             Stamping Table
                             List of Simpsons characters
                             Character
                             Actor
                             Homer Simpson
                             Dan Castellaneta
                             Marge Simpson
                             Julie Kavner
                             Bart Simpson
                             Nancy Cartwright
                             Kent Brockman
                             Harry Shearer
                             Disco Stu
                             Hank Azaria
                             Krusty the Clown
                             Dan Castellaneta
                                                          
                             There are 6 characters in the above table.""",
                     string);
    }
}
