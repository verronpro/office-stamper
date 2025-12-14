package pro.verron.officestamper.utils.wml;

import org.docx4j.dml.wordprocessingDrawing.Inline;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPartAbstractImage;
import org.docx4j.openpackaging.parts.WordprocessingML.CommentsPart;
import org.docx4j.wml.*;
import org.docx4j.wml.Comments.Comment;
import org.jspecify.annotations.Nullable;
import pro.verron.officestamper.utils.UtilsException;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static java.util.stream.Collectors.toCollection;

/// Utility class for creating and configuring various WordML (WML) elements. Provides static methods to generate
/// paragraphs, runs, comments, text, and other WML structures. This is intended for handling Office Open XML documents
/// programmatically.
public class WmlFactory {
    private static final Random RANDOM = new Random();

    private WmlFactory() {
        throw new UtilsException("Utility class shouldn't be instantiated");
    }

    /// Creates a new comment with the provided value.
    ///
    /// @param id The ID to assign to the comment.
    /// @param value The string value to be included in the comment.
    ///
    /// @return A new [Comment] object containing the provided value.
    public static Comment newComment(BigInteger id, String value) {
        var comment = new Comment();
        comment.setId(id);
        var commentContent = comment.getContent();
        commentContent.add(newParagraph(value));
        return comment;
    }

    /// Creates a new paragraph containing the provided string value.
    ///
    /// @param value The string value to be added to the new paragraph.
    ///
    /// @return A new [P] containing the provided string value.
    public static P newParagraph(String value) {
        return newParagraph(newRun(value));
    }

    /// Creates a new paragraph containing the provided run.
    ///
    /// @param run The [R] object (run) to be included in the new paragraph.
    ///
    /// @return A new [P] containing the provided run.
    public static P newParagraph(R run) {
        return newParagraph(List.of(run));
    }

    /// Creates a new run containing the provided string value.
    ///
    /// @param value The string value to be included in the new run.
    ///
    /// @return A new [R] containing the provided string value.
    public static R newRun(String value) {
        return newRun(newText(value));
    }

    /// Creates a new paragraph containing the provided list of values.
    ///
    /// @param values A list of objects to be added to the new paragraph. These objects populate the content of
    ///         the paragraph.
    ///
    /// @return A new [P] containing the provided values.
    public static P newParagraph(List<?> values) {
        var paragraph = new P();
        var paragraphContent = paragraph.getContent();
        paragraphContent.addAll(values);
        return paragraph;
    }

    /// Creates a new run containing a single text object.
    ///
    /// @param value The [Text] object to be included in the new run.
    ///
    /// @return A new [R] encapsulating the provided text object.
    public static R newRun(Text value) {
        return newRun(List.of(value));
    }

    /// Creates a new [Text] object with the specified value, preserving spaces.
    ///
    /// @param value The string value to be set in the new [Text] object.
    ///
    /// @return A new [Text] object containing the provided value with space preserved.
    public static Text newText(String value) {
        var text = new Text();
        text.setValue(value);
        text.setSpace("preserve");
        return text;
    }

    /// Creates a new run containing the provided values deemed worth keeping.
    ///
    /// @param values A list of objects to be added to the new run. Objects are filtered based on a predefined
    ///         criteria to determine if they are worth keeping.
    ///
    /// @return A new [R] containing the filtered values.
    public static R newRun(List<Object> values) {
        var run = new R();
        var runContent = run.getContent();
        runContent.addAll(values.stream()
                                .filter(WmlFactory::worthKeeping)
                                .collect(toCollection(ArrayList::new)));
        return run;
    }

    private static boolean worthKeeping(Object o) {
        if (o instanceof Text text) return worthKeeping(text);
        else return true;
    }

    private static boolean worthKeeping(Text text) {
        var textValue = text.getValue();
        return !textValue.isEmpty();
    }

    /// Creates a new [Body] object containing the provided elements.
    ///
    /// @param elements A list of objects to be added to the new [Body].
    ///
    /// @return A new [Body] containing the provided elements.
    public static Body newBody(List<Object> elements) {
        Body body = new Body();
        var bodyContent = body.getContent();
        bodyContent.addAll(elements);
        return body;
    }

    /// Creates a new paragraph containing the provided text values.
    ///
    /// @param texts The array of string values to be included in the new paragraph.
    ///
    /// @return A new [P] containing the provided text values.
    public static P newParagraph(String... texts) {
        return newParagraph(Arrays.stream(texts)
                                  .map(WmlFactory::newRun)
                                  .toList());
    }

    /// Creates a new [PPr] (paragraph properties) object.
    ///
    /// @return A new [PPr] object.
    public static PPr newPPr() {
        return new PPr();
    }

