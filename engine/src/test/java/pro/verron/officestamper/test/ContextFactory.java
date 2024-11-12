package pro.verron.officestamper.test;

import pro.verron.officestamper.preset.Image;
import pro.verron.officestamper.preset.StampTable;

import java.time.temporal.Temporal;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.Arrays.stream;

/**
 * <p>ContextFactory class.</p>
 *
 * @author Joseph Verron
 * @version ${version}
 * @since 1.6.5
 */
public class ContextFactory {

    /**
     * <p>tableContext.</p>
     * TODO make an object version
     * TODO make a Simpsons version
     *
     * @return a {@link HashMap} object
     *
     * @since 1.6.6
     */
    public Object tableContext() {
        var context = new HashMap<String, Object>();

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

        context.put("firstTable", firstTable);
        context.put("secondTable", secondTable);
        context.put("thirdTable", thirdTable);
        return context;
    }

    /**
     * <p>subDocPartContext.</p>
     * TODO make an object version
     * TODO make a Simpsons version
     *
     * @return a {@link HashMap} object
     *
     * @since 1.6.6
     */
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

    public Object spacy() {
        return new SpacyContext();
    }

    public Object show() {
        return new Show("The Simpsons",
                List.of(new CharacterRecord(1, "st", "Homer Simpson", "Dan Castellaneta"),
                        new CharacterRecord(2, "nd", "Marge Simpson", "Julie Kavner"),
                        new CharacterRecord(3, "rd", "Bart Simpson", "Nancy Cartwright"),
                        new CharacterRecord(4, "th", "Lisa Simpson", "Yeardley Smith"),
                        new CharacterRecord(5, "th", "Maggie Simpson", "Julie Kavner")));
    }

