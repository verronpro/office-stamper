package pro.verron.officestamper.test;

/// Functions class.
///
/// @author Joseph Verron
/// @version ${version}
/// @since 1.6.6
public class Functions {

    /// Returns an implementation of the UppercaseFunction interface.
    /// The implementation converts a string to uppercase.
    ///
    /// @return an implementation of UppercaseFunction interface
    public static UppercaseFunction upperCase(){
        return String::toUpperCase;
    }

    /// The UppercaseFunction interface defines a method for converting a string to uppercase.
    public interface UppercaseFunction {

        /// Converts the given string to uppercase.
        ///
        /// @param string the string to be converted to uppercase
        ///
        /// @return the uppercase representation of the given string
        String toUppercase(String string);
    }
}
