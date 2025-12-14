package pro.verron.officestamper.utils.pml;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.PresentationMLPackage;
import org.docx4j.openpackaging.parts.PresentationML.SlidePart;
import org.docx4j.wml.ContentAccessor;
import org.docx4j.wml.SdtBlock;
import org.docx4j.wml.SdtRun;
import org.jspecify.annotations.Nullable;
import org.pptx4j.Pptx4jException;
import org.pptx4j.pml.Shape;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.verron.officestamper.utils.UtilsException;
import pro.verron.officestamper.utils.iterator.ResetableIterator;

import java.util.*;
import java.util.function.Supplier;

import static org.docx4j.XmlUtils.unwrap;

public class PptxIterator
        implements ResetableIterator<Object> {

    private static final Logger log = LoggerFactory.getLogger(PptxIterator.class);
    private final Supplier<Iterator<?>> supplier;
    private Queue<Iterator<?>> iteratorQueue;
    private @Nullable Object next;

    public PptxIterator(PresentationMLPackage presentation) {
        try {
            var mainPresentationPart = presentation.getMainPresentationPart();
            var slideParts = mainPresentationPart.getSlideParts();
            supplier = slideParts::iterator;
        } catch (Pptx4jException e) {
            throw new UtilsException(e);
        }
        var startingIterator = supplier.get();
        this.iteratorQueue = Collections.asLifoQueue(new ArrayDeque<>());
        this.iteratorQueue.add(startingIterator);
        this.next = startingIterator.hasNext() ? unwrap(startingIterator.next()) : null;
    }

    @Override
    public void reset() {
        var startingIterator = supplier.get();
        this.iteratorQueue = Collections.asLifoQueue(new ArrayDeque<>());
        this.iteratorQueue.add(startingIterator);
        this.next = startingIterator.hasNext() ? unwrap(startingIterator.next()) : null;
    }

    @Override
    public boolean hasNext() {
        return next != null;
    }

    @Override
    public Object next() {
        if (next == null) throw new NoSuchElementException("No more elements to iterate");

        var result = next;
        next = null;
        switch (result) {
            case ContentAccessor contentAccessor -> {
                var content = contentAccessor.getContent();
                iteratorQueue.add(content.iterator());
            }
            case SdtRun sdtRun -> {
                var sdtContent = sdtRun.getSdtContent();
                var content = sdtContent.getContent();
                iteratorQueue.add(content.iterator());
            }
            case SdtBlock sdtBlock -> {
                var sdtContent = sdtBlock.getSdtContent();
                var content = sdtContent.getContent();
                iteratorQueue.add(content.iterator());
            }
            case SlidePart slidePart -> {
                List<Object> content;
                try {
                    content = slidePart.getContents()
                                       .getCSld()
                                       .getSpTree()
                                       .getSpOrGrpSpOrGraphicFrame();
                } catch (Docx4JException e) {
                    throw new UtilsException(e);
                }
                iteratorQueue.add(content.iterator());
            }
            case Shape shape -> {
                var content = shape.getTxBody()
                                   .getP();
                iteratorQueue.add(content.iterator());
            }
            default -> log.debug("Unknown type: {}", result.getClass());
        }
        while (!iteratorQueue.isEmpty() && next == null) {
            var nextIterator = iteratorQueue.poll();
            if (nextIterator.hasNext()) {
                next = unwrap(nextIterator.next());
                iteratorQueue.add(nextIterator);
            }
        }
        return result;
    }
}
