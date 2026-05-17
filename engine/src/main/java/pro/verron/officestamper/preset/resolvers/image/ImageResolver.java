package pro.verron.officestamper.preset.resolvers.image;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.jspecify.annotations.Nullable;
import pro.verron.officestamper.api.DocxPart;
import pro.verron.officestamper.api.Insert;
import pro.verron.officestamper.api.ObjectResolver;
import pro.verron.officestamper.api.OfficeStamperException;
import pro.verron.officestamper.preset.Image;
import pro.verron.officestamper.utils.image.ImageRunOptions;
import pro.verron.officestamper.utils.openpackaging.OpenPackage;
import pro.verron.officestamper.utils.openpackaging.OpenpackagingUtils;

import java.util.function.Supplier;

/// This [ObjectResolver] allows context objects to return objects of type [Image]. An expression that resolves to an
/// [Image] object will be replaced by an actual image in the resulting .docx document. The image will be put as an
/// inline into the surrounding paragraph of text.
///
/// @author Joseph Verron
/// @version ${version}
/// @since 1.6.7
public class ImageResolver
        implements ObjectResolver {

    private final boolean deduplicate;

    public ImageResolver(boolean deduplicate) {
        this.deduplicate = deduplicate;
    }

    @Override
    public Insert resolve(DocxPart part, String expression, @Nullable Object object) {
        if (object instanceof Image image) return resolve(part, image);
        String message = "Expected %s to be an Image".formatted(object);
        throw new OfficeStamperException(message);
    }

    @Override
    public boolean canResolve(@Nullable Object object) {
        return object instanceof Image;
    }

    /// Resolves an image and adds it to a [WordprocessingMLPackage] document.
    ///
    /// @param image The image to be resolved and added
    ///
    /// @return The run containing the added image
    ///
    /// @throws OfficeStamperException If an error occurs while adding the image to the document
    private Insert resolve(DocxPart part, Image image) {
        try {
            var document = part.document();
            var imagePart = part.part();
            var openPackage = OpenPackage.getOrCreate(document, imagePart);
            var supplier = (Supplier<byte[]>) image::getBytes;
            var altText = image.getAltText();
            var filenameHint = image.getFilenameHint();
            var maxWidth = image.getMaxWidth()
                                .orElse(null);
            var imageOptions = new ImageRunOptions(altText, filenameHint, maxWidth, deduplicate);
            var imageRun = OpenpackagingUtils.newImageRun(openPackage, supplier, imageOptions);
            return new Insert(imageRun);
        } catch (Exception e) {
            throw new OfficeStamperException("Error while adding image to document!", e);
        }
    }

}
