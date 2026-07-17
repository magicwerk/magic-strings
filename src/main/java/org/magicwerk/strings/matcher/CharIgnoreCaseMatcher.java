package org.magicwerk.strings.matcher;

import org.magicwerk.strings.CharSequenceTools;
import org.magicwerk.strings.chars.CharCaseTools.CharEqual;

/**
 * Class {@link CharIgnoreCaseMatcher} searches for the occurrence of the specified character ignoring case.
 */
public class CharIgnoreCaseMatcher extends CharMatcher {

	final CharEqual equal;

	public CharIgnoreCaseMatcher(char match, CharEqual equal) {
		super(match);

		this.equal = equal;
	}

	@Override
	public int indexOf(CharSequence str, int start) {
		return CharSequenceTools.indexOf(str, findChar, start, equal);
	}

	@Override
	public int indexOfEndReverse(CharSequence str, int end) {
		return CharSequenceTools.reverseIndexOf(str, findChar, end, equal);
	}

	@Override
	public boolean matchAt(CharSequence str, int index) {
		return CharSequenceTools.startsAt(str, findChar, index, equal);
	}

	@Override
	public String toString() {
		return "Match char ignore case: " + findChar;
	}

}
