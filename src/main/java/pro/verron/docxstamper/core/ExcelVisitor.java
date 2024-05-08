package pro.verron.docxstamper.core;

import jakarta.xml.bind.JAXBElement;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.SpreadsheetMLPackage;
import org.docx4j.openpackaging.parts.DocPropsCorePart;
import org.docx4j.openpackaging.parts.DocPropsExtendedPart;
import org.docx4j.openpackaging.parts.Parts;
import org.docx4j.openpackaging.parts.SpreadsheetML.SharedStrings;
import org.docx4j.openpackaging.parts.SpreadsheetML.Styles;
import org.docx4j.openpackaging.parts.SpreadsheetML.WorkbookPart;
import org.docx4j.openpackaging.parts.SpreadsheetML.WorksheetPart;
import org.docx4j.openpackaging.parts.ThemePart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.xlsx4j.sml.*;
import pro.verron.docxstamper.api.OfficeStamperException;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

abstract class ExcelVisitor {

    private static final Logger logger = LoggerFactory.getLogger(ExcelVisitor.class);

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
            if (object instanceof SpreadsheetMLPackage element) visit(element.getParts());

            else if (object instanceof Parts element) visit(element.getParts());
            else if (object instanceof WorksheetPart element) visit(element.getContents());
            else if (object instanceof WorkbookPart element) visit(element.getContents());
            else if (object instanceof DocPropsCorePart ignored) ignore(ignored);
            else if (object instanceof DocPropsExtendedPart ignored) ignore(ignored);
            else if (object instanceof Styles ignored) ignore(ignored);
            else if (object instanceof SharedStrings element) visit(element.getContents());
            else if (object instanceof ThemePart ignored) ignore(ignored);

            else if (object instanceof Workbook element) visit(element.getSheets());
            else if (object instanceof Sheets element) visit(element.getSheet());
            else if (object instanceof Worksheet element) visit(element.getSheetData());
            else if (object instanceof SheetData element) visit(element.getRow());
            else if (object instanceof Row element) visit(element.getC());
            else if (object instanceof Cell element) visit(element.getIs());
            else if (object instanceof CTRst element) visit(element.getR());
            else if (object instanceof CTSst element) visit(element.getSi());
            else if (object instanceof CTRElt element) visit(element.getT());
            else if (object instanceof CTXstringWhitespace ignored) ignore(ignored);
            else if (object instanceof JAXBElement<?> element) visit(element.getValue());
            else if (object instanceof Sheet element) visit(element.getState());
            else if (object instanceof STSheetState ignored) ignore(ignored);

            else if (object instanceof List<?> element) element.forEach(this::visit);
            else if (object instanceof Set<?> element) element.forEach(this::visit);
            else if (object instanceof Map<?, ?> element) visit(element.entrySet());
            else if (object instanceof Entry<?, ?> element) visit(element.getKey(), element.getValue());
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
