package pro.verron.officestamper.preset.resolvers.image;

/// Deprecated compatibility shim: this type moved to
/// [pro.verron.officestamper.docx4j11.preset.resolvers.image.ImageResolver] in
/// the 3.9 two-module split. Update the import to the new location.
///
/// @deprecated use [pro.verron.officestamper.docx4j11.preset.resolvers.image.ImageResolver] instead.
@Deprecated(forRemoval = true)
public class ImageResolver
        extends pro.verron.officestamper.docx4j11.preset.resolvers.image.ImageResolver {

    public ImageResolver(boolean deduplicate) {
        super(deduplicate);
    }
}
