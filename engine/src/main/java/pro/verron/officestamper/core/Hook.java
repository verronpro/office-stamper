package pro.verron.officestamper.core;

import org.docx4j.wml.CTSmartTagRun;
import org.docx4j.wml.CommentRangeStart;
import pro.verron.officestamper.api.Comment;
import pro.verron.officestamper.api.DocxPart;

import java.util.Objects;

public interface Hook {
    Hook NULL_HOOK = new Hook() {
        @Override
        public boolean run(
                EngineFactory engineFactory,
                ContextTree contextTree,
                OfficeStamperEvaluationContextFactory evaluationContextFactory
        ) {
            return false;
        }

        @Override
        public void setContextKey(String contextKey) {
            // DO NOTHING
        }
    };

    static boolean isTagElement(CTSmartTagRun tag, String expectedElement) {
        var actualElement = tag.getElement();
        return Objects.equals(expectedElement, actualElement);
    }

    static Hook newCommentHook(DocxPart part, CommentRangeStart commentRangeStart) {
        return part.comment(commentRangeStart.getId())
                   .map(c -> newCommentHook(part, c))
                   .orElse(NULL_HOOK);
    }

    private static Hook newCommentHook(DocxPart part, Comment comment) {
        return new CommentHook(part, comment);
    }

    static Hook newTagHook(DocxPart part, CTSmartTagRun tag) {
        return new TagHook(part, new Tag(part, tag));
    }

    boolean run(
            EngineFactory engineFactory,
            ContextTree contextTree,
            OfficeStamperEvaluationContextFactory evaluationContextFactory
    );

    void setContextKey(String contextKey);
}
