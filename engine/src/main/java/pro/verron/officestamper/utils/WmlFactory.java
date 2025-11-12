package pro.verron.officestamper.utils;

import org.docx4j.dml.wordprocessingDrawing.Inline;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPartAbstractImage;
import org.docx4j.openpackaging.parts.WordprocessingML.CommentsPart;
import org.docx4j.wml.*;
import org.springframework.lang.Nullable;
import pro.verron.officestamper.api.OfficeStamperException;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static java.util.stream.Collectors.toCollection;

/// Utility class for creating and configuring various WordML (WML) elements.
/// Provides static methods to generate paragraphs, runs, comments, text, and other WML structures.
/// This is intended for handling Office Open XML documents programmatically.
public class WmlFactory {
    private static final Random RANDOM = new Random();

    private WmlFactory() {
        throw new OfficeStamperException("Utility class");
    }

    /// Creates a new paragraph containing a single drawing element.
    ///
    /// @param drawing The Drawing object to be included in the new paragraph.
    ///
    /// @return A new paragraph containing the provided drawing encapsulated in a run.
    public static P newParagraph(Drawing drawing) {
        return newParagraph(List.of(newRun(drawing)));
    }

    /// Creates a new paragraph containing the provided list of values.
    ///
    /// @param values A list of objects to be added to the new paragraph.
    ///               These objects populate the content of the paragraph.
    ///
    /// @return A new paragraph containing the provided values.
    public static P newParagraph(List<?> values) {
        var paragraph = new P();
        var paragraphContent = paragraph.getContent();
        paragraphContent.addAll(values);
        return paragraph;
    }

    /// Creates a new run containing a single drawing.
    ///
    /// @param value The Drawing object to be included in the new run.
    ///
    /// @return A new run encapsulating the provided drawing.
    public static R newRun(Drawing value) {
        return newRun(List.of(value));
    }

    /// Creates a new run containing the provided values deemed worth keeping.
    ///
    /// @param values A list of objects to be added to the new run.
    ///               Objects are filtered based on a predefined criteria to determine if they are worth keeping.
    ///
    /// @return A new run containing the filtered values.
    public static R newRun(List<Object> values) {
        var run = new R();
        var runContent = run.getContent();
        runContent.addAll(values.stream()
                                .filter(WmlFactory::worthKeeping)
                                .collect(toCollection(ArrayList::new)));
        return run;
    }

    private static boolean worthKeeping(Object o) {
        if (o instanceof Text text) {
            var value = text.getValue();
            return !value.isEmpty();
        }
        return true;
    }

    /// Creates a new comment with the provided value.
    ///
    /// @param id    The id to affect to the comment.
    /// @param value The string value to be included in the comment.
    ///
    /// @return A new Comments.Comment object containing the provided value.
    public static Comments.Comment newComment(BigInteger id, String value) {
        var comment = new Comments.Comment();
        comment.setId(id);
        var commentContent = comment.getContent();
        commentContent.add(newParagraph(value));
        return comment;
    }

    /// Creates a new paragraph containing the provided string value.
    ///
    /// @param value The string value to be added to the new paragraph.
    ///
    /// @return A new paragraph containing the provided string value.
    public static P newParagraph(String value) {
        return newParagraph(newRun(value));
    }

    /// Creates a new paragraph containing the provided run.
    ///
    /// @param run The R object (run) to be included in the new paragraph.
    ///
    /// @return A new paragraph containing the provided run.
    public static P newParagraph(R run) {
        return newParagraph(List.of(run));
    }

    /// Creates a new run containing the provided string value.
    ///
    /// @param value The string value to be included in the new run.
    ///
    /// @return A new run containing the provided string value.
    public static R newRun(String value) {
        return newRun(newText(value));
    }

    /// Creates a new run containing a single text object.
    ///
    /// @param value The Text object to be included in the new run.
    ///
    /// @return A new run encapsulating the provided text object.
    public static R newRun(Text value) {
        return newRun(List.of(value));
    }

    /// Creates a new Text object with the specified value, preserving spaces.
    ///
    /// @param value The string value to be set in the new Text object.
    ///
    /// @return A new Text object containing the provided value with space preserved.
    public static Text newText(String value) {
        var text = new Text();
        text.setValue(value);
        text.setSpace("preserve");
        return text;
    }

    /// Creates a new Body object containing the provided elements.
    ///
    /// @param elements A list of objects to be added to the new Body.
    ///
    /// @return A new Body containing the provided elements.
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
    /// @return A new paragraph containing the provided text values.
    public static P newParagraph(String... texts) {
        return newParagraph(Arrays.stream(texts)
                                  .map(WmlFactory::newRun)
                                  .toList());
    }

    /// Creates a new PPr (paragraph properties) object.
    ///
    /// @return A new PPr object.
    public static PPr newPPr() {
        return new PPr();
    }

    /// Creates a new Comments object and populates it with a list of Comment objects.
    ///
    /// @param list A list of Comments.Comment objects to be added to the new Comments object.
    ///
    /// @return A new Comments object containing the provided Comment objects.
    public static Comments newComments(List<Comments.Comment> list) {
        Comments comments = new Comments();
        List<Comments.Comment> commentList = comments.getComment();
        commentList.addAll(list);
        return comments;
    }

