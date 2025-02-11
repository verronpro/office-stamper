package pro.verron.officestamper.test;

import java.util.function.Supplier;

/// ThrowingSupplier interface.
///
/// @author Joseph Verron
/// @version ${version}
/// @since 1.6.5
public interface ThrowingSupplier<T> extends Supplier<T> {

	/// @return a T object
	///
	/// @since 1.6.6
	default T get() {
		try {
			return throwingGet();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/// @return a T object
	/// @throws java.lang.Exception if any.
	/// @since 1.6.6
	T throwingGet() throws Exception;
}
