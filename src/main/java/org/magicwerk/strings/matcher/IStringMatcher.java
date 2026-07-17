package org.magicwerk.strings.matcher;

import org.magicwerk.strings.match.IMatch;
import org.magicwerk.strings.match.Match;

/**
 * Interface {@link IStringMatcher} searches for occurrence of text in a string and returns the first match.
 */
public interface IStringMatcher {

	// Note that the interface must allow to specify a start position to implement features like find next occurrence. 
	// This is different from creating a substring before find() is called for matches with anchors.
	// Example:
	// - str = "aba", matcher = "^a"
	// - matcher.find(str, 2): no match 
	// - matcher.find(str.substring(2)): match

	/**
	 * Searches for occurrence of text in a string and returns the first match.
	 *
	 * @param str	string to search in
	 * @param start	start position where search starts
	 * @return	 	match where the first occurrence is found or null if not found
	 */
	IMatch find(CharSequence str, int start);

	/**
	 * Searches for occurrence of text in a string and returns the first match.
	 *
	 * @param str	string to search in
	 * @return	 	match where the first occurrence is found or null if not found
	 */
	default IMatch find(CharSequence str) {
		return find(str, 0);
	}

	/**
	 * Searches for occurrence of text in a string and returns start index of the first match.
	 *
	 * @param str	string to search in
	 * @return	 	index of match where the first occurrence is found or -1 if not found
	 */
	default int indexOf(CharSequence str) {
		return indexOf(str, 0);
	}

	/**
	 * Searches for occurrence of text in a string and returns start index of the first match.
	 *
	 * @param str	CharSequence to search in
	 * @param start	start position where search starts
	 * @return	 	index of match where the first occurrence is found or -1 if not found
	 */
	default int indexOf(CharSequence str, int start) {
		IMatch match = find(str, start);
		return (match != null) ? match.getStart() : -1;
	}

	/**
	 * Searches for occurrence of text in a string and returns the index of the first match.
	 *
	 * @param str	CharSequence to search in
	 * @return	 	index of match where the first occurrence is found or -1 if not found
	 */
	default int indexOfEnd(CharSequence str) {
		return indexOfEnd(str, 0);
	}

	/**
	 * Searches for occurrence of text in a string and returns the index of the first match.
	 *
	 * @param str	string to search in
	 * @param start	start position where search starts
	 * @return	 	index of match where the first occurrence is found or -1 if not found
	 */
	default int indexOfEnd(CharSequence str, int start) {
		IMatch match = find(str, start);
		return (match != null) ? match.getEnd() : -1;
	}

	//

	// FIXME move to helper class
	default Match indexToMatch(int index, CharSequence str, int len) {
		if (index == -1) {
			return null;
		} else {
			return new Match(str, index, index + len);
		}
	}

	default int getEmptyPos(CharSequence str, int pos) {
		if (pos < 0) {
			return 0;
		}
		int strLen = str.length();
		if (pos > strLen) {
			return strLen;
		}
		return pos;
	}

}
