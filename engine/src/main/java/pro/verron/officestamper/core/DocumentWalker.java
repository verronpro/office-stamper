package pro.verron.officestamper.core;

import org.docx4j.XmlUtils;
import org.docx4j.wml.*;
import org.docx4j.wml.R.CommentReference;
import pro.verron.officestamper.api.DocxPart;

/**
 * This class walks the document and calls abstract methods for each element it encounters.
 * The following elements are supported:
 * <ul>
 * <li>{@link P}</li>
 * <li>{@link R}</li>
 * <li>{@link Tbl}</li>
 * <li>{@link Tr}</li>
 * <li>{@link Tc}</li>
 * <li>{@link CommentRangeStart}</li>
 * <li>{@link CommentRangeEnd}</li>
 * <li>{@link CommentReference}</li>
 * </ul>
 * The following elements are not supported:
 * <ul>
 * <li>{@link SdtBlock}</li>
 * <li>{@link SdtRun}</li>
 * <li>{@link SdtElement}</li>
 * <li>{@link CTSimpleField}</li>
 * <li>{@link CTSdtCell}</li>
 * <li>{@link CTSdtContentCell}</li>
 * </ul>
 *
 * @author Joseph Verron
 * @author Tom Hombergs
 * @version ${version}
 * @since 1.0.0
 */
public abstract class DocumentWalker {

    private final DocxPart source;

    /**
     * Creates a new DocumentWalker that will traverse the given document.
     *
     * @param source the document to traverse.
     */
    protected DocumentWalker(DocxPart source) {
        this.source = source;
    }

    /**
     * Starts the traversal of the document.
     */
    public void walk() {
        for (Object content : source.content()) {
            Object ue = XmlUtils.unwrap(content);
            if (ue instanceof P o) walkParagraph(o);
            else if (ue instanceof R o) walkRun(o);
            else if (ue instanceof Tbl o) walkTable(o);
            else if (ue instanceof Tr o) walkTableRow(o);
            else if (ue instanceof Tc o) walkTableCell(o);
            else if (ue instanceof CommentRangeStart o) onCommentRangeStart(o);
            else if (ue instanceof CommentRangeEnd o) onCommentRangeEnd(o);
            else if (ue instanceof CommentReference o) onCommentReference(o);
        }
    }

    private void walkTable(Tbl table) {
        for (Object contentElement : table.getContent()) {
            Object unwrappedObject = XmlUtils.unwrap(contentElement);
            if (unwrappedObject instanceof Tr row) {
                walkTableRow(row);
            }
        }
    }

    private void walkTableRow(Tr row) {
        for (Object rowContentElement : row.getContent()) {
            Object unwrappedObject = XmlUtils.unwrap(rowContentElement);
            if (unwrappedObject instanceof Tc cell) {
                walkTableCell(cell);
            }
        }
    }

    private void walkTableCell(Tc cell) {
        for (Object cellContentElement : cell.getContent()) {
            Object unwrappedObject = XmlUtils.unwrap(cellContentElement);
            if (unwrappedObject instanceof P) {
                P p = (P) cellContentElement;
                walkParagraph(p);
            }
            else if (unwrappedObject instanceof R) {
                R r = (R) cellContentElement;
                walkRun(r);
            }
            else if (unwrappedObject instanceof Tbl nestedTable) {
                walkTable(nestedTable);
            }
            else if (unwrappedObject instanceof CommentRangeStart commentRangeStart) {
                onCommentRangeStart(commentRangeStart);
            }
            else if (unwrappedObject instanceof CommentRangeEnd commentRangeEnd) {
                onCommentRangeEnd(commentRangeEnd);
            }
        }
    }

    private void walkParagraph(P p) {
        onParagraph(p);
        for (Object element : p.getContent()) {
            Object unwrappedObject = XmlUtils.unwrap(element);
            if (unwrappedObject instanceof R r) {
                walkRun(r);
            }
            else if (unwrappedObject instanceof CommentRangeStart commentRangeStart) {
                onCommentRangeStart(commentRangeStart);
            }
            else if (unwrappedObject instanceof CommentRangeEnd commentRangeEnd) {
                onCommentRangeEnd(commentRangeEnd);
            }
        }
    }

    private void walkRun(R r) {
        for (Object element : r.getContent()) {
            Object unwrappedObject = XmlUtils.unwrap(element);
            if (unwrappedObject instanceof CommentReference commentReference) {
                onCommentReference(commentReference);
            }
        }
    }

    /**
     * This method is called for every {@link P} element in the document.
     *
     * @param paragraph the {@link P} element to process.
     */
    protected abstract void onParagraph(P paragraph);

    /**
     * This method is called for every {@link CommentRangeStart} element in the document.
     *
     * @param commentRangeStart the {@link CommentRangeStart} element to process.
     */
    protected abstract void onCommentRangeStart(CommentRangeStart commentRangeStart);

    /**
     * This method is called for every {@link CommentRangeEnd} element in the document.
     *
     * @param commentRangeEnd the {@link CommentRangeEnd} element to process.
     */
    protected abstract void onCommentRangeEnd(CommentRangeEnd commentRangeEnd);

    /**
     * This method is called for every {@link CommentReference} element in the document.
     *
     * @param commentReference the {@link CommentReference} element to process.
     */
    protected abstract void onCommentReference(CommentReference commentReference);
}
