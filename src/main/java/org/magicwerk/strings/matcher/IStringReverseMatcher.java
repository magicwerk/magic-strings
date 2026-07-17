package org.magicwerk.strings.matcher;

import org.magicwerk.strings.match.IMatch;

/**
 * Interface {@link IStringReverseMatcher} extends {@link IStringMatcher} with the ability to search backwards.
 */
public interface IStringReverseMatcher extends IStringMatcher {

	/**
	 * Searches backwards for occurrence of text in a string and returns the first match.
	 *
	 * @param str	string to search in
	 * @param end	end position where search starts in reverse direction
	 * @return	 	match where the first occurrence is found or null if not found
	 */
	IMatch findReverse(CharSequence str, int end);

	/**
	 * Searches backwards for occurrence of text in a string and returns the first match.
	 *
	 * @param str	string to search in
	 * @return	 	match where the first occurrence is found or null if not found
	 */
	default IMatch findReverse(CharSequence str) {
		return findReverse(str, Integer.MAX_VALUE);
	}

	/**
	 * Searches backwards for occurrence of text in a string and returns index of first match.
	 *
	 * @param str	string to search in
	 * @return	 	index of match where the last occurrence is found or -1 if not found
	 */
	default int indexOfReverse(CharSequence str) {
		return indexOfReverse(str, Integer.MAX_VALUE);
	}

	/**
	 * Searches backwards for occurrence of text in a string and returns index of first match.
	 *
	 * @param str	string to search in
	 * @param end	end position where search starts in reverse direction
	 * @return	 	index of match where the last occurrence is found or -1 if not found
	 */
	default int indexOfReverse(CharSequence str, int end) {
		IMatch match = findReverse(str, end);
		return (match != null) ? match.getStart() : -1;
	}

	/**
	 * Searches backwards for occurrence of text in a string and returns index of first match.
	 *
	 * @param str	string to search in
	 * @return	 	index of match where the last occurrence is found or -1 if not found
	 */
	default int indexOfEndReverse(CharSequence str) {
		return indexOfEndReverse(str, Integer.MAX_VALUE);
	}

	/**
	 * Searches backwards for occurrence of text in a string and returns index of first match.
	 *
	 * @param str	string to search in
	 * @param end	end position where search starts in reverse direction
	 * @return	 	index of match where the last occurrence is found or -1 if not found
	 */
	int indexOfEndReverse(CharSequence str, int end);

}
