package pro.verron.docxstamper.core;

import org.docx4j.dml.CTRegularTextRun;
import org.docx4j.dml.CTTextBody;
import org.docx4j.dml.CTTextParagraph;
import org.docx4j.openpackaging.packages.PresentationMLPackage;
import org.docx4j.openpackaging.parts.PresentationML.MainPresentationPart;
import org.docx4j.openpackaging.parts.PresentationML.SlidePart;
import org.pptx4j.Pptx4jException;
import org.pptx4j.pml.*;
import pro.verron.docxstamper.api.OfficeStamperException;

import java.util.List;

abstract class PowerpointVisitor {
    void visit(Presentation.SldSz element) {
        // Do Nothing
    }

    public final void visit(Object object) {
        before(object);
        if (object instanceof PresentationMLPackage element) visit(element);
        else if (object instanceof MainPresentationPart element)
            visit(element);
        else if (object instanceof List<?> elements) visit(elements);
        else if (object instanceof SlidePart element) visit(element);
        else if (object instanceof Sld element) visit(element);
        else if (object instanceof CommonSlideData element) visit(element);
        else if (object instanceof GroupShape element) visit(element);
        else if (object instanceof Shape element) visit(element);
        else if (object instanceof CTTextBody element) visit(element);
        else if (object instanceof CTTextParagraph element) visit(element);
        else if (object instanceof CTRegularTextRun element) visit(element);
        else if (object instanceof Presentation.SldSz element)
            visit(element);
        else {
            System.out.println(object);
            throw new UnsupportedOperationException(
                    "At least one of the methods needs to be subclassed");
        }
    }

    protected abstract void before(Object object);

    void visit(List<?> elements) {
        for (Object element : elements) {
            visit(element);
        }
    }

    void visit(SlidePart element) {
        visit(element.getJaxbElement());
    }

    void visit(CTTextBody element) {
        visit(element.getP());
    }

    void visit(CTRegularTextRun element) {
        // Do nothing
    }

    void visit(CTTextParagraph element) {
        visit(element.getEGTextRun());
    }

    void visit(Shape element) {
        visit(element.getTxBody());
    }

    void visit(CommonSlideData element) {
        visit(element.getSpTree());
    }

    void visit(GroupShape element) {
        visit(element.getSpOrGrpSpOrGraphicFrame());
    }

    void visit(Sld element) {
        visit(element.getCSld());
    }


    void visit(PresentationMLPackage element) {
        visit(element.getMainPresentationPart());
    }

    void visit(MainPresentationPart element) {
        try {
            visit(element.getSlideParts());
        } catch (Pptx4jException e) {
            throw new OfficeStamperException(e);
        }
    }
}
