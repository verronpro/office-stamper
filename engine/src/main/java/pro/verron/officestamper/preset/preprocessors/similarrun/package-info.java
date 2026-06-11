/// Pre-processor that merges consecutive runs with identical styling into a
/// single run.
///
/// Reduces document complexity by combining adjacent runs that share the same
/// formatting properties, improving processing efficiency and expression
/// matching accuracy.
///
/// Ensures non-null values by default.
@NullMarked
package pro.verron.officestamper.preset.preprocessors.similarrun;

import org.jspecify.annotations.NullMarked;
