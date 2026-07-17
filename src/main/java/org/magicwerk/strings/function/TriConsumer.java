package org.magicwerk.strings.function;

import java.util.function.BiConsumer;

/**
 * Interface {@link TriConsumer} provides an interface for a consumer taking 3 arguments.
 * Such an interface is missing in the JDK, see {@link BiConsumer}.
 */
@FunctionalInterface
public interface TriConsumer<T, U, V> {
	/**
	* Applies this function to the given arguments.
	*
	* @param t the first function argument
	* @param u the second function argument
	* @param v the third function argument
	*/
	void accept(T t, U u, V v);
}