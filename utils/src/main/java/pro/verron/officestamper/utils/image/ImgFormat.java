package pro.verron.officestamper.utils.image;

/// Represents the format of an image, including its name and dimensions.
///
/// @param name      the format name (e.g., "PNG", "JPEG")
/// @param dimension the dimensions of the image in pixels
public record ImgFormat(String name, Dimension dimension) {}
