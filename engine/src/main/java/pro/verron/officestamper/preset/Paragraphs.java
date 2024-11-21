package pro.verron.officestamper.preset;

import org.docx4j.wml.SectPr;
import pro.verron.officestamper.api.Comment;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;

public record Paragraphs(
        Comment comment,
        Iterator<Object> data,
        List<Object> elements,
        Optional<SectPr> previousSectionBreak,
        boolean oddNumberOfBreaks
) {
    public <T> List<T> elements(Class<T> aClass) {
        return elements()
                .stream()
                .filter(aClass::isInstance)
                .map(aClass::cast)
                .toList();
    }
}
