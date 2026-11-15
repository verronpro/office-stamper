package pro.verron.officestamper.docx4j11;

import org.docx4j.wml.CTSmartTagRun;
import org.docx4j.wml.ContentAccessor;
import pro.verron.officestamper.spi.Element;
import pro.verron.officestamper.spi.Hooks;
import pro.verron.officestamper.spi.OfficePart;
import pro.verron.officestamper.spi.Tag;
import pro.verron.officestamper.utils.wml.DocxIterator;
import pro.verron.officestamper.utils.wml.WmlUtils;

import java.util.Collections;
import java.util.Iterator;

/// docx4j 11 implementation of {@link Hooks}: scans a part or an element subtree (recursing through
/// paragraphs, tables, SDTs, textboxes and drawings) for `officestamper` smart tags.
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
        return scan(docxElement.raw(), docxElement.owner());
    }

    private static Iterator<Tag> scan(Object root, DocxPart owner) {
        if (!(root instanceof ContentAccessor accessor)) return Collections.emptyIterator();
        return new DocxIterator(accessor)
                .filter(o -> o instanceof CTSmartTagRun tag && WmlUtils.isTagElement(tag, TAG_ELEMENT))
                .map(o -> new DocxTag(o, owner));
    }
}
