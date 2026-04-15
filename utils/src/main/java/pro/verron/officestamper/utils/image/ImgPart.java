package pro.verron.officestamper.utils.image;

import org.docx4j.relationships.Relationship;

public record ImgPart(ImgFormat format, Relationship relationship) {}
