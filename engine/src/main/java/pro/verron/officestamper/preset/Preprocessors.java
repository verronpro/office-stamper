package pro.verron.officestamper.preset;

import org.docx4j.TraversalUtil;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.utils.TraversalUtilVisitor;
import org.docx4j.wml.ContentAccessor;
import org.docx4j.wml.ProofErr;
import org.docx4j.wml.R;
import org.docx4j.wml.RPr;
import pro.verron.officestamper.api.OfficeStamperException;
import pro.verron.officestamper.api.PreProcessor;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;

/**
 * A helper class that provides pre-processing functionality for WordprocessingMLPackage documents.
 */
public class Preprocessors {

    private Preprocessors() {
        throw new OfficeStamperException("Preprocessors cannot be instantiated");
    }

    /**
     * Returns a PreProcessor object that merges same style runs that are next to each other in a
     * WordprocessingMLPackage document.
     *
     * @return a PreProcessor object that merges similar runs.
     */
    public static PreProcessor mergeSimilarRuns() {
        return new MergeSameStyleRuns();
    }

    /**
     * Returns a PreProcessor object that removes all {@link ProofErr} elements from the WordprocessingMLPackage
     * document.
     *
     * @return a PreProcessor object that removes ProofErr elements.
     */
    public static PreProcessor removeLanguageProof() {
        return new RemoveProofErrors();
    }

    private static class MergeSameStyleRuns
            implements PreProcessor {

        /**
         * {@inheritDoc}
         */
        @Override
        public void process(WordprocessingMLPackage document) {
            var mainDocumentPart = document.getMainDocumentPart();
            var visitor = new SimilarRunVisitor();
            TraversalUtil.visit(mainDocumentPart, visitor);
            for (List<R> similarStyleRuns : visitor.getSimilarStyleRuns()) {
                R firstRun = similarStyleRuns.get(0);
                var runContent = firstRun.getContent();
                var firstRunContent = new LinkedHashSet<>(runContent);
                var firstRunParentContent = ((ContentAccessor) firstRun.getParent()).getContent();
                for (R r : similarStyleRuns.subList(1, similarStyleRuns.size())) {
                    firstRunParentContent.remove(r);
                    firstRunContent.addAll(r.getContent());
                }
                runContent.clear();
                runContent.addAll(firstRunContent);
            }
        }

        private static class SimilarRunVisitor
                extends TraversalUtilVisitor<R> {

            private final List<List<R>> similarStyleRuns = new ArrayList<>();

            public List<List<R>> getSimilarStyleRuns() {
                return similarStyleRuns;
            }

            @Override
            public void apply(R element, Object parent, List<Object> siblings) {
                RPr rPr = element.getRPr();
                int currentIndex = siblings.indexOf(element);
                List<R> similarStyleConcurrentRun = siblings
                        .stream()
                        .skip(currentIndex)
                        .takeWhile(o -> o instanceof R run && Objects.equals(run.getRPr(), rPr))
                        .map(R.class::cast)
                        .toList();

                if (similarStyleConcurrentRun.size() > 1)
                    similarStyleRuns.add(similarStyleConcurrentRun);
            }
        }
    }

    private static class RemoveProofErrors
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

        private static class ProofErrVisitor
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
    }
}
