package pro.verron.docxstamper.preset.resolver;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.R;
import org.wickedsource.docxstamper.api.DocxStamperException;
import org.wickedsource.docxstamper.replace.typeresolver.image.Image;
import org.wickedsource.docxstamper.util.RunUtil;
import pro.verron.docxstamper.api.ObjectResolver;

import static org.docx4j.openpackaging.parts.WordprocessingML.BinaryPartAbstractImage.createImagePart;

/**
 * This {@link ObjectResolver} allows context objects to return objects of
 * type {@link Image}. An expression that resolves to an {@link Image}
 * object will be replaced by an actual image in the resulting .docx document.
 * The image will be put as an inline into the surrounding paragraph of text.
 *
 * @author Joseph Verron
 * @version ${version}
 * @since 1.6.7
 */
class ImageResolver
        implements ObjectResolver {

    /**
     * Resolves an image and adds it to a {@link WordprocessingMLPackage}
     * document.
     *
     * @param document The WordprocessingMLPackage document
     * @param image    The image to be resolved and added
     * @return The run containing the added image
     * @throws DocxStamperException If an error occurs while adding the image to the document
     */
    public R resolve(WordprocessingMLPackage document, Image image) {
        try {
            // TODO: adding the same image twice will put the image twice into the docx-zip file. make the second
            //       addition of the same image a reference instead.
            return RunUtil.createRunWithImage(
                    image.getMaxWidth(),
                    createImagePart(document, image.getImageBytes())
            );
        } catch (Exception e) {
            throw new DocxStamperException(
                    "Error while adding image to document!",
                    e);
        }
    }

    @Override
    public R resolve(
            WordprocessingMLPackage document,
            String expression,
            Object object
    ) {
        if (object instanceof Image image)
            return resolve(document, image);
        String message = "Expected %s to be an Image".formatted(object);
        throw new DocxStamperException(message);
    }

    @Override
    public boolean canResolve(Object object) {
        return object instanceof Image;
    }
}
