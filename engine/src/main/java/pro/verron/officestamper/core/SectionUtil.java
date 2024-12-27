package pro.verron.officestamper.core;

import org.docx4j.XmlUtils;
import org.docx4j.wml.ContentAccessor;
import org.docx4j.wml.P;
import org.docx4j.wml.PPr;
import org.docx4j.wml.SectPr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.verron.officestamper.api.OfficeStamperException;
import pro.verron.officestamper.utils.WmlFactory;

import java.util.List;
import java.util.Optional;

import static java.util.Optional.ofNullable;

/// Utility class to handle section breaks in paragraphs.
///
/// @author Joseph Verron
/// @version ${version}
/// @since 1.6.2
public class SectionUtil {
    private static final Logger log = LoggerFactory.getLogger(SectionUtil.class);

    private SectionUtil() {
        throw new OfficeStamperException("Utility class shouldn't be instantiated");
    }

    /// Creates a new section break object.
    ///
    /// @param firstObject a [Object] object
    /// @param parent      a [ContentAccessor] object
    ///
    /// @return a new section break object.
    public static Optional<SectPr> getPreviousSectionBreakIfPresent(Object firstObject, ContentAccessor parent) {
        List<Object> parentContent = parent.getContent();
        int pIndex = parentContent.indexOf(firstObject);

        int i = pIndex - 1;
        while (i >= 0) {
            if (parentContent.get(i) instanceof P prevParagraph) {
                // the first P preceding the object is the one potentially carrying a section break
                return ofNullable(prevParagraph.getPPr())
                        .map(PPr::getSectPr);
            }
            else log.debug("The previous sibling was not a P, continuing search");
            i--;
        }
        log.info("No previous section break found from : {}, first object index={}", parent, pIndex);
        return Optional.empty();
    }

    /// Creates a new section break object.
    ///
    /// @param objects a [List] object
    ///
    /// @return a new section break object.
    public static boolean hasOddNumberOfSectionBreaks(List<Object> objects) {
        return objects.stream()
                      .filter(P.class::isInstance)
                      .map(P.class::cast)
                      .filter(SectionUtil::hasSectionBreak)
                      .count() % 2 != 0;
    }

    private static boolean hasSectionBreak(P p) {
        var pPPr = p.getPPr();
        if (pPPr == null) return false;
        var pPPrSectPr = pPPr.getSectPr();
        return pPPrSectPr != null;
    }

    /// Creates a new section break object.
    ///
    /// @param sectPr    a [SectPr] object
    /// @param paragraph a [P] object
    public static void applySectionBreakToParagraph(SectPr sectPr, P paragraph) {
        PPr nextPPr = ofNullable(paragraph.getPPr()).orElseGet(WmlFactory::newPPr);
        nextPPr.setSectPr(XmlUtils.deepCopy(sectPr));
        paragraph.setPPr(nextPPr);
    }
}
