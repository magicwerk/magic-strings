/*
 * Copyright 2011 by Thomas Mauch
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

import java.util.function.IntPredicate;
import java.util.function.IntUnaryOperator;
import java.util.regex.Pattern;

/**
 * Class {@link CharCaseTools} contains utilities for working with characters respecting case.
 */
public class CharCaseTools {

	/** 
	 * Determines whether the string contains a character which is case sensitive (i.e. its value changes by toUpperCase/toLowerCase),
	 * The string is treated as as sequence of chars ignoring surrogates.
	 */
	public static boolean isCaseSensitiveChar(CharSequence str) {
		int len = str.length();
		for (int i = 0; i < len; i++) {
			char c = str.charAt(i);
			if (isCaseSensitiveChar(c)) {
				return true;
			}
		}
		return false;
	}

	/** Determines whether the string contains a code point which is case sensitive (i.e. its value changes by toUpperCase()/toLowerCase()) */
	public static boolean isCaseSensitiveCodePoint(CharSequence str) {
		int len = str.length();
		for (int pos = 0; pos < len;) {
			int cp = CodePointTools.codePointAt(str, pos);
			if (isCaseSensitiveCodePoint(cp)) {
				return true;
			}
			pos += Character.charCount(cp);
		}
		return false;
	}

	/** Determines whether the character is case sensitive (i.e. its value changes by toUpperCase()/toLowerCase()) */
	public static boolean isCaseSensitiveChar(char c) {
		return c != Character.toUpperCase(c) || c != Character.toLowerCase(c);
	}

	/** Determines whether the code point is case sensitive (i.e. its value changes by toUpperCase()/toLowerCase()) */
	public static boolean isCaseSensitiveCodePoint(int cp) {
		return cp != Character.toUpperCase(cp) || cp != Character.toLowerCase(cp);
	}

	public static IntPredicate toCodePointPredicate(CharPredicate cp) {
		return c -> cp.test((char) c);
	}

	public static IntUnaryOperator toCodePointOperator(CharOperator co) {
		return c -> co.apply((char) c);
	}

	//

	public static CharPredicate getCharPredicateIgnoreCase(CharPredicate cp) {
		return c -> cp.test(c) || cp.test(Character.toUpperCase(c)) || cp.test(Character.toLowerCase(c));
	}

	public static IntPredicate getCodePointPredicateIgnoreCase(IntPredicate cp) {
		return c -> cp.test(c) || cp.test(Character.toUpperCase(c)) || cp.test(Character.toLowerCase(c));
	}

	//

	/**
	 * Enum {@link CharCaseMode} determines whether characters are handled in a case sensitive or case insensitive mode.
	 * One of {@link #CASE_SENSITIVE}, {@link #CASE_INSENSITIVE}, {@link #CASE_INSENSITIVE_FAST}.
	 */
	public enum CharCaseMode {
		/**
		 * Character handling is case sensitive, i.e. 'a' and 'A' are different.
		 * Behavior is identical to the standard approach used for Java strings by {@link String#equals}, i.e.
		 * the Java equality operator (==) can be used to compare characters or code points.
		 */
		CASE_SENSITIVE,
		/**
		 * Character handling is case insensitive, i.e. 'a' and 'A' are treated as equal.
		 * Equality is determined by the standard approach used for Java strings which is also used by {@link String#equalsIgnoreCase},
		 * so one of the following comparisons must be equal: <br>
		 * - the two characters <br>
		 * - the uppercase variants of them <br>
		 * - the lowercase variants of the uppercase variants of them <br>
		 */
		CASE_INSENSITIVE,
		/**
		 * Character handling is case insensitive, i.e. 'a' and 'A' are treated as equal. 
		 * Equality is determined by a faster approach than used as standard,
		 * so one of the following comparisons must be equal: <br>
		 * - the two characters <br>
		 * - the uppercase variants of them <br>
		 * This approach can produce different results for alphabets having strange rules about case conversion (e.g. Georgian)
		 */
		CASE_INSENSITIVE_FAST
	}

