package org.wickedsource.docxstamper;

import java.nio.file.Path;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import static org.wickedsource.docxstamper.DefaultTests.getResource;
import org.wickedsource.docxstamper.api.DocxStamperException;
import pro.verron.docxstamper.utils.TestDocxStamper;
import pro.verron.docxstamper.utils.context.Contexts.Characters;
import pro.verron.docxstamper.utils.context.Contexts.Role;

/**
 *
 * @author jenei.attila
 */
public class RepeatDocPartBadExpressionTest{
    
    @Test
    public void testBadExpressionShouldNotBlockCallerThread(){
        var template = getResource(Path.of("RepeatDocPartBadExpressionTest.docx"));
        var context = new Characters(List.of(new Role("Homer Simpson",
                                                  "Dan Castellaneta"),
                                         new Role("Marge Simpson",
                                                  "Julie Kavner"),
                                         new Role("Bart Simpson",
                                                  "Nancy Cartwright")));
        
        var stamper = new TestDocxStamper<>(new DocxStamperConfiguration());
        
        var exception = assertThrows(DocxStamperException.class, () -> stamper.stampAndLoadAndExtract(template, context));
        assertTrue(exception.getMessage().contains("${someUnknownField}"));
    }
}
