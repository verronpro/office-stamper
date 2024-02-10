package pro.verron.docxstamper;

/**
 * <p>Functions class.</p>
 *
 * @author Joseph Verron
 * @version 1.6.6
 * @since 1.6.6
 */
public class Functions {

    /**
     * <p>upperCase.</p>
     *
     * @return a {@link pro.verron.docxstamper.Functions.UppercaseFunction} object
     */
    public static UppercaseFunction upperCase(){
        return String::toUpperCase;
    }
    public interface UppercaseFunction {
        String toUppercase(String string);
    }
}