	/**
	 * Interface {@link ICharMode} configures how character / code points are interpreted. 
	 * {@link CharEqual}, {@link CharPredicateEqual}, {@link CharPredicateEqual}, {@link CodePointPredicateEqual} to handle case insensitive operations.
	 */
	public interface ICharMode {

		/**
		 * Determines whether surrogate pairs are supported to form code points.
		 * If surrogates are not supported, performance of some operations is better, but they can also produce different results.
		 */
		boolean supportCodePoints();

		/**
		 * Determines whether characters are handled in a case sensitive or case insensitive mode.
		 * See {@link CharCaseMode} for details.
		 */
		CharCaseMode getCharCaseMode();

		/** Returns {@link CharEqual} for determining whether two chars are equal */
		CharEqual getCharEqual();

		/** Returns {@link CharEqual} for determining whether two code points are equal */
		CodePointEqual getCodePointEqual();

		// TODO
		CharIndexEqual getCharIndexEqual();
	}

	/**
	 * Enumeration {@link CharMode} is the default implementation of {@link ICharMode}.
	 * It offers standard implementations for handling characters: <br>
	 * - {@link #CS_CODEPOINT} is the standard mode if no other mode is selected (or if setIgnoreCase(false) is called).
	 *   It handles characters in a case sensitive way respecting surrogate pairs forming code points. <br>
	 * - {@link #CI_CODEPOINT} is the standard mode for case insensitive behavior (if setIgnoreCase(true) is called).
	 *   It handles characters in a case insensitive way respecting surrogate pairs forming code points 
	 *   (equality is determined by the standard approach used for Java strings, see {@link CharCaseMode#CASE_INSENSITIVE} <br>
	 */
	public enum CharMode implements ICharMode {

		// Code point

		/** CS_CODEPOINT handles strings as collection of code points, i.e. it recognizes surrogates. Equality is case sensitive. */
		CS_CODEPOINT(true, CharCaseMode.CASE_SENSITIVE,
				CharEqual.isEqualChar(), CodePointEqual.isEqualCodePoint(),
				CharIndexEqual.indexOf()),

		/** CI_CODEPOINT handles strings as collection of code points, i.e. it recognizes surrogates. Equality is case insensitive. */
		CI_CODEPOINT(true, CharCaseMode.CASE_INSENSITIVE,
				CharEqual.isEqualCharIgnoreCase(), CodePointEqual.isEqualCodePointIgnoreCase(),
				CharIndexEqual.of(CharEqual.isEqualCharIgnoreCase())),

		/** 
		 * CI_CODEPOINT_FAST handles strings as collection of code points, i.e. it recognizes surrogates.
		 * Equality is case insensitive with a faster implementation which does not handle all edges correctly.
		 */
		CI_CODEPOINT_FAST(true, CharCaseMode.CASE_INSENSITIVE_FAST,
				CharEqual.isEqualCharIgnoreCaseFast(), CodePointEqual.isEqualCodePointIgnoreCaseFast(),
				CharIndexEqual.of(CharEqual.isEqualCharIgnoreCase())),

		// Char

		/** CS_CHAR handles strings as collection of chars without recognizing surrogates. Equality is case sensitive. */
		CS_CHAR(false, CharCaseMode.CASE_SENSITIVE,
				CharEqual.isEqualChar(), null,
				CharIndexEqual.of(CharEqual.isEqualChar())),

		/** CI_CHAR handles strings as collection of chars without recognizing surrogates. Equality is case insensitive. */
		CI_CHAR(false, CharCaseMode.CASE_INSENSITIVE,
				CharEqual.isEqualCharIgnoreCase(), null,
				CharIndexEqual.of(CharEqual.isEqualCharIgnoreCase())),

		/** 
		 * CI_CHAR_FAST handles strings as collection of chars without recognizing surrogates. 
		 * Equality is case insensitive with a faster implementation which does not handle all edges correctly.
		 */
		CI_CHAR_FAST(false, CharCaseMode.CASE_INSENSITIVE_FAST,
				CharEqual.isEqualCharIgnoreCaseFast(), CodePointEqual.isEqualCodePointIgnoreCaseFast(),
				CharIndexEqual.of(CharEqual.isEqualCharIgnoreCase()));

