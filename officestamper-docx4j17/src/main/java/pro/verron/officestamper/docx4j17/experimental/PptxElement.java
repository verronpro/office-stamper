package pro.verron.officestamper.docx4j17.experimental;

import pro.verron.officestamper.api.Content;
import pro.verron.officestamper.api.Element;
import pro.verron.officestamper.api.RunProperties;

import java.util.List;

/// Minimal spi [Element] wrapper for pptx objects. The experimental pptx stamper
/// only needs to carry a raw value through the spi [Insert] container; no
/// structural navigation is provided.
record PptxElement(Object raw)
        implements Element {
    @Override
    public List<Element> children() {
        return List.of();
    }

    @Override
    public Content content() {
        throw new UnsupportedOperationException("Not a container element");
    }

    @Override
    public Element parent() {
        return null;
    }

    @Override
    public Element copy() {
        return this;
    }

    @Override
    public String text() {
        return String.valueOf(raw);
    }

    @Override
    public void setText(String text) {
        throw new UnsupportedOperationException("Cannot set text on this element");
    }

    @Override
    public boolean isRun() {
        return false;
    }

    @Override
    public RunProperties runProperties() {
        return null;
    }
}
