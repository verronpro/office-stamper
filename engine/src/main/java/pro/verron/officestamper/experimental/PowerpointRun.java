package pro.verron.officestamper.experimental;

import org.docx4j.dml.CTRegularTextRun;

/// Represents a run within a PowerPoint slide.
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
    /// @return `true` if the range touches the start or end index of the run,`false` otherwise.
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
