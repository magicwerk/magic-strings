package org.magicwerk.strings.matcher;

import org.magicwerk.strings.CharSequenceTools;
import org.magicwerk.strings.match.Match;

/**
 * Class {@link CharMatcher} searches for the occurrence of the specified literal character.
 */
public class CharMatcher implements StringFixedLenMatcher {

	final char findChar;

	public CharMatcher(char findChar) {
		this.findChar = findChar;
	}

	/** Getter for {@link #findChar} */
	public char getFindChar() {
		return findChar;
	}

	@Override
	public int indexOf(CharSequence str, int start) {
		return CharSequenceTools.indexOf(str, findChar, start);
	}

	@Override
	public int indexOfEndReverse(CharSequence str, int end) {
		return CharSequenceTools.reverseIndexOf(str, findChar, end);
	}

	@Override
	public Match find(CharSequence str, int start) {
		int index = indexOf(str, start);
		return indexToMatch(index, str, 1);
	}

	@Override
	public Match findReverse(CharSequence str, int start) {
		int index = indexOfReverse(str, start);
		return indexToMatch(index, str, 1);
	}

	@Override
	public boolean matchAt(CharSequence str, int index) {
		return CharSequenceTools.startsAt(str, findChar, index);
	}

	@Override
	public int getMatchLength() {
		return 1;
	}

	@Override
	public String toString() {
		return "Match char: " + findChar;
	}

}
