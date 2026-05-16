package pro.verron.officestamper.imageio.wmf.utils;

import pro.verron.officestamper.preset.Image;

/// Factory for creating test contexts.
public sealed interface ContextFactory
        permits MapContextFactory, ObjectContextFactory {
    /// Returns an object context factory.
    ///
    /// @return an object context factory
    static ContextFactory objectContextFactory() {return new ObjectContextFactory();}

    /// Returns a map context factory.
    ///
    /// @return a map context factory
    static ContextFactory mapContextFactory() {return new MapContextFactory();}

    /// Creates an image context.
    ///
    /// @param image image
    ///
    /// @return image context
    Object image(Image image);

    /// Creates a name context.
    ///
    /// @param name name
    ///
    /// @return name context
    Object name(String name);
}
