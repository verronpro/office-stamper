package pro.verron.officestamper.core;

import org.docx4j.model.styles.StyleUtil;
import org.docx4j.wml.*;

import org.springframework.lang.Nullable;
import pro.verron.officestamper.api.OfficeStamperException;

import static pro.verron.officestamper.utils.WmlFactory.newRun;
import static pro.verron.officestamper.utils.WmlFactory.newText;

/// Utility class to handle runs.
///
/// @author Joseph Verron
/// @author Tom Hombergs
/// @version ${version}
/// @since 1.0.0
public class RunUtil {

    private RunUtil() {
        throw new OfficeStamperException("Utility class shouldn't be instantiated");
    }

    /// Creates a new run with the specified text and inherits the style of the parent paragraph.
    ///
    /// @param text the initial text of the run.
    ///
    /// @return the newly created run.
    public static R create(String text, PPr paragraphPr) {
        R run = newRun(text);
        applyParagraphStyle(run, paragraphPr);
        return run;
    }

    /// Applies the style of the given paragraph to the given content object (if the content object is a Run).
    ///
    /// @param run the Run to which the style should be applied.
    public static void applyParagraphStyle(R run, @Nullable PPr paragraphPr) {
        if (paragraphPr == null) return;
        var runPr = paragraphPr.getRPr();
        if (runPr == null) return;
        RPr runProperties = new RPr();
        StyleUtil.apply(runPr, runProperties);
        run.setRPr(runProperties);
    }

    /// Sets the text of the given run to the given value.
    ///
    /// @param run  the run whose text to change.
    /// @param text the text to set.
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
