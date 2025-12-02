package pro.verron.officestamper.core;

import org.docx4j.wml.CTSmartTagRun;
import org.docx4j.wml.CommentRangeStart;
import pro.verron.officestamper.api.DocxPart;
import pro.verron.officestamper.api.Tag;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

public interface Hook {
    static Filter<Object, Optional<Hook>> filter(DocxPart part) {
        return new Filter<>() {
            @Override
            public Predicate<Object> filter() {
                return o -> o instanceof CommentRangeStart || (o instanceof CTSmartTagRun tag && isTagElement(tag,
                        "officestamper"));
            }

            @Override
            public Function<Object, Optional<Hook>> mapper() {
                return o -> switch (o) {
                    case CommentRangeStart commentRangeStart -> Hook.of(part, commentRangeStart);
                    case CTSmartTagRun tag -> Hook.of(part, tag);
                    default -> throw new IllegalArgumentException("Unexpected value: " + o);
                };
            }
        };
    }

    static boolean isTagElement(CTSmartTagRun tag, String expectedElement) {
        var actualElement = tag.getElement();
        return Objects.equals(expectedElement, actualElement);
    }

    private static Optional<Hook> of(DocxPart part, CommentRangeStart commentRangeStart) {
        return part.getComment(commentRangeStart)
                   .map(c -> new CommentHook(part, c));
    }

    private static Optional<Hook> of(DocxPart part, CTSmartTagRun tag) {
        return Optional.of(new TagHook(part, new Tag(part, tag)));
    }

    boolean run(
            EngineFactory engineFactory,
            ContextTree contextTree,
            EvaluationContextFactory evaluationContextFactory
    );
}
