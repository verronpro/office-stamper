/// Pre-processor that removes language settings from paragraphs and runs.
///
/// Strips `w:lang` elements from run properties and paragraph properties, which
/// can interfere with the stamping process when templates use multiple
/// languages.
///
/// Ensures non-null values by default.
@NullMarked
package pro.verron.officestamper.preset.preprocessors.rmlang;

import org.jspecify.annotations.NullMarked;
