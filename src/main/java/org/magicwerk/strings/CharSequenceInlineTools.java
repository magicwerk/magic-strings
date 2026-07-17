package org.magicwerk.strings;

import org.magicwerk.strings.chars.CodePointTools;
import org.magicwerk.strings.chars.CharCaseTools.CharEqual;
import org.magicwerk.strings.chars.CharCaseTools.CodePointEqual;

/**
 * Class {@link CharSequenceInlineTools} implements helper methods for doing inline changes to instances of {@link IString}.
 */
public class CharSequenceInlineTools {

	/** Replace inline a char (1 byte) with another. Length of GapString will not change. */
	public static IString doReplaceInlineChar(IString str, int start, char findChar, char replaceChar) {
		int len = str.length();
		for (int i = start; i < len; i++) {
			if (str.charAt(i) == findChar) {
				str.set(i, replaceChar);
			}
		}
		return str;
	}

	public static IString doReplaceInlineChar(IString str, int start, char findChar, char replaceChar, CharEqual equals) {
		int len = str.length();
		for (int i = start; i < len; i++) {
			if (equals.isEqualChar(str.charAt(i), findChar)) {
				str.set(i, replaceChar);
			}
		}
		return str;
	}

	/** Remove inline a char (1 byte). Length of GapString will decrease if removals are made. */
	public static IString doRemoveInlineChar(IString str, int start, char findChar) {
		int len = str.length();
		int tgt = start;
		for (int src = start; src < len; src++) {
			char c = str.charAt(src);
			if (c != findChar) {
				str.set(tgt, c);
				tgt++;
			}
		}
		str.resize(tgt, (char) 0);
		return str;
	}

	public static IString doRemoveInlineChar(IString str, int start, char findChar, CharEqual equals) {
		int len = str.length();
		int tgt = start;
		for (int src = start; src < len; src++) {
			char c = str.charAt(src);
			if (!equals.isEqualChar(c, findChar)) {
				str.set(tgt, c);
				tgt++;
			}
		}
		str.resize(tgt, (char) 0);
		return str;
	}

	/** Replace inline a code point (2 bytes) with another. Length of GapString will not change. */
	public static IString doReplaceInlineCodePoint(IString str, int start, int find, int replace) {
		assert CodePointTools.charCount(find) == 2;
		assert CodePointTools.charCount(replace) == 2;

		char c0 = Character.highSurrogate(find);
		char c1 = Character.lowSurrogate(find);

		int len = str.length() - 1;
		for (int i = start; i < len; i++) {
			char high = str.charAt(i);
			if (high == c0) {
				char low = str.charAt(i + 1);
				if (low == c1) {
					// code point found
					str.set(i, Character.highSurrogate(replace));
					str.set(i + 1, Character.lowSurrogate(replace));
					i++;
				}
			}
		}
		return str;
	}

	public static IString doReplaceInlineCodePoint(IString str, int start, int find, int replace, CodePointEqual equal) {
		assert CodePointTools.charCount(find) == 2;
		assert CodePointTools.charCount(replace) == 2;

		int len = str.length();
		while (start < len) {
			int codePoint = CodePointTools.codePointAt(str, start);
			if (equal.isEqualCodePoint(codePoint, find)) {
				str.set(start, Character.highSurrogate(replace));
				str.set(start + 1, Character.lowSurrogate(replace));
				start += 2;
			} else {
				start += CodePointTools.charCount(codePoint);
			}
		}
		return str;
	}

	public static IString doRemoveInlineCodePoint(IString str, int start, int find, CodePointEqual equal) {
		assert CodePointTools.charCount(find) == 2;

		int len = str.length();
		int src = start;
		int tgt = start;
		while (src < len) {
			int codePoint = CodePointTools.codePointAt(str, src);
			if (equal.isEqualCodePoint(codePoint, find)) {
				src += 2;
			} else {
				int count = CodePointTools.charCount(codePoint);
				if (count == 1) {
					str.set(tgt, (char) codePoint);
				} else {
					str.set(tgt, Character.highSurrogate(codePoint));
					str.set(tgt + 1, Character.lowSurrogate(codePoint));
				}
				src += count;
				tgt += count;
			}
		}
		str.resize(tgt, (char) 0);
		return str;
	}

	/** Remove inline a code point (2 bytes). Length of GapString will decrease if removals are made. */
	public static IString doRemoveInlineCodePoint(IString str, int start, int find) {
		assert CodePointTools.charCount(find) == 2;

		char c0 = Character.highSurrogate(find);
		char c1 = Character.lowSurrogate(find);

		int len = str.length() - 1;
		int tgt = start;
		for (int src = start; src < len; src++) {
			boolean copy = true;
			char high = str.charAt(src);
			if (high == c0) {
				char low = str.charAt(src + 1);
				if (low == c1) {
					// code point found
					src++;
					copy = false;
				}
			}
			if (copy) {
				str.set(tgt, high);
				tgt++;
			}
		}
		str.resize(tgt, (char) 0);
		return str;
	}

