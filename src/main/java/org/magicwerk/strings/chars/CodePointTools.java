package org.magicwerk.strings.chars;

import java.util.Iterator;

import org.magicwerk.brownies.collections.primitive.IIntList;
import org.magicwerk.brownies.collections.primitive.IntGapList;
import org.magicwerk.strings.CharSequenceTools;

/**
 * Class {@link CodePointTools} implements helper methods for working with code points.
 */
public class CodePointTools {

	public static Iterator<Integer> codePointIterator(String str) {
		return new CodePointStringIterator(str);
	}

	public static Iterator<Integer> codePointIterator(CharSequence str) {
		if (str instanceof String) {
			return new CodePointStringIterator((String) str);
		} else {
			return new CodePointCharSequenceIterator(str);
		}
	}

	/**
	 * Class {@link CodePointStringIterator} implements an {@link Iterator} returning the code points of a string.
	 */
	public static class CodePointStringIterator implements Iterator<Integer> {

		// String.codePoints() returns an IntStream and is therefore inherently slow

		String str;
		int len;
		int pos;

		public CodePointStringIterator(String str) {
			this.str = str;
			len = str.length();
		}

		@Override
		public boolean hasNext() {
			return pos < len;
		}

		@Override
		public Integer next() {
			int cp = str.codePointAt(pos);
			pos += Character.charCount(cp);
			return cp;
		}
	}

	public static class CodePointCharSequenceIterator implements Iterator<Integer> {

		// String.codePoints() returns an IntStream an is therefore inherently slow

		CharSequence str;
		int len;
		int pos;

		public CodePointCharSequenceIterator(CharSequence str) {
			this.str = str;
			len = str.length();
		}

		@Override
		public boolean hasNext() {
			return pos < len;
		}

		@Override
		public Integer next() {
			int cp = codePointAt(str, pos);
			pos += Character.charCount(cp);
			return cp;
		}
	}

	/** Determines whether string contains a code point which can only be represented as int and not as char */
	public static boolean containsCodePoint(CharSequence str) {
		int len = str.length();
		for (int pos = 0; pos < len; pos++) {
			int cp = codePointAt(str, pos);
			int cc = charCount(cp);
			if (cc > 1) {
				return true;
			}
		}
		return false;
	}

	/** Determines if the passed index is the start of a surrogate pair, i.e. a high followed by a low surrogate */
	public static boolean isValidSurrogatePairAt(CharSequence str, int index) {
		return Character.isHighSurrogate(str.charAt(index)) && index + 1 < str.length() && Character.isLowSurrogate(str.charAt(index + 1));
	}

	/** Determines if the passed index is valid for the start of a code point (i.e. no low surrogate) */
	public static boolean isValidStartIndexForCodePoint(CharSequence str, int index) {
		return !Character.isLowSurrogate(str.charAt(index));
	}

	public static boolean isValidEndIndexForCodePoint(CharSequence str, int index) {
		return !Character.isHighSurrogate(str.charAt(index));
	}

	/**
	 * Determines a valid index for the start of a code point.
	 * If the index is no low surrogate, it is returned unchanged.
	 * Otherwise if forward is false, index-1 is returned so the code point containing the passed index can be read.
	 * If forward is true, index+1 is returned so the next code point can be read. 
	 */
	public static int getValidStartIndexForCodePoint(CharSequence str, int index, boolean forward) {
		if (!isValidStartIndexForCodePoint(str, index)) {
			return (forward) ? index + 1 : index - 1;
		}
		return index;
	}

	public static String codePointToString(int codePoint) {
		return Character.toString(codePoint);
	}

	/** Returns true if the passed code point can be represented as single char */
	public static boolean isCharCodePoint(int codePoint) {
		// TODO what about negative values
		// Note that method Character.isSupplementaryCodePoint() is not exactly the opposite as it checks for range
		return charCount(codePoint) == 1;
	}

	public static int charCount(int codePoint) {
		return Character.charCount(codePoint);
	}

	/**
	 * Returns code point staring at specified index.
	 * If the index contains a high surrogate and the next character is a low surrogate, the code point represented by the two characters is returned.
	 * Otherwise the character stored at the specified index is returned.
	 * <p>
	 * This method behaves like {@link String#codePointAt} but accepts a {@link CharSequence} as input.
	 */
	public static int codePointAt(CharSequence str, int index) {
		char c1 = str.charAt(index);
		if (index < str.length() - 1 && Character.isHighSurrogate(c1)) {
			char c2 = str.charAt(index + 1);
			if (Character.isLowSurrogate(c2)) {
				return Character.toCodePoint(c1, c2);
			}
		}
		return c1;
	}

