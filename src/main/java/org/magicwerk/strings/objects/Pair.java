package org.magicwerk.strings.objects;

import java.util.Collection;
import java.util.function.Function;

/**
 * Class {@link Pair} stores 2 objects of the same type.
 * <p>
 * Use {@link Tuple} for different types.
 */
public class Pair<E> extends IArray2<E, E, E> {

	public static <U> Pair<U> of(U e0, U e1) {
		// Reason for offering a static constructor is to offer a short way for creation ("Pair.of" vs "new Pair<>")
		return new Pair<>(e0, e1);
	}

	public Pair() {
	}

	public Pair(E item0, E item1) {
		super(item0, item1);
	}

	public Pair(Collection<E> coll) {
		init(coll);
	}

	/**
	 * Swap the two entries.
	 */
	public void swap() {
		E item = item0;
		item0 = item1;
		item1 = item;
	}

	/** Convert {@link Pair} using the specified {@link Function} for both items */
	public static <T, U> Pair<U> convert(Pair<T> pair, Function<T, U> mapper) {
		U u0 = (pair.getItem0() != null) ? mapper.apply(pair.getItem0()) : null;
		U u1 = (pair.getItem1() != null) ? mapper.apply(pair.getItem1()) : null;
		return Pair.of(u0, u1);
	}
}