    /// Creates a new [Comments] object and populates it with a list of [Comment] objects.
    ///
    /// @param list A list of [Comment] objects to be added to the new [Comments] object.
    ///
    /// @return A new [Comments] object containing the provided [Comment] objects.
    public static Comments newComments(List<Comment> list) {
        Comments comments = new Comments();
        List<Comment> commentList = comments.getComment();
        commentList.addAll(list);
        return comments;
    }

    /// Creates a new run containing an image with the specified attributes.
    ///
    /// @param maxWidth The maximum width of the image, it can be null.
    /// @param abstractImage The binary part abstract image to be included in the run.
    /// @param filenameHint The filename hint for the image.
    /// @param altText The alternative text for the image.
    ///
    /// @return A new [R] element containing the image.
    public static R newRun(
            @Nullable Integer maxWidth,
            BinaryPartAbstractImage abstractImage,
            String filenameHint,
            String altText
    ) {
        var inline = newInline(abstractImage, filenameHint, altText, maxWidth);
        return newRun(newDrawing(inline));
    }

    /// Creates a new [Inline] object for the given image part, filename hint, and alt text.
    ///
    /// @param imagePart The binary part abstract image to be used.
    /// @param filenameHint A hint for the filename of the image.
    /// @param altText Alternative text for the image.
    /// @param maxWidth The image width not to exceed, in points.
    ///
    /// @return A new [Inline] object containing the specified image information.
    ///
    /// @throws UtilsException If there is an error creating the image inline.
    public static Inline newInline(
            BinaryPartAbstractImage imagePart,
            String filenameHint,
            String altText,
            @Nullable Integer maxWidth
    ) {
        // creating random ids assuming they are unique,
        // id must not be too large
        // otherwise Word cannot open the document
        var id1 = RANDOM.nextLong(100_000L);
        var id2 = RANDOM.nextInt(100_000);
        try {
            return maxWidth == null
                    ? imagePart.createImageInline(filenameHint, altText, id1, id2, false)
                    : imagePart.createImageInline(filenameHint, altText, id1, id2, false, maxWidth);
        } catch (Exception e) {
            throw new UtilsException(e);
        }
    }

    /// Creates a new run containing a single drawing.
    ///
    /// @param value The [Drawing] object to be included in the new run.
    ///
    /// @return A new [R] encapsulating the provided drawing.
    public static R newRun(Drawing value) {
        return newRun(List.of(value));
    }

    /// Creates a new [Drawing] object containing the provided [Inline] object.
    ///
    /// @param inline The [Inline] object to be contained within the new [Drawing].
    ///
    /// @return A new [Drawing] object encapsulating the provided inline object.
    public static Drawing newDrawing(Inline inline) {
        var drawing = new Drawing();
        var anchorOrInline = drawing.getAnchorOrInline();
        anchorOrInline.add(inline);
        return drawing;
    }

    /// Creates a new [CommentRangeStart] object with the specified ID and parent.
    ///
    /// @param id The unique identifier for the [CommentRangeStart] object.
    /// @param parent The parent element ([P]) to which this [CommentRangeStart] belongs.
    ///
    /// @return A new [CommentRangeStart] object with the specified ID and parent.
    public static CommentRangeStart newCommentRangeStart(BigInteger id, ContentAccessor parent) {
        var commentRangeStart = new CommentRangeStart();
        commentRangeStart.setId(id);
        commentRangeStart.setParent(parent);
        return commentRangeStart;
    }

    /// Creates a new [CommentRangeEnd] object with the specified ID and parent.
    ///
    /// @param id The unique identifier for the [CommentRangeEnd] object.
    /// @param parent The parent element ([P]) to which this [CommentRangeEnd] belongs.
    ///
    /// @return A new [CommentRangeEnd] object with the specified ID and parent.
    public static CommentRangeEnd newCommentRangeEnd(BigInteger id, ContentAccessor parent) {
        var commentRangeEnd = new CommentRangeEnd();
        commentRangeEnd.setId(id);
        commentRangeEnd.setParent(parent);
        return commentRangeEnd;
    }

    /// Creates a new [R.CommentReference] object with the specified ID and parent.
    ///
    /// @param id The unique identifier for the [R.CommentReference].
    /// @param parent The parent element ([P]) to which this [R.CommentReference] belongs.
    ///
    /// @return A new [R.CommentReference] object with the specified ID and parent.
    public static R.CommentReference newCommentReference(BigInteger id, ContentAccessor parent) {
        var commentReference = new R.CommentReference();
        commentReference.setId(id);
        commentReference.setParent(parent);
        return commentReference;
    }

