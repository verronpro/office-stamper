package pro.verron.officestamper.core;

import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.Parts;
import org.docx4j.openpackaging.parts.WordprocessingML.CommentsPart;
import org.docx4j.wml.*;
import org.docx4j.wml.R.CommentReference;
import pro.verron.officestamper.api.Comment;
import pro.verron.officestamper.api.OfficeStamperException;

import java.math.BigInteger;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.docx4j.XmlUtils.unwrap;
import static pro.verron.officestamper.utils.WmlFactory.newBody;
import static pro.verron.officestamper.utils.WmlFactory.newComments;

/// Utility class for working with comments in a DOCX document.
///
/// @author Joseph Verron
/// @author Tom Hombergs
/// @version ${version}
/// @since 1.0.0
public class CommentUtil {
    private static final PartName WORD_COMMENTS_PART_NAME;

    static {
        try {
            WORD_COMMENTS_PART_NAME = new PartName("/word/comments.xml");
        } catch (InvalidFormatException e) {
            throw new OfficeStamperException(e);
        }
    }

    private CommentUtil() {
        throw new OfficeStamperException("Utility class shouldn't be instantiated");
    }

    /// Retrieves the comment associated with a given paragraph content within a WordprocessingMLPackage document.
    ///
    /// @param document the WordprocessingMLPackage document containing the paragraph and its comments.
    ///
    /// @return an Optional containing the found comment, or Optional.empty() if no comment is associated with the given
    /// paragraph content.
    public static Collection<Comments.Comment> getCommentFor(
            ContentAccessor contentAccessor,
            WordprocessingMLPackage document
    ) {
        var comments = getCommentsPart(document.getParts()).map(CommentUtil::extractContent)
                                                           .map(Comments::getComment)
                                                           .stream()
                                                           .flatMap(Collection::stream)
                                                           .toList();

        var result = new ArrayList<Comments.Comment>();
        var commentIterator = DocxIterator.ofCRS(contentAccessor);
        while (commentIterator.hasNext()) {
            var crs = commentIterator.next();
            findCommentById(comments, crs.getId()).ifPresent(result::add);
        }
        return result;
    }

    /// Retrieves the CommentsPart from the given Parts object.
    ///
    /// @param parts the Parts object containing the various parts of the document.
    ///
    /// @return an Optional containing the CommentsPart if found, or an empty Optional if not found.
    public static Optional<CommentsPart> getCommentsPart(Parts parts) {
        return Optional.ofNullable((CommentsPart) parts.get(WORD_COMMENTS_PART_NAME));
    }

    /// Extracts the contents of a given [CommentsPart].
    ///
    /// @param commentsPart the [CommentsPart] from which content will be extracted
    ///
    /// @return the [Comments] instance containing the content of the provided comments part
    ///
    /// @throws OfficeStamperException if an error occurs while retrieving the content
    public static Comments extractContent(CommentsPart commentsPart) {
        try {
            return commentsPart.getContents();
        } catch (Docx4JException e) {
            throw new OfficeStamperException("Error while searching comment.", e);
        }
    }

    private static Optional<Comments.Comment> findCommentById(List<Comments.Comment> comments, BigInteger id) {
        for (Comments.Comment comment : comments) {
            if (id.equals(comment.getId())) {
                return Optional.of(comment);
            }
        }
        return Optional.empty();
    }

    /// Returns the string value of the specified comment object.
    ///
    /// @param comment a [Comment] object
    public static void deleteComment(Comment comment) {
        CommentRangeEnd end = comment.getCommentRangeEnd();
        if (end != null) {
            ContentAccessor endParent = (ContentAccessor) end.getParent();
            endParent.getContent()
                     .remove(end);
        }
        CommentRangeStart start = comment.getCommentRangeStart();
        if (start != null) {
            ContentAccessor startParent = (ContentAccessor) start.getParent();
            startParent.getContent()
                       .remove(start);
        }
        CommentReference reference = comment.getCommentReference();
        if (reference != null) {
            ContentAccessor referenceParent = (ContentAccessor) reference.getParent();
            referenceParent.getContent()
                           .remove(reference);
        }
    }

