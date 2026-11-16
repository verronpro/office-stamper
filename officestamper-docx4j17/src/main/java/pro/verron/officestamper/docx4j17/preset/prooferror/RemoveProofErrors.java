package pro.verron.officestamper.docx4j17.preset.prooferror;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.ContentAccessor;
import org.docx4j.wml.ProofErr;
import pro.verron.officestamper.spi.OfficeDocument;
import pro.verron.officestamper.spi.PreProcessor;

import static pro.verron.officestamper.docx4j17.wml.WmlUtils.visitDocument;

/// This pre-processor removes all [ProofErr] elements from the document.
///
/// Proof errors are markup elements that indicate potential grammar or spelling errors in the document. This
/// pre-processor removes them to clean up the document before further processing.
///
/// <p>docx4j-coupled processor relocated from the engine preset; it implements the docx4j-free
/// [PreProcessor] contract and reaches its native document through [OfficeDocument#unwrap].
public class RemoveProofErrors
        implements PreProcessor {

    @Override
    public void process(OfficeDocument document) {
        var pkg = document.unwrap(WordprocessingMLPackage.class);
        var visitor = new ProofErrVisitor();
        visitDocument(pkg, visitor);
        for (ProofErr proofErr : visitor.getProofErrs()) {
            var proofErrParent = proofErr.getParent();
            if (proofErrParent instanceof ContentAccessor parent) {
                var parentContent = parent.getContent();
                parentContent.remove(proofErr);
            }
        }
    }
}
