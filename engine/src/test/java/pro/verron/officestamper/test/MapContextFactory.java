package pro.verron.officestamper.test;

import pro.verron.officestamper.preset.Image;
import pro.verron.officestamper.preset.StampTable;

import java.time.temporal.Temporal;
import java.util.*;
import java.util.stream.IntStream;

import static java.util.Arrays.stream;

public final class MapContextFactory
        implements ContextFactory {

    @Override
    public Object units(Image... images) {
        return Map.of("units",
                Arrays.stream(images)
                      .map(coverImage -> Map.of("coverImage", coverImage))
                      .map(productionFacility -> Map.of("productionFacility", productionFacility))
                      .toList());
    }

    /// TODO make a Simpsons version
    @Override
    public Object tableContext() {
        var context = new HashMap<String, Object>();

        var firstTable = new ArrayList<>();
        firstTable.add(Map.of("value", "firstTable value1"));
        firstTable.add(Map.of("value", "firstTable value2"));

        var secondTable = new ArrayList<>();
        secondTable.add(Map.of("value", "repeatDocPart value1"));
        secondTable.add(Map.of("value", "repeatDocPart value2"));
        secondTable.add(Map.of("value", "repeatDocPart value3"));

        var thirdTable = new ArrayList<>();
        thirdTable.add(Map.of("value", "secondTable value1"));
        thirdTable.add(Map.of("value", "secondTable value2"));
        thirdTable.add(Map.of("value", "secondTable value3"));
        thirdTable.add(Map.of("value", "secondTable value4"));

        context.put("firstTable", firstTable);
        context.put("secondTable", secondTable);
        context.put("thirdTable", thirdTable);
        return context;
    }

    /// TODO make a Simpsons version
    @Override
    public Object subDocPartContext() {
        var context = new HashMap<String, Object>();
        var subDocParts = new ArrayList<Map<String, Object>>();

        var firstPart = new HashMap<String, Object>();
        firstPart.put("name", "first doc part");
        subDocParts.add(firstPart);

        var secondPart = new HashMap<String, Object>();
        secondPart.put("name", "second doc part");
        subDocParts.add(secondPart);

        context.put("subDocParts", subDocParts);
        return context;
    }

    @Override
    public Object spacy() {
        return Map.of("expressionWithLeadingAndTrailingSpace",
                " Expression ",
                "expressionWithLeadingSpace",
                " Expression",
                "expressionWithTrailingSpace",
                "Expression ",
                "expressionWithoutSpaces",
                "Expression");
    }

    @Override
    public Object show() {
        return Map.of("name",
                "The Simpsons",
                "characters",
                List.of(Map.of("index",
                                1,
                                "indexSuffix",
                                "st",
                                "characterName",
                                "Homer Simpson",
                                "actorName",
                                "Dan Castellaneta"),
                        Map.of("index",
                                2,
                                "indexSuffix",
                                "nd",
                                "characterName",
                                "Marge Simpson",
                                "actorName",
                                "Julie Kavner"),
                        Map.of("index",
                                3,
                                "indexSuffix",
                                "rd",
                                "characterName",
                                "Bart Simpson",
                                "actorName",
                                "Nancy Cartwright"),
                        Map.of("index",
                                4,
                                "indexSuffix",
                                "th",
                                "characterName",
                                "Lisa Simpson",
                                "actorName",
                                "Yeardley Smith"),
                        Map.of("index",
                                5,
                                "indexSuffix",
                                "th",
                                "characterName",
                                "Maggie Simpson",
                                "actorName",
                                "Julie Kavner")));
    }

    /// TODO make a Simpsons version
    @Override
    public Object schoolContext() {
        var grades = new ArrayList<>();
        for (int grade1 = 0; grade1 < 3; grade1++) {
            var classes = new ArrayList<>();
            for (int classroom1 = 0; classroom1 < 3; classroom1++) {
                var students = new ArrayList<>();
                for (int i = 0; i < 5; i++) {
                    students.add(Map.of("number", i, "name", "Bruce·No" + i, "age", 1 + i));
                }
                classes.add(Map.of("number", classroom1, "students", students));
            }
            grades.add(Map.of("number", grade1, "classes", classes));
        }
        return Map.of("schoolName", "South Park Primary School", "grades", grades);
    }

    /// Creates a Characters object from an array of string inputs containing names and actors.
    ///
    /// @param input an array of strings where each pair of strings represents a character's name and actor's name.
    ///
    /// @return a Characters object containing a list of Role objects constructed from the input array.
    @Override
    public Object roles(String... input) {
        var roles = IntStream.iterate(0, i -> i < input.length, i -> i + 2)
                             .mapToObj(i -> Map.of("name", input[i], "actor", (Object) input[i + 1]))
                             .toList();
        return Map.of("characters", roles);
    }

    /// TODO make a Simpsons version
    @Override
    public Object nullishContext() {
        var stringList = List.of("Fullish3", "Fullish4", "Fullish5");
        var subContext = Map.of("value", "Fullish2", "li", stringList);
        Map<String, Object> stringObjectMap = new HashMap<>();
        stringObjectMap.put("fullish_value", "Fullish1");
        stringObjectMap.put("fullish", subContext);
        stringObjectMap.put("nullish_value", null);
        stringObjectMap.put("nullish", null);
        return stringObjectMap;
    }

    /// TODO make a Simpsons version
    @Override
    public Object mapAndReflectiveContext() {
        var context = new HashMap<String, Object>();
        context.put("FLAT_STRING", "Flat string has been resolved");

        var listProp = new ArrayList<>();
        listProp.add(Map.of("value", "first value"));
        listProp.add(Map.of("value", "second value"));
        context.put("OBJECT_LIST_PROP", listProp);

        return context;
    }

    /// Represents the context for an insertable image.
    @Override
    public Object image(Image image) {
        return Map.of("monalisa", image);
    }

    @Override
    public Object date(Temporal date) {
        return Map.of("date", date);
    }

    /// TODO make a Simpsons version
    @Override
    public Object coupleContext() {
        var context = new HashMap<String, Object>();
        var name1 = Map.of("name", "Homer");
        var name2 = Map.of("name", "Marge");
        var names = List.of(name1, name2);
        context.put("repeatValues", names);
        return context;
    }

    @Override
    public Object characterTable(List<String> headers, List<List<String>> records) {
        return Map.of("characters", new StampTable(headers, records));
    }

    @Override
    public Object names(String... names) {
        return names(List.class, names);
    }

    @Override
    public <T extends Iterable<?>> Object names(Class<T> clazz, String... names) {
        var nameList = stream(names).map(name -> Map.of("name", name))
                                    .toList();
        if (Queue.class.equals(clazz)) return Map.of("names", new ArrayDeque<>(nameList));
        else if (List.class.equals(clazz)) return Map.of("names", new ArrayList<>(nameList));
        else if (Set.class.equals(clazz)) return Map.of("names", new HashSet<>(nameList));
        throw new IllegalStateException("Unexpected value: " + clazz);
    }

    @Override
    public Object name(String name) {
        Map<String, String> map = new HashMap<>();
        map.put("name", name);
        return map;
    }

    @Override
    public Object empty() {
        return Map.of();
    }

    @Override
    public Object sectionName(String firstName, String secondName) {
        return Map.of("firstName", firstName, "secondName", secondName);
    }

    @Override
    public Object imagedName(String name, Image image) {
        return Map.of("name", name, "butterfly", image);
    }

    @Override
    public Object sentence(String sentence) {
        return Map.of("sentence", sentence);
    }
}