		//

		final boolean supportCodePoints;
		final CharCaseMode charCaseMode;
		final CharEqual charEqual;
		final CodePointEqual codePointEqual;
		final CharIndexEqual charIndexEqual;
		// TODO codePointIndexEqual

		//

		/** 
		 * Returns standard {@link CharMode} for case sensitive ({@link CharMode#CS_CODEPOINT} or
		 * case insensitive ({@link CharMode#CI_CODEPOINT}) character handling.
		 */
		public static CharMode getCharMode(boolean ignoreCase) {
			return (ignoreCase) ? CI_CODEPOINT : CS_CODEPOINT;
		}

		CharMode(boolean supportCodePoints, CharCaseMode charCaseMode,
				CharEqual charEqual, CodePointEqual codePointEqual,
				CharIndexEqual charIndexEqual) {

			this.supportCodePoints = supportCodePoints;
			this.charCaseMode = charCaseMode;
			this.charEqual = charEqual;
			this.codePointEqual = codePointEqual;
			this.charIndexEqual = charIndexEqual;
		}

		@Override
		public boolean supportCodePoints() {
			return supportCodePoints;
		}

		@Override
		public CharCaseMode getCharCaseMode() {
			return charCaseMode;
		}

		@Override
		public CharEqual getCharEqual() {
			return charEqual;
		}

		@Override
		public CodePointEqual getCodePointEqual() {
			return codePointEqual;
		}

		@Override
		public CharIndexEqual getCharIndexEqual() {
			return charIndexEqual;
		}
	}

	/**
	 * Interface {@link CharEqual} determines whether two characters are equal.
	 * This interface can be used to implement case insensitive comparison. 
	 */
	public interface CharEqual {

		int compareChar(char c0, char c1);

		default boolean isEqualChar(char c0, char c1) {
			return compareChar(c0, c1) == 0;
		}

		public static CharEqual of(boolean ignoreCase) {
			return (ignoreCase) ? isEqualCharIgnoreCase() : isEqualChar();
		}

		public static CharEqual isEqualChar() {
			return CharCaseTools::compareChar;
		}

		public static CharEqual isEqualCharIgnoreCase() {
			return CharCaseTools::compareCharCI;
		}

		public static CharEqual isEqualCharIgnoreCaseFast() {
			return CharCaseTools::compareCharCIFast;
		}
	}

	/**
	 * Interface {@link CodePointEqual} determines whether two code points are equal.
	 * This interface can be used to implement case insensitive comparison. 
	 */
	public interface CodePointEqual {

		int compareCodePoint(int cp0, int cp1);

		default boolean isEqualCodePoint(int cp0, int cp1) {
			return compareCodePoint(cp0, cp1) == 0;
		}

		public static CodePointEqual of(boolean ignoreCase) {
			return (ignoreCase) ? isEqualCodePointIgnoreCase() : isEqualCodePoint();
		}

		public static CodePointEqual isEqualCodePoint() {
			return CharCaseTools::compareCodePoint;
		}

		public static CodePointEqual isEqualCodePointIgnoreCase() {
			return CharCaseTools::compareCodePointCI;
		}

		public static CodePointEqual isEqualCodePointIgnoreCaseFast() {
			return CharCaseTools::compareCodePointCIFast;
		}

		default CharEqual asCharEqual() {
			return (char c0, char c1) -> compareCodePoint(c0, c1);
		}
	}

	public interface CharConvertEqual {

		char convert(char c);

		public static CharConvertEqual of(boolean ignoreCase) {
			return (ignoreCase) ? convertIgnoreCase() : convert();
		}

		public static CharConvertEqual convert() {
			return c -> c;
		}

		public static CharConvertEqual convertIgnoreCase() {
			return c -> Character.toLowerCase(Character.toUpperCase(c));
		}

