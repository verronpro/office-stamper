package pro.verron.officestamper.docx4j17;

import org.docx4j.wml.ContentAccessor;
import org.jvnet.jaxb.lang.Child;
import pro.verron.officestamper.spi.Content;
import pro.verron.officestamper.spi.Element;

import java.util.ArrayList;
import java.util.List;

/// docx4j 11 implementation of {@link Content}: a live view over a docx4j content list.
final class DocxContent
        implements Content {

    private final Object container;
    private final List<Object> backing;
    private final DocxPart owner;

    DocxContent(Object container, List<Object> backing, DocxPart owner) {
        this.container = container;
        this.backing = backing;
        this.owner = owner;
    }

    DocxContent(org.docx4j.openpackaging.parts.JaxbXmlPart part, DocxPart owner) {
        this(part instanceof ContentAccessor ca ? ca : null,
             part instanceof ContentAccessor ca ? ca.getContent() : null,
             owner);
    }

    @Override
    public List<Element> elements() {
        if (backing == null) return List.of();
        var out = new ArrayList<Element>();
        for (Object o : backing) {
            out.add(new DocxElement(o, owner));
        }
        return List.copyOf(out);
    }

    @Override
    public int indexOf(Element element) {
        requireBacking();
        return backing.indexOf(DocxElement.rawOf(element));
    }

    @Override
    public void addAll(int index, List<Element> elements) {
        requireBacking();
        for (int i = 0; i < elements.size(); i++) {
            var raw = DocxElement.rawOf(elements.get(i));
            if (raw instanceof Child child && container != null) child.setParent(container);
            backing.add(index + i, raw);
        }
    }

    @Override
    public void remove(Element element) {
        requireBacking();
        backing.remove(DocxElement.rawOf(element));
    }

    private void requireBacking() {
        if (backing == null) throw new UnsupportedOperationException(
                "This element has no mutable content");
    }
}
