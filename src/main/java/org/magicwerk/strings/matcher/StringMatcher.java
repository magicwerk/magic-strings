package org.magicwerk.strings.matcher;

import org.magicwerk.strings.CharSequenceTools;
import org.magicwerk.strings.chars.CharCaseTools.CharEqual;
import org.magicwerk.strings.chars.CharCaseTools.CodePointEqual;
import org.magicwerk.strings.match.Match;

/**
 * Class {@link StringMatcher} searches for the occurrence of the specified literal string.
 */
public class StringMatcher implements StringFixedLenMatcher {

	public static StringMatcher of(CharSequence find) {
		if (find.length() == 0) {
			return new EmptyStringMatcher();
		} else {
			return new StringMatcher(find);
		}
	}

	public static StringMatcher of(CharSequence match, CharEqual equal) {
		if (match.length() == 0) {
			return new EmptyStringMatcher();
		} else {
			return new StringIgnoreCaseCharMatcher(match, equal);
		}
	}

	public static StringMatcher of(CharSequence match, CodePointEqual equal) {
		if (match.length() == 0) {
			return new EmptyStringMatcher();
		} else {
			return new StringIgnoreCaseCodePointMatcher(match, equal);
		}
	}

	/**
	 * Class {@link StringIgnoreCaseCharMatcher} searches for the occurrence of the specified literal string ignoring case by character.
	 */
	public static class StringIgnoreCaseCharMatcher extends StringMatcher {

		final CharEqual equal;

		StringIgnoreCaseCharMatcher(CharSequence match, CharEqual charEqual) {
			super(match);

			this.equal = charEqual;
		}

		@Override
		public int indexOf(CharSequence str, int start) {
			return CharSequenceTools.indexOf(str, searchStr, start, equal);
		}

		@Override
		public int indexOfEndReverse(CharSequence str, int end) {
			return CharSequenceTools.reverseIndexOf(str, searchStr, end, equal);
		}

		@Override
		public boolean matchAt(CharSequence str, int start) {
			return CharSequenceTools.startsAt(str, searchStr, start, equal);
		}

		@Override
		public String toString() {
			return "Match string ignore case: " + searchStr;
		}
	}

	/**
	 * Class {@link StringIgnoreCaseCodePointMatcher} searches for the occurrence of the specified literal string ignoring case by code point.
	 */
	public static class StringIgnoreCaseCodePointMatcher extends StringMatcher {

		final CodePointEqual equal;

		StringIgnoreCaseCodePointMatcher(CharSequence match, CodePointEqual equal) {
			super(match);

			this.equal = equal;
		}

		@Override
		public int indexOf(CharSequence str, int start) {
			return CharSequenceTools.indexOf(str, searchStr, start, equal);
		}

		@Override
		public int indexOfEndReverse(CharSequence str, int end) {
			return CharSequenceTools.reverseIndexOf(str, searchStr, end, equal);
		}

		@Override
		public boolean matchAt(CharSequence str, int start) {
			return CharSequenceTools.startsAt(str, searchStr, start, equal);
		}

		@Override
		public String toString() {
			return "Match string ignore case: " + searchStr;
		}
	}

	/**
	 * Class {@link EmptyStringMatcher} searches for the occurrence of an empty string.
	 * An empty string matches at each valid position.
	 */
	public static class EmptyStringMatcher extends StringMatcher {

		/** Create {@link EmptyStringMatcher} */
		public EmptyStringMatcher() {
			super("");
		}

		@Override
		public int indexOf(CharSequence str, int start) {
			return getEmptyPos(str, start);
		}

		@Override
		public int indexOfEndReverse(CharSequence str, int end) {
			return getEmptyPos(str, end);
		}

		@Override
		public Match find(CharSequence str, int start) {
			int pos = getEmptyPos(str, start);
			return new Match(str, pos, pos);
		}

		@Override
		public Match findReverse(CharSequence str, int end) {
			int pos = getEmptyPos(str, end);
			return new Match(str, pos, pos);
		}

		@Override
		public boolean matchAt(CharSequence str, int index) {
			return true;
		}

		@Override
		public int getMatchLength() {
			return 0;
		}

		// IStringStartsAtMatcher

		@Override
		public boolean startsAt(CharSequence str, int start) {
			return start == getEmptyPos(str, start);
		}

		// IStringEndsAtMatcher

		@Override
		public boolean endsAt(CharSequence str, int end) {
			return end == getEmptyPos(str, end);
		}
	}

	//

	CharSequence searchStr;

	//

	StringMatcher(CharSequence searchStr) {
		this.searchStr = searchStr;
	}

	/** Getter for {@link #searchStr} */
	public CharSequence getSearchStr() {
		return searchStr;
	}

	@Override
	public int indexOf(CharSequence str, int start) {
		return CharSequenceTools.indexOf(str, searchStr, start);
	}

	@Override
	public int indexOfEndReverse(CharSequence str, int end) {
		return CharSequenceTools.reverseIndexOf(str, searchStr, end);
	}

	@Override
	public Match find(CharSequence str, int start) {
		int index = indexOf(str, start);
		return indexToMatch(index, str, searchStr.length());
	}

	@Override
	public Match findReverse(CharSequence str, int end) {
		int index = indexOfReverse(str, end);
		int len = searchStr.length();
		return indexToMatch(index, str, len);
	}

	@Override
	public int getMatchLength() {
		return searchStr.length();
	}

	@Override
	public boolean matchAt(CharSequence str, int index) {
		return CharSequenceTools.startsAt(str, searchStr, index);
	}

	@Override
	public String toString() {
		return "Match string: " + searchStr;
	}

}
