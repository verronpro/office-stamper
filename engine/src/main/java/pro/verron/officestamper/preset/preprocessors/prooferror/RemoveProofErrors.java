package pro.verron.officestamper.preset.preprocessors.prooferror;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.ContentAccessor;
import org.docx4j.wml.ProofErr;
import pro.verron.officestamper.api.PreProcessor;

import static pro.verron.officestamper.core.DocumentUtil.visitDocument;

public class RemoveProofErrors
        implements PreProcessor {

    @Override
    public void process(WordprocessingMLPackage document) {
        var visitor = new ProofErrVisitor();
        visitDocument(document, visitor);
        for (ProofErr proofErr : visitor.getProofErrs()) {
            var proofErrParent = proofErr.getParent();
            if (proofErrParent instanceof ContentAccessor parent) {
                var parentContent = parent.getContent();
                parentContent.remove(proofErr);
            }
        }
    }

}
