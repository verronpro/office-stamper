package pro.verron.officestamper.docx4j11;

import org.docx4j.wml.CTSmartTagRun;
import org.docx4j.wml.ContentAccessor;
import pro.verron.officestamper.spi.Hooks;
import pro.verron.officestamper.spi.OfficePart;
import pro.verron.officestamper.spi.Tag;
import pro.verron.officestamper.utils.wml.DocxIterator;
import pro.verron.officestamper.utils.wml.WmlUtils;

import java.util.Collections;
import java.util.Iterator;

/// docx4j 11 implementation of {@link Hooks}: scans a part's content (recursing through paragraphs,
/// tables, SDTs, textboxes and drawings) for `officestamper` smart tags.
final class DocxHooks
        implements Hooks {

    private static final String TAG_ELEMENT = "officestamper";

    @Override
    public Iterator<Tag> ofHooks(OfficePart part) {
        if (!(part instanceof DocxPart docxPart)) throw new IllegalArgumentException(
                "OfficePart is not a docx4j part: " + part.getClass().getName());
        if (!(docxPart.part() instanceof ContentAccessor accessor)) return Collections.emptyIterator();
        return new DocxIterator(accessor)
                .filter(o -> o instanceof CTSmartTagRun tag && WmlUtils.isTagElement(tag, TAG_ELEMENT))
                .map(o -> new DocxTag(o, docxPart));
    }
}