    /**
     * <p>schoolContext.</p>
     * TODO make a Simpsons version
     *
     * @return a {@link SchoolContext} object
     *
     * @since 1.6.6
     */
    public Object schoolContext() {
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

    /**
     * Creates a Characters object from an array of string inputs containing names and actors.
     *
     * @param input an array of strings where each pair of strings represents a character's name and actor's name.
     *
     * @return a Characters object containing a list of Role objects constructed from the input array.
     */
    public Object roles(String... input) {
        var roles = IntStream.iterate(0, i -> i < input.length, i -> i + 2)
                             .mapToObj(i -> new Role(input[i], input[i + 1]))
                             .toList();
        return new Characters(roles);
    }

    /**
     * <p>nullishContext.</p>
     * TODO make an object version
     * TODO make a Simpsons version
     *
     * @return a {@link NullishContext} object
     *
     * @since 1.6.6
     */
    public Object nullishContext() {
        var stringList = List.of("Fullish3", "Fullish4", "Fullish5");
        var subContext = new SubContext("Fullish2", stringList);
        return new NullishContext("Fullish1", subContext, null, null);
    }

    /**
     * <p>mapAndReflectiveContext.</p>
     * TODO make an object version
     * TODO make a Simpsons version
     *
     * @return a {@link HashMap} object
     *
     * @since 1.6.6
     */
    public Object mapAndReflectiveContext() {
        var context = new HashMap<String, Object>();
        context.put("FLAT_STRING", "Flat string has been resolved");

        var listProp = new ArrayList<Container>();
        listProp.add(new Container("first value"));
        listProp.add(new Container("second value"));
        context.put("OBJECT_LIST_PROP", listProp);

        return context;
    }

    /**
     * Represents the context for an insertable image.
     */
    public Object image(Image image) {
        return new ImageContext(image);
    }

    public Object date(Temporal date) {
        return new DateContext(date);
    }

    /**
     * <p>coupleContext.</p>
     * TODO make an object version
     * TODO make a Simpsons version
     *
     * @return a {@link Map} object
     *
     * @since 1.6.6
     */
    public Object coupleContext() {
        Map<String, Object> context = new HashMap<>();

        Name name1 = new Name("Homer");
        Name name2 = new Name("Marge");

        List<Name> repeatValues = new ArrayList<>();
        repeatValues.add(name1);
        repeatValues.add(name2);

        context.put("repeatValues", repeatValues);
        return context;
    }

    public Object characterTable(List<String> headers, List<List<String>> records) {
        return new TableContext(new StampTable(headers, records));
    }

    /**
     * <p>names.</p>
     *
     * @param names a {@link String} object
     *
     * @return a {@link Object} object
     *
     * @since 1.6.6
     */
    public Object names(String... names) {
        record Name(String name) {}
        record Names(List<Name> names) {}
        var nameList = stream(names).map(Name::new)
                                    .collect(Collectors.toCollection(ArrayList::new));
        return new Names(nameList);
    }

    /**
     * <p>name.</p>
     *
     * @param name a {@link String} object
     *
     * @return a {@link Object} object
     *
     * @since 1.6.6
     */
    public Object name(String name) {
        record Name(String name) {}
        return new Name(name);
    }

    /**
     * <p>empty.</p>
     *
     * @return a {@link Object} object
     *
     * @since 1.6.6
     */
    public Object empty() {
        return new Object();
    }

    private record Role(String name, String actor) {}

    /**
     * The Characters class represents a list of characters played by actors.
     */
    private record Characters(List<Role> characters) {}

    private record ZonedDateContext(java.time.ZonedDateTime date) {}

    /**
     * A static inner class representing a Spacy context.
     */
    private static class SpacyContext {
        private final String expressionWithLeadingAndTrailingSpace = " Expression ";
        private final String expressionWithLeadingSpace = " Expression";
        private final String expressionWithTrailingSpace = "Expression ";
        private final String expressionWithoutSpaces = "Expression";

        /**
         * Retrieves the expression with leading and trailing spaces.
         *
         * @return The expression with leading and trailing spaces.
         */
        public String getExpressionWithLeadingAndTrailingSpace() {
            return " Expression ";
        }

        /**
         * Retrieves the expression with a leading space.
         *
         * @return The expression with a leading space.
         */
        public String getExpressionWithLeadingSpace() {
            return " Expression";
        }

        /**
         * Retrieves the expression with a trailing space.
         *
         * @return The expression with a trailing space.
         */
        public String getExpressionWithTrailingSpace() {
            return "Expression ";
        }

        /**
         * Retrieves the expression without spaces.
         *
         * @return The expression without spaces.
         */
        public String getExpressionWithoutSpaces() {
            return "Expression";
        }
    }

    private record ImageContext(Image monalisa) {}

    private record Container(String value) {}

    private static final class NullishContext {
        private String fullish_value;
        private SubContext fullish;
        private String nullish_value;
        private SubContext nullish;

        /**
         * Represents a NullishContext object.
         */
        public NullishContext() {
        }

        /**
         * Represents a NullishContext object.
         *
         * @param fullish_value The value associated with the fullish context.
         * @param fullish       An instance of the SubContext related to the fullish context.
         * @param nullish_value The value associated with the nullish context.
         * @param nullish       An instance of the SubContext related to the nullish context.
         */
        public NullishContext(
                String fullish_value, SubContext fullish, String nullish_value, SubContext nullish
        ) {
            this.fullish_value = fullish_value;
            this.fullish = fullish;
            this.nullish_value = nullish_value;
            this.nullish = nullish;
        }

        /**
         * Returns the value of the fullish_value attribute in the NullishContext object.
         *
         * @return The value of the fullish_value attribute.
         */
        public String getFullish_value() {
            return fullish_value;
        }

        /**
         * Sets the value of the fullish_value attribute in the NullishContext object.
         *
         * @param fullish_value The new value for the fullish_value attribute.
         */
        public void setFullish_value(String fullish_value) {
            this.fullish_value = fullish_value;
        }

        /**
         * Returns the fullish attribute of the NullishContext object.
         *
         * @return The fullish attribute.
         */
        public SubContext getFullish() {
            return fullish;
        }

        /**
         * Sets the fullish attribute of the NullishContext object.
         *
         * @param fullish The new value for the fullish attribute.
         */
        public void setFullish(SubContext fullish) {
            this.fullish = fullish;
        }

        /**
         * Returns the value of the nullish_value attribute in the NullishContext object.
         *
         * @return The value of the nullish_value attribute.
         */
        public String getNullish_value() {
            return nullish_value;
        }

        /**
         * Sets the value of the nullish_value attribute in the NullishContext object.
         *
         * @param nullish_value The new value for the nullish_value attribute.
         */
        public void setNullish_value(String nullish_value) {
            this.nullish_value = nullish_value;
        }

        /**
         * Returns the nullish attribute of the NullishContext object.
         *
         * @return The nullish attribute.
         */
        public SubContext getNullish() {
            return nullish;
        }

        /**
         * Sets the nullish attribute of the NullishContext object.
         *
         * @param nullish The new value for the nullish attribute.
         */
        public void setNullish(SubContext nullish) {
            this.nullish = nullish;
        }

        @Override public int hashCode() {
            return Objects.hash(fullish_value, fullish, nullish_value, nullish);
        }

        @Override public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass()) return false;
            var that = (NullishContext) obj;
            return Objects.equals(this.fullish_value, that.fullish_value) && Objects.equals(this.fullish, that.fullish)
                   && Objects.equals(this.nullish_value, that.nullish_value) && Objects.equals(this.nullish,
                    that.nullish);
        }

