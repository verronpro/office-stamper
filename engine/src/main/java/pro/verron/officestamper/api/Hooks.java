package pro.verron.officestamper.api;

import org.docx4j.wml.CTSmartTagRun;
import org.docx4j.wml.ContentAccessor;
import pro.verron.officestamper.utils.iterator.ResetableIterator;
import pro.verron.officestamper.utils.wml.DocxIterator;
import pro.verron.officestamper.utils.wml.WmlUtils;

/// Provides utility methods for working with [Hook] instances in the context of a WordprocessingML-based document. The
/// `Hooks` class contains static methods to create and manage [ResetableIterator] instances of hooks for a given
/// document part. These hooks allow the customization or extension of document processing behavior in a flexible and
/// context-driven manner.
public class Hooks {

    private Hooks() {
        throw new IllegalStateException("Utility class");
    }

    /// Creates a [ResetableIterator] of Hook instances for a specific document part, based on the provided content
    /// accessor. This method filters potential hooks from the content and maps them to Hook instances associated with
    /// the given document part.
    ///
    /// @param contentAccessor the content accessor providing access to the document's content
    ///
    /// @return a [ResetableIterator] of Hook instances for the specified document part
    public static ResetableIterator<Hook> ofHooks(ContentAccessor contentAccessor) {
        return new DocxIterator(contentAccessor).filter(Hooks::isHook)
                                                .map(Hooks::asHook);
    }

    private static boolean isHook(Object o) {
        return o instanceof CTSmartTagRun tag && WmlUtils.isTagElement(tag, "officestamper");
    }

    private static Hook asHook(Object o) {
        if (o instanceof CTSmartTagRun tag && WmlUtils.isTagElement(tag, "officestamper"))
            return contextKey -> WmlUtils.setTagAttribute(tag, "context", contextKey);
        throw new IllegalArgumentException("Unexpected value: " + o);
    }
}
