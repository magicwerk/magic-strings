package org.magicwerk.strings.chars;

/**
 * Predicate interface for type char.
 */
public class CharPredicateTools {

	public static boolean containsAny(String str, CharPredicate predicate) {
		for (int i = 0; i < str.length(); i++) {
			if (predicate.test(str.charAt(i))) {
				return true;
			}
		}
		return false;
	}

	public static boolean containsAll(String str, CharPredicate predicate) {
		for (int i = 0; i < str.length(); i++) {
			if (!predicate.test(str.charAt(i))) {
				return false;
			}
		}
		return true;
	}

}