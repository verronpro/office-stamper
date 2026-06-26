package pro.verron.officestamper;

import pro.verron.officestamper.api.OfficeStamperConfiguration;

import java.io.InputStream;
import java.io.OutputStream;

import static pro.verron.officestamper.experimental.ExperimentalStampers.pptxStamper;
import static pro.verron.officestamper.preset.OfficeStampers.docxStamper;

enum TemplateKind {
    WORD {
        @Override
        void stamp(InputStream templateStream, Object context, OfficeStamperConfiguration configuration, OutputStream os) {
            docxStamper(configuration).stamp(templateStream, context, os);
        }
    }, POWERPOINT {
        @Override
        void stamp(InputStream templateStream, Object context, OfficeStamperConfiguration configuration, OutputStream os) {
            pptxStamper().stamp(templateStream, context, os);
        }
    };

    abstract void stamp(InputStream templateStream, Object context, OfficeStamperConfiguration configuration, OutputStream os);
}
