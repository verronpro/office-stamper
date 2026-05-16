package pro.verron.officestamper.imageio.wmf.utils;

import pro.verron.officestamper.preset.Image;

/// ContextFactory class.
///
/// @author Joseph Verron
/// @version ${version}
/// @since 1.6.5
/// Factory for creating object-based contexts.
public final class ObjectContextFactory
        implements ContextFactory {

    /// Default constructor.
    public ObjectContextFactory() {
    }

    /// Represents the context for an insertable image.
    @Override
    public Object image(Image image) {
        record ImageContext(Image monalisa) {}
        return new ImageContext(image);
    }

    @Override
    public Object name(String name) {
        record Name(String name) {}
        return new Name(name);
    }

}
