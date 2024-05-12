package org.wickedsource.docxstamper.api.commentprocessor;

import pro.verron.officestamper.api.CommentProcessor;

/**
 * <p>In a .docx template used by DocxStamper, you can comment paragraphs of text to manipulate them. The comments in
 * the .docx template are passed to an implementation of ICommentProcessor that understands the expression used
 * within the comment. Thus, you can implement your own ICommentProcessor to extend the expression language available
 * in comments in the .docx template.</p>
 * <p>To implement a comment processor, you have to do the following steps:</p>
 * <ol>
 * <li>Create an interface that defines the custom method(s) you want to expose to the expression language used in .docx comments</li>
 * <li>Create an implementation of your interface</li>
 * <li>Register your comment processor with DocxStamper</li>
 * </ol>
 * <p><strong>1. Creating a comment processor interface</strong><br/>
 * For example, if you want to create a comment processor that
 * makes a paragraph of text bold based on some condition, you would create an interface with the method
 * boldIf(boolean condition).</p>
 * <p><strong>2. Creating an implementation of your interface</strong><br/>
 * Your implementation class must also implement the Interface
 * ICommentProcessor. To stay in the above example, when the boldIf method is called, simply keep track of the paragraphs that are to be made bold.
 * The currently processed paragraph is passed into the method setCurrentParagraph() before your own method
 * (in this case boldIf()) is called.
 * Within the method commitChanges() you then do the manipulations on the word document, i.e. make the paragraphs
 * that were commented bold.</p>
 * <p><strong>3. Registering you comment processor with DocxStamper</strong><br/>
 * Register your comment processor in DocxStamper by calling DocxStamperConfiguration#addCommentProcessor().</p>
 *
 * @author Joseph Verron
 * @author Tom Hombergs
 * @version ${version}
 * @since 1.0.0
 * @deprecated since 1.6.8, This class has been deprecated in the effort
 * of the library modularization.
 * It is recommended to use the {@link CommentProcessor} class instead.
 * This class will not be exported in the future releases of the module.
 */
@Deprecated(since = "1.6.8", forRemoval = true)
public interface ICommentProcessor
        extends CommentProcessor {
}
