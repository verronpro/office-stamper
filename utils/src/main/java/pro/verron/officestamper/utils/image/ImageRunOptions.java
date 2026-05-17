package pro.verron.officestamper.utils.image;

import org.jspecify.annotations.Nullable;

public record ImageRunOptions(String altText, String filenameHint, @Nullable Integer maxWidth, boolean deduplicate) {}
