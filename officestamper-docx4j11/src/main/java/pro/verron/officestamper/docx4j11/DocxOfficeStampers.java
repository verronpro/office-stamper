package pro.verron.officestamper.docx4j11;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import pro.verron.officestamper.api.ObjectResolver;
import pro.verron.officestamper.api.OfficeStamperException;

import pro.verron.officestamper.api.OfficeStamperConfiguration;
import pro.verron.officestamper.docx4j11.preset.CommentHooker;
import pro.verron.officestamper.docx4j11.preset.CommentRemover;
import pro.verron.officestamper.docx4j11.preset.HookRemover;
import pro.verron.officestamper.docx4j11.preset.PlaceholderHooker;
import pro.verron.officestamper.docx4j11.preset.cleanendnotes.RemoveOrphanedEndnotesProcessor;
import pro.verron.officestamper.docx4j11.preset.cleanfootnotes.RemoveOrphanedFootnotesProcessor;
import pro.verron.officestamper.docx4j11.preset.resolvers.image.ImageResolver;
import pro.verron.officestamper.docx4j11.preset.malformedcomments.RemoveMalformedComments;
import pro.verron.officestamper.docx4j11.preset.repeat.RepeatDocPartProcessor;
import pro.verron.officestamper.docx4j11.preset.repeat.RepeatParagraphProcessor;
import pro.verron.officestamper.docx4j11.preset.repeat.RepeatProcessor;
import pro.verron.officestamper.docx4j11.preset.prooferror.RemoveProofErrors;
import pro.verron.officestamper.docx4j11.preset.rmlang.RemoveLang;
import pro.verron.officestamper.docx4j11.preset.similarrun.MergeSameStyleRuns;
import pro.verron.officestamper.preset.CommentProcessorFactory;
import pro.verron.officestamper.preset.OfficeStamperConfigurations;
import pro.verron.officestamper.preset.Resolvers;
import pro.verron.officestamper.spi.OfficeDocument;
import pro.verron.officestamper.spi.OfficeStamper;
import pro.verron.officestamper.spi.StreamStamper;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/// Entry point for stamping DOCX documents. Composes the docx4j-free configuration presets
/// ([OfficeStamperConfigurations]) with the docx4j-backed processors, and provides the
/// stream-based stamping facade.
///
/// @author Joseph Verron
/// @since 1.9.0
public final class DocxOfficeStampers {

    private DocxOfficeStampers() {
        throw new pro.verron.officestamper.api.OfficeStamperException(
                "DocxOfficeStampers cannot be instantiated");
    }

    /// Creates a default full-featured stream stamper for DOCX templates.
    ///
    /// @return a stamper reading DOCX templates from streams
    public static StreamStamper<OfficeDocument> docxStamper() {
        return docxStamper(OfficeStamperConfigurations.full());
    }

    /// Creates a stamper operating directly on [WordprocessingMLPackage] documents, with the
    /// legacy full configuration.
    ///
    /// @return a stamper for docx4j [WordprocessingMLPackage] documents
    public static OfficeStamper<WordprocessingMLPackage> docxPackageStamper() {
        return new LegacyOfficeStamper();
    }

    /// Creates a stamper operating directly on [WordprocessingMLPackage] documents, with the given
    /// configuration used as-is.
    ///
    /// @param configuration the configuration to use for this stamper
    /// @return a stamper for docx4j [WordprocessingMLPackage] documents
    public static OfficeStamper<WordprocessingMLPackage> docxPackageStamper(OfficeStamperConfiguration configuration) {
        var stamper = pro.verron.officestamper.preset.OfficeStampers.docxStamper(configuration);
        return (template, context) -> ((DocxDocument) stamper.stamp(new DocxDocument(template), context)).pkg();
    }

    /// Creates a stream stamper for DOCX templates from the given configuration.
    ///
    /// @param configuration the configuration to use for this stamper
    /// @return a stamper reading DOCX templates from streams
    public static StreamStamper<OfficeDocument> docxStamper(OfficeStamperConfiguration configuration) {
        var stamper = pro.verron.officestamper.preset.OfficeStampers.docxStamper(configuration);
        return new StreamStamper<>(DocxOfficeStampers::load, stamper, DocxOfficeStampers::save);
    }

