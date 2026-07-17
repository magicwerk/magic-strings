/*
 * Copyright 2014 by Thomas Mauch
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * $Id$
 */
package org.magicwerk.strings.chars;

import java.util.Arrays;
import java.util.function.IntPredicate;

import org.magicwerk.strings.chars.CharCaseTools.CharEqual;

/**
 * Class {@link CharPredicates} contains standard predicates implementing {@link CharPredicate}.
 */
public class CharPredicates {

	public static final CharPredicate whitespace = c -> Character.isWhitespace(c);

	public static final CharPredicate noWhitespace = c -> !Character.isWhitespace(c);

	public static CharPredicate of(char c) {
		return new OneChar(c);
	}

	public static CharPredicate of(char c1, char c2) {
		return new TwoChars(c1, c2);
	}

	public static CharPredicate of(char c1, char c2, char c3) {
		return new ThreeChars(c1, c2, c3);
	}

	public static CharPredicate of(IntPredicate cp) {
		return c -> cp.test(c);
	}

	//

	public static class NoChar implements CharPredicate {

		public static final NoChar INSTANCE = new NoChar();

		NoChar() {
		}

		@Override
		public boolean test(char c) {
			return false;
		}
	}

	public static class OneChar implements CharPredicate {

		final char ch;

		public OneChar(char ch) {
			this.ch = ch;
		}

		@Override
		public boolean test(char c) {
			return c == ch;
		}
	}

	public static class NotOneChar implements CharPredicate {

		final char ch;

		public NotOneChar(char ch) {
			this.ch = ch;
		}

		@Override
		public boolean test(char c) {
			return c != ch;
		}
	}

	public static class OneCharIgnoreCase implements CharPredicate {

		final char ch;
		final CharEqual equal;

		public OneCharIgnoreCase(char ch, CharEqual equal) {
			this.ch = ch;
			this.equal = equal;
		}

		@Override
		public boolean test(char c) {
			return equal.isEqualChar(c, ch);
		}
	}

	public static class TwoChars implements CharPredicate {

		final char ch0;
		final char ch1;

		public TwoChars(char ch0, char ch1) {
			this.ch0 = ch0;
			this.ch1 = ch1;
		}

		@Override
		public boolean test(char c) {
			return c == ch0 || c == ch1;
		}
	}

	public static class TwoCharsIgnoreCase implements CharPredicate {

		final char ch0;
		final char ch1;
		final CharEqual equal;

		public TwoCharsIgnoreCase(char ch0, char ch1, CharEqual equal) {
			this.ch0 = ch0;
			this.ch1 = ch1;
			this.equal = equal;
		}

		@Override
		public boolean test(char c) {
			return equal.isEqualChar(c, ch0) || equal.isEqualChar(c, ch1);
		}
	}

	public static class ThreeChars implements CharPredicate {

		final char ch0;
		final char ch1;
		final char ch2;

		public ThreeChars(char ch0, char ch1, char ch2) {
			this.ch0 = ch0;
			this.ch1 = ch1;
			this.ch2 = ch2;
		}

		@Override
		public boolean test(char c) {
			return c == ch0 || c == ch1 || c == ch2;
		}
	}

	public static class ThreeCharsIgnoreCase implements CharPredicate {

		final char ch0;
		final char ch1;
		final char ch2;
		final CharEqual equal;

		public ThreeCharsIgnoreCase(char ch0, char ch1, char ch2, CharEqual equal) {
			this.ch0 = ch0;
			this.ch1 = ch1;
			this.ch2 = ch2;
			this.equal = equal;
		}

		@Override
		public boolean test(char c) {
			return equal.isEqualChar(c, ch0) || equal.isEqualChar(c, ch1) || equal.isEqualChar(c, ch2);
		}
	}

	public static class ManyChars implements CharPredicate {

		final char[] chars;

		public ManyChars(CharSequence chars) {
			this.chars = chars.toString().toCharArray();
			Arrays.sort(this.chars);
		}

