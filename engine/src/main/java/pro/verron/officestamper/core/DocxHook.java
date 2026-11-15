package pro.verron.officestamper.core;

import pro.verron.officestamper.spi.AnchoredComment;
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
    /// @param part the document part to scan
    /// @return the resolved hooks, in document order
    static List<DocxHook> ofHooks(OfficePart part) {
        var discovery = part.hooks();
        var comments = new ArrayList<AnchoredComment>();
        discovery.ofComments(part).forEachRemaining(comments::add);
        var result = new ArrayList<DocxHook>();
        discovery.ofHooks(part).forEachRemaining(tag -> asHook(part, tag, comments, result));
        return result;
    }

    private static void asHook(
            OfficePart part,
            Tag tag,
            List<AnchoredComment> comments,
            List<DocxHook> result
    ) {
        if ("processor".equals(tag.type())) {
            comments.stream()
                    .filter(AnchoredComment -> tag.equals(AnchoredComment.startAnchor().parent()))
                    .findFirst()
                    .ifPresent(AnchoredComment -> result.add(new CommentHook(part, tag, AnchoredComment)));
        }
        else {
            result.add(new TagHook(part, tag));
        }
    }
}

