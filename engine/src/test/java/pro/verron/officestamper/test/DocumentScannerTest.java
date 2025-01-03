package pro.verron.officestamper.test;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.wml.P;
import org.docx4j.wml.R;
import org.docx4j.wml.Text;
import org.junit.jupiter.api.Test;
import pro.verron.officestamper.utils.DocumentScanner;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static pro.verron.officestamper.test.TestUtils.makeResource;

class DocumentScannerTest {
    @Test
    void test()
            throws Docx4JException {
        var test = makeResource("""
                Test1
                Test2
                """);
        var loaded = WordprocessingMLPackage.load(test);
        var iterator = new DocumentScanner(loaded);
        List<String> actual = new ArrayList<>();
        while (iterator.hasNext()) {
            var current = iterator.next();
            actual.add(stringify(current));
        }
        List<String> expected = List.of("MainDocumentPart: size=5224",
                "P: Test1",
                "R: size=1",
                "Text: Test1",
                "P: Test2",
                "R: size=1",
                "Text: Test2");
        assertEquals(expected, actual);
    }

    private String stringify(Object current) {
        Class<?> currentClass = current.getClass();
        var simpleName = currentClass.getSimpleName();
        var representation = switch (current) {
            case MainDocumentPart mdp -> "size=" + mdp.getContentLengthAsLoaded();
            case P p -> String.valueOf(p);
            case R r -> "size=" + r.getContent()
                                   .size();
            case Text text -> String.valueOf(text.getValue());
            default -> throw new IllegalStateException("Unexpected value type: " + currentClass);
        };
        return "%s: %s".formatted(simpleName, representation);
    }

}
