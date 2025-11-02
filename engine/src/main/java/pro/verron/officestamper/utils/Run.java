package pro.verron.officestamper.utils;

import org.docx4j.wml.R;
import org.docx4j.wml.RPr;

/// Represents a run (i.e., a text fragment) in a paragraph. The run is indexed relative to the containing paragraph
/// and also relative to the containing document.
///
/// @param startIndex    the start index of the run relative to the containing paragraph.
/// @param indexInParent the index of the run relative to the containing document.
/// @param run           the run itself.
///
/// @author Joseph Verron
/// @author Tom Hombergs
/// @version ${version}
/// @since 1.0.0
public record Run(int startIndex, int indexInParent, R run) {

    /// Retrieves a substring from the text content of this run, starting at the specified begin index.
    ///
    /// @param beginIndex the beginning index, inclusive, for the substring.
    ///
    /// @return the substring of the run's text starting from the specified begin index to the end of the text.
    public String substring(int beginIndex) {
        return getText().substring(beginIndex);
    }

    /// Retrieves a substring from the text content of this run, starting
    /// at the specified begin index and ending at the specified end index.
    ///
    /// @param beginIndex the beginning index, inclusive, for the substring.
    /// @param endIndex   the ending index, exclusive, for the substring.
    ///
    /// @return the substring of the run's text from the specified begin index to the specified end index.
    public String substring(int beginIndex, int endIndex) {
        return getText().substring(beginIndex, endIndex);
    }

    /// Finds the index of the first occurrence of the specified substring in the text of the current run.
    ///
    /// @param full the substring to search for within the run's text.
    ///
    /// @return the index of the first occurrence of the specified substring,
    /// or &ndash;1 if the substring is not found.
    public int indexOf(String full) {
        return getText().indexOf(full);
    }

    /// Returns the text string of a run.
    ///
    /// @return [String] representation of the run.
    public String getText() {
        return WmlUtils.asString(run);
    }

    /// Retrieves the properties associated with this run.
    ///
    /// @return the [RPr] object representing the properties of the run.
    public RPr getPr() {
        return run.getRPr();
    }

    /// Determines whether the current run is affected by the specified range of global start and end indices.
    /// A run is considered "touched" if any part of it overlaps with the given range.
    ///
    /// @param globalStartIndex the global start index of the range.
    /// @param globalEndIndex   the global end index of the range.
    ///
    /// @return `true` if the current run is touched by the specified range; `false` otherwise.
    public boolean isTouchedByRange(int globalStartIndex, int globalEndIndex) {
        return startsInRange(globalStartIndex, globalEndIndex) || endsInRange(globalStartIndex, globalEndIndex)
               || englobesRange(globalStartIndex, globalEndIndex);
    }

    private boolean startsInRange(int globalStartIndex, int globalEndIndex) {
        return globalStartIndex < startIndex && startIndex <= globalEndIndex;
    }

    private boolean endsInRange(int globalStartIndex, int globalEndIndex) {
        return globalStartIndex < endIndex() && endIndex() <= globalEndIndex;
    }

    private boolean englobesRange(int globalStartIndex, int globalEndIndex) {
        return startIndex <= globalStartIndex && globalEndIndex <= endIndex();
    }

    /// Calculates the end index of the current run based on its start index and length.
    ///
    /// @return the end index of the run.
    public int endIndex() {
        return startIndex + length();
    }

    /// Calculates the length of the text content of this run.
    ///
    /// @return the length of the text in the current run.
    public int length() {
        return getText().length();
    }

    /// Replaces the substring starting at the given index with the given replacement string.
    ///
    /// @param globalStartIndex the global index at which to start the replacement.
    /// @param globalEndIndex   the global index at which to end the replacement.
    /// @param replacement      the string to replace the substring at the specified global index.
    public void replace(int globalStartIndex, int globalEndIndex, String replacement) {
        int localStartIndex = globalIndexToLocalIndex(globalStartIndex);
        int localEndIndex = globalIndexToLocalIndex(globalEndIndex);
        var text = substring(0, localStartIndex);
        text += replacement;
        String runText = getText();
        if (!runText.isEmpty()) {
            text += substring(localEndIndex);
        }
        WmlUtils.setText(run, text);
    }

    /// Converts a global index to a local index within the context of this run.
    /// (meaning the index relative to multiple aggregated runs)
    ///
    /// @param globalIndex the global index to convert.
    ///
    /// @return the local index corresponding to the given global index.
    private int globalIndexToLocalIndex(int globalIndex) {
        if (globalIndex < startIndex) return 0;
        else if (globalIndex > endIndex()) return length();
        else return globalIndex - startIndex;
    }
}
