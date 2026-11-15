package pro.verron.officestamper;

import pro.verron.officestamper.api.OfficeStamperConfiguration;
import pro.verron.officestamper.api.OfficeStamperException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static pro.verron.officestamper.docx4j11.DocxOfficeStampers.docxStamper;

enum TemplateKind {
    WORD {
        @Override
        void stamp(InputStream templateStream, Object context, OfficeStamperConfiguration configuration, OutputStream os)
                throws IOException {
            docxStamper(configuration).stamp(templateStream, context, os);
        }
    }, POWERPOINT {
        @Override
        void stamp(InputStream templateStream, Object context, OfficeStamperConfiguration configuration, OutputStream os) {
            // The experimental pptx stamper is not yet ported to the docx4j-free contracts (P3);
            // excluded from compilation until then.
            throw new OfficeStamperException("PPTX stamping is temporarily unavailable in this build");
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
