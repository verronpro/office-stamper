package pro.verron.officestamper.utils.openpackaging;

import org.docx4j.openpackaging.packages.OpcPackage;
import org.docx4j.openpackaging.parts.Part;

public record OpenPackage<T extends OpcPackage>(T document, Part part) {}