        @Override public String toString() {
            return "NullishContext[" + "fullish_value=" + fullish_value + ", " + "fullish=" + fullish + ", "
                   + "nullish_value=" + nullish_value + ", " + "nullish=" + nullish + ']';
        }

    }

    private static final class SubContext {
        private String value;
        private List<String> li;

        /**
         * This class represents a SubContext object.
         */
        public SubContext() {
            // DO NOTHING, FOR BEAN SPEC
        }

        /**
         * Creates a SubContext object with the given value and list of strings.
         *
         * @param value The value for the SubContext.
         * @param li    The list of strings for the SubContext.
         */
        public SubContext(
                String value, List<String> li
        ) {
            this.value = value;
            this.li = li;
        }

        /**
         * Returns the value of the SubContext object.
         *
         * @return The value of the SubContext object.
         */
        public String getValue() {
            return value;
        }

        /**
         * Sets the value of the object.
         *
         * @param value The new value to be set.
         */
        public void setValue(String value) {
            this.value = value;
        }

        /**
         * Returns the list of strings in the SubContext object.
         *
         * @return The list of strings in the SubContext object.
         */
        public List<String> getLi() {
            return li;
        }

        /**
         * Sets the list of strings in the SubContext object.
         *
         * @param li The new list of strings to be set.
         */
        public void setLi(List<String> li) {
            this.li = li;
        }

        @Override public int hashCode() {
            return Objects.hash(value, li);
        }

        @Override public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass()) return false;
            var that = (SubContext) obj;
            return Objects.equals(this.value, that.value) && Objects.equals(this.li, that.li);
        }

        @Override public String toString() {
            return "SubContext[" + "value=" + value + ", " + "li=" + li + ']';
        }

    }


    private record SchoolContext(String schoolName, List<Grade> grades) {}

    private record Grade(int number, List<AClass> classes) {}

    private record Show(String name, List<CharacterRecord> characters) {}

    private record CharacterRecord(int index, String indexSuffix, String characterName, String actorName) {}

    private record AClass(int number, List<Student> students) {}

    private record Student(int number, String name, int age) {}

    private record TableValue(String value) {}

    private record Name(String name) {}

    private record TableContext(StampTable characters) {}

    private record DateContext(Temporal date) {}
}
