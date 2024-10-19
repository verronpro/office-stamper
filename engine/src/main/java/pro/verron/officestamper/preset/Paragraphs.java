package pro.verron.officestamper.preset;

import org.docx4j.wml.SectPr;
import pro.verron.officestamper.api.Comment;

import java.util.Deque;
import java.util.List;
import java.util.Optional;

public record Paragraphs(
        Comment comment,
        Deque<Object> data,
        List<Object> elements,
        Optional<SectPr> previousSectionBreak,
        boolean oddNumberOfBreaks
) {
}
