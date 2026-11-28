/// AnchoredComment processor that replaces the current run with the evaluated
/// expression result.
///
/// Implements the `replaceWith` directive for substituting AnchoredComment content with
/// a SpEL expression value.
///
/// Ensures non-null values by default.
@NullMarked
package pro.verron.officestamper.preset.processors.replacewith;

import org.jspecify.annotations.NullMarked;