		public static CharConvertEqual convertIgnoreCaseFast() {
			return c -> Character.toUpperCase(c);
		}
	}

	public interface CodePointConvertEqual {

		int convert(int c);

		public static CodePointConvertEqual of(boolean ignoreCase) {
			return (ignoreCase) ? convertIgnoreCase() : convert();
		}

		public static CodePointConvertEqual convert() {
			return c -> c;
		}

		public static CodePointConvertEqual convertIgnoreCase() {
			return c -> Character.toLowerCase(Character.toUpperCase(c));
		}

		public static CodePointConvertEqual convertIgnoreCaseFast() {
			return c -> Character.toUpperCase(c);
		}

		default CharConvertEqual asCharConvertEqual() {
			return (char c) -> (char) convert(c);
		}
	}

	/**
	 * Interface {@link CharIndexEqual} is the base interface for comparing code points with {@link #indexOf}.
	 */
	public interface CharIndexEqual {

		int indexOf(String anyChar, char c);

		public static CharIndexEqual of(boolean ignoreCase) {
			return (ignoreCase) ? indexOfIgnoreCase(CharEqual.isEqualCharIgnoreCase()) : indexOf();
		}

		public static CharIndexEqual of(CharEqual charEqual) {
			return indexOfIgnoreCase(charEqual);
		}

		public static CharIndexEqual indexOf() {
			return (s, c) -> matchIndex(s, c);
		}

		public static CharIndexEqual indexOfIgnoreCase(CharEqual equal) {
			return (s, c) -> matchIndex(s, c, equal);
		}

		static int matchIndex(String anyChar, char c) {
			return anyChar.indexOf(c);
		}

		static int matchIndex(String anyChar, char c, CharEqual equal) {
			for (int i = 0; i < anyChar.length(); i++) {
				if (equal.isEqualChar(anyChar.charAt(i), c)) {
					return i;
				}
			}
			return -1;
		}
	}

	//

	/** Returns true if the two characters are equal. */
	public static boolean isEqualChar(char c1, char c2) {
		return c1 == c2;
	}

	public static int compareChar(char c1, char c2) {
		return c1 - c2;
	}

	/** Returns true if the two code points are equal. */
	public static boolean isEqualCodePoint(int c1, int c2) {
		return c1 == c2;
	}

	public static int compareCodePoint(int c1, int c2) {
		return c1 - c2;
	}

	/** 
	 * Returns true if the two characters are case insensitive equal, i.e. one of the following is equal: <br>
	 * - the two characters <br>
	 * - the uppercase variants of them <br>
	 * - the lowercase variants of the uppercase variants of them <br>
	 * This approach is also used by String.equalsIgnoreCase().
	 */
	public static boolean isEqualCharCI(char c1, char c2) {
		// Compare characters as passed
		if (c1 == c2) {
			return true;
		}
		// Convert both characters to uppercase and compare
		char uc1 = Character.toUpperCase(c1);
		char uc2 = Character.toUpperCase(c2);
		if (uc1 == uc2) {
			return true;
		}
		// Unfortunately, conversion to uppercase does not work properly for the Georgian alphabet, which has strange rules about case conversion.
		return Character.toLowerCase(uc1) == Character.toLowerCase(uc2);
	}

	public static int compareCharCI(char c1, char c2) {
		// Compare characters as passed
		if (c1 == c2) {
			return 0;
		}
		// Convert both characters to uppercase and compare
		char uc1 = Character.toUpperCase(c1);
		char uc2 = Character.toUpperCase(c2);
		if (uc1 == uc2) {
			return 0;
		}
		// Unfortunately, conversion to uppercase does not work properly for the Georgian alphabet, which has strange rules about case conversion.
		char lc1 = Character.toLowerCase(uc1);
		char lc2 = Character.toLowerCase(uc2);
		return lc1 - lc2;
	}

