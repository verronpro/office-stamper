package pro.verron.officestamper.preset.preprocessors.malformedcomments;

import org.docx4j.TraversalUtil;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.CommentsPart;
import org.docx4j.wml.*;
import org.jvnet.jaxb2_commons.ppp.Child;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.verron.officestamper.api.OfficeStamperException;
import pro.verron.officestamper.api.PreProcessor;
import pro.verron.officestamper.utils.wml.WmlUtils;

import java.math.BigInteger;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toSet;

/// This pre-processor removes malformed comments from a WordprocessingMLPackage document.
///
/// Malformed comments are those that:
///
///     - Are opened but never closed (unbalanced comments)
///     - Are referenced in the document body but have no corresponding comment content
///
///
/// The processor traverses all comment-related elements in the document, validates their structure, and removes any
/// malformed comment references, range starts, and range ends.
///
/// @author Joseph Verron
/// @version ${version}
public class RemoveMalformedComments
        implements PreProcessor {
    private static final Logger log = LoggerFactory.getLogger(RemoveMalformedComments.class);

    @Override
    public void process(WordprocessingMLPackage document) {
        var commentElements = WmlUtils.extractCommentElements(document);

        var commentIds = new ArrayList<BigInteger>(commentElements.size());
        var openedCommentsIds = new ArrayDeque<BigInteger>();
        for (Child commentElement : commentElements) {
            switch (commentElement) {
                case CommentRangeStart crs -> {
                    var lastOpenedCommentId = crs.getId();
                    assert lastOpenedCommentId != null;
                    log.trace("Comment {} opened.", lastOpenedCommentId);
                    commentIds.add(lastOpenedCommentId);
                    openedCommentsIds.add(lastOpenedCommentId);
                }
                case CommentRangeEnd cre -> {
                    var lastClosedCommentId = cre.getId();
                    assert lastClosedCommentId != null;
                    log.trace("Comment {} closed.", lastClosedCommentId);
                    commentIds.add(lastClosedCommentId);

                    var lastOpenedCommentId = openedCommentsIds.pollLast();
                    if (!lastClosedCommentId.equals(lastOpenedCommentId)) {
                        log.debug("Comment {} is closing just after comment {} starts.",
                                lastClosedCommentId,
                                lastOpenedCommentId);
                        throw new OfficeStamperException("Cannot figure which comment contains the other !");
                    }
                }
                case R.CommentReference cr -> {
                    var commentId = cr.getId();
                    assert commentId != null;
                    log.trace("Comment {} referenced.", commentId);
                    commentIds.add(commentId);
                }
                default -> { /* Do Nothing */ }
            }
        }

        log.debug("These comments have been opened, but never closed: {}", openedCommentsIds);
        var malformedCommentIds = new ArrayList<>(openedCommentsIds);

        var mainDocumentPart = document.getMainDocumentPart();
        var writtenCommentsId = Optional.ofNullable(mainDocumentPart.getCommentsPart())
                                        .map(RemoveMalformedComments::tryGetCommentsPart)
                                        .map(Comments::getComment)
                                        .orElse(emptyList())
                                        .stream()
                                        .filter(c -> !isEmpty(c))
                                        .map(CTMarkup::getId)
                                        .collect(toSet());

        commentIds.removeAll(writtenCommentsId);

        if (!commentIds.isEmpty()) log.debug("Comments referenced in body, without related content: {}", commentIds);
        malformedCommentIds.addAll(commentIds);

        var crVisitor = new CommentReferenceRemoverVisitor(malformedCommentIds);
        var crsVisitor = new CommentRangeStartRemoverVisitor(malformedCommentIds);
        var creVisitor = new CommentRangeEndRemoverVisitor(malformedCommentIds);
        TraversalUtil.visit(document, true, List.of(crVisitor, crsVisitor, creVisitor));
        crVisitor.run();
        crsVisitor.run();
        creVisitor.run();
    }

    private static Comments tryGetCommentsPart(CommentsPart commentsPart) {
        try {
            return commentsPart.getContents();
        } catch (Docx4JException e) {
            throw new OfficeStamperException(e);
        }
    }

    private static boolean isEmpty(Comments.Comment c) {
        var content = c.getContent();
        return content == null || content.isEmpty();
    }
}
