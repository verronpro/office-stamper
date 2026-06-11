/// Low-level utility classes shared across the OfficeStamper modules.
///
/// This module provides helper classes for working with WordprocessingML (WML),
/// PresentationML (PML), and SpreadsheetML (SML) structures from docx4j,
/// Open Packaging Format utilities, SVG and image handling, along with
/// general-purpose utilities for functional programming and iteration.
///
/// ## Exported Packages
/// - [pro.verron.officestamper.utils.wml] - Utilities and factories for
/// WordprocessingML and DOCX documents
/// - [pro.verron.officestamper.utils.openpackaging] - Utilities for the Open
/// Packaging Format
/// - [pro.verron.officestamper.utils.function] - Functional programming
/// helpers
/// - [pro.verron.officestamper.utils.iterator] - Iterator implementations and
/// utilities
/// - [pro.verron.officestamper.utils.pml] - Utilities for PresentationML and
/// PPTX documents
/// - [pro.verron.officestamper.utils.sml] - Utilities for SpreadsheetML and
/// XLSX documents
/// - [pro.verron.officestamper.utils.svg] - SVG processing utilities
/// - [pro.verron.officestamper.utils.image] - Image handling utilities
module pro.verron.officestamper.utils {
    requires org.docx4j.core;

    requires jakarta.xml.bind;
    requires org.jspecify;
    requires org.slf4j;
    requires java.desktop;

    exports pro.verron.officestamper.utils.iterator;
    exports pro.verron.officestamper.utils.function;
    exports pro.verron.officestamper.utils.openpackaging;
    exports pro.verron.officestamper.utils.wml;
    exports pro.verron.officestamper.utils.pml;
    exports pro.verron.officestamper.utils.sml;
    exports pro.verron.officestamper.utils.svg;
    exports pro.verron.officestamper.utils.image;
}
