package pro.verron.officestamper;

import pro.verron.officestamper.api.OfficeStamperConfiguration;
import pro.verron.officestamper.api.OfficeStamperException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static pro.verron.officestamper.docx.DocxOfficeStampers.docxStamper;
import static pro.verron.officestamper.docx.experimental.ExperimentalStampers.pptxStamper;

enum TemplateKind {
    WORD {
        @Override
        void stamp(InputStream templateStream, Object context, OfficeStamperConfiguration configuration, OutputStream os)
                throws IOException {
            docxStamper(configuration).stamp(templateStream, context, os);
        }
    }, POWERPOINT {
        @Override
        void stamp(InputStream templateStream, Object context, OfficeStamperConfiguration configuration, OutputStream os)
                throws IOException {
            // The experimental pptx stamper predates the configuration seam and runs with its own
            // minimal resolution pipeline (direct SpEL against the context).
            pptxStamper().stamp(templateStream, context, os);
        }
    };

    static TemplateKind templateKind(String templatePath) {
        var lower = templatePath.toLowerCase();
        if (lower.endsWith(".docx")) return WORD;
        if (lower.endsWith(".pptx")) return POWERPOINT;
        var msg = "Unsupported template type (expected .docx or .pptx): %s";
        throw new OfficeStamperException(msg.formatted(templatePath));
    }

    abstract void stamp(InputStream templateStream, Object context, OfficeStamperConfiguration configuration,
            OutputStream os) throws IOException;
}
