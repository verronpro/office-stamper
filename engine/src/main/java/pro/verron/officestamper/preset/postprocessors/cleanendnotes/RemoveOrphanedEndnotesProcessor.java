package pro.verron.officestamper.preset.postprocessors.cleanendnotes;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.EndnotesPart;
import org.docx4j.wml.CTEndnotes;
import org.docx4j.wml.CTFtnEdn;
import pro.verron.officestamper.api.PostProcessor;
import pro.verron.officestamper.preset.postprocessors.NoteRefsVisitor;
import pro.verron.officestamper.utils.wml.WmlUtils;

import java.util.Collection;
import java.util.Optional;

import static org.docx4j.wml.STFtnEdn.NORMAL;
import static pro.verron.officestamper.api.OfficeStamperException.throwing;
import static pro.verron.officestamper.utils.wml.WmlUtils.visitDocument;

/// A post-processor that removes endnotes which are not referenced in the document.
///
/// This processor analyzes the document to find all endnote references and then removes any endnotes that are not
/// referenced. It helps keep the document clean by eliminating orphaned endnotes.
public class RemoveOrphanedEndnotesProcessor
        implements PostProcessor {
    @Override
    public void process(WordprocessingMLPackage document) {
        var visitor = new NoteRefsVisitor();
        visitDocument(document, visitor);
        var referencedNoteIds = visitor.referencedNoteIds();
        var mainDocumentPart = document.getMainDocumentPart();

        var ednPart = mainDocumentPart.getEndNotesPart();
        Optional.ofNullable(ednPart)
                .stream()
                .map(throwing(EndnotesPart::getContents))
                .map(CTEndnotes::getEndnote)
                .flatMap(Collection::stream)
                .filter(RemoveOrphanedEndnotesProcessor::normalNotes)
                .filter(note -> !referencedNoteIds.contains(note.getId()))
                .toList()
                .forEach(WmlUtils::remove);
    }

    private static boolean normalNotes(CTFtnEdn note) {
        return Optional.ofNullable(note.getType())
                       .orElse(NORMAL)
                       .equals(NORMAL);
    }
}
