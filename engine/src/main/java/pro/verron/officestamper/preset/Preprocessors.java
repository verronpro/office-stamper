package pro.verron.officestamper.preset;

import org.docx4j.wml.ProofErr;
import pro.verron.officestamper.api.OfficeStamperException;
import pro.verron.officestamper.api.PreProcessor;
import pro.verron.officestamper.preset.preprocessors.googledocscomments.RealignGoogleDocsCommentAnchors;
import pro.verron.officestamper.preset.preprocessors.malformedcomments.RemoveMalformedComments;
import pro.verron.officestamper.preset.preprocessors.prooferror.RemoveProofErrors;
import pro.verron.officestamper.preset.preprocessors.rmlang.RemoveLang;
import pro.verron.officestamper.preset.preprocessors.similarrun.MergeSameStyleRuns;

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

    public static PreProcessor removeLanguageInfo() {
        return new RemoveLang();
    }

    public static PreProcessor removeMalformedComments() {
        return new RemoveMalformedComments();
    }

    public static PreProcessor realignGoogleDocsCommentAnchors() {
        return new RealignGoogleDocsCommentAnchors();
    }
}