    /// Creates a new run containing an image with the specified attributes.
    ///
    /// @param maxWidth      the maximum width of the image, it can be null
    /// @param abstractImage the binary part abstract image to be included in the run
    /// @param filenameHint  the filename hint for the image
    /// @param altText       the alternative text for the image
    ///
    /// @return a new run element containing the image
    public static R newRun(
            @Nullable Integer maxWidth,
            BinaryPartAbstractImage abstractImage,
            String filenameHint,
            String altText
    ) {
        var inline = newInline(abstractImage, filenameHint, altText, maxWidth);
        return newRun(newDrawing(inline));
    }

    /// Creates a new Inline object for the given image part, filename hint, and alt text.
    ///
    /// @param imagePart    The binary part abstract image to be used.
    /// @param filenameHint A hint for the filename of the image.
    /// @param altText      Alternative text for the image.
    /// @param maxWidth     The image width to not exceeds, in point.
    ///
    /// @return A new Inline object containing the specified image information.
    ///
    /// @throws OfficeStamperException If there is an error creating the image inline.
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
            throw new OfficeStamperException(e);
        }
    }

    /// Creates a new Drawing object containing the provided Inline object.
    ///
    /// @param inline The Inline object to be contained within the new Drawing.
    ///
    /// @return A new Drawing object encapsulating the provided inline object.
    public static Drawing newDrawing(Inline inline) {
        var drawing = new Drawing();
        var anchorOrInline = drawing.getAnchorOrInline();
        anchorOrInline.add(inline);
        return drawing;
    }

    /// Creates a new CommentRangeStart object with the specified ID and parent.
    ///
    /// @param id     The unique identifier for the CommentRangeStart object.
    /// @param parent The parent element (P) to which this CommentRangeStart belongs.
    ///
    /// @return A new CommentRangeStart object with the specified ID and parent.
    public static CommentRangeStart newCommentRangeStart(BigInteger id, P parent) {
        var commentRangeStart = new CommentRangeStart();
        commentRangeStart.setId(id);
        commentRangeStart.setParent(parent);
        return commentRangeStart;
    }

    /// Creates a new CommentRangeEnd object with the specified ID and parent.
    ///
    /// @param id     The unique identifier for the CommentRangeEnd object.
    /// @param parent The parent element (P) to which this CommentRangeEnd belongs.
    ///
    /// @return A new CommentRangeEnd object with the specified ID and parent.
    public static CommentRangeEnd newCommentRangeEnd(BigInteger id, P parent) {
        var commentRangeEnd = new CommentRangeEnd();
        commentRangeEnd.setId(id);
        commentRangeEnd.setParent(parent);
        return commentRangeEnd;
    }

    /// Creates a new CommentReference object with the specified ID and parent.
    ///
    /// @param id     The unique identifier for the CommentReference.
    /// @param parent The parent element (P) to which this CommentReference belongs.
    ///
    /// @return A new CommentReference object with the specified ID and parent.
    public static R.CommentReference newCommentReference(BigInteger id, P parent) {
        var commentReference = new R.CommentReference();
        commentReference.setId(id);
        commentReference.setParent(parent);
        return commentReference;
    }

    /// Creates a new table object.
    ///
    /// @return A new instance of Tbl.
    public static Tbl newTbl() {
        return new Tbl();
    }

    /// Creates a new cell object.
    ///
    /// @return A new instance of Tc.
    public static Tc newCell() {
        return new Tc();
    }

    /// Creates a new row object.
    ///
    /// @return A new instance of Tr.
    public static Tr newRow() {
        return new Tr();
    }

    /// Creates a new WordprocessingMLPackage object initialized with a main document part,
    /// and an empty comments part.
    ///
    /// @return A new instance of WordprocessingMLPackage.
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
            throw new OfficeStamperException(e);
        }
    }

    /// Creates a new CommentsPart object.
    /// This method attempts to create a new instance of CommentsPart.
    /// If an InvalidFormatException occurs during the creation process, it wraps the exception in an
    /// OfficeStamperException and throws it.
    ///
    /// @return A new instance of CommentsPart.
    public static CommentsPart newCommentsPart() {
        try {
            return new CommentsPart();
        } catch (InvalidFormatException e) {
            throw new OfficeStamperException(e);
        }
    }

    private static Comments newComments() {
        return new Comments();
    }

    /// Creates a new Br (break) object with text wrapping enabled.
    ///
    /// @return A new Br object with text wrapping type and no clear attribute set.
    public static Br newBr() {
        var br = new Br();
        br.setType(STBrType.TEXT_WRAPPING);
        br.setClear(null);
        return br;
    }

    public static CTSmartTagRun newSmartTag(String element, R run) {
        var smartTag = new CTSmartTagRun();
        var smartTagContent = smartTag.getContent();
        smartTag.setElement(element);
        smartTagContent.add(run);
        return smartTag;
    }
}
