package pro.verron.officestamper.core;

import jakarta.xml.bind.JAXBElement;
import org.docx4j.model.styles.StyleUtil;
import org.docx4j.wml.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import pro.verron.officestamper.api.OfficeStamperException;

import java.util.Objects;

import static java.util.stream.Collectors.joining;
import static pro.verron.officestamper.utils.WmlFactory.newRun;
import static pro.verron.officestamper.utils.WmlFactory.newText;

/**
 * Utility class to handle runs.
 *
 * @author Joseph Verron
 * @author Tom Hombergs
 * @version ${version}
 * @since 1.0.0
 */
public class RunUtil {


    private static final String PRESERVE = "preserve";
    private static final Logger log = LoggerFactory.getLogger(RunUtil.class);

    private RunUtil() {
        throw new OfficeStamperException("Utility class shouldn't be instantiated");
    }

    /**
     * Extracts textual content from a given object, handling various object types,
     * such as runs, text elements, and other specific constructs.
     * The method accounts for different cases, such as run breaks, hyphens,
     * and other document-specific constructs, and converts them into
     * corresponding string representations.
     *
     * @param content the object from which text content is to be extracted.
     *                This could be of various types such as R, JAXBElement, Text,
     *                or specific document elements.
     *
     * @return a string representation of the extracted textual content.
     * If the object's type is not handled, an empty string is returned.
     */
    public static String getText(Object content) {
        return switch (content) {
            case R run -> run.getContent()
                             .stream()
                             .map(RunUtil::getText)
                             .collect(joining());
            case JAXBElement<?> jaxbElement when jaxbElement.getName()
                                                            .getLocalPart()
                                                            .equals("instrText") -> "<instrText>";
            case JAXBElement<?> jaxbElement when !jaxbElement.getName()
                                                             .getLocalPart()
                                                             .equals("instrText") -> getText(jaxbElement.getValue());
            case Text text -> getText(text);
            case R.Tab ignored -> "\t";
            case R.Cr ignored -> "\n";
            case Br br when br.getType() == null -> "\n";
            case Br br when br.getType() == STBrType.TEXT_WRAPPING -> "\n";
            case Br br when br.getType() == STBrType.PAGE -> "\n";
            case Br br when br.getType() == STBrType.COLUMN -> "\n";
            case R.NoBreakHyphen ignored -> "‑";
            case R.SoftHyphen ignored -> "\u00AD";
            case R.LastRenderedPageBreak ignored -> "";
            case R.AnnotationRef ignored -> "";
            case R.CommentReference ignored -> "";
            case Drawing ignored -> "";
            case FldChar ignored -> "<fldchar>";
            case CTFtnEdnRef ref -> ref.getId()
                                       .toString();
            case R.Sym sym -> "<sym(%s, %s)>".formatted(sym.getFont(), sym.getChar());
            default -> {
                log.debug("Unhandled object type: {}", content.getClass());
                yield "";
            }
        };
    }

    /**
     * Processes and retrieves text content from a given Text object.
     * It handles situations where the text may or may not preserve spaces,
     * trimming the text if spaces are not to be preserved.
     *
     * @param text the Text object from which the value is to be processed and returned.
     *             The object contains the textual value, and an associated space property
     *             that determines if spaces should be preserved.
     *
     * @return a processed string value from the Text object.
     * If spaces are to be preserved, the original value is returned;
     * otherwise, the value is trimmed of leading and trailing spaces.
     */
    private static String getText(Text text) {
        var value = text.getValue();
        var space = text.getSpace();
        return Objects.equals(space, PRESERVE) ? value : value.trim();
    }

    /**
     * Creates a new run with the specified text and inherits the style of the parent paragraph.
     *
     * @param text the initial text of the run.
     *
     * @return the newly created run.
     */
    public static R create(String text, PPr paragraphPr) {
        R run = newRun(text);
        applyParagraphStyle(run, paragraphPr);
        return run;
    }

    /**
     * Applies the style of the given paragraph to the given content object (if the content object is a Run).
     *
     * @param run the Run to which the style should be applied.
     */
    public static void applyParagraphStyle(R run, @Nullable PPr paragraphPr) {
        if (paragraphPr == null) return;
        var runPr = paragraphPr.getRPr();
        if (runPr == null) return;
        RPr runProperties = new RPr();
        StyleUtil.apply(runPr, runProperties);
        run.setRPr(runProperties);
    }

    /**
     * Sets the text of the given run to the given value.
     *
     * @param run  the run whose text to change.
     * @param text the text to set.
     */
    public static void setText(R run, String text) {
        run.getContent()
           .clear();
        Text textObj = newText(text);
        run.getContent()
           .add(textObj);
    }

    static R create(String text, RPr rPr) {
        R newStartRun = newRun(text);
        newStartRun.setRPr(rPr);
        return newStartRun;
    }
}
