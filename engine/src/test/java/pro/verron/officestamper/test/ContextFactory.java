package pro.verron.officestamper.test;

import pro.verron.officestamper.preset.Image;

import java.time.temporal.Temporal;
import java.util.List;

public sealed interface ContextFactory
        permits ObjectContextFactory, MapContextFactory {
    static ContextFactory objectContextFactory() {return new ObjectContextFactory();}

    static ContextFactory mapContextFactory() {return new MapContextFactory();}

    Object units(Image... images);

    Object tableContext();

    Object subDocPartContext();

    Object spacy();

    Object show();

    Object schoolContext();

    Object roles(String... input);

    Object nullishContext();

    Object mapAndReflectiveContext();

    Object image(Image image);

    Object date(Temporal date);

    Object coupleContext();

    Object characterTable(List<String> headers, List<List<String>> records);

    Object names(String... names);

    <T extends Iterable<?>> Object names(Class<T> clazz, String... names);

    Object name(String name);

    Object empty();

    Object sectionName(String firstName, String secondName);

    Object imagedName(String name, Image image);

    Object sentence(String sentence);
}
