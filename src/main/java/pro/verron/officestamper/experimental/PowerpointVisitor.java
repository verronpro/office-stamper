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

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

abstract class PowerpointVisitor {

    private static final Logger logger = LoggerFactory.getLogger(PowerpointVisitor.class);

    private static void unexpectedVisit(Object object) {
        var env = System.getenv();
        var throwOnUnexpectedVisit = Boolean.parseBoolean(env.getOrDefault("throw-on-unexpected-visit", "false"));
        var message = "Unknown case : %s %s".formatted(object, object.getClass());
        if (throwOnUnexpectedVisit)
            throw new OfficeStamperException(message);
        else
            logger.debug(message);
    }

    private static void ignore(@Nullable Object ignored1) {
        logger.trace("ignored visit of '{}' object", ignored1);
    }

    public final void visit(@Nullable Object object) {
        before(object);
        try {
            if (object instanceof PresentationMLPackage element) visit(element.getParts());
            else if (object instanceof PartName ignored) ignore(ignored);
            else if (object instanceof Parts element) visit(element.getParts());
            else if (object instanceof SlideLayoutPart ignored) ignore(ignored);
            else if (object instanceof ImageJpegPart ignored) ignore(ignored);
            else if (object instanceof ThemePart ignored) ignore(ignored);
            else if (object instanceof DocPropsCorePart ignored) ignore(ignored);
            else if (object instanceof DocPropsExtendedPart ignored) ignore(ignored);
            else if (object instanceof SlideMasterPart ignored) ignore(ignored);
            else if (object instanceof ViewPropertiesPart ignored) ignore(ignored);
            else if (object instanceof PresentationPropertiesPart ignored) ignore(ignored);
            else if (object instanceof TableStylesPart ignored) ignore(ignored);
            else if (object instanceof MainPresentationPart element) visit(element.getContents());

            else if (object instanceof SlidePart element) visit(element.getContents());
            else if (object instanceof Sld element) visit(element.getCSld());
            else if (object instanceof CommonSlideData element) visit(element.getSpTree());
            else if (object instanceof GroupShape element) visit(element.getSpOrGrpSpOrGraphicFrame());
            else if (object instanceof Shape element) visit(element.getTxBody());
            else if (object instanceof CTTextBody element) visit(element.getP());
            else if (object instanceof CTTextParagraph element) visit(element.getEGTextRun());
            else if (object instanceof CTRegularTextRun ignored) ignore(ignored);
            else if (object instanceof Presentation.SldSz ignored) ignore(ignored);
            else if (object instanceof Presentation ignored) ignore(ignored);

            else if (object instanceof List<?> element) element.forEach(this::visit);
            else if (object instanceof Set<?> element) element.forEach(this::visit);
            else if (object instanceof Map<?, ?> element) visit(element.entrySet());
            else if (object instanceof Map.Entry<?, ?> element) visit(element.getKey(), element.getValue());

            else if (object == null) ignore(null);
            else unexpectedVisit(object);
        } catch (Docx4JException e) {
            throw new OfficeStamperException(e);
        }
    }

    private void visit(Object... objs) {
        Arrays.stream(objs)
              .forEach(this::visit);
    }

    protected abstract void before(@Nullable Object object);

}
