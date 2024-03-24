package pro.verron.docxstamper.test;

import org.docx4j.dml.CTTextParagraph;
import org.docx4j.openpackaging.packages.PresentationMLPackage;
import pro.verron.docxstamper.core.PowerpointCollector;
import pro.verron.docxstamper.core.PowerpointParagraph;

public class Stringifiers {
    public static String stringifyPowerpoint(PresentationMLPackage presentation) {
        var collector = new PowerpointCollector<>(CTTextParagraph.class);
        collector.visit(presentation);
        var collected = collector.collect();

        var powerpoint = new StringBuilder();
        for (CTTextParagraph paragraph : collected) {
            powerpoint.append(new PowerpointParagraph(paragraph).asString());
        }
        return powerpoint.toString();
    }

}
