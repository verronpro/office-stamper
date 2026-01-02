package pro.verron.officestamper.preset;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.ProofErr;
import pro.verron.officestamper.api.CommentHooker;
import pro.verron.officestamper.api.OfficeStamperException;
import pro.verron.officestamper.api.PlaceholderHooker;
import pro.verron.officestamper.api.PreProcessor;
import pro.verron.officestamper.preset.preprocessors.malformedcomments.RemoveMalformedComments;
import pro.verron.officestamper.preset.preprocessors.prooferror.RemoveProofErrors;
import pro.verron.officestamper.preset.preprocessors.rmlang.RemoveLang;
import pro.verron.officestamper.preset.preprocessors.similarrun.MergeSameStyleRuns;

/// A helper class that provides pre-processing functionality for [WordprocessingMLPackage] documents.
public class Preprocessors {

    private Preprocessors() {
        throw new OfficeStamperException("Preprocessors cannot be instantiated");
    }

    /// Returns a [PreProcessor] object that merges same style runs that are next to each other in a
    /// [WordprocessingMLPackage] document.
    ///
    /// @return a [PreProcessor] object that merges similar runs.
    public static PreProcessor mergeSimilarRuns() {
        return new MergeSameStyleRuns();
    }

    /// Returns a [PreProcessor] object that removes all [ProofErr] elements from the [WordprocessingMLPackage]
    /// document.
    ///
    /// @return a [PreProcessor] object that removes [ProofErr] elements.
    public static PreProcessor removeLanguageProof() {
        return new RemoveProofErrors();
    }

    /// Returns a [PreProcessor] object that removes all language informations such as grammatical and orthographics
    /// markers in a [WordprocessingMLPackage] document.
    ///
    /// @return a [PreProcessor] object that removes language markers.
    public static PreProcessor removeLanguageInfo() {
        return new RemoveLang();
    }

    /// Returns a [PreProcessor] object that removes comments information that is not conforming to the expected
    /// patterns.
    ///
    /// @return a [PreProcessor] object that removes malformed comments markers.
    public static PreProcessor removeMalformedComments() {
        return new RemoveMalformedComments();
    }

    /// Returns a [PreProcessor] object that prepares inline placeholders based on the provided regex and element name.
    ///
    /// @param regex the regular expression used to identify placeholders in the document
    /// @param element the name of the smart tag element to be used for the placeholders
    ///
    /// @return a [PreProcessor] object that prepares inline placeholders.
    public static PreProcessor preparePlaceholders(String regex, String element) {
        return new PlaceholderHooker(regex, element);
    }


    /// Returns a [PreProcessor] object that prepares comment processors for use with the stamper.
    ///
    /// @return a [PreProcessor] object that prepares comment processors.
    public static PreProcessor prepareCommentProcessor() {
        return new CommentHooker();
    }
}
