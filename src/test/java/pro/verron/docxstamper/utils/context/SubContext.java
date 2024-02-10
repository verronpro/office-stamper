package pro.verron.docxstamper.utils.context;

import java.util.List;
import java.util.Objects;

/**
 * <p>SubContext class.</p>
 *
 * @author Joseph Verron
 * @version 1.6.6
 * @since 1.6.6
 */
public final class SubContext {
    private String value;
    private List<String> li;

    /**
     * <p>Constructor for SubContext.</p>
     */
    public SubContext() {
    }

    /**
     * <p>Constructor for SubContext.</p>
     *
     * @param value a {@link java.lang.String} object
     * @param li    a {@link java.util.List} object
     */
    public SubContext(
            String value,
            List<String> li
    ) {
        this.value = value;
        this.li = li;
    }

    /**
     * <p>Getter for the field <code>value</code>.</p>
     *
     * @return a {@link java.lang.String} object
     */
    public String getValue() {
        return value;
    }

    /**
     * <p>Setter for the field <code>value</code>.</p>
     *
     * @param value a {@link java.lang.String} object
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * <p>Getter for the field <code>li</code>.</p>
     *
     * @return a {@link java.util.List} object
     */
    public List<String> getLi() {
        return li;
    }

    /**
     * <p>Setter for the field <code>li</code>.</p>
     *
     * @param li a {@link java.util.List} object
     */
    public void setLi(List<String> li) {
        this.li = li;
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (SubContext) obj;
        return Objects.equals(this.value, that.value) &&
               Objects.equals(this.li, that.li);
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return Objects.hash(value, li);
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "SubContext[" +
               "value=" + value + ", " +
               "li=" + li + ']';
    }

}
