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

    /// Returns a [PreProcessor] object that prepares inline placeholders introduced by the given opening delimiter.
    /// The matching closing brace is found by brace balancing, so expressions may nest braces freely (for instance a
    /// SpEL inline list or map).
    ///
    /// @param opening the literal opening delimiter of a placeholder, for instance `${` or `#{`. It must end with an
    ///         opening brace.
    /// @param element the name of the smart tag type to wrap matching placeholders with.
    /// @return a [PreProcessor] object that prepares inline placeholders.
    /// @see PlaceholderHooker
    public static PreProcessor preparePlaceholders(String opening, String element) {
        return new PlaceholderHooker(opening, element);
    }


    /// Returns a [PreProcessor] object that prepares comment processors for use with the stamper.
    ///
    /// @return a [PreProcessor] object that prepares comment processors.
    public static PreProcessor prepareCommentProcessor() {
        return new CommentHooker();
    }
}
