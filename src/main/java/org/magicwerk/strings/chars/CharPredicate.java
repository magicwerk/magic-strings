package org.magicwerk.strings.chars;

import java.util.Objects;

/**
 * Interface {@link CharPredicate} defines a predicate interface for type char.
 */
public interface CharPredicate {

	/**
	 * Test if character matches predicate.
	 * 
	 * @param c	character to test
	 * @return true if character matches, false otherwise
	 */
	boolean test(char c);

	default CharPredicate and(CharPredicate other) {
		Objects.requireNonNull(other);
		return (t) -> test(t) && other.test(t);
	}

	default CharPredicate negate() {
		return (t) -> !test(t);
	}

	default CharPredicate or(CharPredicate other) {
		Objects.requireNonNull(other);
		return (t) -> test(t) || other.test(t);
	}

}