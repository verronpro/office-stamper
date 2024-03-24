package pro.verron.docxstamper.core;

import org.docx4j.dml.CTRegularTextRun;

public record PowerpointRun(int startIndex, int endIndex, int indexInParent, CTRegularTextRun run) {
    public boolean isTouchedByRange(int globalStartIndex, int globalEndIndex) {
        return ((startIndex >= globalStartIndex) && (startIndex <= globalEndIndex))
                || ((endIndex >= globalStartIndex) && (endIndex <= globalEndIndex))
                || ((startIndex <= globalStartIndex) && (endIndex >= globalEndIndex));
    }

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
        else if (globalIndex > endIndex) return lastIndex(run.getT());
        else return globalIndex - startIndex;
    }

    private int lastIndex(String string) {
        return string.length() - 1;
    }
}