	/** 
	 * Returns true if the two code points are case insensitive equal, i.e. one of the following is equal: <br>
	 * - the two code points <br>
	 * - the uppercase variants of them <br>
	 * - the lowercase variants of the uppercase variants of them <br>
	 * This approach is also used by String.equalsIgnoreCase().
	 */
	public static boolean isEqualCodePointCI(int c1, int c2) {
		// Compare characters as passed
		if (c1 == c2) {
			return true;
		}
		// Convert both characters to uppercase and compare
		int uc1 = Character.toUpperCase(c1);
		int uc2 = Character.toUpperCase(c2);
		if (uc1 == uc2) {
			return true;
		}
		// Unfortunately, conversion to uppercase does not work properly for the Georgian alphabet, which has strange rules about case conversion.
		return Character.toLowerCase(uc1) == Character.toLowerCase(uc2);
	}

	public static int compareCodePointCI(int c1, int c2) {
		// Compare characters as passed
		if (c1 == c2) {
			return 0;
		}
		// Convert both characters to uppercase and compare
		int uc1 = Character.toUpperCase(c1);
		int uc2 = Character.toUpperCase(c2);
		if (uc1 == uc2) {
			return 0;
		}
		// Unfortunately, conversion to uppercase does not work properly for the Georgian alphabet, which has strange rules about case conversion.
		int lc1 = Character.toLowerCase(uc1);
		int lc2 = Character.toLowerCase(uc2);
		return lc1 - lc2;
	}

	/** 
	 * Returns true if the two characters are case insensitive equal, i.e. one of the following is equal: <br>
	 * - the two characters <br>
	 * - the uppercase variants of them <br>
	 */
	public static boolean isEqualCIFast(char c1, char c2) {
		// Compare characters as passed
		if (c1 == c2) {
			return true;
		}
		// Convert both characters to uppercase and compare
		char uc1 = Character.toUpperCase(c1);
		char uc2 = Character.toUpperCase(c2);
		return uc1 == uc2;
	}

	public static int compareCharCIFast(char c1, char c2) {
		// Compare characters as passed
		if (c1 == c2) {
			return 0;
		}
		// Convert both characters to uppercase and compare
		char uc1 = Character.toUpperCase(c1);
		char uc2 = Character.toUpperCase(c2);
		return uc1 - uc2;
	}

	/** 
	 * Returns true if the two code points are case insensitive equal, i.e. one of the following is equal: <br>
	 * - the two code points <br>
	 * - the uppercase variants of them <br>
	 */
	public static boolean isEqualCodePointCIFast(int c1, int c2) {
		if (c1 == c2) {
			return true;
		}
		int uc1 = Character.toUpperCase(c1);
		int uc2 = Character.toUpperCase(c2);
		return uc1 == uc2;
	}

	public static int compareCodePointCIFast(int c1, int c2) {
		if (c1 == c2) {
			return 0;
		}
		int uc1 = Character.toUpperCase(c1);
		int uc2 = Character.toUpperCase(c2);
		return uc1 - uc2;
	}

	// CharSupport 

	// There are 18 characters which needs toLowerCase(toUpperCase(c)) instead of simpler toUpperCase(c).
	// Using upper case is better than lower case (with 67 characters).
	static final String CHARS_NEED_UC_LC_CASE = "IKikÅåİıΘΩθωϑϴẞΩKÅ";

	static final int CI_CASES = 0x0f;
	static final int CI_CASE_NONE = 0x00;
	static final int CI_CASE_ASCII = 0x01;
	static final int CI_CASE_UPPER = 0x02;
	static final int CI_CASE_UPPER_LOWER = 0x04;
	static final int CODEPOINT = 0xf0;

	public static int getCharSupport(ICharMode cm, char c) {
		CharCaseMode ccm = cm.getCharCaseMode();
		if (ccm == CharCaseMode.CASE_SENSITIVE) {
			return 0;
		}

		boolean ciFast = (cm.getCharCaseMode() == CharCaseMode.CASE_INSENSITIVE_FAST);
		return doGetCharSupportForChar(c, ciFast);
	}

