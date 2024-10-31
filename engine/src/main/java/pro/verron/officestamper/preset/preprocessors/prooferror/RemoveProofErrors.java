package pro.verron.officestamper.preset.preprocessors.prooferror;

import org.docx4j.TraversalUtil;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.ContentAccessor;
import org.docx4j.wml.ProofErr;
import pro.verron.officestamper.api.PreProcessor;

public class RemoveProofErrors
        implements PreProcessor {

    /**
     * {@inheritDoc}
     */
    @Override
    public void process(WordprocessingMLPackage document) {
        var mainDocumentPart = document.getMainDocumentPart();
        var visitor = new ProofErrVisitor();
        TraversalUtil.visit(mainDocumentPart, visitor);
        for (ProofErr proofErr : visitor.getProofErrs()) {
            var proofErrParent = proofErr.getParent();
            if (proofErrParent instanceof ContentAccessor parent) {
                var parentContent = parent.getContent();
                parentContent.remove(proofErr);
            }
        }
    }

}
