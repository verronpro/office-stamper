package pro.verron.officestamper.test;

import pro.verron.officestamper.preset.Image;
import pro.verron.officestamper.preset.StampTable;

import java.time.temporal.Temporal;
import java.util.*;
import java.util.stream.IntStream;

import static java.util.Arrays.stream;

/// ContextFactory class.
///
/// @author Joseph Verron
/// @version ${version}
/// @since 1.6.5
public final class ObjectContextFactory
        implements ContextFactory {

    @Override
    public Object units(Image... images) {
        record Facility(Image coverImage) {}
        record Unit(Facility productionFacility) {}
        record Units(List<Unit> units) {}
        return new Units(stream(images).map(Facility::new)
                                       .map(Unit::new)
                                       .toList());
    }

    // TODO make a Simpsons version
    @Override
    public Object tableContext() {
        record TableValue(String value) {}
        record TableHolder(
                List<TableValue> firstTable, List<TableValue> secondTable, Iterable<TableValue> thirdTable
        ) {}
        var firstTable = new ArrayList<TableValue>();
        firstTable.add(new TableValue("firstTable value1"));
        firstTable.add(new TableValue("firstTable value2"));

        var secondTable = new ArrayList<TableValue>();
        secondTable.add(new TableValue("repeatDocPart value1"));
        secondTable.add(new TableValue("repeatDocPart value2"));
        secondTable.add(new TableValue("repeatDocPart value3"));

        List<TableValue> thirdTable = new ArrayList<>();
        thirdTable.add(new TableValue("secondTable value1"));
        thirdTable.add(new TableValue("secondTable value2"));
        thirdTable.add(new TableValue("secondTable value3"));
        thirdTable.add(new TableValue("secondTable value4"));
        return new TableHolder(firstTable, secondTable, thirdTable);
    }

    // TODO make a Simpsons version
    @Override
    public Object subDocPartContext() {
        record Named(String name) {}
        record SubDocPartHolder(List<Named> subDocParts) {}
        return new SubDocPartHolder(List.of(new Named("first doc part"), new Named("second doc part")));
    }

    @Override
    public Object spacy() {
        class SpacyContext {
            public String getExpressionWithLeadingAndTrailingSpace() {
                return " Expression ";
            }

            public String getExpressionWithLeadingSpace() {
                return " Expression";
            }

            public String getExpressionWithTrailingSpace() {
                return "Expression ";
            }

            public String getExpressionWithoutSpaces() {
                return "Expression";
            }
        }
        return new SpacyContext();
    }

    @Override
    public Object show() {
        record CharacterRecord(int index, String indexSuffix, String characterName, String actorName) {}
        record Show(String name, List<CharacterRecord> characters) {}
        return new Show("The Simpsons",
                List.of(new CharacterRecord(1, "st", "Homer Simpson", "Dan Castellaneta"),
                        new CharacterRecord(2, "nd", "Marge Simpson", "Julie Kavner"),
                        new CharacterRecord(3, "rd", "Bart Simpson", "Nancy Cartwright"),
                        new CharacterRecord(4, "th", "Lisa Simpson", "Yeardley Smith"),
                        new CharacterRecord(5, "th", "Maggie Simpson", "Julie Kavner")));
    }

    // TODO make a Simpsons version
    @Override
    public Object schoolContext() {
        record Student(int number, String name, int age) {}
        record AClass(int number, List<Student> students) {}
        record Grade(int number, List<AClass> classes) {}
        record SchoolContext(String schoolName, List<Grade> grades) {}
        List<Grade> grades = new ArrayList<>();
        for (int grade1 = 0; grade1 < 3; grade1++) {
            var classes = new ArrayList<AClass>();
            for (int classroom1 = 0; classroom1 < 3; classroom1++) {
                var students = new ArrayList<Student>();
                for (int i = 0; i < 5; i++) {
                    students.add(new Student(i, "Bruce·No" + i, 1 + i));
                }
                classes.add(new AClass(classroom1, students));
            }
            grades.add(new Grade(grade1, classes));
        }
        return new SchoolContext("South Park Primary School", grades);
    }

    /// Creates a Characters object from an array of string inputs containing names and actors.
    ///
    /// @param input an array of strings where each pair of strings represents a character's name and actor's name.
    ///
    /// @return a Characters object containing a list of Role objects constructed from the input array.
    @Override
    public Object roles(String... input) {
        record Role(String name, String actor) {}
        record Characters(List<Role> characters) {}
        var roles = IntStream.iterate(0, i -> i < input.length, i -> i + 2)
                             .mapToObj(i -> new Role(input[i], input[i + 1]))
                             .toList();
        return new Characters(roles);
    }

    // TODO make a Simpsons version
    @Override
    public Object nullishContext() {
        record SubContext(String value, List<String> li) {
            /// Add an empty constructor to respect the Bean interface contract
            /// Insert a new ArrayList() instead of null or Collections emptyList to permit Spel autofill
            /// capacities.
            public SubContext() {
                this(null, new ArrayList<>());
            }
        }
        final class NullishContext {
            @SuppressWarnings("java:S116") // The nameof the variable is directly used in the corresponding template.
            private String fullish_value;
            private SubContext fullish;
            @SuppressWarnings("java:S116") // The nameof the variable is directly used in the corresponding template.
            private String nullish_value;
            private SubContext nullish;

            public NullishContext() {this(null, null, null, null);}


            @SuppressWarnings("java:S117")
                // The nameof the variable is directly used in the corresponding template.
                // TODO: implement stamping of name regardless of case
            NullishContext(
                    String fullish_value, SubContext fullish,
                    String nullish_value, SubContext nullish
            ) {
                this.fullish_value = fullish_value;
                this.fullish = fullish;
                this.nullish_value = nullish_value;
                this.nullish = nullish;
            }

            public String getFullish_value() {return fullish_value;}

            @SuppressWarnings("java:S117") // The nameof the variable is directly used in the corresponding template.
            public void setFullish_value(String fullish_value) {this.fullish_value = fullish_value;}

            public SubContext getFullish() {return fullish;}

            public void setFullish(SubContext fullish) {this.fullish = fullish;}

            public String getNullish_value() {return nullish_value;}

            @SuppressWarnings("java:S117") // The nameof the variable is directly used in the corresponding template.
            public void setNullish_value(String nullish_value) {this.nullish_value = nullish_value;}

            public SubContext getNullish() {return nullish;}

            public void setNullish(SubContext nullish) {this.nullish = nullish;}
        }
        var stringList = List.of("Fullish3", "Fullish4", "Fullish5");
        var subContext = new SubContext("Fullish2", stringList);
        return new NullishContext("Fullish1", subContext, null, null);
    }

    // TODO make a Simpsons version
    @Override
    public Object mapAndReflectiveContext() {
        record Container(String value) {}
        record MappyContext(String FLAT_STRING, List<Container> OBJECT_LIST_PROP) {}
        return new MappyContext("Flat string has been resolved",
                List.of(new Container("first value"), new Container("second value")));
    }

    /// Represents the context for an insertable image.
    @Override
    public Object image(Image image) {
        record ImageContext(Image monalisa) {}
        return new ImageContext(image);
    }

    @Override
    public Object date(Temporal date) {
        record DateContext(Temporal date) {}
        return new DateContext(date);
    }

    // TODO make a Simpsons version
    @Override
    public Object coupleContext() {
        record Name(String name) {}
        record MapHolder(List<Name> repeatValues) {}
        return new MapHolder(List.of(new Name("Homer"), new Name("Marge")));
    }

    @Override
    public Object characterTable(List<String> headers, List<List<String>> records) {
        record TableContext(StampTable characters) {}
        return new TableContext(new StampTable(headers, records));
    }

    @Override
    public Object names(String... names) {
        return names(List.class, names);
    }

    @Override
    public <T extends Iterable<?>> Object names(Class<T> clazz, String... names) {
        record Name(String name) {}
        record Names(Iterable<Name> names) {}
        var nameList = stream(names).map(Name::new)
                                    .toList();
        if (Queue.class.equals(clazz)) return new Names(new ArrayDeque<>(nameList));
        else if (List.class.equals(clazz)) return new Names(new ArrayList<>(nameList));
        else if (Set.class.equals(clazz)) return new Names(new HashSet<>(nameList));
        throw new IllegalStateException("Unexpected value: " + clazz);
    }

    @Override
    public Object name(String name) {
        record Name(String name) {}
        return new Name(name);
    }

    @Override
    public Object empty() {
        return new Object();
    }

    @Override
    public Object sectionName(String firstName, String secondName) {
        record NamesContext(String firstName, String secondName) {}
        return new NamesContext(firstName, secondName);
    }

    @Override
    public Object imagedName(String name, Image image) {
        record ImagedName(String name, Image butterfly) {}
        return new ImagedName(name, image);
    }
}
