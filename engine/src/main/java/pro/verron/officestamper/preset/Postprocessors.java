package pro.verron.officestamper.preset;

import pro.verron.officestamper.api.OfficeStamperException;
import pro.verron.officestamper.api.PostProcessor;
import pro.verron.officestamper.preset.postprocessors.cleanendnotes.RemoveOrphanedEndnotesProcessor;
import pro.verron.officestamper.preset.postprocessors.cleanfootnotes.RemoveOrphanedFootnotesProcessor;
import pro.verron.officestamper.preset.postprocessors.linebreaker.Linebreaker;

public class Postprocessors {
    private Postprocessors() {
        throw new OfficeStamperException("This is a utility class and cannot be instantiated");
    }

    public static PostProcessor removeOrphanedFootnotes() {
        return new RemoveOrphanedFootnotesProcessor();
    }

    public static PostProcessor removeOrphanedEndnotes() {
        return new RemoveOrphanedEndnotesProcessor();
    }

    public static PostProcessor linebreaker(String linebreakPlaceholder) {
        return new Linebreaker(linebreakPlaceholder);
    }
}
