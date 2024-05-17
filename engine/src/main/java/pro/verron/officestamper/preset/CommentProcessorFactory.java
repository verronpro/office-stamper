package pro.verron.officestamper.preset;

import jakarta.xml.bind.JAXBElement;
import org.docx4j.XmlUtils;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.*;
import org.jvnet.jaxb2_commons.ppp.Child;
import org.springframework.lang.Nullable;
import pro.verron.officestamper.api.*;
import pro.verron.officestamper.core.*;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toMap;
import static org.docx4j.TextUtils.getText;
import static pro.verron.officestamper.core.DocumentUtil.walkObjectsAndImportImages;

/**
 * Factory class to create the correct comment processor for a given comment.
 *
 * @author Joseph Verron
 * @version ${version}
 * @since 1.6.4
 */
public class CommentProcessorFactory {
    private final OfficeStamperConfiguration configuration;

    /**
     * Creates a new CommentProcessorFactory.
     *
     * @param configuration the configuration to use for the created processors.
     */
    public CommentProcessorFactory(OfficeStamperConfiguration configuration) {
        this.configuration = configuration;
    }

    /**
     * Creates a new CommentProcessorFactory with default configuration.
     *
     * @param pr a {@link PlaceholderReplacer} object
     *
     * @return a {@link CommentProcessor} object
     */
    public CommentProcessor repeatParagraph(ParagraphPlaceholderReplacer pr) {
        return ParagraphRepeatProcessor.newInstance(pr);
    }

    /**
     * Creates a new CommentProcessorFactory with default configuration.
     *
     * @param pr a {@link PlaceholderReplacer} object
     *
     * @return a {@link CommentProcessor} object
     */
    public CommentProcessor repeatDocPart(ParagraphPlaceholderReplacer pr) {
        return RepeatDocPartProcessor.newInstance(pr, getStamper());
    }

    private OfficeStamper<WordprocessingMLPackage> getStamper() {
        return (template, context, output) -> new DocxStamper(configuration).stamp(template, context, output);
    }

    /**
     * Creates a new CommentProcessorFactory with default configuration.
     *
     * @param pr a {@link PlaceholderReplacer} object
     *
     * @return a {@link CommentProcessor} object
     */
    public CommentProcessor repeat(ParagraphPlaceholderReplacer pr) {
        return RepeatProcessor.newInstance(pr);
    }

    /**
     * Creates a new CommentProcessorFactory with default configuration.
     *
     * @param pr a {@link PlaceholderReplacer} object
     *
     * @return a {@link CommentProcessor} object
     */
    public CommentProcessor tableResolver(ParagraphPlaceholderReplacer pr) {
        return TableResolver.newInstance(pr);
    }

    /**
     * Creates a new CommentProcessorFactory with default configuration.
     *
     * @param pr a {@link PlaceholderReplacer} object
     *
     * @return a {@link CommentProcessor} object
     */
    public CommentProcessor displayIf(ParagraphPlaceholderReplacer pr) {
        return DisplayIfProcessor.newInstance(pr);
    }

    /**
     * Creates a new CommentProcessorFactory with default configuration.
     *
     * @param pr a {@link PlaceholderReplacer} object
     *
     * @return a {@link CommentProcessor} object
     */
    public CommentProcessor replaceWith(ParagraphPlaceholderReplacer pr) {
        return ReplaceWithProcessor.newInstance(pr);
    }

    /**
     * This interface is used to resolve a table in the template document.
     * The table is passed to the resolveTable method and will be used to fill an existing Tbl object in the document.
     *
     * @author Joseph Verron
     * @version ${version}
     * @since 1.6.2
     */
    public interface ITableResolver {
        /**
         * Resolves the given table by manipulating the given table in the template
         *
         * @param table the table to resolve.
         */
        void resolveTable(StampTable table);
    }

    /**
     * Interface for processors that replace a single word with an expression defined
     * in a comment.
     *
     * @author Joseph Verron
     * @author Tom Hombergs
     * @version ${version}
     * @since 1.0.8
     */
    public interface IReplaceWithProcessor {

        /**
         * May be called to replace a single word inside a paragraph with an expression
         * defined in a comment. The comment must be applied to a single word for the
         * replacement to take effect!
         *
         * @param expression the expression to replace the text with
         */
        void replaceWordWith(@Nullable String expression);
    }

    /**
     * Implementations of this interface are responsible for processing the repeat paragraph instruction.
     * The repeat paragraph instruction is a comment that contains the following text:
     * <p>
     * <code>
     * repeatParagraph(...)
     * </code>
     * <p>
     * Where the three dots represent an expression that evaluates to a list of objects.
     * The processor then copies the paragraph once for each object in the list and evaluates all expressions
     * within each copy against the respective object.
     *
     * @author Joseph Verron
     * @author Romain Lamarche
     * @version ${version}
     * @since 1.0.0
     */
    public interface IParagraphRepeatProcessor {

