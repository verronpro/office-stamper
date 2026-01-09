package pro.verron.officestamper.core;

import org.docx4j.wml.CTSmartTagRun;
import org.docx4j.wml.CommentRangeStart;
import org.docx4j.wml.ContentAccessor;
import pro.verron.officestamper.api.DocxPart;
import pro.verron.officestamper.api.Hook;
import pro.verron.officestamper.utils.iterator.ResetableIterator;
import pro.verron.officestamper.utils.wml.DocxIterator;

import static pro.verron.officestamper.utils.wml.WmlUtils.isTagElement;

/// Interface for hooks that process specific parts of a DOCX document.
public interface DocxHook
        extends Hook {

    /// Creates an iterator over the hooks in the given content accessor.
    ///
    /// @param contentAccessor the content accessor to search for hooks.
    /// @param part the document part.
    ///
    /// @return an iterator over the found hooks.
    static ResetableIterator<DocxHook> ofHooks(ContentAccessor contentAccessor, DocxPart part) {
        return new DocxIterator(contentAccessor).filter(DocxHook::isPotentialHook)
                                                .map(o -> asHook(part, o));
    }

    /// Checks if the given object is a potential hook.
    ///
    /// @param o the object to check.
    ///
    /// @return `true` if it is a potential hook.
    static boolean isPotentialHook(Object o) {
        return o instanceof CTSmartTagRun tag && isTagElement(tag, "officestamper");
    }

    /// Converts an object to a hook.
    ///
    /// @param part the document part.
    /// @param o    the object to convert.
    /// @return the hook.
    static DocxHook asHook(DocxPart part, Object o) {
        return switch (o) {
            case CTSmartTagRun tag when isType(tag, "processor", "type") -> newCommentHook(part, tag);
            case CTSmartTagRun tag -> new TagHook(part, new Tag(part, tag));
            default -> throw new IllegalArgumentException("Unexpected value: " + o);
        };
    }

    /// Checks if the given tag is of the specified type.
    ///
    /// @param tag     the tag to check.
    /// @param type    the expected type value.
    /// @param typeKey the attribute name for the type.
    /// @return `true` if the tag matches the type.
    static boolean isType(CTSmartTagRun tag, String type, String typeKey) {
        return tag.getSmartTagPr()
                  .getAttr()
                  .stream()
                  .anyMatch(attr -> typeKey.equals(attr.getName()) && type.equals(attr.getVal()));
    }

    /// Creates a new comment hook.
    ///
    /// @param part the document part.
    /// @param tag  the tag.
    /// @return the comment hook.
    static DocxHook newCommentHook(DocxPart part, CTSmartTagRun tag) {
        var tagContent = tag.getContent();
        var commentRangeStart = (CommentRangeStart) tagContent.getFirst();
        var myTag = new Tag(part, tag);
        var comment = CommentUtil.comment(part, commentRangeStart, part.document(), part::content);
        return new CommentHook(part, myTag, comment);
    }

    /// Executes the hook's logic within the context of a document processing flow.
    ///
    /// @param engineFactory               a factory responsible for creating instances of the `Engine` class, which
    /// may be
    ///                                    used during the execution of the hook's logic
    /// @param contextTree                 the root of the context tree, representing the hierarchical structure of
    /// context
    ///                                    branches available during document processing
    /// @param officeStamperContextFactory a factory for creating evaluation contexts, which are used to
    ///                                    evaluate expressions and handle dynamic behavior during the document
    /// processing flow
    /// @return `true` if the execution of the hook was successful, otherwise `false`
    boolean run(
            EngineFactory engineFactory,
            ContextRoot contextTree,
            OfficeStamperEvaluationContextFactory officeStamperContextFactory
    );
}
