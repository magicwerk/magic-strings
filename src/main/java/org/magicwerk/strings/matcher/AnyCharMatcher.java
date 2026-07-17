package org.magicwerk.strings.matcher;

import org.magicwerk.strings.CharSequenceTools;
import org.magicwerk.strings.chars.CharCaseTools.CharEqual;
import org.magicwerk.strings.match.Match;

/**
 * Class {@link AnyCharMatcher} searches for the occurrence of any of the specified characters.
 * It returns a match as {@link IndexedMatch} indicating which character matched.
 */
public class AnyCharMatcher implements StringFixedLenMatcher {

	/**
	 * Class {@link IndexedMatch} extends {@link Match} with information about the number of the match.
	 */
	public static class IndexedMatch extends Match {

		int index;

		public IndexedMatch(CharSequence input, int start, int end, int index) {
			super(input, start, end);
			this.index = index;
		}

		public int getIndex() {
			return index;
		}
	}

	//

	public static class AnyCharIgnoreCaseMatcher extends AnyCharMatcher {

		final CharEqual equal;

		public AnyCharIgnoreCaseMatcher(String anyChar, CharEqual equal) {
			super(anyChar);

			this.equal = equal;
		}

		@Override
		int matchIndex(char c) {
			return CharSequenceTools.indexOf(anyChar, c, 0, equal);
		}
	}

	//

	final String anyChar;

	public AnyCharMatcher(String anyChar) {
		this.anyChar = anyChar;
	}

	/** Getter for {@link #anyChar} */
	public String getAnyChar() {
		return anyChar;
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

	int matchIndex(char c) {
		return CharSequenceTools.indexOf(anyChar, c);
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
	public boolean matchAt(CharSequence str, int index) {
		if (index < 0 || index >= str.length()) {
			return false;
		}

		return matchIndex(str.charAt(index)) != -1;
	}

	@Override
	public int getMatchLength() {
		return 1;
	}

	@Override
	public String toString() {
		return "Match any char: " + anyChar;
	}

}
