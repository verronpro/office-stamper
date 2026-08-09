package pro.verron.officestamper.utils.wml;

import org.docx4j.XmlUtils;
import org.docx4j.com.microsoft.schemas.office.word.x2010.wordprocessingShape.CTTextboxInfo;
import org.docx4j.com.microsoft.schemas.office.word.x2010.wordprocessingShape.CTWordprocessingShape;
import org.docx4j.dml.Graphic;
import org.docx4j.dml.GraphicData;
import org.docx4j.dml.wordprocessingDrawing.Anchor;
import org.docx4j.dml.wordprocessingDrawing.Inline;
import org.docx4j.mce.AlternateContent;
import org.docx4j.vml.CTTextbox;
import org.docx4j.vml.VmlShapeElements;
import org.docx4j.wml.*;
import org.jspecify.annotations.Nullable;
import pro.verron.officestamper.utils.UtilsException;
import pro.verron.officestamper.utils.iterator.ResetableIterator;

import java.util.*;
import java.util.function.Supplier;

import static org.docx4j.XmlUtils.unwrap;

/// An iterator that allows the traversal of objects within a
/// WordprocessingML-based document part. The iterator
/// supports nested structures, enabling iteration over content that may have
/// hierarchical data, like paragraphs,
/// structured document tags (SDTs), and runs.
///
/// This class implements the [ResetableIterator] interface, allowing for the
/// iteration to be reset to its initial
/// state, ensuring reusability of the same iterator instance.
public class DocxIterator implements ResetableIterator<Content.Element> {

    private final Supplier<Iterator<Content.Element>> supplier;
    private Queue<Iterator<Content.Element>> iteratorQueue;
    private Content.@Nullable Element next;

    /// Creates a new [DocxIterator] instance that iterates over the content of
    /// the given [ContentAccessor].
    ///
    /// @param parent the content accessor whose content will be
    /// iterated over
    public DocxIterator(Content content) {
        this(content.getContent());
    }

    public DocxIterator(List<Content.Element> elements) {
        this(elements::iterator);
    }

    private DocxIterator(Supplier<Iterator<Content.Element>> supplier) {
        this.supplier = supplier;
        initialize();
    }

    private void initialize() {
        var startingIterator = supplier.get();
        this.iteratorQueue = Collections.asLifoQueue(new ArrayDeque<>());
        this.iteratorQueue.add(startingIterator);
        this.next = startingIterator.hasNext() ? new Content.Element(unwrap(startingIterator.next().get())) : null;
    }

    /// Selects and casts elements of the specified class type from the
    /// iterator.
    ///
    /// @param aClass the class type to filter and cast elements to
    /// @param <T>    the type of elements to select
    /// @return a new [ResetableIterator] containing only elements of the
    /// specified class type
    public <T> ResetableIterator<T> selectClass(Class<T> aClass) {
        return filter(obj -> aClass.isInstance(obj.get())).map(obj -> aClass.cast(obj.get()));
    }

    @Override
    public void reset() {
        initialize();
    }

    @Override
    public boolean hasNext() {
        return next != null;
    }


    @Override
    public Content.Element next() {
        if (next == null) throw new NoSuchElementException("No more elements to iterate");

        var result = next;

        next = null;
        switch (result.get()) {
            case ContentAccessor contentAccessor -> {
                var content = contentAccessor.getContent();
                iteratorQueue.add(new ElementIterator(content.iterator()));
            }
            case SdtRun sdtRun -> {
                var sdtContent = sdtRun.getSdtContent();
                var content = sdtContent.getContent();
                iteratorQueue.add(new ElementIterator(content.iterator()));
            }
            case SdtBlock sdtBlock -> {
                var sdtContent = sdtBlock.getSdtContent();
                var content = sdtContent.getContent();
                iteratorQueue.add(new ElementIterator(content.iterator()));
            }
            case Pict pict -> {
                var content = pict.getAnyAndAny();
                iteratorQueue.add(new ElementIterator(content.iterator()));
            }
            case VmlShapeElements rr -> {
                var content = rr.getEGShapeElements().stream().map(XmlUtils::unwrap).toList();
                iteratorQueue.add(new ElementIterator(content.iterator()));
            }
            case CTTextbox tb -> {
                var content = tb.getTxbxContent();
                var contentContent = content.getContent();
                iteratorQueue.add(new ElementIterator(contentContent.iterator()));
            }
            case AlternateContent ac -> {
                var choiceList = ac.getChoice();
                iteratorQueue.add(new ElementIterator(choiceList.iterator()));
                var fallback = ac.getFallback();
                var fallbackContent = fallback.getAny();
                iteratorQueue.add(new ElementIterator(fallbackContent.iterator()));
            }
            case AlternateContent.Choice c -> {
                var content = c.getAny();
                iteratorQueue.add(new ElementIterator(content.iterator()));
            }
            case Drawing d -> {
                var content = d.getAnchorOrInline();
                iteratorQueue.add(new ElementIterator(content.iterator()));
            }
            case Anchor a -> {
                var content = List.of(a.getGraphic());
                iteratorQueue.add(new ElementIterator(content.iterator()));
            }
            case Graphic g -> {
                var content = List.of(g.getGraphicData());
                iteratorQueue.add(new ElementIterator(content.iterator()));
            }
            case GraphicData gd -> {
                var content = gd.getAny();
                iteratorQueue.add(new ElementIterator(content.iterator()));
            }
            case CTWordprocessingShape ws -> {
                var content = List.of(ws.getTxbx());
                iteratorQueue.add(new ElementIterator(content.iterator()));
            }
            case CTTextboxInfo ti -> {
                var content = List.of(ti.getTxbxContent());
                iteratorQueue.add(new ElementIterator(content.iterator()));
            }
            case Inline i -> {
                var content = List.of(i.getGraphic());
                iteratorQueue.add(new ElementIterator(content.iterator()));
            }
            case CTSdtCell c -> {
                var sdtContent = c.getSdtContent();
                var content = sdtContent.getContent();
                iteratorQueue.add(new ElementIterator(content.iterator()));
            }
            case Parent _ -> throw new UtilsException("Parent not supported");
            case Text _, ProofErr _ -> { /*DO NOTHING*/ }
            default -> { /*DO NOTHING*/ }
        }
        while (!iteratorQueue.isEmpty() && next == null) {
            var nextIterator = iteratorQueue.poll();
            if (nextIterator.hasNext()) {
                next = new Content.Element(unwrap(nextIterator.next().get()));
                iteratorQueue.add(nextIterator);
            }
        }
        return result;
    }

    private static class ElementIterator implements Iterator<Content.Element> {
        private final Iterator<?> iterator;

        public ElementIterator(Iterator<?> iterator) {
            this.iterator = iterator;
        }

        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        @Override
        public Content.Element next() {
            return new Content.Element(iterator.next());
        }
    }
}
