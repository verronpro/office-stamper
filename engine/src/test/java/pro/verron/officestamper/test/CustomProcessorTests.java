package pro.verron.officestamper.test;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import pro.verron.officestamper.api.CommentProcessor;
import pro.verron.officestamper.api.ProcessorContext;

import java.nio.file.Path;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.argumentSet;
import static pro.verron.officestamper.preset.OfficeStamperConfigurations.raw;
import static pro.verron.officestamper.test.ContextFactory.mapContextFactory;
import static pro.verron.officestamper.test.ContextFactory.objectContextFactory;
import static pro.verron.officestamper.test.TestUtils.getResource;
import static pro.verron.officestamper.utils.WmlFactory.newRun;

@DisplayName("Custom processors features") class CustomProcessorTests {

    private static Stream<Arguments> factories() {
        return Stream.of(//
                argumentSet("Object-based", objectContextFactory()),//
                argumentSet("Map-based", mapContextFactory())//
        );
    }

    @MethodSource("factories")
    @DisplayName("Should allow to inject custom processors")
    @ParameterizedTest(name = "Should allow to inject custom processors ({argumentSetName})")
    void should_allow_custom_processors_injection(ContextFactory factory) {
        var config = raw().addCommentProcessor(ICustomProcessor.class, CustomProcessor::new);
        var template = getResource(Path.of("CustomCommentProcessorTest.docx"));
        var expected = """     
                == Custom Comment Processor Test
                
                
                Visited
                
                This paragraph is untouched.
                
                Visited
                
                """;
        var stamper = new TestDocxStamper<>(config);
        var actual = stamper.stampAndLoadAndExtract(template, factory.empty());
        assertEquals(expected, actual);
    }

    /// A custom processor interface that defines methods to handle specific actions during document processing.
    public interface ICustomProcessor {

        /// This method is invoked to perform actions on a paragraph element if it holds a comment with the content
        /// "`visitParagraph()`".
        void visitParagraph();
    }

    /// CustomProcessor is a concrete implementation of the CommentProcessor abstract class and the ICustomProcessor
    /// interface. It is designed to process comments and associated content within a paragraph in a custom manner.
    ///
    /// This class modifies the content of a paragraph when the `visitParagraph` method is invoked. Specifically, it
    /// clears the content of the paragraph and replaces it with the word "Visited".
    public static class CustomProcessor
            extends CommentProcessor
            implements ICustomProcessor {

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
