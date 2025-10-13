package pro.verron.officestamper.experimental;

import org.docx4j.dml.CTRegularTextRun;
import org.docx4j.dml.CTTextBody;
import org.docx4j.dml.CTTextParagraph;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.PresentationMLPackage;
import org.docx4j.openpackaging.parts.*;
import org.docx4j.openpackaging.parts.PresentationML.*;
import org.docx4j.openpackaging.parts.WordprocessingML.ImageJpegPart;
import org.pptx4j.pml.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import pro.verron.officestamper.api.OfficeStamperException;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Arrays.stream;

abstract class PowerpointVisitor {

    private static final Logger logger = LoggerFactory.getLogger(PowerpointVisitor.class);

    private static void unexpectedVisit(Object object) {
        assert object != null : "Cannot visit a null object";
        var env = System.getenv();
        var throwOnUnexpectedVisit = Boolean.parseBoolean(env.getOrDefault("throw-on-unexpected-visit", "false"));
        var message = "Unknown case : %s %s".formatted(object, object.getClass());
        if (throwOnUnexpectedVisit) throw new OfficeStamperException(message);
        else logger.debug(message);
    }

    private static void ignore(@Nullable Object ignored) {
        logger.trace("ignored visit of '{}' object", ignored);
    }

    /// Signal the visited object through the before method,
    /// then apply logic to know the visit next elements or ignore deeper nesting
    /// based on the object type.
    ///
    /// @param object the object to visit
    public final void visit(@Nullable Object object) {
        before(object);
        try {
            switch (object) {
                case PresentationMLPackage element -> visit(element.getParts());
                case PartName ignored -> ignore(ignored);
                case Parts element -> visit(element.getParts());
                case SlideLayoutPart ignored -> ignore(ignored);
                case ImageJpegPart ignored -> ignore(ignored);
                case ThemePart ignored -> ignore(ignored);
                case DocPropsCorePart ignored -> ignore(ignored);
                case DocPropsExtendedPart ignored -> ignore(ignored);
                case SlideMasterPart ignored -> ignore(ignored);
                case ViewPropertiesPart ignored -> ignore(ignored);
                case PresentationPropertiesPart ignored -> ignore(ignored);
                case TableStylesPart ignored -> ignore(ignored);
                case MainPresentationPart element -> visit(element.getContents());
                case SlidePart element -> visit(element.getContents());
                case Sld element -> visit(element.getCSld());
                case CommonSlideData element -> visit(element.getSpTree());
                case GroupShape element -> visit(element.getSpOrGrpSpOrGraphicFrame());
                case Shape element -> visit(element.getTxBody());
                case CTTextBody element -> visit(element.getP());
                case CTTextParagraph element -> visit(element.getEGTextRun());
                case CTRegularTextRun ignored -> ignore(ignored);
                case Presentation.SldSz ignored -> ignore(ignored);
                case Presentation ignored -> ignore(ignored);
                case List<?> element -> element.forEach(this::visit);
                case Set<?> element -> element.forEach(this::visit);
                case Map<?, ?> element -> visit(element.entrySet());
                case Map.Entry<?, ?> element -> visit(element.getKey(), element.getValue());
                case null -> ignore(null);
                default -> unexpectedVisit(object);
            }
        } catch (Docx4JException e) {
            throw new OfficeStamperException(e);
        }
    }

    private void visit(Object... objs) {
        stream(objs).forEach(this::visit);
    }

    /// This abstract method is responsible for executing some tasks before a specific operation.
    /// It is intended to be implemented by subclasses.
    ///
    /// @param object The optional object that can be used as a parameter for the pre-operation tasks.
    protected abstract void before(@Nullable Object object);

}