    /// Creates a sub Word document
    /// by extracting a specified comment and its associated content from the original document.
    ///
    /// @param comment The comment to be extracted from the original document.
    ///
    /// @return The sub Word document containing the content of the specified comment.
    public static WordprocessingMLPackage createSubWordDocument(Comment comment) {
        var elements = comment.getElements();

        var target = createWordPackageWithCommentsPart();

        // copy the elements without comment range anchors
        var finalElements = elements.stream()
                                    .map(XmlUtils::deepCopy)
                                    .collect(Collectors.toCollection(ArrayList::new));
        deleteCommentFromElements(comment, finalElements);
        target.getMainDocumentPart()
              .getContent()
              .addAll(finalElements);

        // copy the images from parent document using the original repeat elements
        var fakeBody = newBody(elements);
        DocumentUtil.walkObjectsAndImportImages(fakeBody, comment.getDocument(), target);

        var comments = extractComments(comment.getChildren());
        target.getMainDocumentPart()
              .getCommentsPart()
              .setContents(comments);
        return target;
    }

    private static WordprocessingMLPackage createWordPackageWithCommentsPart() {
        try {
            CommentsPart targetCommentsPart = new CommentsPart();
            var target = WordprocessingMLPackage.createPackage();
            var mainDocumentPart = target.getMainDocumentPart();
            mainDocumentPart.addTargetPart(targetCommentsPart);
            return target;
        } catch (InvalidFormatException e) {
            throw new OfficeStamperException("Failed to create a Word package with comment Part", e);
        }
    }

    /// Deletes all elements associated with the specified comment from the provided list of items.
    ///
    /// @param comment the comment whose associated elements should be removed
    /// @param items   the list of items from which elements associated with the comment will be deleted
    public static void deleteCommentFromElements(Comment comment, List<Object> items) {
        record DeletableItems(List<Object> container, List<Object> items) {
            static List<DeletableItems> findAll(List<Object> items, BigInteger commentId) {
                Predicate<BigInteger> predicate = bi -> Objects.equals(bi, commentId);
                List<DeletableItems> elementsToRemove = new ArrayList<>();
                items.forEach(item -> {
                    Object unwrapped = unwrap(item);
                    elementsToRemove.addAll(switch (unwrapped) {
                        case CommentRangeStart crs when predicate.test(crs.getId()) -> from(items, item);
                        case CommentRangeEnd cre when predicate.test(cre.getId()) -> from(items, item);
                        case CommentReference rcr when predicate.test(rcr.getId()) -> from(items, item);
                        case ContentAccessor ca -> findAll(ca, commentId);
                        case SdtRun sdtRun -> findAll(sdtRun, commentId);
                        default -> Collections.emptyList();
                    });
                });
                return elementsToRemove;
            }

            private static Collection<DeletableItems> findAll(SdtRun sdtRun, BigInteger commentId) {
                return findAll(sdtRun.getSdtContent(), commentId);
            }

            private static Collection<DeletableItems> findAll(ContentAccessor ca, BigInteger commentId) {
                return findAll(ca.getContent(), commentId);
            }

            private static List<DeletableItems> from(List<Object> items, Object item) {
                return Collections.singletonList(new DeletableItems(items, List.of(item)));
            }
        }
        var docx4jComment = comment.getComment();
        var commentId = docx4jComment.getId();
        DeletableItems.findAll(items, commentId)
                      .forEach(p -> p.container.removeAll(p.items));
    }

    private static Comments extractComments(Set<Comment> commentChildren) {
        var list = new ArrayList<Comments.Comment>();
        var queue = new ArrayDeque<>(commentChildren);
        while (!queue.isEmpty()) {
            var comment = queue.remove();
            list.add(comment.getComment());
            queue.addAll(comment.getChildren());
        }
        return newComments(list);
    }
}
