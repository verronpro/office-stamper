package pro.verron.officestamper.api;

import java.util.LinkedHashMap;
import java.util.Map;

/// Pre-processor that wraps inline placeholders in a document with smart tags so the engine can
/// process them later.
///
/// The concrete, library-specific traversal is supplied by a provider (R2-3).
///
/// **Design note**: supporting several opening delimiters in a single hooker (the map-based
/// constructor) is a deliberate decision. It lets one hooker mix expression styles within the
/// same traversal pass — e.g. `${...}` placeholders and `#{...}` processor-style tags — instead
/// of running and maintaining one hooker per delimiter.
public abstract class PlaceholderHooker
        implements PreProcessor {

    private final Map<String, String> elementByOpening;

    /// @param opening the literal opening delimiter (e.g. `${` or `#{`); must end with `{`
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
}
