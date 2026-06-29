package pro.verron.officestamper;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import pro.verron.officestamper.excel.ExcelMergeStrategy;

@Command(name = "excel strategy")
public class ExcelStrategy {
    @Option(
            names = {"--excel-merge-strategy"},
            required = true,
            defaultValue = "MAP",
            description = "Excel merge strategy: MAP (default, each sheet is a key) or JOIN (inner join sheets)"
    )
    private ExcelMergeStrategy mergeStrategy;

    @Option(
            names = {"--excel-join-key"},
            required = false,
            defaultValue = "ID",
            description = "Key to use for joining Excel sheets (used with JOIN strategy)"
    )
    private String joinKey;

    public ExcelStrategy() {
    }

    public ExcelStrategy(ExcelMergeStrategy mergeStrategy, String joinKey) {
        this.mergeStrategy = mergeStrategy;
        this.joinKey = joinKey;
    }

    public ExcelMergeStrategy getMergeStrategy() {
        return mergeStrategy;
    }

    public String getJoinKey() {
        return joinKey;
    }
}
