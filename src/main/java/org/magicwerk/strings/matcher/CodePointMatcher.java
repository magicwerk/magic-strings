package org.magicwerk.strings.matcher;

import org.magicwerk.strings.CharSequenceTools;
import org.magicwerk.strings.match.Match;

/**
 * Class {@link CodePointMatcher} searches for the occurrence of the specified code point.
 */
public class CodePointMatcher implements StringFixedLenMatcher {

	int findCodePoint;
	int charCount;

	public CodePointMatcher(int searchCodePoint) {
		this.findCodePoint = searchCodePoint;
		this.charCount = Character.charCount(searchCodePoint);
	}

	/** Getter for {@link #findCodePoint} */
	public int getFindCodePoint() {
		return findCodePoint;
	}

	@Override
	public int indexOf(CharSequence str, int start) {
		return CharSequenceTools.indexOf(str, findCodePoint, start);
	}

	@Override
	public int indexOfEndReverse(CharSequence str, int start) {
		return CharSequenceTools.reverseIndexOf(str, findCodePoint, start);
	}

	@Override
	public Match find(CharSequence str, int start) {
		int index = indexOf(str, start);
		return indexToMatch(index, str, getMatchLength());
	}

	@Override
	public Match findReverse(CharSequence str, int start) {
		int index = indexOfReverse(str, start);
		return indexToMatch(index, str, getMatchLength());
	}

	@Override
	public int getMatchLength() {
		return charCount;
	}

	@Override
	public boolean matchAt(CharSequence str, int index) {
		return CharSequenceTools.startsAt(str, findCodePoint, index);
	}

	@Override
	public String toString() {
		return "Match codepoint: " + findCodePoint;
	}

}
