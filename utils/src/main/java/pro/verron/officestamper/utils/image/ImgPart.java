package pro.verron.officestamper.utils.image;

import org.docx4j.relationships.Relationship;

/// Represents an image part with its format and the associated relationship in
/// the document.
///
/// @param format       the format of the image
/// @param relationship the relationship of the image in the document package
public record ImgPart(ImgFormat format, Relationship relationship) {}
