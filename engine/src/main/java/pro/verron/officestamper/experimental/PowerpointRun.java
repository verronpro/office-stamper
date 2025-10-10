package pro.verron.officestamper.experimental;

import org.docx4j.dml.CTRegularTextRun;

/// A record that represents a run of text in a PowerPoint slide. It holds information
/// about the run's indices and the underlying text content. The indices define the
/// range of the text run within a global context, and the backing content is stored
/// as a `CTRegularTextRun` instance from the supporting library.
///
/// This class provides methods to determine if a text run is affected by a global range
/// of indices and to replace specific substrings within its text.
///
/// @param startIndex the start index of the run within the global context.
/// @param endIndex   the end index of the run within the global context.
/// @param indexInParent the index of the run within its parent paragraph.
/// @param run the underlying `CTRegularTextRun` instance.
public record PowerpointRun(
        int startIndex,
        int endIndex,
        int indexInParent,
        CTRegularTextRun run
) {
    /// Checks if the given range of indices touches the start or end index of the run.
    ///
    /// @param globalStartIndex the start index of the global range.
    /// @param globalEndIndex   the end index of the global range.
    ///
    /// @return `true` if the range touches the start or end index of the run, `false` otherwise.
    public boolean isTouchedByRange(int globalStartIndex, int globalEndIndex) {
        return ((startIndex >= globalStartIndex) && (startIndex <= globalEndIndex))
                || ((endIndex >= globalStartIndex) && (endIndex <= globalEndIndex))
                || ((startIndex <= globalStartIndex) && (endIndex >= globalEndIndex));
    }

    /// Replaces a substring within the run's text.
    ///
    /// @param globalStartIndex the start index of the substring to be replaced.
    /// @param globalEndIndex   the end index of the substring to be replaced.
    /// @param replacement      the replacement string.
    public void replace(
            int globalStartIndex,
            int globalEndIndex,
            String replacement
    ) {
        int localStartIndex = globalIndexToLocalIndex(globalStartIndex);
        int localEndIndex = globalIndexToLocalIndex(globalEndIndex);
        var source = run.getT();
        var target = source.substring(0, localStartIndex)
                + replacement
                + source.substring(localEndIndex + 1);
        run.setT(target);
    }

    private int globalIndexToLocalIndex(int globalIndex) {
        if (globalIndex < startIndex) return 0;
        else if (globalIndex > endIndex) return lastIndex();
        else return globalIndex - startIndex;
    }

    private int lastIndex() {
        return lastIndex(run.getT());
    }

    private int lastIndex(String string) {
        return string.length() - 1;
    }
}
