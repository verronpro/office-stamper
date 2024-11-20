package pro.verron.officestamper.test;

import org.junit.jupiter.api.Test;
import pro.verron.officestamper.preset.OfficeStamperConfigurations;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static pro.verron.officestamper.test.TestUtils.getResource;


/**
 * A test class that verifies that stampTable feature works correctly
 */
class StampTableTest {

    public static final ContextFactory FACTORY = ContextFactory.objectContextFactory();

    @Test void stampTableTest() {
        var testDocx = getResource("StampTableTest.docx");

        var configuration = OfficeStamperConfigurations.standard();
        var stamper = new TestDocxStamper<>(configuration);

        String string = stamper.stampAndLoadAndExtract(testDocx,
                FACTORY.characterTable(List.of("Character", "Actor"),
                        List.of(List.of("Homer Simpson", "Dan Castellaneta"),
                                List.of("Marge Simpson", "Julie Kavner"),
                                List.of("Bart Simpson", "Nancy Cartwright"),
                                List.of("Kent Brockman", "Harry Shearer"),
                                List.of("Disco Stu", "Hank Azaria"),
                                List.of("Krusty the Clown", "Dan Castellaneta"))));
        assertEquals("""
                Stamping Table
                List of Simpsons characters
                |===
                |Character
                |Actor
                
                |Homer Simpson
                |Dan Castellaneta
                
                |Marge Simpson
                |Julie Kavner
                
                |Bart Simpson
                |Nancy Cartwright
                
                |Kent Brockman
                |Harry Shearer
                
                |Disco Stu
                |Hank Azaria
                
                |Krusty the Clown
                |Dan Castellaneta
                
                
                |===
                
                There are 6 characters in the above table.
                """, string);
    }
}
