package org.magicwerk.strings.matcher;

import org.magicwerk.strings.CharSequenceTools;
import org.magicwerk.strings.chars.CodePointTools;
import org.magicwerk.strings.chars.CharCaseTools.CodePointEqual;
import org.magicwerk.strings.match.IMatch;
import org.magicwerk.strings.matcher.AnyCharMatcher.IndexedMatch;

/**
 * Class {@link AnyCodePointMatcher} searches for the occurrence of any of the specified code points.
 * It returns a match as {@link IndexedMatch} indicating which character matched.
 */
public class AnyCodePointMatcher implements StringStartsEndsMatcher {

	public static class AnyCodePointIgnoreCaseMatcher extends AnyCodePointMatcher {

		final CodePointEqual equal;

		public AnyCodePointIgnoreCaseMatcher(String anyChar, CodePointEqual equal) {
			super(anyChar);

			this.equal = equal;
		}

		@Override
		int matchIndex(int c) {
			return CharSequenceTools.doIndexOf(anyCodePoint, c, 0, equal);
		}
	}

	//

	final String anyCodePoint;

	public AnyCodePointMatcher(String anyChar) {
		this.anyCodePoint = anyChar;
	}

	/** Getter for {@link #anyCodePoint} */
	public String getAnyCodePoint() {
		return anyCodePoint;
	}

	@Override
	public int indexOf(CharSequence str, int start) {
		if (start < 0) {
			start = 0;
		}
		int len = str.length();
		for (int i = start; i < len; i++) {
			int mi = matchIndex(str.charAt(i));
			if (mi != -1) {
				return i;
			}
		}
		return -1;
	}

	int matchIndex(int c) {
		return CharSequenceTools.indexOf(anyCodePoint, c);
	}

	@Override
	public int indexOfEndReverse(CharSequence str, int end) {
		end = Math.min(end, str.length());
		while (end > 0) {
			end--;
			int mi = matchIndex(str.charAt(end));
			if (mi != -1) {
				return end + 1;
			}
		}
		return -1;
	}

	@Override
	public IndexedMatch find(CharSequence str, int start) {
		if (start < 0) {
			start = 0;
		}
		int len = str.length();
		for (int i = start; i < len; i++) {
			int mi = matchIndex(str.charAt(i));
			if (mi != -1) {
				return new IndexedMatch(str, i, i + 1, mi);
			}
		}
		return null;
	}

	@Override
	public IndexedMatch findReverse(CharSequence str, int end) {
		end = Math.min(end, str.length());
		while (end > 0) {
			end--;
			int mi = matchIndex(str.charAt(end));
			if (mi != -1) {
				return new IndexedMatch(str, end, end + 1, mi);
			}
		}
		return null;
	}

	@Override
	public IMatch matchStartingAt(CharSequence str, int start) {
		if (start < 0 || start >= str.length()) {
			return null;
		}

		int mi = matchIndex(str.charAt(start));
		if (mi == -1) {
			return null;
		}

		int cp = CodePointTools.codePointAt(str, start);
		int len = CodePointTools.charCount(cp);
		return indexToMatch(start, str, len);
	}

	@Override
	public IMatch matchEndingAt(CharSequence str, int end) {
		if (end <= 0 || end > str.length()) {
			return null;
		}

		int mi = matchIndex(str.charAt(end - 1));
		if (mi == -1) {
			return null;
		}

		int cp = CodePointTools.codePointBefore(str, end);
		int len = CodePointTools.charCount(cp);
		return indexToMatch(end - len, str, len);
	}

	@Override
	public String toString() {
		return "Match any code point: " + anyCodePoint;
	}

}
