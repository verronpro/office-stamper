package pro.verron.docxstamper.core;

import org.docx4j.dml.CTRegularTextRun;
import org.docx4j.dml.CTTextBody;
import org.docx4j.dml.CTTextParagraph;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.PresentationMLPackage;
import org.docx4j.openpackaging.parts.*;
import org.docx4j.openpackaging.parts.PresentationML.*;
import org.docx4j.openpackaging.parts.WordprocessingML.ImageJpegPart;
import org.pptx4j.pml.*;
import pro.verron.docxstamper.api.OfficeStamperException;

import java.util.List;
import java.util.Map;

abstract class PowerpointVisitor {

    public final void visit(Object object) {
        before(object);
        try {
            if (object instanceof PresentationMLPackage element) visit(element.getParts());

            else if (object instanceof PartName ignored) { /* Do nothing */ }
            else if (object instanceof Parts element) visit(element.getParts());
            else if (object instanceof SlideLayoutPart ignored) { /* Do nothing */ }
            else if (object instanceof ImageJpegPart ignored) { /* Do nothing */ }
            else if (object instanceof ThemePart ignored) { /* Do nothing */ }
            else if (object instanceof DocPropsCorePart ignored) { /* Do nothing */ }
            else if (object instanceof DocPropsExtendedPart ignored) { /* Do nothing */ }
            else if (object instanceof SlideMasterPart ignored) { /* Do nothing */ }
            else if (object instanceof ViewPropertiesPart ignored) { /* Do nothing */ }
            else if (object instanceof PresentationPropertiesPart ignored) { /* Do nothing */ }
            else if (object instanceof TableStylesPart ignored) { /* Do nothing */ }
            else if (object instanceof MainPresentationPart element) visit(element.getContents());

            else if (object instanceof List<?> elements) elements.forEach(this::visit);
            else if (object instanceof Map<?, ?> elements) elements.forEach(this::visit);
            else if (object instanceof SlidePart element) visit(element.getContents());
            else if (object instanceof Sld element) visit(element.getCSld());
            else if (object instanceof CommonSlideData element) visit(element.getSpTree());
            else if (object instanceof GroupShape element) visit(element.getSpOrGrpSpOrGraphicFrame());
            else if (object instanceof Shape element) visit(element.getTxBody());
            else if (object instanceof CTTextBody element) visit(element.getP());
            else if (object instanceof CTTextParagraph element) visit(element.getEGTextRun());
            else if (object instanceof CTRegularTextRun ignored) { /* Do nothing */ }
            else if (object instanceof Presentation.SldSz ignored) { /* Do Nothing */ }
            else if (object instanceof Presentation ignored) { /* Do Nothing */ }
            else if (object == null) { /* Do Nothing */ }
            else throw new OfficeStamperException("Unknown case %s : %s".formatted(object.getClass(), object));
        } catch (Docx4JException e) {
            throw new OfficeStamperException(e);
        }
    }

    private void visit(Object o1, Object o2) {
        visit(o1);
        visit(o2);
    }

    protected abstract void before(Object object);

}
