package pro.verron.officestamper.api;

import java.util.LinkedHashMap;
import java.util.Map;

/// Pre-processor that wraps inline placeholders in a document with smart tags so the engine can
/// process them later.
///
/// <p>The concrete, library-specific traversal is supplied by a provider (R2-3).
public abstract class PlaceholderHooker
        implements PreProcessor {

    private final Map<String, String> elementByOpening;

    /// @param opening the literal opening delimiter (e.g. {@code ${} or {@code #{}); must end with {@code {}
    /// @param element the smart-tag element name to wrap matching placeholders with
    protected PlaceholderHooker(String opening, String element) {
        this(Map.of(opening, element));
    }

    /// @param elementByOpening the smart-tag element name for each literal opening delimiter; every delimiter must end
    ///         with an opening brace
    protected PlaceholderHooker(Map<String, String> elementByOpening) {
        this.elementByOpening = new LinkedHashMap<>();
        for (var entry : elementByOpening.entrySet()) {
            this.elementByOpening.put(validate(entry.getKey()), entry.getValue());
        }
    }

    /// @return the smart-tag element name for each opening delimiter
    public Map<String, String> elementByOpening() {
        return elementByOpening;
    }

    private static String validate(String opening) {
        if (opening.isEmpty() || opening.charAt(opening.length() - 1) != '{')
            throw new IllegalArgumentException(
                    "A placeholder opening delimiter must end with '{', but was '%s'".formatted(opening));
        return opening;
    }

    @Override
    public abstract void process(OfficeDocument document);
}
