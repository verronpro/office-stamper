package pro.verron.officestamper.experimental;

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
import pro.verron.officestamper.api.OfficeStamperException;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import static java.util.Arrays.stream;

/// The ExcelVisitor class provides a mechanism for visiting different types of Excel objects.
/// It contains visit methods for various types of objects and performs specific actions based on the object type.
/// Subclasses can extend this class and override the before method to define custom behavior before visiting an object.
abstract class ExcelVisitor {

    private static final Logger logger = LoggerFactory.getLogger(ExcelVisitor.class);

    private static void unexpectedVisit(Object object) {
        assert object != null : "Cannot visit a null object";
        var env = System.getenv();
        var throwOnUnexpectedVisit = Boolean.parseBoolean(env.getOrDefault("throw-on-unexpected-visit", "false"));
        var message = "Unknown case : %s %s".formatted(object, object.getClass());
        if (throwOnUnexpectedVisit) throw new OfficeStamperException(message);
        else logger.debug(message);
    }

    private static void ignore(@Nullable Object ignored1) {
        logger.trace("ignored visit of '{}' object", ignored1);
    }

    /// Visits the given object and performs specific operations based on its type.
    ///
    /// @param object the object to visit
    public final void visit(@Nullable Object object) {
        before(object);
        try {
            switch (object) {
                case SpreadsheetMLPackage element -> visit(element.getParts());
                case Parts element -> visit(element.getParts());
                case WorksheetPart element -> visit(element.getContents());
                case WorkbookPart element -> visit(element.getContents());
                case DocPropsCorePart ignored -> ignore(ignored);
                case DocPropsExtendedPart ignored -> ignore(ignored);
                case Styles ignored -> ignore(ignored);
                case SharedStrings element -> visit(element.getContents());
                case ThemePart ignored -> ignore(ignored);
                case Workbook element -> visit(element.getSheets());
                case Sheets element -> visit(element.getSheet());
                case Worksheet element -> visit(element.getSheetData());
                case SheetData element -> visit(element.getRow());
                case Row element -> visit(element.getC());
                case Cell element -> visit(element.getIs());
                case CTRst element -> visit(element.getR());
                case CTSst element -> visit(element.getSi());
                case CTRElt element -> visit(element.getT());
                case CTXstringWhitespace ignored -> ignore(ignored);
                case JAXBElement<?> element -> visit(element.getValue());
                case Sheet element -> visit(element.getState());
                case STSheetState ignored -> ignore(ignored);
                case List<?> element -> element.forEach(this::visit);
                case Set<?> element -> element.forEach(this::visit);
                case Map<?, ?> element -> visit(element.entrySet());
                case Entry<?, ?> element -> visit(element.getKey(), element.getValue());
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

    /// This method is called before performing a visit.
    /// It provides an opportunity to perform any necessary setup or validation
    /// before the actual visit takes place.
    ///
    /// @param object the object on which the visit will be performed.
    protected abstract void before(@Nullable Object object);
}
