package pro.verron.officestamper.test.utils;

import pro.verron.officestamper.preset.Image;

import java.time.temporal.Temporal;
import java.util.List;

/// Factory for creating test contexts.
public sealed interface ContextFactory
        permits ObjectContextFactory, MapContextFactory {
    /// Returns an object context factory.
    ///
    /// @return an object context factory
    static ContextFactory objectContextFactory() {return new ObjectContextFactory();}

    /// Returns a map context factory.
    ///
    /// @return a map context factory
    static ContextFactory mapContextFactory() {return new MapContextFactory();}

    /// Creates a units context.
    ///
    /// @param images images
    ///
    /// @return units context
    Object units(Image... images);

    /// Creates a table context.
    ///
    /// @return table context
    Object tableContext();

    /// Creates a sub document part context.
    ///
    /// @return sub document part context
    Object subDocPartContext();

    /// Creates a spacy context.
    ///
    /// @return spacy context
    Object spacy();

    /// Creates a show context.
    ///
    /// @return show context
    Object show();

    /// Creates a school context.
    ///
    /// @return school context
    Object schoolContext();

    /// Creates a roles context.
    ///
    /// @param input roles
    ///
    /// @return roles context
    Object roles(String... input);

    /// Creates a nullish context.
    ///
    /// @return nullish context
    Object nullishContext();

    /// Creates a map and reflective context.
    ///
    /// @return map and reflective context
    Object mapAndReflectiveContext();

    /// Creates an image context.
    ///
    /// @param image image
    ///
    /// @return image context
    Object image(Image image);

    /// Creates a date context.
    ///
    /// @param date date
    ///
    /// @return date context
    Object date(Temporal date);

    /// Creates a couple context.
    ///
    /// @return couple context
    Object coupleContext();

    /// Creates a character table context.
    ///
    /// @param headers headers
    /// @param records records
    ///
    /// @return character table context
    Object characterTable(List<String> headers, List<List<String>> records);

    /// Creates a names context.
    ///
    /// @param names names
    ///
    /// @return names context
    Object names(String... names);

    /// Creates a names context with a specific class.
    ///
    /// @param clazz class
    /// @param names names
    /// @param <T> iterable type
    ///
    /// @return names context
    <T extends Iterable<?>> Object names(Class<T> clazz, String... names);

    /// Creates a name context.
    ///
    /// @param name name
    ///
    /// @return name context
    Object name(String name);

    /// Creates an empty context.
    ///
    /// @return empty context
    Object empty();

    /// Creates a section name context.
    ///
    /// @param firstName first name
    /// @param secondName second name
    ///
    /// @return section name context
    Object sectionName(String firstName, String secondName);

    /// Creates an imaged name context.
    ///
    /// @param name name
    /// @param image image
    ///
    /// @return imaged name context
    Object imagedName(String name, Image image);

    /// Creates a sentence context.
    ///
    /// @param sentence sentence
    ///
    /// @return sentence context
    Object sentence(String sentence);
}