    private static OfficeDocument load(InputStream in) {
        try {
            return DocxDocument.load(in);
        } catch (IOException e) {
            throw new pro.verron.officestamper.api.OfficeStamperException("Failed to load DOCX", e);
        }
    }

    /// Applies the docx4j-backed processors of the legacy presets to a docx4j-free configuration:
    /// placeholder and comment preparation, tag/comment cleanup, the docx4j-coupled pre-/post-processors,
    /// the image resolver and the repeat processors.
    ///
    /// @param configuration the docx4j-free configuration to enhance
    /// @return the same configuration instance, enhanced
    public static OfficeStamperConfiguration withDocxProcessors(OfficeStamperConfiguration configuration) {
        withPlaceholderPreparation(configuration);
        configuration.addPreprocessor(new RemoveMalformedComments());
        configuration.addPreprocessor(new RemoveProofErrors());
        configuration.addPreprocessor(new RemoveLang());
        configuration.addPreprocessor(new MergeSameStyleRuns());
        configuration.addPostprocessor(new RemoveOrphanedFootnotesProcessor());
        configuration.addPostprocessor(new RemoveOrphanedEndnotesProcessor());
        configuration.addResolver(new ImageResolver(true));
        configuration.addCommentProcessor(CommentProcessorFactory.IRepeatProcessor.class, RepeatProcessor::new);
        configuration.addCommentProcessor(CommentProcessorFactory.IParagraphRepeatProcessor.class,
                RepeatParagraphProcessor::new);
        configuration.addCommentProcessor(CommentProcessorFactory.IRepeatDocPartProcessor.class,
                RepeatDocPartProcessor::new);
        return configuration;
    }

    /// Equivalent of the legacy full preset: docx4j-free standard base + docx4j-backed processors.
    ///
    /// @return a fully configured [OfficeStamperConfiguration]
    public static OfficeStamperConfiguration full() {
        return withDocxProcessors(OfficeStamperConfigurations.full());
    }

    /// Equivalent of the legacy standard preset: the pure processors and resolvers plus placeholder
    /// preparation, malformed-comment cleanup, the image resolver and the repeat processors.
    ///
    /// @return a standard configured [OfficeStamperConfiguration]
    public static OfficeStamperConfiguration standard() {
        return standard(Resolvers.fallback("\n"));
    }

    /// Equivalent of the legacy standard preset with a custom fallback resolver.
    ///
    /// @param fallback the fallback resolver to use for unresolvable placeholders
    /// @return a standard configured [OfficeStamperConfiguration]
    public static OfficeStamperConfiguration standard(ObjectResolver fallback) {
        var configuration = withPlaceholderPreparation(OfficeStamperConfigurations.standard(fallback));
        configuration.addPreprocessor(new RemoveMalformedComments());
        configuration.addResolver(new ImageResolver(true));
        configuration.addCommentProcessor(CommentProcessorFactory.IRepeatProcessor.class, RepeatProcessor::new);
        configuration.addCommentProcessor(CommentProcessorFactory.IParagraphRepeatProcessor.class,
                RepeatParagraphProcessor::new);
        configuration.addCommentProcessor(CommentProcessorFactory.IRepeatDocPartProcessor.class,
                RepeatDocPartProcessor::new);
        return configuration;
    }

    /// Equivalent of the legacy minimal preset: docx4j-free minimal base + placeholder/comment preparation
    /// and cleanup.
    ///
    /// @return a minimally configured [OfficeStamperConfiguration]
    public static OfficeStamperConfiguration minimal() {
        return withPlaceholderPreparation(OfficeStamperConfigurations.minimal());
    }

    private static OfficeStamperConfiguration withPlaceholderPreparation(OfficeStamperConfiguration configuration) {
        configuration.addPreprocessor(new PlaceholderHooker("${", "placeholder"));
        configuration.addPreprocessor(new PlaceholderHooker("#{", "inlineProcessor"));
        configuration.addPreprocessor(new CommentHooker());
        configuration.addPostprocessor(new HookRemover("officestamper"));
        configuration.addPostprocessor(new CommentRemover());
        return configuration;
    }

    private static void save(OfficeDocument document, OutputStream out) {
        try {
            document.save(out);
        } catch (IOException e) {
            throw new pro.verron.officestamper.api.OfficeStamperException("Failed to save DOCX", e);
        }
    }
}

