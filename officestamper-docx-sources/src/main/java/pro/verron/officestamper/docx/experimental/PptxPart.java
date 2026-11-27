package pro.verron.officestamper.docx.experimental;

import org.docx4j.openpackaging.packages.PresentationMLPackage;

/// The PptxPart class represents a specific implementation of the OfficePart interface designed for handling parts within
/// a PowerPoint document.
///
/// @param document the presentation document.
public record PptxPart(PresentationMLPackage document) {}


