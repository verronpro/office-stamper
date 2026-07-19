package pro.verron.officestamper.test;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import pro.verron.officestamper.api.CommentProcessor;
import pro.verron.officestamper.api.ProcessorContext;
import pro.verron.officestamper.test.utils.ContextFactory;
import pro.verron.officestamper.test.utils.OfficeStamperTestBase;
import pro.verron.officestamper.test.utils.ResourceUtils;

import java.nio.file.Path;

import static pro.verron.officestamper.preset.OfficeStamperConfigurations.minimal;
import static pro.verron.officestamper.test.utils.ResourceUtils.getWordResource;
import static pro.verron.officestamper.utils.wml.WmlFactory.newRun;

@DisplayName("Custom processors features")
class CustomProcessorTests extends OfficeStamperTestBase {

    @MethodSource("factories")
    @DisplayName("Should allow to inject custom processors")
    @ParameterizedTest(name = "Should allow to inject custom processors ({argumentSetName})")
    void should_allow_custom_processors_injection(ContextFactory factory) {
        var config = minimal().addCommentProcessor(ICustomProcessor.class, CustomProcessor::new);
        var context = factory.empty();
        var template = getWordResource(Path.of("CustomCommentProcessorTest.docx"));
        var expected = """
                == Custom Comment Processor Test
                
                Visited
                
                This paragraph is untouched.
                
                Visited
                
                // section {docGrid={charSpace=-6145, linePitch=240}, pgMar={bottom=1134, left=1134, right=1134, top=1134}, pgSz={h=16838, w=11906}, space=720}
                
                """;
        testStamper(config, context, template, expected);
    }

    /// A custom processor interface that defines methods to handle specific actions during document processing.
    public interface ICustomProcessor {

        /// Invoked to perform actions on a paragraph if it holds a comment with the content "visitParagraph()".
        void visitParagraph();
    }

    /// CustomProcessor is a concrete implementation of the CommentProcessor abstract class and the ICustomProcessor
    /// interface. It is designed to process comments and associated content within a paragraph in a custom manner.
    ///
    /// This class modifies the content of a paragraph when the [#visitParagraph()] method is invoked. Specifically, it
    /// clears the content of the paragraph and replaces it with the word "Visited".
    public static class CustomProcessor extends CommentProcessor implements ICustomProcessor {

        CustomProcessor(ProcessorContext processorContext) {
            super(processorContext);
        }

        @Override
        public void visitParagraph() {
            paragraph().apply(contentAccessor -> {
                var content = contentAccessor.getContent();
                content.clear();
                content.add(newRun("Visited"));
            });
        }
    }
}
