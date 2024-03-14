package pro.verron.docxstamper.test.utils;

import java.util.function.Supplier;

/**
 * <p>ThrowingSupplier interface.</p>
 *
 * @since 1.6.5
 * @author Joseph Verron
 * @version ${version}
 */
public interface ThrowingSupplier<T> extends Supplier<T> {
	/**
	 * <p>get.</p>
	 *
	 * @return a T object
	 * @since 1.6.6
	 */
	default T get() {
		try {
			return throwingGet();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * <p>throwingGet.</p>
	 *
	 * @return a T object
	 * @throws java.lang.Exception if any.
	 * @since 1.6.6
	 */
	T throwingGet() throws Exception;
}
