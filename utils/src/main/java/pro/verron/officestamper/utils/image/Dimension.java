package pro.verron.officestamper.utils.image;

/// Immutable two-dimensional size, expressed in pixels.
///
/// This is the docx-free replacement for `java.awt.geom.Dimension2D`, so that
/// image handling in the docx-free modules does not drag the java.desktop
/// module in through the type surface.
///
/// @param width  the width in pixels
/// @param height the height in pixels
public record Dimension(int width, int height) {}
