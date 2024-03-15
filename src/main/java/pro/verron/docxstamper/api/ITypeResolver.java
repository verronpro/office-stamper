package pro.verron.docxstamper.api;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.R;

public interface ITypeResolver<T> {
    /**
     * This method is called when an expression is found in the .docx template.
     * It creates an object of the DOCX4J api in the place of the found expression.
     *
     * @param document         the Word document that can be accessed via the
     *                         DOCX4J api.
     * @param expressionResult the result of an expression. Only objects of classes this type resolver is registered for
     *                         within the TypeResolverRegistry are passed into this method.
     * @return an object of the DOCX4J api (usually of type {@link R} = "run
     * of text" that will be put in the place of an
     * expression found in the .docx document.
     */
    R resolve(WordprocessingMLPackage document, T expressionResult);
}
