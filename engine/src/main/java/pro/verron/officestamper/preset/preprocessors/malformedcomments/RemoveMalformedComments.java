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
import pro.verron.officestamper.utils.WmlUtils;

import java.math.BigInteger;
import java.util.*;

import static java.util.stream.Collectors.toSet;

public class RemoveMalformedComments
        implements PreProcessor {
    private static final Logger log = LoggerFactory.getLogger(RemoveMalformedComments.class);

    @Override public void process(WordprocessingMLPackage document) {
        var commentElements = WmlUtils.extractCommentElements(document);

        var commentIds = new ArrayList<BigInteger>(commentElements.size());
        var openedCommentsIds = new ArrayDeque<BigInteger>();
        for (Child commentElement : commentElements) {
            if (commentElement instanceof CommentRangeStart crs) {
                var lastOpenedCommentId = crs.getId();
                assert lastOpenedCommentId != null;
                log.debug("Comment {} opened.", lastOpenedCommentId);
                commentIds.add(lastOpenedCommentId);
                openedCommentsIds.add(lastOpenedCommentId);
            }
            else if (commentElement instanceof CommentRangeEnd cre) {
                var lastClosedCommentId = cre.getId();
                assert lastClosedCommentId != null;
                log.debug("Comment {} closed.", lastClosedCommentId);
                commentIds.add(lastClosedCommentId);

                var lastOpenedCommentId = openedCommentsIds.pollLast();
                if (!lastClosedCommentId.equals(lastOpenedCommentId)) {
                    log.debug("Comment {} is closing just after comment {} starts.",
                            lastClosedCommentId,
                            lastOpenedCommentId);
                    throw new OfficeStamperException("Cannot figure which comment contains the other !");
                }
            }
            else if (commentElement instanceof R.CommentReference cr) {
                var commentId = cr.getId();
                assert commentId != null;
                log.debug("Comment {} referenced.", commentId);
                commentIds.add(commentId);
            }
        }

        log.debug("These comments have been opened, but never closed: {}", openedCommentsIds);
        var malformedCommentIds = new ArrayList<>(openedCommentsIds);

        var mainDocumentPart = document.getMainDocumentPart();
        Set<BigInteger> writtenCommentsId = Optional.ofNullable(mainDocumentPart.getCommentsPart())
                                                    .map(RemoveMalformedComments::tryGetCommentsPart)
                                                    .map(Comments::getComment)
                                                    .orElse(Collections.emptyList())
                                                    .stream()
                                                    .filter(c -> !isEmpty(c))
                                                    .map(CTMarkup::getId)
                                                    .collect(toSet());

        commentIds.removeAll(writtenCommentsId);

        log.debug("These comments have been referenced in body, but have no related content: {}", commentIds);
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
