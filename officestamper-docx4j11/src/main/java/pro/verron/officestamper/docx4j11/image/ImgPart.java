package pro.verron.officestamper.docx4j11.image;

import pro.verron.officestamper.utils.image.ImgFormat;

import org.docx4j.relationships.Relationship;

/// Represents an image part with its format and the associated relationship in
/// the document.
///
/// @param format       the format of the image
/// @param relationship the relationship of the image in the document package
public record ImgPart(ImgFormat format, Relationship relationship) {}