	/**
	 * Returns code point staring at specified index.
	 * If the index contains a high surrogate and the next character is a low surrogate, the code point represented by the two characters is returned.
	 * Otherwise the character stored at the specified index is returned.
	 * <p>
	 * This method behaves like {@link String#codePointBefore} but accepts a {@link CharSequence} as input.
	 */
	public static int codePointBefore(CharSequence str, int endIndex) {
		char c2 = str.charAt(endIndex);
		if (endIndex > 0 && Character.isLowSurrogate(c2)) {
			char c1 = str.charAt(endIndex - 1);
			if (Character.isHighSurrogate(c1)) {
				return Character.toCodePoint(c1, c2);
			}
		}
		return c2;
	}

	/**
	 * Return last code point, -1 if input is empty.
	 */
	public static int lastCodePoint(CharSequence str) {
		int len = str.length();
		if (len == 0) {
			return -1;
		}
		char c2 = str.charAt(len - 1);
		if (len >= 2 && Character.isLowSurrogate(c2)) {
			char c1 = str.charAt(len - 2);
			if (Character.isHighSurrogate(c1)) {
				return Character.toCodePoint(c1, c2);
			}
		}
		return c2;
	}

	/**
	 * Return single code point representing the whole string, -1 if input is longer.
	 */
	public static int singleCodePoint(CharSequence str) {
		if (str.length() == 1) {
			return str.charAt(0);
		}
		int cp = firstCodePoint(str);
		if (cp != -1) {
			if (Character.charCount(cp) == 2) {
				return cp;
			}
		}
		return -1;
	}

	/**
	 * Return first code point, -1 if input is empty.
	 */
	public static int firstCodePoint(CharSequence str) {
		if (str.length() == 0) {
			return -1;
		}
		return codePointAt(str, 0);
	}

	public static int codePointCount(CharSequence str) {
		return codePointCount(str, 0, str.length());
	}

	/** 
	 * Returns number of code points in the passed string.
	 * Note that this method must iterate over the string and is therefore somehow slow. 
	 * <p>
	 * This method behaves like {@link String#codePointCount} but accepts a {@link CharSequence} as input.
	 */
	public static int codePointCount(CharSequence str, int start, int end) {
		if (str instanceof String) {
			return ((String) str).codePointCount(start, end);
		}

		CharSequenceTools.checkStringRange(str, start, end);
		int num = 0;
		for (int pos = start; pos < end;) {
			int cp = codePointAt(str, pos);
			pos += Character.charCount(cp);
			num++;
		}
		return num;
	}

	public static int charCount(int[] codePoints) {
		return charCount(codePoints, 0, codePoints.length);
	}

	public static int charCount(int[] codePoints, int start, int end) {
		int len = 0;
		for (int i = start; i < end; i++) {
			len += charCount(codePoints[i]);
		}
		return len;
	}

	public static char[] getCharArray(int[] codePoints) {
		return getCharArray(codePoints, 0, codePoints.length);
	}

	public static char[] getCharArray(int[] codePoints, int start, int end) {
		int len = charCount(codePoints, start, end);
		char[] chars = new char[len];
		int tgt = 0;
		for (int i = start; i < end; i++) {
			int cp = codePoints[i];
			if (charCount(cp) == 1) {
				chars[tgt++] = (char) cp;
			} else {
				chars[tgt++] = Character.highSurrogate(cp);
				chars[tgt++] = Character.lowSurrogate(cp);
			}
		}
		return chars;
	}

	/** Return an array with code points representing the passed string */
	public static int[] getCodePointArray(CharSequence str) {
		// String.codePoints() returns an IntStream an is therefore inherently slow		
		int len = codePointCount(str, 0, str.length());
		int[] cps = new int[len];
		int pos = 0;
		for (int i = 0; i < len; i++) {
			int cp = codePointAt(str, pos);
			pos += Character.charCount(cp);
			cps[i] = cp;
		}
		return cps;
	}

	/** Return a list with code points representing the passed string */
	public static IIntList getCodePoints(CharSequence str) {
		// String.codePoints() returns an IntStream an is therefore inherently slow
		int len = str.length();
		IntGapList cps = new IntGapList(len);
		for (int pos = 0; pos < len;) {
			int cp = codePointAt(str, pos);
			cps.add(cp);
			pos += Character.charCount(cp);
		}
		return cps;
	}

	/** Return a list with code points if there are characters which cannot be represented as {@code char}, otherwise null. */
	public static IIntList getCodePointsIf(CharSequence str) {
		int len = str.length();
		IntGapList cps = null;
		for (int pos = 0; pos < len;) {
			int cp = codePointAt(str, pos);
			int cc = Character.charCount(cp);
			if (cc > 1) {
				if (cps == null) {
					cps = new IntGapList(len);
					for (int i = 0; i < pos; i++) {
						int c = str.charAt(i);
						cps.add(c);
					}
				}
			}
			cps.add(cp);
			pos += cc;
		}
		return cps;
	}

}