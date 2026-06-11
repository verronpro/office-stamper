/// Pre-processor that removes proofing error markup from the document.
///
/// Strips `w:proofErr` elements that indicate grammar or spelling errors, which
/// can interfere with expression matching inside runs.
///
/// Ensures non-null values by default.
@NullMarked
package pro.verron.officestamper.preset.preprocessors.prooferror;

import org.jspecify.annotations.NullMarked;
