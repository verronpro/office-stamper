package pro.verron.docxstamper.preset.resolver;

import org.docx4j.dml.wordprocessingDrawing.Inline;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPartAbstractImage;
import org.docx4j.wml.Drawing;
import org.docx4j.wml.ObjectFactory;
import org.docx4j.wml.R;
import org.wickedsource.docxstamper.api.DocxStamperException;
import org.wickedsource.docxstamper.replace.typeresolver.image.Image;
import pro.verron.docxstamper.api.ObjectResolver;

import java.util.Random;

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
public class ImageResolver
        implements ObjectResolver {

    private static final Random random = new Random();

    /**
     * Creates a run containing the given image.
     *
     * @param maxWidth      max width of the image
     * @param abstractImage the image
     * @return the run containing the image
     */
    public static R createRunWithImage(
            Integer maxWidth,
            BinaryPartAbstractImage abstractImage
    ) {
        // creating random ids assuming they are unique
        // id must not be too large, otherwise Word cannot open the document
        int id1 = random.nextInt(100000);
        int id2 = random.nextInt(100000);
        var filenameHint = "dummyFileName";
        var altText = "dummyAltText";

        Inline inline = tryCreateImageInline(filenameHint,
                                             altText,
                                             maxWidth,
                                             abstractImage,
                                             id1,
                                             id2);

        // Now add the inline in w:p/w:r/w:drawing
        ObjectFactory factory = new ObjectFactory();
        R run = factory.createR();
        Drawing drawing = factory.createDrawing();
        run.getContent()
                .add(drawing);
        drawing.getAnchorOrInline()
                .add(inline);

        return run;

    }

    private static Inline tryCreateImageInline(
            String filenameHint,
            String altText,
            Integer maxWidth,
            BinaryPartAbstractImage abstractImage,
            int id1,
            int id2
    ) {
        try {
            return maxWidth == null
                    ? abstractImage.createImageInline(filenameHint,
                                                      altText,
                                                      id1,
                                                      id2,
                                                      false)
                    : abstractImage.createImageInline(filenameHint,
                                                      altText,
                                                      id1,
                                                      id2,
                                                      false,
                                                      maxWidth);
        } catch (Exception e) {
            throw new DocxStamperException(e);
        }
    }

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
            return createRunWithImage(
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
