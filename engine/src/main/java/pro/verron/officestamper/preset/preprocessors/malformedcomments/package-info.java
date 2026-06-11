/// Pre-processor that removes malformed comments from the document.
///
/// Handles unbalanced comment ranges and dangling comment references that can
/// corrupt template processing.
///
/// Ensures non-null values by default.
@NullMarked
package pro.verron.officestamper.preset.preprocessors.malformedcomments;

import org.jspecify.annotations.NullMarked;
