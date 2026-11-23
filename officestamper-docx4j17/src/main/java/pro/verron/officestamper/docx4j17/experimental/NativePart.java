package pro.verron.officestamper.docx4j17.experimental;

import org.docx4j.openpackaging.parts.JaxbXmlPart;
import pro.verron.officestamper.api.Comment;
import pro.verron.officestamper.api.Content;
import pro.verron.officestamper.api.ElementFactory;
import pro.verron.officestamper.api.Hooks;
import pro.verron.officestamper.api.OfficeDocument;
import pro.verron.officestamper.api.OfficePart;
import pro.verron.officestamper.api.OfficeStamperException;

import java.util.List;

/// Minimal [OfficePart] adapter over a docx4j [JaxbXmlPart], used by the experimental
/// pptx/xlsx document adapters. The processor-pipeline services (content view, hooks,
/// element factory) are docx-specific and are not provided for these experimental formats.
final class NativePart implements OfficePart {

    private final JaxbXmlPart part;
    private final OfficeDocument document;

    NativePart(JaxbXmlPart part, OfficeDocument document) {
        this.part = part;
        this.document = document;
    }

    @Override
    public String name() {
        return part.getPartName().getName();
    }

    @Override
    public Content content() {
        throw new OfficeStamperException(
                "content() is not supported for experimental pptx/xlsx parts");
    }

    @Override
    public List<Comment> comments() {
        return List.of();
    }

    @Override
    public OfficeDocument document() {
        return document;
    }

    @Override
    public Hooks hooks() {
        throw new OfficeStamperException(
                "hooks() is not supported for experimental pptx/xlsx parts");
    }

    @Override
    public ElementFactory factory() {
        throw new OfficeStamperException(
                "factory() is not supported for experimental pptx/xlsx parts");
    }

    @Override
    public <T> T unwrap(Class<T> type) {
        if (type.isInstance(part)) return type.cast(part);
        throw new IllegalArgumentException("Cannot unwrap OfficePart as " + type.getName());
    }
}
