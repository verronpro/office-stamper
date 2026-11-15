package pro.verron.officestamper.core;

import pro.verron.officestamper.spi.Hook;
import pro.verron.officestamper.spi.OfficePart;
import pro.verron.officestamper.spi.Tag;

import java.util.ArrayList;
import java.util.List;

/// Interface for hooks that process specific parts of a document.
///
/// <p>Hook resolution: a hook tag carrying {@code type=processor} wraps a AnchoredComment range and drives a
/// AnchoredComment hook; any other hook tag drives a placeholder (inline) hook. AnchoredComment resolution relies on
/// raw-identity equality between the tag element and the AnchoredComment's start anchor parent (the provider
/// element adapters compare by wrapped native object).
public interface DocxHook
        extends Hook {

    /// Executes the hook's logic within the context of a document processing flow.
    ///
    /// @param engineFactory a factory responsible for creating instances of the [Engine] class, which may be
    ///         used during the execution of the hook's logic
    /// @param contextTree the root of the context tree, representing the hierarchical structure of context
    ///         branches available during document processing
    /// @param evaluationContextFactory a factory for creating evaluation contexts, which are used to
    ///         evaluate expressions and handle dynamic behavior during the document processing flow
    /// @return `true` if the execution of the hook was successful, otherwise `false`
    boolean run(
            EngineFactory engineFactory,
            ContextRoot contextTree,
            OfficeStamperEvaluationContextFactory evaluationContextFactory
    );

    /// Resolves the hooks of the given part into executable hook instances.
    ///
    /// <p>A type=processor tag resolves its anchored comment through the provider ({@code
    /// tag.asComment()} reads the comment range start embedded in the tag's own content — the
    /// legacy {@code newCommentHook} path); no identity matching is involved, so the resolution
    /// does not depend on JAXB parent back-pointers.
    ///
    /// @param part the document part to scan
    /// @return the resolved hooks, in document order
    static List<DocxHook> ofHooks(OfficePart part) {
        var discovery = part.hooks();
        var result = new ArrayList<DocxHook>();
        discovery.ofHooks(part).forEachRemaining(tag -> asHook(part, tag, result));
        return result;
    }

    private static void asHook(OfficePart part, Tag tag, List<DocxHook> result) {
        if ("processor".equals(tag.type())) {
            var comment = tag.asComment();
            if (comment != null) {
                result.add(new CommentHook(part, tag, comment));
                return;
            }
        }
        result.add(new TagHook(part, tag));
    }
}

