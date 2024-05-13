package pro.verron.officestamper.test;

import java.util.Objects;

/**
 * <p>NullishContext class.</p>
 *
 * @author Joseph Verron
 * @version ${version}
 * @since 1.6.6
 */
public final class NullishContext {
    private String fullish_value;
    private SubContext fullish;
    private String nullish_value;
    private SubContext nullish;

    /**
     * <p>Constructor for NullishContext.</p>
     */
    public NullishContext() {
    }

    /**
     * <p>Constructor for NullishContext.</p>
     *
     * @param fullish_value a {@link java.lang.String} object
     * @param fullish       a {@link SubContext} object
     * @param nullish_value a {@link java.lang.String} object
     * @param nullish       a {@link SubContext} object
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
     * <p>Getter for the field <code>fullish_value</code>.</p>
     *
     * @return a {@link java.lang.String} object
     */
    public String getFullish_value() {
        return fullish_value;
    }

    /**
     * <p>Setter for the field <code>fullish_value</code>.</p>
     *
     * @param fullish_value a {@link java.lang.String} object
     */
    public void setFullish_value(String fullish_value) {
        this.fullish_value = fullish_value;
    }

    /**
     * <p>Getter for the field <code>fullish</code>.</p>
     *
     * @return a {@link SubContext} object
     */
    public SubContext getFullish() {
        return fullish;
    }

    /**
     * <p>Setter for the field <code>fullish</code>.</p>
     *
     * @param fullish a {@link SubContext} object
     */
    public void setFullish(SubContext fullish) {
        this.fullish = fullish;
    }

    /**
     * <p>Getter for the field <code>nullish_value</code>.</p>
     *
     * @return a {@link java.lang.String} object
     */
    public String getNullish_value() {
        return nullish_value;
    }

    /**
     * <p>Setter for the field <code>nullish_value</code>.</p>
     *
     * @param nullish_value a {@link java.lang.String} object
     */
    public void setNullish_value(String nullish_value) {
        this.nullish_value = nullish_value;
    }

    /**
     * <p>Getter for the field <code>nullish</code>.</p>
     *
     * @return a {@link SubContext} object
     */
    public SubContext getNullish() {
        return nullish;
    }

    /**
     * <p>Setter for the field <code>nullish</code>.</p>
     *
     * @param nullish a {@link SubContext} object
     */
    public void setNullish(SubContext nullish) {
        this.nullish = nullish;
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (NullishContext) obj;
        return Objects.equals(this.fullish_value,
                              that.fullish_value) && Objects.equals(
                this.fullish, that.fullish) && Objects.equals(
                this.nullish_value, that.nullish_value) && Objects.equals(
                this.nullish, that.nullish);
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return Objects.hash(fullish_value, fullish, nullish_value, nullish);
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "NullishContext[" + "fullish_value=" + fullish_value + ", " + "fullish=" + fullish + ", " + "nullish_value=" + nullish_value + ", " + "nullish=" + nullish + ']';
    }

}
