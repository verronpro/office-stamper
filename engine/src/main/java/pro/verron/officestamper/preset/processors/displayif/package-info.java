/// Comment processor for conditional display of document content.
///
/// Implements the `displayIf` directive, which shows or hides the surrounding
/// paragraph based on a boolean SpEL expression.
///
/// Ensures non-null values by default.
@NullMarked
package pro.verron.officestamper.preset.processors.displayif;

import org.jspecify.annotations.NullMarked;
