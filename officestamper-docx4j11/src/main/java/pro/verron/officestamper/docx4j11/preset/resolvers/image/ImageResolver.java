package pro.verron.officestamper.docx4j11.preset.resolvers.image;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.JaxbXmlPart;
import org.jspecify.annotations.Nullable;
import pro.verron.officestamper.api.OfficeStamperException;
import pro.verron.officestamper.api.ObjectResolver;
import pro.verron.officestamper.docx4j11.DocxElement;
import pro.verron.officestamper.docx4j11.DocxPart;
import pro.verron.officestamper.spi.Insert;
import pro.verron.officestamper.spi.OfficePart;
import pro.verron.officestamper.preset.Image;
import pro.verron.officestamper.utils.image.ImageRunOptions;
import pro.verron.officestamper.utils.openpackaging.OpenPackage;
import pro.verron.officestamper.utils.openpackaging.OpenpackagingUtils;

import java.util.List;
import java.util.function.Supplier;

/// This [ObjectResolver] allows context objects to return objects of type [Image]. An expression that resolves to an
/// [Image] object will be replaced by an actual image in the resulting .docx document. The image will be put as an
/// inline into the surrounding paragraph of text.
///
/// @author Joseph Verron
/// @since 1.6.7
public class ImageResolver
        implements ObjectResolver {

    private final boolean deduplicate;

    public ImageResolver(boolean deduplicate) {
        this.deduplicate = deduplicate;
    }

    @Override
    public Insert resolve(OfficePart part, String expression, @Nullable Object object) {
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
    /// @return The run containing the added image
    /// @throws OfficeStamperException If an error occurs while adding the image to the document
    private Insert resolve(OfficePart part, Image image) {
        if (!(part instanceof DocxPart docxPart)) throw new OfficeStamperException(
                "OfficePart is not a docx4j part: " + part.getClass().getName());
        try {
            var document = part.document().unwrap(WordprocessingMLPackage.class);
            var imagePart = part.unwrap(JaxbXmlPart.class);
            var openPackage = OpenPackage.getOrCreate(document, imagePart);
            var supplier = (Supplier<byte[]>) image::getBytes;
            var altText = image.getAltText();
            var filenameHint = image.getFilenameHint();
            var maxWidth = image.getMaxWidth()
                                .orElse(null);
            var imageOptions = new ImageRunOptions(altText, filenameHint, maxWidth, deduplicate);
            var imageRun = OpenpackagingUtils.newImageRun(openPackage, supplier, imageOptions);
            return new Insert(List.of(new DocxElement(imageRun, docxPart)));
        } catch (Exception e) {
            throw new OfficeStamperException("Error while adding image to document!", e);
        }
    }
}
