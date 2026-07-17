package pro.verron.officestamper.utils.wml;


import org.docx4j.wml.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static pro.verron.officestamper.utils.wml.WmlUtils.newAttribute;

public class SmartTag implements Parent {
    private final CTSmartTagRun smartTag;

    public SmartTag(CTSmartTagRun smartTag) {
        this.smartTag = smartTag;
    }

    public Parent getParent() {
        var parent = smartTag.getParent();
        return switch (parent) {
            case P p -> new Paragraph(p);
            case CTSdtContentRun sdtContent -> new SdtContent(sdtContent);
            default -> throw new RuntimeException("whatever");
        };
    }

    @Override
    public List<Object> getContent() {
        return smartTag.getContent();
    }

    public Optional<String> getProperty(String name) {
        return smartTag.getSmartTagPr()
                .getAttr()
                .stream()
                .filter(a -> Objects.equals(name, a.getName()))
                .map(CTAttr::getVal)
                .findFirst();
    }

    public void setProperty(String name, String value) {
        var smartTagPr = smartTag.getSmartTagPr();
        if (smartTagPr == null) {
            smartTagPr = new CTSmartTagPr();
            smartTag.setSmartTagPr(smartTagPr);
        }
        var smartTagPrAttr = smartTagPr.getAttr();
        if (smartTagPrAttr == null) {
            smartTagPrAttr = new ArrayList<>();
            smartTag.setSmartTagPr(smartTagPr);
        }
        for (CTAttr attribute : smartTagPrAttr) {
            if (name.equals(attribute.getName())) {
                attribute.setVal(value);
                return;
            }
        }
        var ctAttr = newAttribute(name, value);
        smartTagPrAttr.add(ctAttr);
    }

    /// Removes the current tag from its parent's content list.
    ///
    /// This method locates the parent content accessor of the tag, retrieves its sibling elements, and removes the tag
    /// from the sibling list, detaching it from its parent content.
    public void remove() {
        var parent = (ContentAccessor) smartTag.getParent();
        var siblings = parent.getContent();
        siblings.remove(smartTag);
    }
}
