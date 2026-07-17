package org.magicwerk.strings.objects;

import java.util.Collection;

/**
 * Class {@link Triad} stores 3 objects of the same type.
 * <p>
 * Use {@link Triple} for different types.
 */
public class Triad<E> extends IArray3<E, E, E, E> {

	public static <U> Triad<U> of(U e0, U e1, U e2) {
		return new Triad<>(e0, e1, e2);
	}

	public Triad() {
	}

	public Triad(E item0, E item1, E item2) {
		super(item0, item1, item2);
	}

	public Triad(Collection<E> coll) {
		init(coll);
	}

}