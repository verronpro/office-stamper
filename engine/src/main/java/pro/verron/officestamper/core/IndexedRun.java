package pro.verron.officestamper.core;

import org.docx4j.wml.R;
import org.docx4j.wml.RPr;

/// Represents a run (i.e., a text fragment) in a paragraph. The run is indexed relative to the containing paragraph
/// and also relative to the containing document.
///
/// @param startIndex    the start index of the run relative to the containing paragraph.
/// @param endIndex      the end index of the run relative to the containing paragraph.
/// @param indexInParent the index of the run relative to the containing document.
/// @param run           the run itself.
///
/// @author Joseph Verron
/// @author Tom Hombergs
/// @version ${version}
/// @since 1.0.0
public record IndexedRun(int startIndex, int endIndex, int indexInParent, R run) {

    public int length() {
        return getText().length();
    }

    public String getText() {
        return RunUtil.getText(run());
    }

    public String substring(int endIndex) {
        return getText().substring(endIndex);
    }

    public String substring(int beginIndex, int endIndex) {
        return getText().substring(beginIndex, endIndex);
    }

    public int indexOf(String full) {
        return getText().indexOf(full);
    }

    public RPr getPr() {
        return run.getRPr();
    }

    /// Determines whether the specified range of start and end index touches this run.
    ///
    /// Example:
    /// given this run: (a,b,c,d,e,f,g,h,i,j)
    /// And the range [2,5]
    /// This method will return true, because the range touches the run at the indices 2, 3, 4 and 5.
    ///
    /// @param globalStartIndex the global index (meaning the index relative to multiple aggregated runs) at which to
    ///                                                                         start the range.
    /// @param globalEndIndex   the global index (meaning the index relative to multiple aggregated runs) at which to
    ///                                                 end
    ///                                                                         the range.
    ///
    /// @return true, if the range touches this run, false otherwise.
    public boolean isTouchedByRange(int globalStartIndex, int globalEndIndex) {
        var startBetweenIndices = (globalStartIndex < startIndex) && (startIndex <= globalEndIndex);
        var endBetweenIndices = (globalStartIndex < endIndex) && (endIndex <= globalEndIndex);
        return startBetweenIndices
               || endBetweenIndices
               || ((startIndex <= globalStartIndex)
                   && (globalEndIndex <= endIndex));
    }

    /// Replaces the substring starting at the given index with the given replacement string.
    ///
    /// @param globalStartIndex the global index at which to
    ///                         start the replacement.
    /// @param globalEndIndex   the global index at which to end
    ///                         the replacement.
    /// @param replacement      the string to replace the substring at the specified global index.
    public void replace(int globalStartIndex, int globalEndIndex, String replacement) {
        int localStartIndex = globalIndexToLocalIndex(globalStartIndex);
        int localEndIndex = globalIndexToLocalIndex(globalEndIndex);
        var text = RunUtil.getSubstring(run, 0, localStartIndex);
        text += replacement;
        String runText = RunUtil.getText(run);
        if (!runText.isEmpty()) {
            text += RunUtil.getSubstring(run, localEndIndex);
        }
        RunUtil.setText(run, text);
    }

    /// Converts a global index to a local index within the context of this run.
    /// (meaning the index relative to multiple aggregated runs)
    ///
    /// @param globalIndex the global index to convert.
    ///
    /// @return the local index corresponding to the given global index.
    private int globalIndexToLocalIndex(int globalIndex) {
        if (globalIndex < startIndex) return 0;
        else if (globalIndex > endIndex) return RunUtil.getLength(run);
        else return globalIndex - startIndex;
    }
}
