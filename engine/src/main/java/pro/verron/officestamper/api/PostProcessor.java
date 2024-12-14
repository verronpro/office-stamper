package pro.verron.officestamper.api;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;

public interface PostProcessor {
    void process(WordprocessingMLPackage document);
}
