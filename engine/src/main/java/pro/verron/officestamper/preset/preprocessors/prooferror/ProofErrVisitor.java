package pro.verron.officestamper.preset.preprocessors.prooferror;

import org.docx4j.utils.TraversalUtilVisitor;
import org.docx4j.wml.ProofErr;

import java.util.ArrayList;
import java.util.List;

public class ProofErrVisitor
        extends TraversalUtilVisitor<ProofErr> {
    private final List<ProofErr> proofErrs = new ArrayList<>();

    @Override
    public void apply(ProofErr element, Object parent1, List<Object> siblings) {
        proofErrs.add(element);
    }

    public List<ProofErr> getProofErrs() {
        return proofErrs;
    }
}
