package pro.verron.officestamper.docx.image;

import java.awt.geom.Dimension2D;

/// Represents the format of an image, including its name and dimensions.
///
/// @param name      the format name (e.g., "PNG", "JPEG")
/// @param dimension the dimensions of the image
public record ImgFormat(String name, Dimension2D dimension) {}
