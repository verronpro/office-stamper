package pro.verron.officestamper.api;

import org.docx4j.wml.R;

public record ProcessorContext(
        Paragraph paragraph, R run, Comment comment, Placeholder placeholder
) {}
