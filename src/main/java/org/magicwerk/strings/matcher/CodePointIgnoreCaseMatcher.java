package org.magicwerk.strings.matcher;

import org.magicwerk.strings.CharSequenceTools;
import org.magicwerk.strings.chars.CharCaseTools.CodePointEqual;

/**
 * Class {@link CodePointIgnoreCaseMatcher} searches for the occurrence of the specified code point ignoring case.
 */
public class CodePointIgnoreCaseMatcher extends CodePointMatcher {

	final CodePointEqual equal;

	public CodePointIgnoreCaseMatcher(int match, CodePointEqual equal) {
		super(match);

		this.equal = equal;
	}

	@Override
	public int indexOf(CharSequence str, int start) {
		return CharSequenceTools.indexOf(str, findCodePoint, start, equal);
	}

	@Override
	public int indexOfEndReverse(CharSequence str, int end) {
		return CharSequenceTools.reverseIndexOf(str, findCodePoint, end, equal);
	}

	@Override
	public boolean matchAt(CharSequence str, int index) {
		return CharSequenceTools.startsAt(str, findCodePoint, index, equal);
	}

	@Override
	public String toString() {
		return "Match codepoint ignore case: " + findCodePoint;
	}

}
