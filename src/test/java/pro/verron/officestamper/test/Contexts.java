package pro.verron.officestamper.test;

import pro.verron.officestamper.api.Image;
import pro.verron.officestamper.api.StampTable;

import java.util.*;

/**
 * <p>Contexts class.</p>
 *
 * @author Joseph Verron
 * @version ${version}
 * @since 1.6.5
 */
public class Contexts {
    private Contexts() {
        throw new RuntimeException(
                "Static utility class should not be instantiated");
    }

    /**
     * <p>empty.</p>
     *
     * @return a {@link java.lang.Object} object
     * @since 1.6.6
     */
    public static Object empty() {
        record EmptyContext() {
        }
        return new EmptyContext();
    }

    /**
     * <p>name.</p>
     *
     * @param name a {@link java.lang.String} object
     * @return a {@link java.lang.Object} object
     * @since 1.6.6
     */
    public static Object name(String name) {
        record Name(String name) {}
        return new Name(name);
    }

    /**
     * <p>names.</p>
     *
     * @param names a {@link java.lang.String} object
     * @return a {@link java.lang.Object} object
     * @since 1.6.6
     */
    public static Object names(String... names) {
        record Name(String name) {}
        record Names(List<Name> names) {}
        return new Names(Arrays.stream(names)
                                 .map(Name::new)
                                 .toList());
    }

    /**
     * <p>role.</p>
     *
     * @param character a {@link java.lang.String} object
     * @param actor     a {@link java.lang.String} object
     * @return a {@link Contexts.Role} object
     * @since 1.6.6
     */
    public static Role role(String character, String actor) {
        return new Role(character, actor);
    }

    /**
     * <p>roles.</p>
     *
     * @param roles a {@link Contexts.Role} object
     * @return a {@link Contexts.Characters} object
     * @since 1.6.6
     */
    public static Characters roles(Role... roles) {
        return new Characters(List.of(roles));
    }

