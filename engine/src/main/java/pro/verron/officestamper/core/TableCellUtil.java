package pro.verron.officestamper.core;

import jakarta.xml.bind.JAXBElement;
import org.docx4j.wml.ObjectFactory;
import org.docx4j.wml.P;
import org.docx4j.wml.Tbl;
import org.docx4j.wml.Tc;
import pro.verron.officestamper.api.OfficeStamperException;

import java.util.function.Predicate;

/// Utility class for table cells
///
/// @author Joseph Verron
/// @author Tom Hombergs
/// @version ${version}
/// @since 1.0.0
public class TableCellUtil {

    private static final ObjectFactory objectFactory = new ObjectFactory();

    private TableCellUtil() {
        throw new OfficeStamperException("Utility class shouldn't be instantiated");
    }

    /// Checks if a table cell contains a paragraph or a table
    ///
    /// @param cell the table cell
    ///
    /// @return true if the table cell contains a paragraph or a table, false otherwise
    public static boolean hasNoParagraphOrTable(Tc cell) {
        Predicate<Object> isP = P.class::isInstance;
        Predicate<Object> isTbl = e -> e instanceof JAXBElement<?> jaxbElement && jaxbElement.getValue() instanceof Tbl;
        return cell.getContent()
                   .stream()
                   .noneMatch(isP.or(isTbl));
    }

    /// Checks if a table cell contains a paragraph
    ///
    /// @param cell the table cell
    public static void addEmptyParagraph(Tc cell) {
        P paragraph = objectFactory.createP();
        paragraph.getContent()
                 .add(objectFactory.createR());
        cell.getContent()
            .add(paragraph);
    }
}
