package pro.verron.officestamper.docx4j17;

import org.docx4j.wml.R;
import org.docx4j.wml.RPr;
import pro.verron.officestamper.spi.Element;
import pro.verron.officestamper.spi.ElementFactory;
import pro.verron.officestamper.spi.RunProperties;
import pro.verron.officestamper.docx4j17.wml.WmlFactory;

import java.util.List;

/// docx4j 11 implementation of {@link ElementFactory}.
final class DocxElementFactory
        implements ElementFactory {

    private final DocxPart owner;

    DocxElementFactory(DocxPart owner) {
        this.owner = owner;
    }

    @Override
    public Element run(String text) {
        return new DocxElement(WmlFactory.newRun(text), owner);
    }

    @Override
    public Element run(String text, RunProperties properties) {
        var r = WmlFactory.newRun(text);
        if (properties != null) r.setRPr(nativeRPr(properties));
        return new DocxElement(r, owner);
    }

    @Override
    public Element lineBreak(String text) {
        return new DocxElement(WmlFactory.newRun(List.of(WmlFactory.newText(text), WmlFactory.newBr())), owner);
    }

    @Override
    public Element paragraph(String text) {
        return new DocxElement(WmlFactory.newParagraph(text), owner);
    }

    @Override
    public Element paragraph(List<Element> content) {
        var raws = content.stream()
                .map(DocxElement::rawOf)
                .toList();
        return new DocxElement(WmlFactory.newParagraph(raws), owner);
    }

    private static RPr nativeRPr(RunProperties properties) {
        if (properties instanceof DocxRunProperties docxProperties) return docxProperties.rPr();
        throw new IllegalArgumentException(
                "Unsupported RunProperties implementation: " + properties.getClass().getName());
    }
}
