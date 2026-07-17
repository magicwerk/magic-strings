package org.magicwerk.strings.function;

import java.util.Collection;
import java.util.function.Predicate;

/**
 * Class {@link MultiPredicate} allows to test multiple values against a single predicate.
 * The {@link #mode} determines when the true will be returned.
 * Use {@link #testArray} or {@link #testCollection} to evaluate the input.
 */
public class MultiPredicate<T> {

	/** One of ALL, NONE, ANY, ONE, ONE_OR_NONE */
	public enum Mode {
		/** If mode is ALL, all values must evaluate to true for the whole predicate being true */
		ALL,
		/** If mode is NONE, all values must evaluate to false for the whole predicate being true */
		NONE,
		/** If mode is ANY, at least one value must evaluate to true for the whole predicate being true */
		ANY,
		/** If mode is ONE, exactly one value must evaluate to true for the whole predicate being true */
		ONE,
		/** If mode is ONE_OR_NONE, one or none value must evaluate to true for the whole predicate being true */
		ONE_OR_NONE,
	}

	final Mode mode;
	final Predicate<T> predicate;

	public MultiPredicate(Mode mode, Predicate<T> predicate) {
		this.mode = mode;
		this.predicate = predicate;
	}

	public Mode getMode() {
		return mode;
	}

	public Predicate<T> getPredicate() {
		return predicate;
	}

	@SuppressWarnings("unchecked")
	public boolean testArray(T... coll) {
		int count = 0;
		for (T elem : coll) {
			if (predicate.test(elem)) {
				count++;
				if (mode == Mode.NONE) {
					return false;
				} else if (mode == Mode.ANY) {
					return true;
				} else if (mode == Mode.ONE && count > 1) {
					return false;
				} else if (mode == Mode.ONE_OR_NONE && count > 1) {
					return false;
				}
			} else {
				if (mode == Mode.ALL) {
					return false;
				}
			}
		}

		if (mode == Mode.ONE) {
			return count == 1;
		} else if (mode == Mode.ANY) {
			assert count == 0;
			return false;
		} else {
			return true;
		}
	}

	public boolean testCollection(Collection<T> coll) {
		int count = 0;
		for (T elem : coll) {
			if (predicate.test(elem)) {
				count++;
				if (mode == Mode.NONE) {
					return false;
				} else if (mode == Mode.ANY) {
					return true;
				} else if (mode == Mode.ONE && count > 1) {
					return false;
				} else if (mode == Mode.ONE_OR_NONE && count > 1) {
					return false;
				}
			} else {
				if (mode == Mode.ALL) {
					return false;
				}
			}
		}

		if (mode == Mode.ONE) {
			return count == 1;
		} else if (mode == Mode.ANY) {
			assert count == 0;
			return false;
		} else {
			return true;
		}
	}

	@Override
	public String toString() {
		return mode.toString().toLowerCase() + " elements must be " + predicate;
	}
}