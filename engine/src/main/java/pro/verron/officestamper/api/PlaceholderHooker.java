package pro.verron.officestamper.api;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.utils.TraversalUtilVisitor;
import org.docx4j.wml.P;
import pro.verron.officestamper.utils.wml.WmlUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.SequencedMap;

import static java.util.Comparator.comparingInt;
import static pro.verron.officestamper.utils.wml.WmlUtils.asString;
import static pro.verron.officestamper.utils.wml.WmlUtils.insertSmartTag;

/// The [PlaceholderHooker] class is a pre-processor that prepares inline placeholders in a
/// [WordprocessingMLPackage] document. It searches for placeholders introduced by one of the configured opening
/// delimiters and wraps them with a smart tag, so the OfficeStamper engine can process them later.
///
/// ## Brace balancing
///
/// A placeholder ends at the closing brace that *balances* its opening brace, not at the first closing brace
/// encountered. Braces nested inside the placeholder are therefore part of the expression, which is what makes SpEL
/// inline lists and inline maps usable as placeholders:
///
/// ```text
/// ${ {1, 2, 3} }              -> expression " {1, 2, 3} "   (a SpEL inline list)
/// ${ {'a': 1, 'b': 2} }       -> expression " {'a': 1, 'b': 2} " (a SpEL inline map)
/// ${ {1, 2, 3}.?[#this > 1] } -> expression " {1, 2, 3}.?[#this > 1] "
/// ```
///
/// ## Malformed placeholders
///
/// An opening delimiter that is never balanced by a closing brace is *malformed*. Rather than leaving the stray
/// delimiter behind — and rather than letting the text that follows it be interpreted as further placeholders — the
/// malformed placeholder spans the remainder of the paragraph and is captured verbatim, delimiters included. The
/// engine then fails to parse it and hands it to the configured [ExceptionResolver], so the failure is reported
/// through the usual channel instead of silently corrupting the output.
///
/// ## Single pass
///
/// All delimiters are matched in one left-to-right pass, and scanning resumes *after* each placeholder that has been
/// wrapped. Consequently a placeholder nested inside another one is part of the outer expression and is never wrapped
/// on its own.
public class PlaceholderHooker
        implements PreProcessor {

    private static final char OPENING_BRACE = '{';
    private static final char CLOSING_BRACE = '}';

    private final SequencedMap<String, String> elementByOpening;

    /// Constructs a new [PlaceholderHooker] recognizing a single opening delimiter.
    ///
    /// @param opening the literal opening delimiter of a placeholder, for instance `${` or `#{`. It must end with
    ///         an opening brace.
    /// @param element the name of the smart tag type to wrap matching placeholders with.
    public PlaceholderHooker(String opening, String element) {
        this(Map.of(opening, element));
    }

    /// Constructs a new [PlaceholderHooker] recognizing several opening delimiters in a single pass.
    ///
    /// @param elementByOpening the smart tag type to use for each literal opening delimiter. Every delimiter must
    ///         end with an opening brace. When several delimiters could match at the same position, the longest one
    ///         wins.
    public PlaceholderHooker(Map<String, String> elementByOpening) {
        this.elementByOpening = elementByOpening.entrySet()
                                                .stream()
                                                .sorted(comparingInt((Map.Entry<String, String> e) -> e.getKey()
                                                                                                       .length())
                                                        .reversed())
                                                .collect(LinkedHashMap::new,
                                                        (map, e) -> map.put(validate(e.getKey()), e.getValue()),
                                                        LinkedHashMap::putAll);
    }

    private static String validate(String opening) {
        if (opening.isEmpty() || opening.charAt(opening.length() - 1) != OPENING_BRACE)
            throw new OfficeStamperException(
                    "A placeholder opening delimiter must end with '%c', but was '%s'".formatted(OPENING_BRACE,
                            opening));
        return opening;
    }

    @Override
    public void process(WordprocessingMLPackage document) {
        var visitor = new ParagraphCollector(elementByOpening.keySet());
        WmlUtils.visitDocument(document, visitor);
        for (var paragraph : visitor.paragraphs()) hook(paragraph);
    }

    /// Wraps every placeholder of the given paragraph with a smart tag, in a single left-to-right pass.
    private void hook(P paragraph) {
        var text = asString(paragraph);
        var cursor = 0;
        while (cursor < text.length()) {
            var opening = openingAt(text, cursor);
            if (opening.isEmpty()) {
                cursor++;
                continue;
            }
            var delimiter = opening.get();
            var placeholder = scan(text, cursor, delimiter);
            var expression = placeholder.expression();
            insertSmartTag(elementByOpening.get(delimiter), paragraph, expression, cursor, placeholder.end());
            // The tag holds exactly the expression, delimiters stripped, so scanning resumes right after it.
            cursor += expression.length();
            text = asString(paragraph);
        }
    }

    /// Returns the opening delimiter starting at the given index, if any.
    private Optional<String> openingAt(String text, int index) {
        return elementByOpening.keySet()
                               .stream()
                               .filter(opening -> text.startsWith(opening, index))
                               .findFirst();
    }

    /// Scans a placeholder starting at `start`, balancing nested braces.
    ///
    /// When the opening brace is balanced, the placeholder stops right after the matching closing brace and the
    /// expression excludes both delimiters. When it is never balanced, the placeholder is malformed: it spans the rest
    /// of the text and the expression keeps the delimiters, so that the engine reports it as unparseable.
    private static Placeholder scan(String text, int start, String opening) {
        var depth = 1;
        for (var index = start + opening.length(); index < text.length(); index++) {
            var character = text.charAt(index);
            if (character == OPENING_BRACE) depth++;
            else if (character == CLOSING_BRACE && --depth == 0)
                return new Placeholder(index + 1, text.substring(start + opening.length(), index));
        }
        return new Placeholder(text.length(), text.substring(start));
    }

    /// A placeholder located in a paragraph's text.
    ///
    /// @param end the index right after the placeholder.
    /// @param expression the expression the smart tag will carry.
    private record Placeholder(int end, String expression) {}

    /// A [TraversalUtilVisitor] implementation that collects the paragraphs possibly holding a placeholder.
    ///
    /// This class is used to traverse a document and collect all paragraph elements ([P]) containing at least one of
    /// the given opening delimiters. The collected paragraphs can be retrieved using the [#paragraphs()] method.
    public static class ParagraphCollector
            extends TraversalUtilVisitor<P> {

        private final List<String> openings;
        private final List<P> results = new ArrayList<>();

        /// Constructs a new [ParagraphCollector] with the specified opening delimiters.
        ///
        /// @param openings the opening delimiters to look for in paragraphs
        public ParagraphCollector(Collection<String> openings) {
            this.openings = List.copyOf(openings);
        }

        @Override
        public void apply(P element) {
            var string = asString(element);
            if (openings.stream()
                        .anyMatch(string::contains)) {
                results.add(element);
            }
        }

        /// Returns the list of collected paragraphs possibly holding a placeholder.
        ///
        /// @return a list of paragraphs containing at least one opening delimiter
        public List<P> paragraphs() {
            return results;
        }
    }
}
