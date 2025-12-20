package pro.verron.officestamper.preset.preprocessors.prooferror;

import org.docx4j.utils.TraversalUtilVisitor;
import org.docx4j.wml.ProofErr;

import java.util.ArrayList;
import java.util.List;

/// A visitor implementation for traversing and collecting [ProofErr] elements in a DOCX document. This class extends
/// [TraversalUtilVisitor] to visit all [ProofErr] elements (proofing errors) in the document structure and maintains a
/// collection of encountered elements.
public class ProofErrVisitor
        extends TraversalUtilVisitor<ProofErr> {
    private final List<ProofErr> proofErrs = new ArrayList<>();

    @Override
    public void apply(ProofErr element, Object parent1, List<Object> siblings) {
        proofErrs.add(element);
    }


    /// Returns the list of collected [ProofErr] elements.
    ///
    /// @return a list of [ProofErr] objects that were encountered during traversal
    public List<ProofErr> getProofErrs() {
        return proofErrs;
    }
}
