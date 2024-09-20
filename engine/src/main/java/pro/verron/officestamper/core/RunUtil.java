package pro.verron.officestamper.core;

import jakarta.xml.bind.JAXBElement;
import org.docx4j.dml.wordprocessingDrawing.Inline;
import org.docx4j.jaxb.Context;
import org.docx4j.model.styles.StyleUtil;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPartAbstractImage;
import org.docx4j.wml.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import pro.verron.officestamper.api.OfficeStamperException;

import java.util.Objects;
import java.util.Random;

import static java.util.stream.Collectors.joining;

/**
 * Utility class to handle runs.
 *
 * @author Joseph Verron
 * @author Tom Hombergs
 * @version ${version}
 * @since 1.0.0
 */
public class RunUtil {
    private static final Random random = new Random();

    private static final String PRESERVE = "preserve";
    private static final ObjectFactory factory = Context.getWmlObjectFactory();
    private static final Logger log = LoggerFactory.getLogger(RunUtil.class);

    private RunUtil() {
        throw new OfficeStamperException("Utility class shouldn't be instantiated");
    }

    /**
     * Returns the text string of a run.
     *
     * @param run the run whose text to get.
     *
     * @return {@link String} representation of the run.
     */
    public static String getText(R run) {
        return run.getContent()
                  .stream()
                  .map(RunUtil::getText)
                  .collect(joining());
    }

    /**
     * Returns the textual representation of a run child
     *
     * @param content the run child to represent textually
     *
     * @return {@link String} representation of run child
     */
    public static CharSequence getText(Object content) {
        if (content instanceof JAXBElement<?> jaxbElement) return getText(jaxbElement.getValue());
        if (content instanceof Text text) return getText(text);
        if (content instanceof R.Tab) return "\t";
        if (content instanceof R.Cr) return "\n";
        if (content instanceof Br br && br.getType() == null) return "\n";
        if (content instanceof Br br && br.getType() == STBrType.TEXT_WRAPPING) return "\n";
        if (content instanceof Br br && br.getType() == STBrType.PAGE) return "\n";
        if (content instanceof Br br && br.getType() == STBrType.COLUMN) return "\n";
        if (content instanceof R.NoBreakHyphen) return "‑";
        if (content instanceof R.SoftHyphen) return "\u00AD";
        if (content instanceof R.LastRenderedPageBreak) return "";
        if (content instanceof R.Sym sym) return "<sym(" + sym.getFont() + ", " + sym.getChar() + ")>";

        log.debug("Unhandled object type: {}", content.getClass());
        return "";
    }

    private static CharSequence getText(Text text) {
        String value = text.getValue();
        String space = text.getSpace();
        return Objects.equals(space, PRESERVE)
                ? value // keeps spaces if spaces are to be preserved (LibreOffice seems to ignore the "space" property)
                : value.trim(); // trimming value if spaces are not to be preserved (simulates behavior of Word;)
    }

    /**
     * Creates a new run with the specified text and inherits the style of the parent paragraph.
     *
     * @param text the initial text of the run.
     *
     * @return the newly created run.
     */
    public static R create(String text, PPr paragraphPr) {
        R run = create(text);
        applyParagraphStyle(run, paragraphPr);
        return run;
    }

    /**
     * Creates a new run with the specified text.
     *
     * @param text the initial text of the run.
     *
     * @return the newly created run.
     */
    public static R create(String text) {
        R run = factory.createR();
        setText(run, text);
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
        Text textObj = createText(text);
        run.getContent()
           .add(textObj);
    }

    /**
     * Creates a text object with the given text.
     *
     * @param text the text to set.
     *
     * @return the newly created text object.
     */
    public static Text createText(String text) {
        Text textObj = factory.createText();
        textObj.setValue(text);
        textObj.setSpace(PRESERVE); // make the text preserve spaces
        return textObj;
    }


    /**
     * Creates a run containing the given image.
     *
     * @param maxWidth      max width of the image
     * @param abstractImage the image
     *
     * @return the run containing the image
     */
    public static R createRunWithImage(
            @Nullable Integer maxWidth,
            BinaryPartAbstractImage abstractImage
    ) {
        // creating random ids assuming they are unique,
        // id must not be too large;
        // otherwise Word cannot open the document
        int id1 = random.nextInt(100000);
        int id2 = random.nextInt(100000);
        var filenameHint = "dummyFileName";
        var altText = "dummyAltText";

        Inline inline = tryCreateImageInline(
                filenameHint,
                altText,
                maxWidth,
                abstractImage,
                id1,
                id2);


        // Now add the inline in w:p/w:r/w:drawing
        ObjectFactory factory = new ObjectFactory();
        R run = factory.createR();
        Drawing drawing = factory.createDrawing();
        run.getContent()
           .add(drawing);
        drawing.getAnchorOrInline()
               .add(inline);

        return run;

    }

    private static Inline tryCreateImageInline(
            String filenameHint,
            String altText,
            @Nullable Integer maxWidth,
            BinaryPartAbstractImage abstractImage,
            int id1,
            int id2
    ) {
        try {
            return maxWidth == null
                    ? abstractImage.createImageInline(filenameHint, altText, id1, id2, false)
                    : abstractImage.createImageInline(filenameHint, altText, id1, id2, false, maxWidth);
        } catch (Exception e) {
            throw new OfficeStamperException(e);
        }
    }

    static int getLength(R run) {
        return getText(run)
                .length();
    }

    static String getSubstring(R run, int beginIndex) {
        return getText(run)
                .substring(beginIndex);
    }

    static String getSubstring(R run, int beginIndex, int endIndex) {
        return getText(run)
                .substring(beginIndex, endIndex);
    }
}
