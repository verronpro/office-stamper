package pro.verron.officestamper.core;

import org.docx4j.wml.CTSmartTagRun;
import org.docx4j.wml.CommentRangeStart;
import org.docx4j.wml.ContentAccessor;
import pro.verron.officestamper.api.Comment;
import pro.verron.officestamper.api.DocxPart;
import pro.verron.officestamper.utils.iterator.ResetableIterator;
import pro.verron.officestamper.utils.wml.DocxIterator;

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

    static ResetableIterator<Hook> ofHooks(ContentAccessor contentAccessor, DocxPart part) {
        return new DocxIterator(contentAccessor).filter(Hook::isPotentialHook)
                                                .map(o -> asHook(part, o));
    }

    static boolean isPotentialHook(Object o) {
        return switch (o) {
            case CommentRangeStart _ -> true;
            case CTSmartTagRun tag when isTagElement(tag, "officestamper") -> true;
            default -> false;
        };
    }

    static Hook asHook(DocxPart part, Object o) {
        return switch (o) {
            case CommentRangeStart commentRangeStart -> newCommentHook(part, commentRangeStart);
            case CTSmartTagRun tag -> newTagHook(part, tag);
            default -> throw new IllegalArgumentException("Unexpected value: " + o);
        };
    }

    static boolean isTagElement(CTSmartTagRun tag, String expectedElement) {
        var actualElement = tag.getElement();
        return Objects.equals(expectedElement, actualElement);
    }

    static Hook newCommentHook(DocxPart part, CommentRangeStart commentRangeStart) {
        return part.comment(commentRangeStart.getId())
                   .map(c -> newCommentHook(part, c))
                   .orElse(NULL_HOOK);
    }

    static Hook newTagHook(DocxPart part, CTSmartTagRun tag) {
        return new TagHook(part, new Tag(part, tag));
    }

    private static Hook newCommentHook(DocxPart part, Comment comment) {
        return new CommentHook(part, comment);
    }

    boolean run(
            EngineFactory engineFactory,
            ContextTree contextTree,
            OfficeStamperEvaluationContextFactory evaluationContextFactory
    );

    void setContextKey(String contextKey);
}