		@Override
		public boolean test(char c) {
			return search(c) >= 0;
		}

		public int indexOf(char c) {
			int index = search(c);
			return (index >= 0) ? index : -1;
		}

		int search(char c) {
			return Arrays.binarySearch(chars, c);
		}
	}

	public static class ManyCharsIgnoreCase implements CharPredicate {

		final CharSequence chars;
		final CharEqual equal;

		public ManyCharsIgnoreCase(CharSequence chars, CharEqual equal) {
			this.chars = chars;
			this.equal = equal;
		}

		@Override
		public boolean test(char c) {
			for (int i = 0; i < chars.length(); i++) {
				if (equal.isEqualChar(c, chars.charAt(i))) {
					return true;
				}
			}
			return false;
		}
	}

	/**
	 * Class {@link RangeChars} tests whether a character is in the defined range of characters. 
	 */
	public static class RangeChars implements CharPredicate {

		final char firstChar;
		final char lastChar;

		public RangeChars(char firstChar, char lastChar) {
			this.firstChar = firstChar;
			this.lastChar = lastChar;
		}

		@Override
		public boolean test(char c) {
			return firstChar <= c && c <= lastChar;
		}

		/** Getter for {@link #firstChar} */
		public char getFirstChar() {
			return firstChar;
		}

		/** Getter for {@link #lastChar} */
		public char getLastChar() {
			return lastChar;
		}
	}

	//-- Character specific methods

	public static CharPredicate equals(char c) {
		return new OneChar(c);
	}

	public static CharPredicate equals(char c, CharEqual equal) {
		return new OneCharIgnoreCase(c, equal);
	}

	public static CharPredicate notEquals(char c) {
		return new NotOneChar(c);
	}

	/**
	 * Create {@link CharPredicate} which matches any of the chars respecting case as defined.
	 * Note that this method does not support surrogates, surrogate pairs will be handled as two separate chars.
	 */
	public static CharPredicate oneOf(CharSequence chars) {
		switch (chars.length()) {
		case 0:
			return NoChar.INSTANCE;
		case 1:
			return new OneChar(chars.charAt(0));
		case 2:
			return new TwoChars(chars.charAt(0), chars.charAt(1));
		case 3:
			return new ThreeChars(chars.charAt(0), chars.charAt(1), chars.charAt(2));
		default:
			return new ManyChars(chars);
		}
	}

	/**
	 * Create {@link CharPredicate} which matches any of the chars.
	 * Note that this method does not support surrogates, surrogate pairs will be handled as two separate chars.
	 */
	public static CharPredicate oneOf(CharSequence chars, CharEqual equal) {
		switch (chars.length()) {
		case 0:
			return NoChar.INSTANCE;
		case 1:
			return new OneCharIgnoreCase(chars.charAt(0), equal);
		case 2:
			return new TwoCharsIgnoreCase(chars.charAt(0), chars.charAt(1), equal);
		case 3:
			return new ThreeCharsIgnoreCase(chars.charAt(0), chars.charAt(1), chars.charAt(2), equal);
		default:
			return new ManyCharsIgnoreCase(chars, equal);
		}
	}

	/** Returns the first character matching the predicate, null if none */
	public static Character getFirstMatch(CharPredicate predicate, char start, boolean match) {
		for (char c = start; true; c++) {
			boolean m = predicate.test(c);
			if (m == match) {
				return c;
			}
			if (c == Character.MAX_VALUE) {
				return null;
			}
		}
	}

	/** Returns all characters matching the predicate, empty string if none */
	public static String getAllMatches(CharPredicate predicate, char start, boolean match) {
		StringBuilder buf = new StringBuilder();
		for (char c = start; true; c++) {
			boolean m = predicate.test(c);
			if (m == match) {
				buf.append(c);
			}
			if (c == Character.MAX_VALUE) {
				break;
			}
		}
		return buf.toString();
	}

}
