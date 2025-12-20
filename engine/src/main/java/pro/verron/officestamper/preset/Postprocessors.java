package pro.verron.officestamper.preset;

import pro.verron.officestamper.api.OfficeStamperException;
import pro.verron.officestamper.api.PostProcessor;
import pro.verron.officestamper.preset.postprocessors.cleanendnotes.RemoveOrphanedEndnotesProcessor;
import pro.verron.officestamper.preset.postprocessors.cleanfootnotes.RemoveOrphanedFootnotesProcessor;
import pro.verron.officestamper.preset.postprocessors.cleantags.RemoveTagsProcessor;

/// The Postprocessors class provides static utility methods for obtaining implementations of the [PostProcessor]
/// interface that perform specific post-processing operations on WordprocessingMLPackage documents.
///
/// This class is a utility class and cannot be instantiated.
public class Postprocessors {
    private Postprocessors() {
        throw new OfficeStamperException("This is a utility class and cannot be instantiated");
    }

    /// Creates a PostProcessor that removes orphaned footnotes from a WordprocessingMLPackage document. An orphaned
    /// footnote is a footnote that is defined in the document but does not have a corresponding reference in the main
    /// content. This PostProcessor ensures that such unused footnotes are cleaned up, maintaining the structural
    /// integrity of the document.
    ///
    /// @return a PostProcessor instance that performs the removal of orphaned footnotes
    public static PostProcessor removeOrphanedFootnotes() {
        return new RemoveOrphanedFootnotesProcessor();
    }

    /// Creates a PostProcessor that removes orphaned endnotes from a WordprocessingMLPackage document. An orphaned
    /// endnote is an endnote that is defined in the document but does not have a corresponding reference in the main
    /// content. This PostProcessor ensures that such unused endnotes are cleaned up, maintaining the integrity and
    /// consistency of the document.
    ///
    /// @return a PostProcessor instance that performs the removal of orphaned endnotes
    public static PostProcessor removeOrphanedEndnotes() {
        return new RemoveOrphanedEndnotesProcessor();
    }

    /// Creates a PostProcessor that removes specified XML elements from a WordprocessingMLPackage document. This
    /// PostProcessor searches for and removes all occurrences of the specified element tag within the document, helping
    /// to clean up temporary or unwanted markup that may have been used during the stamping process.
    ///
    /// @param element the name of the XML element to be removed from the document
    ///
    /// @return a [PostProcessor] instance that performs the removal of specified tags
    public static PostProcessor removeTags(String element) {
        return new RemoveTagsProcessor(element);
    }
}
