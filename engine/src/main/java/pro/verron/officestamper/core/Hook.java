package pro.verron.officestamper.core;

import org.docx4j.wml.CTSmartTagRun;
import org.docx4j.wml.CommentRangeStart;
import org.docx4j.wml.ContentAccessor;
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
        return o instanceof CTSmartTagRun tag && isTagElement(tag, "officestamper");
    }

    static Hook asHook(DocxPart part, Object o) {
        return switch (o) {
            case CTSmartTagRun tag when isTagElement(tag, "officestamper") && isType(tag, "cProcessor") ->
                    newCommentHook(part, tag);
            case CTSmartTagRun tag when isTagElement(tag, "officestamper") -> new TagHook(part, new Tag(part, tag));
            default -> throw new IllegalArgumentException("Unexpected value: " + o);
        };
    }

    static boolean isTagElement(CTSmartTagRun tag, String expectedElement) {
        var actualElement = tag.getElement();
        return Objects.equals(expectedElement, actualElement);
    }

    static boolean isType(CTSmartTagRun tag, String type) {
        return tag.getSmartTagPr()
                  .getAttr()
                  .stream()
                  .anyMatch(attr -> "type".equals(attr.getName()) && type.equals(attr.getVal()));
    }

    static Hook newCommentHook(DocxPart part, CTSmartTagRun tag) {
        var tagContent = tag.getContent();
        var commentRangeStart = (CommentRangeStart) tagContent.getFirst();
        var myTag = new Tag(part, tag);
        var comment = CommentUtil.comment(part, commentRangeStart, part.document(), part::content);
        return new CommentHook(part, myTag, comment);
    }

    boolean run(
            EngineFactory engineFactory,
            ContextTree contextTree,
            OfficeStamperEvaluationContextFactory evaluationContextFactory
    );

    void setContextKey(String contextKey);
}
