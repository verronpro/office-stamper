package pro.verron.officestamper.docx4j11;

import org.docx4j.wml.ContentAccessor;
import pro.verron.officestamper.spi.Content;
import pro.verron.officestamper.spi.Element;

import java.util.ArrayList;
import java.util.List;

/// docx4j 11 implementation of {@link Content}.
public final class DocxContent implements Content {

    private final List<Element> elements;

    DocxContent(org.docx4j.openpackaging.parts.JaxbXmlPart part, DocxPart owner) {
        var list = new ArrayList<Element>();
        if (part instanceof ContentAccessor ca) {
            for (Object o : ca.getContent()) {
                list.add(new DocxElement(o, owner));
            }
        }
        this.elements = List.copyOf(list);
    }

    @Override
    public List<Element> elements() {
        return elements;
    }
}
