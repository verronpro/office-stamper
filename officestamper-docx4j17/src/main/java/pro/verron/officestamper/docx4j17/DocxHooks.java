package pro.verron.officestamper.docx4j17;

import org.docx4j.openpackaging.packages.OpcPackage;
import org.docx4j.wml.CTSmartTagRun;
import org.docx4j.wml.CommentRangeEnd;
import org.docx4j.wml.CommentRangeStart;
import org.docx4j.wml.ContentAccessor;
import org.docx4j.wml.R.CommentReference;
import pro.verron.officestamper.api.AnchoredComment;
import pro.verron.officestamper.api.Element;
import pro.verron.officestamper.api.Hooks;
import pro.verron.officestamper.api.OfficePart;
import pro.verron.officestamper.api.Tag;
import pro.verron.officestamper.docx4j17.wml.DocxIterator;
import pro.verron.officestamper.docx4j17.wml.WmlUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Objects;

/// docx4j 11 implementation of {@link Hooks}: scans a part or an element subtree (recursing through
/// paragraphs, tables, SDTs, textboxes and drawings) for `officestamper` smart tags, and resolves
/// the anchored comments of a part.
final class DocxHooks
        implements Hooks {

    private static final String TAG_ELEMENT = "officestamper";

    @Override
    public Iterator<Tag> ofHooks(OfficePart part) {
        if (!(part instanceof DocxPart docxPart)) throw new IllegalArgumentException(
                "OfficePart is not a docx4j part: " + part.getClass().getName());
        return scan(docxPart.part(), docxPart);
    }

    @Override
    public Iterator<Tag> ofHooks(Element scope) {
        if (!(scope instanceof DocxElement docxElement)) throw new IllegalArgumentException(
                "Element is not a docx4j element: " + scope.getClass().getName());
        // A top-level table may be JAXBElement-wrapped; unwrap so the scan sees the Tbl.
        return scan(org.docx4j.XmlUtils.unwrap(docxElement.raw()), docxElement.owner());
    }

    @Override
    public Iterator<AnchoredComment> ofComments(OfficePart part) {
        if (!(part instanceof DocxPart docxPart)) throw new IllegalArgumentException(
                "OfficePart is not a docx4j part: " + part.getClass().getName());
        var result = new ArrayList<AnchoredComment>();
        var iterator = new DocxIterator((ContentAccessor) docxPart.part()).selectClass(CommentRangeStart.class);
        while (iterator.hasNext()) {
            var comment = resolve(docxPart, iterator.next());
            if (comment != null) result.add(comment);
        }
        return result.iterator();
    }

    /// Resolves a comment range start into a fully-anchored comment (port of the legacy
    /// `CommentUtil.comment`), or {@code null} when the range or the comment object cannot resolve.
    /// Package-visible: {@link DocxTag#asComment()} resolves the comment embedded in a
    /// type=processor tag through this same path.
    static AnchoredComment resolve(DocxPart part, CommentRangeStart crs) {
        var pkg = part.part().getPackage();
        var iterator = new DocxIterator((ContentAccessor) part.part()).slice(crs, null);
        CommentRangeEnd cre = null;
        CommentReference cr = null;
        var commentId = crs.getId();
        while (iterator.hasNext() && (cr == null || cre == null)) {
            var element = iterator.next();
            if (element instanceof CommentRangeEnd found && cre == null
                    && Objects.equals(found.getId(), commentId)) {
                cre = found;
            } else if (element instanceof CommentReference found && cr == null
                    && Objects.equals(found.getId(), commentId)) {
                cr = found;
            }
        }
        if (cre == null) return null;
        var comment = DocxComments.byId(pkg, commentId);
        if (comment == null) return null;
        var startTagRun = crs.getParent() instanceof CTSmartTagRun smartTag ? smartTag : null;
        return DocxAnchoredComment.of(part, startTagRun, crs, cre, comment, cr);
    }

    private static Iterator<Tag> scan(Object root, DocxPart owner) {
        if (!(root instanceof ContentAccessor accessor)) return Collections.emptyIterator();
        return new DocxIterator(accessor)
                .filter(o -> o instanceof CTSmartTagRun tag && WmlUtils.isTagElement(tag, TAG_ELEMENT))
                .map(o -> new DocxTag(o, owner));
    }
}
