package pro.verron.officestamper.core;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.CommentRangeEnd;
import org.docx4j.wml.CommentRangeStart;
import org.docx4j.wml.Comments;
import org.docx4j.wml.R;
import org.jvnet.jaxb2_commons.ppp.Child;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.expression.spel.SpelParserConfiguration;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import pro.verron.officestamper.api.*;
import pro.verron.officestamper.utils.WmlUtils;

import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.util.*;
import java.util.function.Function;

import static org.docx4j.openpackaging.parts.relationships.Namespaces.*;
import static pro.verron.officestamper.core.Invokers.streamInvokers;

/// The DocxStamper class is an implementation of the [OfficeStamper]
/// interface that is used to stamp DOCX templates with a context object and
/// write the result to an output stream.
///
/// @author Tom Hombergs
/// @author Joseph Verron
/// @version ${version}
/// @since 1.0.0
public class DocxStamper
        implements OfficeStamper<WordprocessingMLPackage> {

    private static final Logger log = LoggerFactory.getLogger(DocxStamper.class);
    private final List<PreProcessor> preprocessors;
    private final List<PostProcessor> postprocessors;
    private final PlaceholderReplacer placeholderReplacer;
    private final CommentProcessorRegistry commentProcessorRegistry;

    /// Creates a new DocxStamper with the given configuration.
    ///
    /// @param configuration the configuration to use for this DocxStamper.
    public DocxStamper(OfficeStamperConfiguration configuration) {
        this(configuration.getEvaluationContextConfigurer(),
                configuration.getExpressionFunctions(),
                configuration.customFunctions(),
                configuration.getResolvers(),
                configuration.getCommentProcessors(),
                configuration.getPreprocessors(),
                configuration.getPostprocessors(),
                configuration.getSpelParserConfiguration(),
                configuration.getExceptionResolver());
    }

    private DocxStamper(
            EvaluationContextConfigurer evaluationContextConfigurer,
            Map<Class<?>, Object> expressionFunctions,
            List<CustomFunction> functions,
            List<ObjectResolver> resolvers,
            Map<Class<?>, Function<ParagraphPlaceholderReplacer, CommentProcessor>> configurationCommentProcessors,
            List<PreProcessor> preprocessors,
            List<PostProcessor> postprocessors,
            SpelParserConfiguration spelParserConfiguration,
            ExceptionResolver exceptionResolver
    ) {
        var expressionParser = new SpelExpressionParser(spelParserConfiguration);

        var evaluationContext = new StandardEvaluationContext();
        evaluationContextConfigurer.configureEvaluationContext(evaluationContext);

        var expressionResolver = new ExpressionResolver(evaluationContext, expressionParser);
        var typeResolverRegistry = new ObjectResolverRegistry(resolvers);
        this.placeholderReplacer = new PlaceholderReplacer(typeResolverRegistry, expressionResolver, exceptionResolver);

        var commentProcessors = buildCommentProcessors(configurationCommentProcessors);
        evaluationContext.addMethodResolver(new Invokers(streamInvokers(commentProcessors)));
        evaluationContext.addMethodResolver(new Invokers(streamInvokers(expressionFunctions)));
        evaluationContext.addMethodResolver(new Invokers(functions.stream()
                                                                  .map(Invokers::ofCustomFunction)));

        this.commentProcessorRegistry = new CommentProcessorRegistry(expressionResolver,
                commentProcessors,
                exceptionResolver);

        this.preprocessors = new ArrayList<>(preprocessors);
        this.postprocessors = new ArrayList<>(postprocessors);
    }

    private CommentProcessors buildCommentProcessors(
            Map<Class<?>, Function<ParagraphPlaceholderReplacer, CommentProcessor>> commentProcessors
    ) {
        var processors = new HashMap<Class<?>, CommentProcessor>();
        for (var entry : commentProcessors.entrySet()) {
            processors.put(entry.getKey(),
                    entry.getValue()
                         .apply(placeholderReplacer));
        }
        return new CommentProcessors(processors);
    }

    /// Reads in a .docx template and "stamps" it into the given OutputStream, using the specified context object to
    /// fill out any expressions it finds.
    ///
    /// In the .docx template you have the following options to influence the "stamping" process:
    ///   - Use expressions like ${name} or ${person.isOlderThan(18)} in the template's text. These expressions are
    ///     resolved
    ///     against the contextRoot object you pass into this method and are replaced by the results.
    ///   - Use comments within the .docx template to mark certain paragraphs to be manipulated.
    ///
    /// Within comments, you can put expressions in which you can use the following methods by default:
    ///   - _displayParagraphIf(boolean)_ to conditionally display paragraphs or not
    ///   - _displayTableRowIf(boolean)_ to conditionally display table rows or not
    ///   - _displayTableIf(boolean)_ to conditionally display whole tables or not
    ///   - _repeatTableRow(List&lt;Object&gt;)_ to create a new table row for each object in the list and
    ///     resolve expressions
    ///     within the table cells against one of the objects within the list.
    ///
    /// If you need a wider vocabulary of methods available in the comments, you can create your own ICommentProcessor
    /// and register it via [OfficeStamperConfiguration#addCommentProcessor(Class, Function)].
    public void stamp(InputStream template, Object contextRoot, OutputStream out) {
        try {
            WordprocessingMLPackage document = WordprocessingMLPackage.load(template);
            stamp(document, contextRoot, out);
        } catch (Docx4JException e) {
            throw new OfficeStamperException(e);
        }
    }

    /// Same as [#stamp(InputStream, Object, OutputStream)] except that you
    /// may pass in a DOCX4J document as a template instead of an InputStream.
    @Override
    public void stamp(WordprocessingMLPackage document, Object contextRoot, OutputStream out) {
        try {
            preprocess(document);
            process(new TextualDocxPart(document), contextRoot);
            postprocess(document);
            document.save(out);
        } catch (Docx4JException e) {
            throw new OfficeStamperException(e);
        }
    }

    private void preprocess(WordprocessingMLPackage document) {
        preprocessors.forEach(processor -> processor.process(document));
    }

    private void process(DocxDocument document, Object contextRoot) {
        document.process(part -> processPart(part, contextRoot));
    }

    private void postprocess(WordprocessingMLPackage document) {
        postprocessors.forEach(processor -> processor.process(document));
    }

    private void processPart(DocxPart part, Object contextRoot) {
        var type = part.type();
        switch (type) {
            case DOCUMENT, HEADER, FOOTER -> processTextualPart(part, contextRoot);
            default -> log.info("Unknown part type: {}", type);
        }

    }

    private void processTextualPart(DocxPart part, Object contextRoot) {
        var processors = commentProcessorRegistry;

        var paragraphIterator = DocxIterator.ofParagraphs(part);
        while (paragraphIterator.hasNext()) {
            var p = paragraphIterator.next();
            var document = part.document();

            var rootComments = new HashMap<BigInteger, Comment>();
            var allComments = new HashMap<BigInteger, Comment>();
            var stack = Collections.asLifoQueue(new ArrayDeque<Comment>());

            var list = WmlUtils.extractCommentElements(document);
            for (Child commentElement : list) {
                if (commentElement instanceof CommentRangeStart crs)
                    onRangeStart(part, crs, allComments, stack, rootComments);
                else if (commentElement instanceof CommentRangeEnd cre) onRangeEnd(cre, allComments, stack);
                else if (commentElement instanceof R.CommentReference cr) onReference(part, cr, allComments);
            }
            CommentUtil.getCommentsPart(document.getParts())
                       .map(CommentUtil::extractContent)
                       .map(Comments::getComment)
                       .stream()
                       .flatMap(Collection::stream)
                       .filter(comment -> allComments.containsKey(comment.getId()))
                       .forEach(comment -> allComments.get(comment.getId())
                                                      .setComment(comment));
            var comments = new HashMap<>(rootComments);
            var paragraphComment = p.getComment();
            var updates = 0;
            for (Comments.Comment pc : paragraphComment) {
                updates += processors.runProcessorsOnParagraphComment(part, comments, contextRoot, p, pc.getId());
            }
            if (updates > 0) paragraphIterator.reset();
        }

        var processorTagIterator = DocxIterator.ofTags(part::content, "processor", part);
        while (processorTagIterator.hasNext()) {
            var tag1 = processorTagIterator.next();
            processors.runProcessorsOnInlineContent(part, contextRoot, tag1);
            paragraphIterator.reset();
        }

        var placeholderTagIterator = DocxIterator.ofTags(part::content, "placeholder", part);
        while (placeholderTagIterator.hasNext()) {
            var tag = placeholderTagIterator.next();
            placeholderReplacer.resolveExpressionsForParagraph(part, tag, contextRoot);
            placeholderTagIterator.reset();
        }
    }

    static void onRangeStart(
            DocxPart source,
            CommentRangeStart crs,
            HashMap<BigInteger, Comment> allComments,
            Queue<Comment> stack,
            HashMap<BigInteger, Comment> rootComments
    ) {
        Comment comment = allComments.get(crs.getId());
        if (comment == null) {
            comment = new StandardComment(source);
            allComments.put(crs.getId(), comment);
            if (stack.isEmpty()) {
                rootComments.put(crs.getId(), comment);
            }
            else {
                stack.peek()
                     .getChildren()
                     .add(comment);
            }
        }
        comment.setCommentRangeStart(crs);
        stack.add(comment);
    }

    static void onRangeEnd(CommentRangeEnd cre, HashMap<BigInteger, Comment> allComments, Queue<Comment> stack) {
        Comment comment = allComments.get(cre.getId());
        if (comment == null)
            throw new OfficeStamperException("Found a comment range end before the comment range start !");

        comment.setCommentRangeEnd(cre);

        if (!stack.isEmpty()) {
            var peek = stack.peek();
            if (peek.equals(comment)) stack.remove();
            else throw new OfficeStamperException("Cannot figure which comment contains the other !");
        }
    }

    static void onReference(DocxPart source, R.CommentReference cr, HashMap<BigInteger, Comment> allComments) {
        Comment comment = allComments.get(cr.getId());
        if (comment == null) {
            comment = new StandardComment(source);
            allComments.put(cr.getId(), comment);
        }
        comment.setCommentReference(cr);
    }

}
