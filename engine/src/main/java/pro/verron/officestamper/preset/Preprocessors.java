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

    }
}