	/** Replace inline a code point (2 bytes) with a char (1 byte). Length of the GapString will decrease if replacements are made. */
	public static IString doReplaceInlineCodePointWithChar(IString str, int start, int find, char replace) {
		assert CodePointTools.charCount(find) == 2;

		int index = CharSequenceTools.doIndexOfCodePoint(str, find, start, str.length());
		if (index == -1) {
			return str; // not found at all -> no change to do
		}

		char c0 = Character.highSurrogate(find);
		char c1 = Character.lowSurrogate(find);

		int len = str.length() - 1;
		int tgt = index;
		for (int src = index; src < len; src++) {
			boolean copy = true;
			char high = str.charAt(src);
			if (high == c0) {
				char low = str.charAt(src + 1);
				if (low == c1) {
					// replace
					str.set(tgt, replace);
					src++;
					copy = false;
				}
			}
			if (copy) {
				// copy
				str.set(tgt, high);
			}
			tgt++;
		}
		str.resize(tgt, (char) 0);
		return str;
	}

	public static IString doReplaceInlineCodePointWithChar(IString str, int start, int find, char replace, CodePointEqual equal) {
		assert CodePointTools.charCount(find) == 2;

		int len = str.length();
		int src = start;
		int tgt = start;
		while (src < len) {
			int codePoint = CodePointTools.codePointAt(str, src);
			if (equal.isEqualCodePoint(codePoint, find)) {
				// replace
				str.set(tgt, replace);
				src += 2;
				tgt++;
			} else {
				// copy
				int count = CodePointTools.charCount(codePoint);
				if (count == 1) {
					str.set(tgt, (char) codePoint);
				} else {
					str.set(tgt, Character.highSurrogate(codePoint));
					str.set(tgt + 1, Character.lowSurrogate(codePoint));
				}
				src += count;
				tgt += count;
			}
		}
		str.resize(tgt, (char) 0);
		return str;
	}

	/** Replace a char (1 byte) with a code point (2 bytes). Length of the GapString will increase if replacements are made.  */
	public static IString doReplaceInlineCharWithCodePoint(IString str, int start, char find, int replace) {
		assert CodePointTools.charCount(replace) == 2;

		int index = CharSequenceTools.doIndexOfChar(str, find, start);
		if (index == -1) {
			return str; // not found at all -> no change to do
		}

		int gap = str.length() - index;
		do {
			str.rotate(-index);
			str.remove(0);
			gap--;
			str.add(Character.highSurrogate(replace));
			str.add(Character.lowSurrogate(replace));
			index = CharSequenceTools.doIndexOfChar(str, find, 0, gap);
			if (index == -1) {
				break;
			}
			gap -= index;
		} while (index <= gap);
		str.rotate(-gap);
		return str;
	}

	public static IString doReplaceInlineCharWithCodePoint(IString str, int start, char find, int replace, CharEqual equals) {
		assert CodePointTools.charCount(replace) == 2;

		int index = CharSequenceTools.doIndexOfChar(str, find, start, equals);
		if (index == -1) {
			return str; // not found at all -> no change to do
		}

		int gap = str.length() - index;
		do {
			str.rotate(-index);
			str.remove(0);
			gap--;
			str.add(Character.highSurrogate(replace));
			str.add(Character.lowSurrogate(replace));
			index = CharSequenceTools.doIndexOfChar(str, find, 0, gap, equals);
			if (index == -1) {
				break;
			}
			gap -= index;
		} while (index <= gap);
		str.rotate(-gap);
		return str;
	}

	public static IString doReplaceInlineString(IString str, int start, CharSequence find, CharSequence replace) {
		int findLen = find.length();
		if (findLen == 0) {
			return doReplaceInlineStringEmpty(str, start, replace);
		}

		int index = CharSequenceTools.doIndexOfCharSequence(str, find, start);
		if (index == -1) {
			return str; // not found at all -> no change to do
		}

		int gap = str.length() - index;
		do {
			str.rotate(-index);
			str.remove(0, findLen);
			gap -= findLen;
			str.addString(replace);
			index = CharSequenceTools.doIndexOfCharSequence(str, find, 0, gap);
			if (index == -1) {
				break;
			}
			gap -= index;
		} while (index <= gap);
		str.rotate(-gap);
		return str;
	}

	/** Special case of empty find string (which matches at each position, including before and after string) */
	public static IString doReplaceInlineStringEmpty(IString str, int start, CharSequence replace) {
		int end = str.length();
		int replaceLen = replace.length();
		for (int i = start; i <= end; i++) {
			str.addString(0, replace);
			str.rotate(-replaceLen - 1);
		}
		str.rotate(1);
		return str;
	}

	public static IString doReplaceInlineString(IString str, int start, CharSequence find, CharSequence replace, CodePointEqual equal) {
		int findLen = find.length();
		if (findLen == 0) {
			return doReplaceInlineStringEmpty(str, start, replace);
		}

		int index = CharSequenceTools.doIndexOfCharSequence(str, find, start, equal);
		if (index == -1) {
			return str; // not found at all -> no change to do
		}

		int gap = str.length() - index;
		do {
			str.rotate(-index);
			str.remove(0, findLen);
			gap -= findLen;
			str.addString(replace);
			index = CharSequenceTools.doIndexOfCharSequence(str, find, 0, gap, equal);
			if (index == -1) {
				break;
			}
			gap -= index;
		} while (index <= gap);
		str.rotate(-gap);
		return str;
	}

}