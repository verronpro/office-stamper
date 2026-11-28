package pro.verron.officestamper.docx;

import pro.verron.officestamper.api.Content;
import pro.verron.officestamper.api.Element;

import java.util.ArrayList;
import java.util.List;

/// docx4j 11 implementation of [Content]: a live view over a docx4j content list.
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

    @Override
    public List<Element> elements() {
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
            JaxbCompat.setParent(raw, container);
            backing.add(index + i, raw);
        }
    }

    @Override
    public void remove(Element element) {
        requireBacking();
        backing.remove(DocxElement.rawOf(element));
    }

    private void requireBacking() {
        if (!DocxElement.isContainer(container)) throw new UnsupportedOperationException(
                "This element has no mutable content");
    }
}
