package pro.verron.officestamper.preset.resolvers.image;

/// Deprecated compatibility shim: this type moved to
/// [pro.verron.officestamper.docx.preset.resolvers.image.ImageResolver] in
/// the 4.0 two-module split. Update the import to the new location.
///
/// @deprecated use [pro.verron.officestamper.docx.preset.resolvers.image.ImageResolver] instead.
@Deprecated(forRemoval = true)
@SuppressWarnings("squid:S2176") // intentional binary-compatibility shim: keeps the legacy simple name
public class ImageResolver
        extends pro.verron.officestamper.docx.preset.resolvers.image.ImageResolver {

    public ImageResolver(boolean deduplicate) {
        super(deduplicate);
    }
}
