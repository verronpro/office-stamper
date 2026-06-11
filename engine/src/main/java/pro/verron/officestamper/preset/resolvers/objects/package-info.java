/// Resolver that converts arbitrary objects to their string representation.
///
/// Uses [Object.toString()][java.lang.Object#toString()] to resolve expression
/// results that are not handled by more specific resolvers.
///
/// Ensures non-null values by default.
@NullMarked
package pro.verron.officestamper.preset.resolvers.objects;

import org.jspecify.annotations.NullMarked;