    /// Creates a new table object.
    ///
    /// @return A new instance of [Tbl].
    public static Tbl newTbl() {
        return new Tbl();
    }

    /// Creates a new cell object.
    ///
    /// @return A new instance of [Tc].
    public static Tc newCell() {
        return new Tc();
    }

    /// Creates a new row object.
    ///
    /// @return A new instance of [Tr].
    public static Tr newRow() {
        return new Tr();
    }

    /// Creates a new [WordprocessingMLPackage] object initialized with a main document part, and an empty comments
    /// part.
    ///
    /// @return A new instance of [WordprocessingMLPackage].
    public static WordprocessingMLPackage newWord() {
        try {
            var aPackage = WordprocessingMLPackage.createPackage();
            var mainDocumentPart = aPackage.getMainDocumentPart();
            var cp = newCommentsPart();
            cp.init();
            cp.setJaxbElement(newComments());
            mainDocumentPart.addTargetPart(cp);
            return aPackage;
        } catch (InvalidFormatException e) {
            throw new UtilsException(e);
        }
    }

    /// Creates a new [CommentsPart] object. This method attempts to create a new instance of [CommentsPart]. If an
    /// [InvalidFormatException] occurs during the creation process, it wraps the exception in an
    /// [UtilsException] and throws it.
    ///
    /// @return A new instance of [CommentsPart].
    public static CommentsPart newCommentsPart() {
        try {
            return new CommentsPart();
        } catch (InvalidFormatException e) {
            throw new UtilsException(e);
        }
    }

    private static Comments newComments() {
        return new Comments();
    }

    /// Creates a new [Br] (break) object with text wrapping enabled.
    ///
    /// @return A new [Br] object with text wrapping type and no clear attribute set.
    public static Br newBr() {
        var br = new Br();
        br.setType(STBrType.TEXT_WRAPPING);
        br.setClear(null);
        return br;
    }

    /// Creates a new smart tag run with the specified element, run and attribute.
    ///
    /// @param element The element name for the smart tag.
    /// @param run The [R] to include in the smart tag content.
    /// @param attribute The [CTAttr] to add to the smart tag properties.
    ///
    /// @return A new [CTSmartTagRun] object configured with the specified parameters.
    public static CTSmartTagRun newSmartTag(String element, R run, CTAttr attribute) {
        var smartTag = new CTSmartTagRun();
        smartTag.setElement(element);
        var smartTagPr = new CTSmartTagPr();
        var smartTagPrAttr = smartTagPr.getAttr();
        smartTagPrAttr.add(attribute);
        var smartTagContent = smartTag.getContent();
        smartTag.setSmartTagPr(smartTagPr);
        smartTagContent.add(run);
        return smartTag;
    }

    /// Creates a new [CTAttr] object with the specified name and value.
    ///
    /// @param name The name of the attribute.
    /// @param value The value of the attribute.
    ///
    /// @return A new [CTAttr] object with the specified name and value.
    public static CTAttr newCtAttr(String name, String value) {
        var ctAttr = new CTAttr();
        ctAttr.setName(name);
        ctAttr.setVal(value);
        return ctAttr;
    }

    /// Creates a new [Pict] object containing the provided inner object.
    ///
    /// @param innerObj The object to be included in the new pict element.
    ///
    /// @return A new [Pict] object containing the provided inner object.
    public static Object newPict(Object innerObj) {
        var pict = new Pict();
        pict.getAnyAndAny()
            .add(innerObj);
        return pict;
    }

    /// Creates a new [SdtBlock] object containing the provided inner object.
    ///
    /// @param innerObj The object to be included in the new structured document tag block.
    ///
    /// @return A new [SdtBlock] object containing the provided inner object.
    public static SdtBlock newSdtBlock(Object innerObj) {
        var block = new SdtContentBlock();
        var blockContent = block.getContent();
        blockContent.add(innerObj);
        var sdtBlock = new SdtBlock();
        sdtBlock.setSdtContent(block);
        return sdtBlock;
    }

    /// Creates a new [SdtRun] object containing the provided inner object.
    ///
    /// @param innerObj The object to be included in the new structured document tag run.
    ///
    /// @return A new [SdtRun] object containing the provided inner object.
    public static SdtRun newSdtRun(Object innerObj) {
        var sdtContentRun = new CTSdtContentRun();
        var sdtContentRunContent = sdtContentRun.getContent();
        sdtContentRunContent.add(innerObj);
        var sdtRun = new SdtRun();
        sdtRun.setSdtContent(sdtContentRun);
        return sdtRun;
    }
}