        /**
         * May be called to mark a paragraph to be copied once for each element in the passed-in list.
         * Within each copy of the row, all expressions are evaluated against one of the objects in the list.
         *
         * @param objects the objects which serve as context root for expressions found in the template table row.
         */
        void repeatParagraph(List<Object> objects);
    }

    /**
     * Interface for processors which may be called to mark a document part to be copied once for each element in the
     * passed-in list.
     * Within each copy of the row, all expressions are evaluated against one of the objects in the list.
     *
     * @author Joseph Verron
     * @author Artem Medvedev
     * @version ${version}
     * @since 1.0.0
     */
    public interface IRepeatDocPartProcessor {

        /**
         * May be called to mark a document part to be copied once for each element in the passed-in list.
         * Within each copy of the row, all expressions are evaluated against one of the objects in the list.
         *
         * @param objects the objects which serve as context root for expressions found in the template table row.
         *
         * @throws Exception if the processing fails.
         */
        void repeatDocPart(@Nullable List<Object> objects)
                throws Exception;
    }

    /**
     * Interface for processors that can repeat a table row.
     *
     * @author Joseph Verron
     * @author Tom Hombergs
     * @version ${version}
     * @since 1.0.0
     */
    public interface IRepeatProcessor {

        /**
         * May be called to mark a table row to be copied once for each element in the passed-in list.
         * Within each copy of the row, all expressions are evaluated against one of the objects in the list.
         *
         * @param objects the objects which serve as context root for expressions found in the template table row.
         */
        void repeatTableRow(List<Object> objects);

    }

    /**
     * Interface for processors that may be used to delete commented paragraphs or tables from the document, depending
     * on a given condition.
     *
     * @author Joseph Verron
     * @author Tom Hombergs
     * @version ${version}
     * @since 1.0.0
     */
    public interface IDisplayIfProcessor {

        /**
         * May be called to delete the commented paragraph or not, depending on the given boolean condition.
         *
         * @param condition if true, the commented paragraph will remain in the document. If false, the commented
         *                  paragraph
         *                  will be deleted at stamping.
         */
        void displayParagraphIf(Boolean condition);

        /**
         * May be called to delete the commented paragraph or not, depending on the presence of the given data.
         *
         * @param condition if non-null, the commented paragraph will remain in
         *                  the document. If null, the commented paragraph
         *                  will be deleted at stamping.
         */
        void displayParagraphIfPresent(@Nullable Object condition);

        /**
         * May be called to delete the table surrounding the commented paragraph, depending on the given boolean
         * condition.
         *
         * @param condition if true, the table row surrounding the commented paragraph will remain in the document. If
         *                  false, the table row
         *                  will be deleted at stamping.
         */
        void displayTableRowIf(Boolean condition);

        /**
         * May be called to delete the table surrounding the commented paragraph, depending on the given boolean
         * condition.
         *
         * @param condition if true, the table surrounding the commented paragraph will remain in the document. If
         *                  false, the table
         *                  will be deleted at stamping.
         */
        void displayTableIf(Boolean condition);

    }

