package pro.verron.officestamper.utils.image;

import org.jspecify.annotations.Nullable;

/// Holds the options for inserting an image into a document run.
///
/// @param altText      alternative text for the image
/// @param filenameHint filename hint for the image
/// @param maxWidth     max width of the image in twip, or null if unspecified
/// @param deduplicate  whether to deduplicate identical images in the document
public record ImageRunOptions(
        String altText,
        String filenameHint,
        @Nullable Integer maxWidth,
        boolean deduplicate
) {}