    /**
     * <p>subDocPartContext.</p>
     *
     * @return a {@link java.util.HashMap} object
     * @since 1.6.6
     */
    public static HashMap<String, Object> subDocPartContext() {
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

    /**
     * <p>schoolContext.</p>
     *
     * @return a {@link Contexts.SchoolContext} object
     * @since 1.6.6
     */
    public static SchoolContext schoolContext() {
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
     * <p>tableContext.</p>
     *
     * @return a {@link java.util.HashMap} object
     * @since 1.6.6
     */
    public static HashMap<String, Object> tableContext() {
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
     * <p>coupleContext.</p>
     *
     * @return a {@link java.util.Map} object
     * @since 1.6.6
     */
    public static Map<String, Object> coupleContext() {
        Map<String, Object> context = new HashMap<>();

        Name name1 = new Name("Homer");
        Name name2 = new Name("Marge");

        List<Name> repeatValues = new ArrayList<>();
        repeatValues.add(name1);
        repeatValues.add(name2);

        context.put("repeatValues", repeatValues);
        return context;
    }

    /**
     * <p>nowContext.</p>
     *
     * @return a {@link Contexts.DateContext} object
     * @since 1.6.6
     */
    public static DateContext nowContext() {
        var now = new Date();
        return new DateContext(now);
    }

    /**
     * <p>mapAndReflectiveContext.</p>
     *
     * @return a {@link java.util.HashMap} object
     * @since 1.6.6
     */
    public static HashMap<String, Object> mapAndReflectiveContext() {
        var listProp = new ArrayList<Container>();
        listProp.add(new Container("first value"));
        listProp.add(new Container("second value"));

        var context = new HashMap<String, Object>();
        context.put("FLAT_STRING", "Flat string has been resolved");
        context.put("OBJECT_LIST_PROP", listProp);
        return context;
    }

    /**
     * <p>nullishContext.</p>
     *
     * @return a {@link Contexts.NullishContext} object
     * @since 1.6.6
     */
    public static NullishContext nullishContext() {
        return new NullishContext(
                "Fullish1",
                new SubContext(
                        "Fullish2",
                        List.of(
                                "Fullish3",
                                "Fullish4",
                                "Fullish5"
                        )
                ),
                null,
                null
        );
    }

    public static TableContext characterTable(
            List<String> headers,
            List<List<String>> records
    ) {
        return new TableContext(new StampTable(
                headers,
                records
        ));
    }

    /**
     * The Role class represents a role played by an actor.
     */
    public record Role(String name, String actor) {
    }

    /**
     * The Characters class represents a list of characters played by actors.
     */
    public record Characters(List<Role> characters) {
    }

    /**
     * Represents a Date context.
     *
     * @param date The Date value to be encapsulated in the context.
     */
    public record DateContext(Date date) {
    }

    /**
     * A static inner class representing a Spacy context.
     */
    public static class SpacyContext {
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

    /**
     * Represents the context for an image that will be inserted into a document.
     */
    public record ImageContext(Image monalisa) {
    }

    record Container(String value) {
    }

    /**
     * This class represents a NullishContext object.
     */
    public static final class NullishContext {
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
                String fullish_value,
                SubContext fullish,
                String nullish_value,
                SubContext nullish
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

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass()) return false;
            var that = (NullishContext) obj;
            return Objects.equals(this.fullish_value, that.fullish_value) &&
                   Objects.equals(this.fullish, that.fullish) &&
                   Objects.equals(this.nullish_value, that.nullish_value) &&
                   Objects.equals(this.nullish, that.nullish);
        }

        @Override
        public int hashCode() {
            return Objects.hash(fullish_value, fullish, nullish_value, nullish);
        }

        @Override
        public String toString() {
            return "NullishContext[" +
                   "fullish_value=" + fullish_value + ", " +
                   "fullish=" + fullish + ", " +
                   "nullish_value=" + nullish_value + ", " +
                   "nullish=" + nullish + ']';
        }

    }

    /**
     * This class represents a SubContext object.
     */
    public static final class SubContext {
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
                String value,
                List<String> li
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

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass()) return false;
            var that = (SubContext) obj;
            return Objects.equals(this.value, that.value) &&
                   Objects.equals(this.li, that.li);
        }

        @Override
        public int hashCode() {
            return Objects.hash(value, li);
        }

        @Override
        public String toString() {
            return "SubContext[" +
                   "value=" + value + ", " +
                   "li=" + li + ']';
        }

    }

    /**
     * Represents the context of a school.
     *
     * @param schoolName the name of the school
     * @param grades     the list of grades in the school
     */
    public record SchoolContext(String schoolName, List<Grade> grades) {}

    /**
     * Represents a grade in a school.
     *
     * @param number  the grade number
     * @param classes the list of classes in the grade
     */
    record Grade(int number, List<AClass> classes) {}

    /**
     * Represents a Show, which is a collection of CharacterRecords.
     *
     * @param characters The list of CharacterRecords in the Show.
     */
    public record Show(List<CharacterRecord> characters) {}

    /**
     * Represents a character record.
     */
    public record CharacterRecord(int index, String indexSuffix, String characterName, String actorName) {}

    /**
     * Represents a character in a movie or play.
     */
    public record Character(String name, String actor) {}

    /**
     * Represents a class.
     */
    public record AClass(int number, List<Student> students) {}

    /**
     * Represents a student.
     *
     * @param number the student number
     * @param name   the student name
     * @param age    the student age
     */
    record Student(int number, String name, int age) {}

    /**
     * Represents a value in a table.
     */
    record TableValue(String value) {
    }

    /**
     * Represents a name.
     */
    public record Name(String name) {}

    /**
     * Represents an empty context.
     */
    public static class EmptyContext {
    }

    /**
     * The {@code Context} class represents a context object that contains a {@code CustomType}.
     * It is used in various contexts within the application.
     *
     * @param name The {@code CustomType} object.
     */
    public record Context(CustomType name) {}

    /**
     * CustomType is a static nested class that represents a custom type.
     * It is used in various contexts within the application.
     */
    public static class CustomType {}

    public record TableContext(StampTable characters) {}
}
