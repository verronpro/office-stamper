package pro.verron.docxstamper.api;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.*;

import java.util.List;
import java.util.Set;

public interface CommentWrapper {
    ContentAccessor getParent();

    List<Object> getRepeatElements();

    WordprocessingMLPackage getSubTemplate(WordprocessingMLPackage document) throws Exception;

    WordprocessingMLPackage tryBuildingSubtemplate(WordprocessingMLPackage document);

    CommentRangeEnd getCommentRangeEnd();

    void setCommentRangeEnd(CommentRangeEnd commentRangeEnd);

    CommentRangeStart getCommentRangeStart();

    void setCommentRangeStart(CommentRangeStart commentRangeStart);

    R.CommentReference getCommentReference();

    void setCommentReference(R.CommentReference commentReference);

    Set<CommentWrapper> getChildren();

    void setChildren(Set<CommentWrapper> commentWrappers);

    Comments.Comment getComment();

    void setComment(Comments.Comment comment);
}
