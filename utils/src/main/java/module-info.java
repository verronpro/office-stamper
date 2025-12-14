/// The `pro.verron.officestamper.utils` module provides low-level utilities shared by the OfficeStamper engine and
/// associated tools.
///
/// This module contains various helper classes for working with WordprocessingML structures from docx4j, along with
/// general-purpose utilities for bits, bytes, functions, and iterators that are used by higher-level modules.
/// ## Exported Packages
///
///     - [pro.verron.officestamper.utils.wml] - Utilities and factories for working with WordprocessingML (WML) and
/// DOCX documents
///     - [pro.verron.officestamper.utils.openpackaging] - Utilities for working with Open Packaging Format
///     - [pro.verron.officestamper.utils.function] - Functional programming helpers and utilities
///     - [pro.verron.officestamper.utils.bit] - Low-level bit and byte manipulation utilities
///     - [pro.verron.officestamper.utils.iterator] - Iterator implementations and utilities
///
/// @since 3.0
module pro.verron.officestamper.utils {
    requires jakarta.xml.bind;
    requires org.slf4j;
    requires org.docx4j.core;
    requires org.jspecify;

    exports pro.verron.officestamper.utils.iterator;
    exports pro.verron.officestamper.utils.wml;
    exports pro.verron.officestamper.utils.function;
    exports pro.verron.officestamper.utils.bit;
    exports pro.verron.officestamper.utils.openpackaging;
}