    /**
     * TableResolver class.
     *
     * @author Joseph Verron
     * @version ${version}
     * @since 1.6.2
     */
    private static class TableResolver
            extends AbstractCommentProcessor
            implements ITableResolver {
        private final Map<Tbl, StampTable> cols = new HashMap<>();
        private final Function<Tbl, List<Object>> nullSupplier;

        private TableResolver(
                ParagraphPlaceholderReplacer placeholderReplacer,
                Function<Tbl, List<Object>> nullSupplier
        ) {
            super(placeholderReplacer);
            this.nullSupplier = nullSupplier;
        }

        /**
         * Generate a new {@link TableResolver} instance where value is replaced by an empty list when <code>null</code>
         *
         * @param pr a {@link PlaceholderReplacer} instance
         *
         * @return a new {@link TableResolver} instance
         */
        public static CommentProcessor newInstance(ParagraphPlaceholderReplacer pr) {
            return new TableResolver(pr, table -> Collections.emptyList());
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void resolveTable(StampTable givenTable) {
            P p = getParagraph();
            if (p.getParent() instanceof Tc tc && tc.getParent() instanceof Tr tr
                    && tr.getParent() instanceof Tbl table) {
                cols.put(table, givenTable);
            }
            else throw new OfficeStamperException(format("Paragraph is not within a table! : %s", getText(p)));
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void commitChanges(WordprocessingMLPackage document) {
            for (Map.Entry<Tbl, StampTable> entry : cols.entrySet()) {
                Tbl wordTable = entry.getKey();

                StampTable stampedTable = entry.getValue();

                if (stampedTable != null) {
                    replaceTableInplace(wordTable, stampedTable);
                }
                else {
                    List<Object> tableParentContent = ((ContentAccessor) wordTable.getParent()).getContent();
                    int tablePosition = tableParentContent.indexOf(wordTable);
                    List<Object> toInsert = nullSupplier.apply(wordTable);
                    tableParentContent.set(tablePosition, toInsert);
                }
            }
        }

        private void replaceTableInplace(Tbl wordTable, StampTable stampedTable) {
            var headers = stampedTable.headers();

            var rows = wordTable.getContent();
            var headerRow = (Tr) rows.get(0);
            var firstDataRow = (Tr) rows.get(1);

            growAndFillRow(headerRow, headers);

            if (stampedTable.isEmpty())
                rows.remove(firstDataRow);
            else {
                growAndFillRow(firstDataRow, stampedTable.get(0));
                for (var rowContent : stampedTable.subList(1, stampedTable.size()))
                    rows.add(copyRowFromTemplate(firstDataRow, rowContent));
            }
        }

        private void growAndFillRow(Tr row, List<String> values) {
            List<Object> cellRowContent = row.getContent();

            //Replace text in first cell
            JAXBElement<Tc> cell0 = (JAXBElement<Tc>) cellRowContent.get(0);
            Tc cell0tc = cell0.getValue();
            setCellText(cell0tc, values.isEmpty() ? "" : values.get(0));

            if (values.size() > 1) {
                //Copy the first cell and replace content for each remaining value
                for (String cellContent : values.subList(1, values.size())) {
                    JAXBElement<Tc> xmlCell = XmlUtils.deepCopy(cell0);
                    setCellText(xmlCell.getValue(), cellContent);
                    cellRowContent.add(xmlCell);
                }
            }
        }

        private Tr copyRowFromTemplate(Tr firstDataRow, List<String> rowContent) {
            Tr newXmlRow = XmlUtils.deepCopy(firstDataRow);
            List<Object> xmlRow = newXmlRow.getContent();
            for (int i = 0; i < rowContent.size(); i++) {
                String cellContent = rowContent.get(i);
                Tc xmlCell = ((JAXBElement<Tc>) xmlRow.get(i)).getValue();
                setCellText(xmlCell, cellContent);
            }
            return newXmlRow;
        }

        private void setCellText(Tc tableCell, String content) {
            tableCell.getContent()
                     .clear();
            tableCell.getContent()
                     .add(ParagraphUtil.create(content));
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void reset() {
            cols.clear();
        }
    }

    /**
     * Processor that replaces the current run with the provided expression.
     * This is useful for replacing an expression in a comment with the result of the expression.
     *
     * @author Joseph Verron
     * @author Tom Hombergs
     * @version ${version}
     * @since 1.0.7
     */
    private static class ReplaceWithProcessor
            extends AbstractCommentProcessor
            implements IReplaceWithProcessor {

        private final Function<R, List<Object>> nullSupplier;

        private ReplaceWithProcessor(
                ParagraphPlaceholderReplacer placeholderReplacer,
                Function<R, List<Object>> nullSupplier
        ) {
            super(placeholderReplacer);
            this.nullSupplier = nullSupplier;
        }

        /**
         * Creates a new processor that replaces the current run with the result of the expression.
         *
         * @param pr the placeholder replacer to use
         *
         * @return the processor
         */
        public static CommentProcessor newInstance(ParagraphPlaceholderReplacer pr) {
            return new ReplaceWithProcessor(pr, R::getContent);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void commitChanges(WordprocessingMLPackage document) {
            // nothing to commit
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void reset() {
            // nothing to reset
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void replaceWordWith(@Nullable String expression) {
            R run = this.getCurrentRun();
            if (run == null)
                throw new OfficeStamperException(format("Impossible to put expression %s in a null run", expression));

            List<Object> target;
            if (expression != null) {
                target = List.of(RunUtil.createText(expression));
            }
            else {
                target = nullSupplier.apply(run);
            }
            run.getContent()
               .clear();
            run.getContent()
               .addAll(target);
        }
    }

    /**
     * This class is used to repeat paragraphs and tables.
     * <p>
     * It is used internally by the DocxStamper and should not be instantiated by
     * clients.
     *
     * @author Joseph Verron
     * @author Youssouf Naciri
     * @version ${version}
     * @since 1.2.2
     */
    private static class ParagraphRepeatProcessor
            extends AbstractCommentProcessor
            implements IParagraphRepeatProcessor {
        private final Supplier<? extends List<? extends P>> nullSupplier;
        private Map<P, Paragraphs> pToRepeat = new HashMap<>();

        /**
         * @param placeholderReplacer replaces placeholders with values
         * @param nullSupplier        supplies a list of paragraphs if the list of objects to repeat is null
         */
        private ParagraphRepeatProcessor(
                ParagraphPlaceholderReplacer placeholderReplacer,
                Supplier<? extends List<? extends P>> nullSupplier
        ) {
            super(placeholderReplacer);
            this.nullSupplier = nullSupplier;
        }

        /**
         * <p>newInstance.</p>
         *
         * @param placeholderReplacer replaces expressions with values
         *
         * @return a new instance of ParagraphRepeatProcessor
         */
        public static CommentProcessor newInstance(ParagraphPlaceholderReplacer placeholderReplacer) {
            return new ParagraphRepeatProcessor(placeholderReplacer,
                    Collections::emptyList);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void repeatParagraph(List<Object> objects) {
            P paragraph = getParagraph();

            Deque<P> paragraphs = getParagraphsInsideComment(paragraph);

            Paragraphs toRepeat = new Paragraphs();
            toRepeat.comment = getCurrentCommentWrapper();
            toRepeat.data = new ArrayDeque<>(objects);
            toRepeat.paragraphs = paragraphs;
            toRepeat.sectionBreakBefore = SectionUtil.getPreviousSectionBreakIfPresent(
                    paragraph,
                    (ContentAccessor) paragraph.getParent());
            toRepeat.firstParagraphSectionBreak = SectionUtil.getParagraphSectionBreak(
                    paragraph);
            toRepeat.hasOddSectionBreaks = SectionUtil.isOddNumberOfSectionBreaks(
                    new ArrayList<>(toRepeat.paragraphs));

            if (paragraph.getPPr() != null && paragraph.getPPr()
                                                       .getSectPr() != null) {
                // we need to clear the first paragraph's section break to be able to control how to repeat it
                paragraph.getPPr()
                         .setSectPr(null);
            }
            pToRepeat.put(paragraph, toRepeat);
        }

        /**
         * Returns all paragraphs inside the comment of the given paragraph.
         * <p>
         * If the paragraph is not inside a comment, the given paragraph is returned.
         *
         * @param paragraph the paragraph to analyze
         *
         * @return all paragraphs inside the comment of the given paragraph
         */
        public static Deque<P> getParagraphsInsideComment(P paragraph) {
            BigInteger commentId = null;
            boolean foundEnd = false;

            Deque<P> paragraphs = new ArrayDeque<>();
            paragraphs.add(paragraph);

            for (Object object : paragraph.getContent()) {
                if (object instanceof CommentRangeStart crs)
                    commentId = crs.getId();
                if (object instanceof CommentRangeEnd cre && Objects.equals(
                        commentId,
                        cre.getId())) foundEnd = true;
            }
            if (foundEnd || commentId == null) return paragraphs;

            Object parent = paragraph.getParent();
            if (parent instanceof ContentAccessor contentAccessor) {
                int index = contentAccessor.getContent()
                                           .indexOf(paragraph);
                for (int i = index + 1; i < contentAccessor.getContent()
                                                           .size() && !foundEnd; i++) {
                    Object next = contentAccessor.getContent()
                                                 .get(i);

                    if (next instanceof CommentRangeEnd cre && cre.getId()
                                                                  .equals(commentId)) {
                        foundEnd = true;
                    }
                    else {
                        if (next instanceof P p) {
                            paragraphs.add(p);
                        }
                        if (next instanceof ContentAccessor childContent) {
                            for (Object child : childContent.getContent()) {
                                if (child instanceof CommentRangeEnd cre && cre.getId()
                                                                               .equals(commentId)) {
                                    foundEnd = true;
                                    break;
                                }
                            }
                        }
                    }
                }
            }
            return paragraphs;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void commitChanges(WordprocessingMLPackage document) {
            for (Map.Entry<P, Paragraphs> entry : pToRepeat.entrySet()) {
                P currentP = entry.getKey();
                ContentAccessor parent = (ContentAccessor) currentP.getParent();
                List<Object> parentContent = parent.getContent();
                int index = parentContent.indexOf(currentP);
                if (index < 0) throw new OfficeStamperException("Impossible");

                Paragraphs paragraphsToRepeat = entry.getValue();
                Deque<Object> expressionContexts = Objects.requireNonNull(
                        paragraphsToRepeat).data;
                Deque<P> collection = expressionContexts == null
                        ? new ArrayDeque<>(nullSupplier.get())
                        : generateParagraphsToAdd(document,
                                paragraphsToRepeat,
                                expressionContexts);
                restoreFirstSectionBreakIfNeeded(paragraphsToRepeat, collection);
                parentContent.addAll(index, collection);
                parentContent.removeAll(paragraphsToRepeat.paragraphs);
            }
        }

        private Deque<P> generateParagraphsToAdd(
                WordprocessingMLPackage document,
                Paragraphs paragraphs,
                Deque<Object> expressionContexts
        ) {
            Deque<P> paragraphsToAdd = new ArrayDeque<>();

            Object lastExpressionContext = expressionContexts.peekLast();
            P lastParagraph = paragraphs.paragraphs.peekLast();

            for (Object expressionContext : expressionContexts) {
                for (P paragraphToClone : paragraphs.paragraphs) {
                    P pClone = XmlUtils.deepCopy(paragraphToClone);

                    if (paragraphs.sectionBreakBefore != null
                            && paragraphs.hasOddSectionBreaks
                            && expressionContext != lastExpressionContext
                            && paragraphToClone == lastParagraph
                    ) {
                        SectionUtil.applySectionBreakToParagraph(paragraphs.sectionBreakBefore,
                                pClone);
                    }

                    CommentUtil.deleteCommentFromElements(pClone.getContent(),
                            paragraphs.comment.getComment()
                                              .getId());
                    placeholderReplacer.resolveExpressionsForParagraph(
                            new StandardParagraph(pClone),
                            expressionContext,
                            document
                    );
                    paragraphsToAdd.add(pClone);
                }
            }
            return paragraphsToAdd;
        }

        private static void restoreFirstSectionBreakIfNeeded(
                Paragraphs paragraphs,
                Deque<P> paragraphsToAdd
        ) {
            if (paragraphs.firstParagraphSectionBreak != null) {
                P breakP = paragraphsToAdd.getLast();
                SectionUtil.applySectionBreakToParagraph(paragraphs.firstParagraphSectionBreak,
                        breakP);
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void reset() {
            pToRepeat = new HashMap<>();
        }

        private static class Paragraphs {
            Comment comment;
            Deque<Object> data;
            Deque<P> paragraphs;
            // hasOddSectionBreaks is true if the paragraphs to repeat contain an odd number of section breaks
            // changing the layout, false otherwise
            boolean hasOddSectionBreaks;
            // section break right before the first paragraph to repeat if present, or null
            SectPr sectionBreakBefore;
            // section break on the first paragraph to repeat if present, or null
            SectPr firstParagraphSectionBreak;
        }
    }

    /**
     * Walks through a document and replaces expressions with values from the given
     * expression context.
     * This walker only replaces expressions in paragraphs, not in tables.
     *
     * @author Joseph Verron
     * @version ${version}
     * @since 1.4.7
     */
    private static class ParagraphResolverDocumentWalker
            extends BaseDocumentWalker {
        private final Object expressionContext;
        private final WordprocessingMLPackage document;
        private final ParagraphPlaceholderReplacer placeholderReplacer;

        /**
         * <p>Constructor for ParagraphResolverDocumentWalker.</p>
         *
         * @param rowClone          The row to start with
         * @param expressionContext The context of the expressions to resolve
         * @param document          The document to walk through
         * @param replacer          The placeholderReplacer to use for resolving
         */
        public ParagraphResolverDocumentWalker(
                Tr rowClone,
                Object expressionContext,
                WordprocessingMLPackage document,
                ParagraphPlaceholderReplacer replacer
        ) {
            super(rowClone);
            this.expressionContext = expressionContext;
            this.document = document;
            this.placeholderReplacer = replacer;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected void onParagraph(P paragraph) {
            placeholderReplacer.resolveExpressionsForParagraph(
                    new StandardParagraph(paragraph),
                    expressionContext, document
            );
        }
    }

    /**
     * This class is responsible for processing the &lt;ds: repeat&gt; tag.
     * It uses the {@link OfficeStamper} to stamp the sub document and then
     * copies the resulting sub document to the correct position in the
     * main document.
     *
     * @author Joseph Verron
     * @author Youssouf Naciri
     * @version ${version}
     * @since 1.3.0
     */
    private static class RepeatDocPartProcessor
            extends AbstractCommentProcessor
            implements IRepeatDocPartProcessor {
        private static final ThreadFactory threadFactory = Executors.defaultThreadFactory();
        private static final ObjectFactory objectFactory = Context.getWmlObjectFactory();

        private final OfficeStamper<WordprocessingMLPackage> stamper;
        private final Map<Comment, List<Object>> contexts = new HashMap<>();
        private final Supplier<? extends List<?>> nullSupplier;

        private RepeatDocPartProcessor(
                ParagraphPlaceholderReplacer placeholderReplacer,
                OfficeStamper<WordprocessingMLPackage> stamper,
                Supplier<? extends List<?>> nullSupplier
        ) {
            super(placeholderReplacer);
            this.stamper = stamper;
            this.nullSupplier = nullSupplier;
        }

        /**
         * <p>newInstance.</p>
         *
         * @param pr      the placeholderReplacer
         * @param stamper the stamper
         *
         * @return a new instance of this processor
         */
        public static CommentProcessor newInstance(
                ParagraphPlaceholderReplacer pr,
                OfficeStamper<WordprocessingMLPackage> stamper
        ) {
            return new RepeatDocPartProcessor(pr, stamper, Collections::emptyList);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void repeatDocPart(@Nullable List<Object> contexts) {
            if (contexts == null)
                contexts = Collections.emptyList();

            Comment currentComment = getCurrentCommentWrapper();
            List<Object> elements = currentComment.getElements();

            if (!elements.isEmpty()) {
                this.contexts.put(currentComment, contexts);
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void commitChanges(WordprocessingMLPackage document) {
            for (Map.Entry<Comment, List<Object>> entry : this.contexts.entrySet()) {
                var comment = entry.getKey();
                var expressionContexts = entry.getValue();
                var gcp = requireNonNull(comment.getParent());
                var repeatElements = comment.getElements();
                var subTemplate = CommentUtil.createSubWordDocument(comment);
                var previousSectionBreak = SectionUtil.getPreviousSectionBreakIfPresent(repeatElements.get(0), gcp);
                var oddNumberOfBreaks = SectionUtil.isOddNumberOfSectionBreaks(repeatElements);
                var changes = expressionContexts == null
                        ? nullSupplier.get()
                        : stampSubDocuments(document,
                                expressionContexts,
                                gcp,
                                subTemplate,
                                previousSectionBreak,
                                oddNumberOfBreaks);
                var gcpContent = gcp.getContent();
                var index = gcpContent.indexOf(repeatElements.get(0));
                gcpContent.addAll(index, changes);
                gcpContent.removeAll(repeatElements);
            }

        }

        private List<Object> stampSubDocuments(
                WordprocessingMLPackage document,
                List<Object> expressionContexts,
                ContentAccessor gcp,
                WordprocessingMLPackage subTemplate,
                SectPr previousSectionBreak,
                boolean oddNumberOfBreaks
        ) {
            var subDocuments = stampSubDocuments(expressionContexts, subTemplate);
            var replacements = subDocuments
                    .stream()
                    .map(p -> walkObjectsAndImportImages(p,
                            document)) // TODO_LATER: move the side effect somewhere else
                    .map(Map::entrySet)
                    .flatMap(Set::stream)
                    .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));

            var changes = new ArrayList<>();
            for (WordprocessingMLPackage subDocument : subDocuments) {
                var os = documentAsInsertableElements(subDocument,
                        oddNumberOfBreaks,
                        previousSectionBreak);
                os.stream()
                  .filter(ContentAccessor.class::isInstance)
                  .map(ContentAccessor.class::cast)
                  .forEach(o -> recursivelyReplaceImages(o, replacements));
                os.forEach(c -> setParentIfPossible(c, gcp));
                changes.addAll(os);
            }
            return changes;
        }

        private List<WordprocessingMLPackage> stampSubDocuments(
                List<Object> subContexts,
                WordprocessingMLPackage subTemplate
        ) {
            var subDocuments = new ArrayList<WordprocessingMLPackage>();
            for (Object subContext : subContexts) {
                var templateCopy = outputWord(os -> copy(subTemplate, os));
                var subDocument = outputWord(os -> stamp(subContext, templateCopy, os));
                subDocuments.add(subDocument);
            }
            return subDocuments;
        }

        private static List<Object> documentAsInsertableElements(
                WordprocessingMLPackage subDocument,
                boolean oddNumberOfBreaks,
                SectPr previousSectionBreak
        ) {
            List<Object> inserts = new ArrayList<>(
                    DocumentUtil.allElements(subDocument));
            // make sure we replicate the previous section break before each repeated doc part
            if (oddNumberOfBreaks && previousSectionBreak != null) {
                if (DocumentUtil.lastElement(subDocument) instanceof P p) {
                    SectionUtil.applySectionBreakToParagraph(previousSectionBreak,
                            p);
                }
                else {
                    // when the last element to be repeated is not a paragraph, we need to add a new
                    // one right after to carry the section break to have a valid xml
                    P p = objectFactory.createP();
                    SectionUtil.applySectionBreakToParagraph(previousSectionBreak,
                            p);
                    inserts.add(p);
                }
            }
            return inserts;
        }

        private static void recursivelyReplaceImages(
                ContentAccessor r,
                Map<R, R> replacements
        ) {
            Queue<ContentAccessor> q = new ArrayDeque<>();
            q.add(r);
            while (!q.isEmpty()) {
                ContentAccessor run = q.remove();
                if (replacements.containsKey(run)
                        && run instanceof Child child
                        && child.getParent() instanceof ContentAccessor parent) {
                    List<Object> parentContent = parent.getContent();
                    parentContent.add(parentContent.indexOf(run),
                            replacements.get(run));
                    parentContent.remove(run);
                }
                else {
                    q.addAll(run.getContent()
                                .stream()
                                .filter(ContentAccessor.class::isInstance)
                                .map(ContentAccessor.class::cast)
                                .toList());
                }
            }
        }

        private static void setParentIfPossible(
                Object object,
                ContentAccessor parent
        ) {
            if (object instanceof Child child)
                child.setParent(parent);
        }

        private WordprocessingMLPackage outputWord(Consumer<OutputStream> outputter) {
            var exceptionHandler = new ProcessorExceptionHandler();
            try (
                    PipedOutputStream os = new PipedOutputStream();
                    PipedInputStream is = new PipedInputStream(os)
            ) {
                // closing on exception to not block the pipe infinitely
                // TODO_LATER: model both PipedxxxStream as 1 class for only 1 close()
                exceptionHandler.onException(is::close); // I know it's redundant,
                exceptionHandler.onException(os::close); // but symmetry

                var thread = threadFactory.newThread(() -> outputter.accept(os));
                thread.setUncaughtExceptionHandler(exceptionHandler);
                thread.start();
                var wordprocessingMLPackage = WordprocessingMLPackage.load(is);
                thread.join();
                return wordprocessingMLPackage;
            } catch (Docx4JException | IOException | InterruptedException e) {
                OfficeStamperException exception = new OfficeStamperException(e);
                exceptionHandler.exception()
                                .ifPresent(exception::addSuppressed);
                throw exception;
            }
        }

        private void copy(
                WordprocessingMLPackage aPackage,
                OutputStream outputStream
        ) {
            try {
                aPackage.save(outputStream);
            } catch (Docx4JException e) {
                throw new OfficeStamperException(e);
            }
        }

        private void stamp(
                Object context,
                WordprocessingMLPackage template,
                OutputStream outputStream
        ) {
            stamper.stamp(template, context, outputStream);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void reset() {
            contexts.clear();
        }

        /**
         * A functional interface representing runnable task able to throw an exception.
         * It extends the {@link Runnable} interface and provides default implementation
         * of the {@link Runnable#run()} method handling the exception by rethrowing it
         * wrapped inside a {@link OfficeStamperException}.
         *
         * @author Joseph Verron
         * @version ${version}
         * @since 1.6.6
         */
        interface ThrowingRunnable
                extends Runnable {

            /**
             * Executes the runnable task, handling any exception by throwing it wrapped
             * inside a {@link OfficeStamperException}.
             */
            default void run() {
                try {
                    throwingRun();
                } catch (Exception e) {
                    throw new OfficeStamperException(e);
                }
            }

            /**
             * Executes the runnable task
             *
             * @throws Exception if an exception occurs executing the task
             */
            void throwingRun()
                    throws Exception;
        }

        /**
         * This class is responsible for capturing and handling uncaught exceptions
         * that occur in a thread.
         * It implements the {@link Thread.UncaughtExceptionHandler} interface and can
         * be assigned to a thread using the
         * {@link Thread#setUncaughtExceptionHandler(Thread.UncaughtExceptionHandler)} method.
         * When an exception occurs in the thread,
         * the {@link ProcessorExceptionHandler#uncaughtException(Thread, Throwable)}
         * method will be called.
         * This class provides the following features:
         * 1. Capturing and storing the uncaught exception.
         * 2. Executing a list of routines when an exception occurs.
         * 3. Providing access to the captured exception, if any.
         * Example usage:
         * <code>
         * ProcessorExceptionHandler exceptionHandler = new
         * ProcessorExceptionHandler(){};
         * thread.setUncaughtExceptionHandler(exceptionHandler);
         * </code>
         *
         * @author Joseph Verron
         * @version ${version}
         * @see Thread.UncaughtExceptionHandler
         * @since 1.6.6
         */
        static class ProcessorExceptionHandler
                implements Thread.UncaughtExceptionHandler {
            private final AtomicReference<Throwable> exception;
            private final List<Runnable> onException;

            /**
             * Constructs a new instance for managing thread's uncaught exceptions.
             * Once set to a thread, it retains the exception information and performs specified routines.
             */
            public ProcessorExceptionHandler() {
                this.exception = new AtomicReference<>();
                this.onException = new CopyOnWriteArrayList<>();
            }

            /**
             * {@inheritDoc}
             * <p>
             * Captures and stores an uncaught exception from a thread run
             * and executes all defined routines on occurrence of the exception.
             */
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                exception.set(e);
                onException.forEach(Runnable::run);
            }

            /**
             * Adds a routine to the list of routines that should be run
             * when an exception occurs.
             *
             * @param runnable The runnable routine to be added
             */
            public void onException(ThrowingRunnable runnable) {
                onException.add(runnable);
            }

            /**
             * Returns the captured exception if present.
             *
             * @return an {@link Optional} containing the captured exception,
             * or an {@link Optional#empty()} if no exception was captured
             */
            public Optional<Throwable> exception() {
                return Optional.ofNullable(exception.get());
            }
        }
    }

    /**
     * Repeats a table row for each element in a list.
     *
     * @author Joseph Verron
     * @author Tom Hombergs
     * @version ${version}
     * @since 1.0.0
     */
    private static class RepeatProcessor
            extends AbstractCommentProcessor
            implements IRepeatProcessor {

        private final BiFunction<WordprocessingMLPackage, Tr, List<Tr>> nullSupplier;
        private Map<Tr, List<Object>> tableRowsToRepeat = new HashMap<>();
        private Map<Tr, Comment> tableRowsCommentsToRemove = new HashMap<>();

        private RepeatProcessor(
                ParagraphPlaceholderReplacer placeholderReplacer,
                BiFunction<WordprocessingMLPackage, Tr, List<Tr>> nullSupplier1
        ) {
            super(placeholderReplacer);
            nullSupplier = nullSupplier1;
        }

        /**
         * Creates a new RepeatProcessor.
         *
         * @param pr The PlaceholderReplacer to use.
         *
         * @return A new RepeatProcessor.
         */
        public static CommentProcessor newInstance(ParagraphPlaceholderReplacer pr) {
            return new RepeatProcessor(pr, (document, row) -> emptyList());
        }

        /** {@inheritDoc} */
        @Override
        public void commitChanges(WordprocessingMLPackage document) {
            repeatRows(document);
        }

        /** {@inheritDoc} */
        @Override
        public void reset() {
            this.tableRowsToRepeat = new HashMap<>();
            this.tableRowsCommentsToRemove = new HashMap<>();
        }

        private void repeatRows(final WordprocessingMLPackage document) {
            for (Map.Entry<Tr, List<Object>> entry : tableRowsToRepeat.entrySet()) {
                Tr row = entry.getKey();
                List<Object> expressionContexts = entry.getValue();

                Tbl table = (Tbl) XmlUtils.unwrap(row.getParent());
                int index = table.getContent()
                                 .indexOf(row);


                List<Tr> changes;
                if (expressionContexts == null) {
                    changes = nullSupplier.apply(document, row);
                }
                else {
                    changes = new ArrayList<>();
                    for (Object expressionContext : expressionContexts) {
                        Tr rowClone = XmlUtils.deepCopy(row);
                        Comment commentWrapper = requireNonNull(
                                tableRowsCommentsToRemove.get(row));
                        Comments.Comment comment = requireNonNull(commentWrapper.getComment());
                        BigInteger commentId = comment.getId();
                        CommentUtil.deleteCommentFromElements(rowClone.getContent(), commentId);
                        new ParagraphResolverDocumentWalker(rowClone,
                                expressionContext,
                                document,
                                this.placeholderReplacer).walk();
                        changes.add(rowClone);
                    }
                }
                table.getContent()
                     .addAll(index + 1, changes);
                table.getContent()
                     .remove(row);
            }
        }

        /** {@inheritDoc} */
        @Override
        public void repeatTableRow(List<Object> objects) {
            P p = getParagraph();
            if (p.getParent() instanceof Tc tc && tc.getParent() instanceof Tr tableRow) {
                tableRowsToRepeat.put(tableRow, objects);
                tableRowsCommentsToRemove.put(tableRow, getCurrentCommentWrapper());
            }
            else throw new OfficeStamperException(format("Paragraph is not within a table! : %s", getText(p)));
        }
    }

    /**
     * Processor for the {@link IDisplayIfProcessor} comment.
     *
     * @author Joseph Verron
     * @author Tom Hombergs
     * @version ${version}
     * @since 1.0.0
     */
    private static class DisplayIfProcessor
            extends AbstractCommentProcessor
            implements IDisplayIfProcessor {

        private List<P> paragraphsToBeRemoved = new ArrayList<>();
        private List<Tbl> tablesToBeRemoved = new ArrayList<>();
        private List<Tr> tableRowsToBeRemoved = new ArrayList<>();

        private DisplayIfProcessor(ParagraphPlaceholderReplacer placeholderReplacer) {
            super(placeholderReplacer);
        }

        /**
         * Creates a new DisplayIfProcessor instance.
         *
         * @param pr the {@link PlaceholderReplacer} used for replacing expressions.
         *
         * @return a new DisplayIfProcessor instance.
         */
        public static CommentProcessor newInstance(ParagraphPlaceholderReplacer pr) {
            return new DisplayIfProcessor(pr);
        }

        /** {@inheritDoc} */
        @Override
        public void commitChanges(WordprocessingMLPackage document) {
            removeParagraphs();
            removeTables();
            removeTableRows();
        }

        /** {@inheritDoc} */
        @Override
        public void reset() {
            paragraphsToBeRemoved = new ArrayList<>();
            tablesToBeRemoved = new ArrayList<>();
            tableRowsToBeRemoved = new ArrayList<>();
        }

        private void removeParagraphs() {
            for (P p : paragraphsToBeRemoved) {
                ObjectDeleter.deleteParagraph(p);
            }
        }

        private void removeTables() {
            for (Tbl table : tablesToBeRemoved) {
                ObjectDeleter.deleteTable(table);
            }
        }

        private void removeTableRows() {
            for (Tr row : tableRowsToBeRemoved) {
                ObjectDeleter.deleteTableRow(row);
            }
        }

        /** {@inheritDoc} */
        @Override
        public void displayParagraphIf(Boolean condition) {
            if (Boolean.TRUE.equals(condition)) return;
            paragraphsToBeRemoved.add(getParagraph());
        }

        /** {@inheritDoc} */
        @Override
        public void displayParagraphIfPresent(@Nullable Object condition) {
            displayParagraphIf(condition != null);
        }

        /** {@inheritDoc} */
        @Override
        public void displayTableIf(Boolean condition) {
            if (Boolean.TRUE.equals(condition)) return;

            P p = getParagraph();
            if (p.getParent() instanceof Tc tc
                    && tc.getParent() instanceof Tr tr
                    && tr.getParent() instanceof Tbl tbl
            ) {
                tablesToBeRemoved.add(tbl);
            }
            else throw new OfficeStamperException(format("Paragraph is not within a table! : %s", getText(p)));
        }

        /** {@inheritDoc} */
        @Override
        public void displayTableRowIf(Boolean condition) {
            if (Boolean.TRUE.equals(condition)) return;
            P p = getParagraph();
            if (p.getParent() instanceof Tc tc && tc.getParent() instanceof Tr tr) {
                tableRowsToBeRemoved.add(tr);
            }
            else throw new OfficeStamperException(format("Paragraph is not within a table! : %s", getText(p)));
        }
    }
}
