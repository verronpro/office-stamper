package pro.verron.officestamper.preset.resolvers.image;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.R;
import org.springframework.lang.Nullable;
import pro.verron.officestamper.api.DocxPart;
import pro.verron.officestamper.api.ObjectResolver;
import pro.verron.officestamper.api.OfficeStamperException;
import pro.verron.officestamper.preset.Image;

/// This [ObjectResolver] allows context objects to return objects of
/// type [Image]. An expression that resolves to an [Image]
/// object will be replaced by an actual image in the resulting .docx document.
/// The image will be put as an inline into the surrounding paragraph of text.
///
/// @author Joseph Verron
/// @version ${version}
/// @since 1.6.7
public class ImageResolver
        implements ObjectResolver {

    @Override
    public boolean canResolve(@Nullable Object object) {
        return object instanceof Image;
    }

    @Override
    public R resolve(
            DocxPart document,
            String expression,
            Object object
    ) {
        if (object instanceof Image image)
            return resolve(document, image);
        String message = "Expected %s to be an Image".formatted(object);
        throw new OfficeStamperException(message);
    }

    /// Resolves an image and adds it to a [WordprocessingMLPackage]
    /// document.
    ///
    /// @param image The image to be resolved and added
    ///
    /// @return The run containing the added image
    ///
    /// @throws OfficeStamperException If an error occurs while adding the image to the document
    private R resolve(DocxPart document, Image image) {
        try {
            return image.newRun(document, "dummyFileName", "dummyAltText");
        } catch (Exception e) {
            throw new OfficeStamperException("Error while adding image to document!", e);
        }
    }
}