	public static int getCharSupport(ICharMode cm, int cp) {
		if (cm.getCharCaseMode() == CharCaseMode.CASE_SENSITIVE) {
			return CODEPOINT;
		}

		return CODEPOINT + doGetCharSupportForCodePoint(cp);

	}

	public static int getCharSupport(ICharMode cm, CharSequence str) {
		if (cm == CharMode.CS_CHAR) {
			// client wants no code point and case support, so need to scan the input string
			return 0;
		}

		boolean ciFast = (cm.getCharCaseMode() == CharCaseMode.CASE_INSENSITIVE_FAST);
		if (cm.supportCodePoints()) {
			// code points with surrogates
			return getCharSupportForCodePoints(str, ciFast);
		} else {
			// only chars, ignore surrogates		
			return getCharSupportForChars(str, ciFast);
		}
	}

	static int getCharSupportForChars(CharSequence str, boolean ciFast) {
		int ciCase = 0;
		int len = str.length();
		for (int i = 0; i < len; i++) {
			char c = str.charAt(i);
			ciCase = Math.max(ciCase, doGetCharSupportForChar(c, ciFast));
		}
		return ciCase;
	}

	static int doGetCharSupportForChar(char c, boolean ciFast) {
		if (!isCaseSensitiveChar(c)) {
			return 0;
		}

		if (!ciFast && CHARS_NEED_UC_LC_CASE.indexOf(c) != -1) {
			return CI_CASE_UPPER_LOWER;
		} else if (!CharTools.isAscii(c)) {
			return CI_CASE_UPPER;
		} else {
			return CI_CASE_ASCII;
		}
	}

	static int getCharSupportForCodePoints(CharSequence str, boolean ciFast) {
		boolean hasCp = false;
		int ciCase = 0;
		int len = str.length();
		for (int i = 0; i < len; i++) {
			char c = str.charAt(i);
			if (Character.isHighSurrogate(c)) {
				if (i + 1 < len) {
					char c1 = str.charAt(i);
					if (Character.isLowSurrogate(c1)) {
						// surrogate pair forming a code point
						int cp = Character.toCodePoint(c, c1);
						ciCase = Math.max(ciCase, doGetCharSupportForCodePoint(cp));
						hasCp = true;
						i++;
						continue;
					}
				}
			}

			// char (no code point)
			ciCase = Math.max(ciCase, doGetCharSupportForChar(c, ciFast));
		}
		int support = (hasCp) ? CODEPOINT : 0;
		support += ciCase;
		return support;
	}

	static int doGetCharSupportForCodePoint(int cp) {
		if (!isCaseSensitiveCodePoint(cp)) {
			return 0;
		}

		// There are no code points needing CASE_UPPER_LOWER support
		return CI_CASE_UPPER;
	}

	/** Return {@link CharMode} to use for determined char support value */
	public static CharMode getCharModeFromCharSupport(int charSupport) {
		if ((charSupport & CODEPOINT) == CODEPOINT) {
			// code point
			int caseSupport = charSupport & CI_CASES;
			switch (caseSupport) {
			case CI_CASE_NONE:
				return CharMode.CS_CODEPOINT;
			case CI_CASE_UPPER_LOWER:
				return CharMode.CI_CODEPOINT;
			default:
				return CharMode.CI_CODEPOINT_FAST;
			}
		} else {
			// char
			switch (charSupport) {
			case CI_CASE_NONE:
				return CharMode.CS_CHAR;
			case CI_CASE_UPPER_LOWER:
				return CharMode.CI_CHAR;
			default:
				return CharMode.CI_CHAR_FAST;
			}
		}
	}

	/** Return flags to use for determined char support value */
	public static int getRegexCaseInsensitiveFlagsFromCharSupport(int charSupport) {
		// Note that the UNICODE_CHARACTER_CLASS (?U) (which implies UNICODE_CASE) must be set by the client
		int caseSupport = charSupport & CI_CASES;
		switch (caseSupport) {
		case CI_CASE_NONE:
			return 0;
		case CI_CASE_ASCII:
			return Pattern.CASE_INSENSITIVE;
		default:
			return Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE;
		}
	}

}
