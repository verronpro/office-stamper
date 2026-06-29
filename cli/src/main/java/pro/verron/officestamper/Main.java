package pro.verron.officestamper;


import picocli.CommandLine;
import picocli.CommandLine.Command;

/// Main class for the CLI.
@Command(
        name = "officestamper",
        mixinStandardHelpOptions = true,
        description = "Office Stamper CLI tool",
        subcommands = {Stamp.class, Preview.class, ReportView.class, Diagnostic.class}
)
public class Main {
    void main(String[] args) {
        var main = new Main();
        var cli = new CommandLine(main);
        int exitCode = cli.execute(args);
        System.exit(exitCode);
    }
}
