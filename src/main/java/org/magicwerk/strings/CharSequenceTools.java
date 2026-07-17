package org.magicwerk.strings;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.IntPredicate;
import java.util.function.IntUnaryOperator;

import org.magicwerk.strings.chars.CharOperator;
import org.magicwerk.strings.chars.CharPredicate;
import org.magicwerk.strings.chars.CharPredicates;
import org.magicwerk.strings.chars.CodePointPredicates;
import org.magicwerk.strings.chars.CodePointTools;
import org.magicwerk.strings.chars.CharCaseTools.CharEqual;
import org.magicwerk.strings.chars.CharCaseTools.CharIndexEqual;
import org.magicwerk.strings.chars.CharCaseTools.CodePointEqual;
import org.magicwerk.strings.helper.CheckTools;

/**
 * Class {@link CharSequenceTools} implements helper methods for working with {@link CharSequence}.
 */
public class CharSequenceTools {

	interface IndexOfString {
		int indexOf(CharSequence str, CharSequence findStr, int start);

		static IndexOfString indexOf() {
			return (CharSequence str, CharSequence findStr, int start) -> CharSequenceTools.doIndexOfCharSequence(str, findStr, start);
		}

		static IndexOfString indexOfChar(CharEqual equal) {
			return (CharSequence str, CharSequence findStr, int start) -> CharSequenceTools.doIndexOfCharSequence(str, findStr, start, equal);
		}

		static IndexOfString indexOfCodePoint(CodePointEqual equal) {
			return (CharSequence str, CharSequence findStr, int start) -> CharSequenceTools.doIndexOfCharSequence(str, findStr, start, equal);
		}
	}

	/**
	 * Class {@link CharSequenceArray} implements a {@link CharSequence} backed by a char array (char[]).
	 */
	public static class CharSequenceArray implements CharSequence {

		char[] chars;

		/** Create {@link CharSequenceArray} */
		public CharSequenceArray(char[] chars) {
			this.chars = chars;
		}

		@Override
		public int length() {
			return chars.length;
		}

		@Override
		public char charAt(int index) {
			return chars[index];
		}

		@Override
		public CharSequence subSequence(int start, int end) {
			return new CharSequenceArray(Arrays.copyOfRange(chars, start, end));
		}

		@Override
		public String toString() {
			return new String(chars);
		}
	}

	/**
	 * Class {@link CharSequenceView} provides a view to a {@link CharSequence} by restricting access to a defined range.
	 * The view delegates all calls to the base CharSequence by correcting the specified indexes.
	 */
	public static class CharSequenceView implements CharSequence {

		/** CharSequence referenced by this view */
		CharSequence str;
		/** Index where view starts (including) */
		int start;
		/** Index where view ends (excluding) */
		int end;

		public CharSequenceView(CharSequence str) {
			this(str, 0, str.length());
		}

		/**
		 * Constructor of {@link CharSequenceView}.
		 * 
		 * @param str		base CharSequence
		 * @param start		start index
		 * @param end		end index in base char sequence, you can specify -1 for the length of the string
		 */
		public CharSequenceView(CharSequence str, int start, int end) {
			this.str = str;
			this.start = start;
			this.end = (end != -1) ? end : str.length();

			CheckTools.checkNonNull(str);
			CheckTools.check(this.start >= 0 && this.end <= str.length() && this.start <= this.end,
					"start= {}, end= {}, str.length()= {}", this.start, this.end, str.length());
		}

		/** Getter for {@link #str} */
		public CharSequence getView() {
			return str;
		}

		/** Getter for {@link #start} */
		public int getViewStart() {
			return start;
		}

		/** Getter for {@link #end} */
		public int getViewEnd() {
			return end;
		}

		@Override
		public int length() {
			return end - start;
		}

		@Override
		public char charAt(int index) {
			CheckTools.check(index >= 0 && index < length());
			return str.charAt(start + index);
		}

		@Override
		public CharSequence subSequence(int start, int end) {
			CheckTools.check(start >= 0 && end <= length() && start <= end);
			return new CharSequenceView(str, this.start + start, this.start + end);
		}

		@Override
		public String toString() {
			return str.subSequence(start, end).toString();
		}
	}

	// AppendReplacer

	public interface AppendReplacer {
		void replaceAppend(StringBuilder buf);

		public static AppendReplacer appendChar(char c) {
			return buf -> buf.append(c);
		}

		public static AppendReplacer appendCodePoint(int cp) {
			return buf -> buf.appendCodePoint(cp);
		}

		public static AppendReplacer appendString(CharSequence str) {
			return buf -> buf.append(str);
		}
	}

	public interface AppendCharReplacer {

		void replaceAppend(char c, StringBuilder buf);

		public static AppendCharReplacer replaceChar(CharOperator op) {
			return (char c, StringBuilder buf) -> buf.append(op.apply(c));
		}

		public static AppendCharReplacer appendChar(char c) {
			return (char unused, StringBuilder buf) -> buf.append(c);
		}

		public static AppendCharReplacer appendCodePoint(int cp) {
			return (char unused, StringBuilder buf) -> buf.appendCodePoint(cp);
		}

		public static AppendCharReplacer appendString(CharSequence str) {
			return (char unused, StringBuilder buf) -> buf.append(str);
		}
	}

	public interface AppendCodePointReplacer {
		void replaceAppend(int cp, StringBuilder buf);

		public static AppendCodePointReplacer replaceCodePoint(IntUnaryOperator op) {
			return (int cp, StringBuilder buf) -> buf.append(op.applyAsInt(cp));
		}

		public static AppendCodePointReplacer appendChar(char c) {
			return (int unused, StringBuilder buf) -> buf.append(c);
		}

		public static AppendCodePointReplacer appendCodePoint(int cp) {
			return (int unused, StringBuilder buf) -> buf.appendCodePoint(cp);
		}

		public static AppendCodePointReplacer appendString(CharSequence str) {
			return (int unused, StringBuilder buf) -> buf.append(str);
		}
	}

	//

	public static void checkStringRange(CharSequence str, int start, int end) {
		if (start < 0 || start > end || end > str.length()) {
			throw new StringIndexOutOfBoundsException();
		}
	}

	//

	public static char[] toCharArray(CharSequence str) {
		if (str instanceof String) {
			return ((String) str).toCharArray();
		}

		char[] cs = new char[str.length()];
		for (int i = 0; i < str.length(); i++) {
			cs[i] = str.charAt(i);
		}
		return cs;
	}

	public static void getChars(CharSequence src, int srcBegin, int srcEnd, char dst[], int dstBegin) {
		for (int i = 0; i < srcEnd - srcBegin; i++) {
			dst[dstBegin + i] = src.charAt(srcBegin + i);
		}
	}

	//

	/**
	 * Return length of string, 0 if string is null
	 */
	public static int length(CharSequence str) {
		return (str != null) ? str.length() : 0;
	}

	//

	public static String substring(CharSequence str, int start) {
		return str.subSequence(start, str.length()).toString();
	}

	public static String substring(CharSequence str, int start, int end) {
		return str.subSequence(start, end).toString();
	}

	public static CharSequence subSequence(CharSequence str, int start) {
		if (start == 0) {
			return str;
		} else {
			return str.subSequence(start, str.length());
		}
	}

	public static CharSequence subSequence(CharSequence str, int start, int end) {
		if (start == 0 && end == str.length()) {
			return str;
		} else {
			return str.subSequence(start, end);
		}
	}

	//

	public static int doSkipHead(CharSequence str, CharPredicate find, int len) {
		for (int i = 0; i < len; i++) {
			if (!find.test(str.charAt(i))) {
				return i;
			}
		}
		return len;
	}

	public static int doSkipBody(CharSequence str, CharPredicate find, int start, int end) {
		for (int i = start; i < end; i++) {
			if (!find.test(str.charAt(i))) {
				return i;
			}
		}
		return end;
	}

	public static int doSkipTail(CharSequence str, CharPredicate find, int len) {
		for (int i = len; i > 0; i--) {
			if (!find.test(str.charAt(i - 1))) {
				return i;
			}
		}
		return 0;
	}

	//

	public static boolean equals(CharSequence str0, CharSequence str1) {
		if (str0.length() != str1.length()) {
			return false;
		}
		return doEquals(str0, 0, str1, 0, str0.length());
	}

	public static boolean equals(CharSequence str0, CharSequence str1, CharEqual equal) {
		if (str0.length() != str1.length()) {
			return false;
		}
		return doEquals(str0, 0, str1, 0, str0.length(), equal);
	}

	public static boolean equals(CharSequence str0, CharSequence str1, CodePointEqual equal) {
		if (str0.length() != str1.length()) {
			return false;
		}
		return doEquals(str0, 0, str1, 0, str0.length(), equal);
	}

	public static boolean equals(CharSequence str0, int index0, CharSequence str1, int index1, int len) {
		if (index0 < 0 || index0 + len > str0.length() || index1 < 0 || index1 + len > str1.length()) {
			return false;
		}
		return doEquals(str0, index0, str1, index1, len);
	}

	public static boolean equals(CharSequence str0, int index0, CharSequence str1, int index1, int len, CharEqual equal) {
		if (index0 < 0 || index0 + len > str0.length() || index1 < 0 || index1 + len > str1.length()) {
			return false;
		}
		return doEquals(str0, index0, str1, index1, len, equal);
	}

	public static boolean equals(CharSequence str0, int index0, CharSequence str1, int index1, int len, CodePointEqual equal) {
		if (index0 < 0 || index0 + len > str0.length() || index1 < 0 || index1 + len > str1.length()) {
			return false;
		}
		return doEquals(str0, index0, str1, index1, len, equal);
	}

	// Compare

	public static int compare(CharSequence str0, CharSequence str1) {
		int len0 = str0.length();
		int len1 = str1.length();
		int len = Math.min(len0, len1);
		int cmp = doCompare(str0, 0, str1, 0, len);
		return (cmp != 0) ? cmp : (len0 - len1);
	}

	public static int compare(CharSequence str0, CharSequence str1, CharEqual equal) {
		int len0 = str0.length();
		int len1 = str1.length();
		int len = Math.min(len0, len1);
		int cmp = doCompare(str0, 0, str1, 0, len, equal);
		return (cmp != 0) ? cmp : (len0 - len1);
	}

	public static int compare(CharSequence str0, CharSequence str1, CodePointEqual equal) {
		int len0 = str0.length();
		int len1 = str1.length();
		int len = Math.min(len0, len1);
		int cmp = doCompare(str0, 0, str1, 0, len, equal);
		return (cmp != 0) ? cmp : (len0 - len1);
	}

	public static int doCompare(CharSequence str0, int index0, CharSequence str1, int index1, int len) {
		// assert index0, index, len are valid for indexing
		for (int i = 0; i < len; i++) {
			char c0 = str0.charAt(index0 + i);
			char c1 = str1.charAt(index1 + i);
			if (c0 != c1) {
				return c0 - c1;
			}
		}
		return 0;
	}

	public static int doCompare(CharSequence str0, int index0, CharSequence str1, int index1, int len, CharEqual equal) {
		// assert index0, index, len are valid for indexing
		for (int i = 0; i < len; i++) {
			char c0 = str0.charAt(index0 + i);
			char c1 = str1.charAt(index1 + i);
			int cmp = equal.compareChar(c0, c1);
			if (cmp != 0) {
				return cmp;
			}
		}
		return 0;
	}

	//

	public static boolean doEquals(CharSequence str0, int index0, CharSequence str1, int index1, int len) {
		// assert index0, index, len are valid for indexing
		for (int i = 0; i < len; i++) {
			if (str0.charAt(index0 + i) != str1.charAt(index1 + i)) {
				return false;
			}
		}
		return true;
	}

	public static boolean doEquals(CharSequence str0, int index0, CharSequence str1, int index1, int len, CharEqual equal) {
		// assert index0, index, len are valid for indexing
		for (int i = 0; i < len; i++) {
			if (!equal.isEqualChar(str0.charAt(index0 + i), str1.charAt(index1 + i))) {
				return false;
			}
		}
		return true;
	}

	public static int doCompare(CharSequence str0, int start0, CharSequence str1, int start1, int len, CodePointEqual equal) {
		// Same behavior StringUTF16.compareToCIImpl()
		int end0 = start0 + len;
		int end1 = start1 + len;
		for (int pos0 = start0, pos1 = start1; pos0 < end0 && pos1 < end1; pos0++, pos1++) {
			int cp0 = str0.charAt(pos0);
			int cp1 = str1.charAt(pos1);
			int cmp = equal.compareCodePoint(cp0, cp1);
			if (cmp != 0) {
				return cmp;
			}

			// Check for supplementary characters case
			cp0 = codePointIncluding(str0, cp0, pos0, start0, end0);
			if (cp0 < 0) {
				pos0++;
				cp0 = -cp0;
			}
			cp1 = codePointIncluding(str1, cp1, pos1, start1, end1);
			if (cp1 < 0) {
				pos1++;
				cp1 = -cp1;
			}

			cmp = equal.compareCodePoint(cp0, cp1);
			if (cmp != 0) {
				return cmp;
			}
		}

		return 0;
	}

	public static boolean doEquals(CharSequence str0, int start0, CharSequence str1, int start1, int len, CodePointEqual equal) {
		// Same behavior StringUTF16.compareToCIImpl()
		int end0 = start0 + len;
		int end1 = start1 + len;
		for (int pos0 = start0, pos1 = start1; pos0 < end0 && pos1 < end1; pos0++, pos1++) {
			int cp0 = str0.charAt(pos0);
			int cp1 = str1.charAt(pos1);
			if (equal.isEqualCodePoint(cp0, cp1)) {
				continue;
			}

			// Check for supplementary characters case
			cp0 = codePointIncluding(str0, cp0, pos0, start0, end0);
			if (cp0 < 0) {
				pos0++;
				cp0 = -cp0;
			}
			cp1 = codePointIncluding(str1, cp1, pos1, start1, end1);
			if (cp1 < 0) {
				pos1++;
				cp1 = -cp1;
			}

			if (!equal.isEqualCodePoint(cp0, cp1)) {
				return false;
			}
		}

		return true;
	}

	static int codePointIncluding(CharSequence str, int cp, int index, int start, int end) {
		// Same behavior StringUTF16.codePointIncluding()
		if (!Character.isSurrogate((char) cp)) {
			return cp;
		}
		if (Character.isLowSurrogate((char) cp)) {
			if (index > start) {
				char c = str.charAt(index - 1);
				if (Character.isHighSurrogate(c)) {
					return Character.toCodePoint(c, (char) cp);
				}
			}
		} else if (index + 1 < end) { // cp == high surrogate
			char c = str.charAt(index + 1);
			if (Character.isLowSurrogate(c)) {
				// negate the code point
				return -Character.toCodePoint((char) cp, c);
			}
		}
		return cp;
	}

	// Contains: delegates to indexOf

	public static boolean contains(CharSequence str, char find) {
		return doIndexOfChar(str, find, 0) != -1;
	}

	public static boolean contains(CharSequence str, char find, CharEqual equal) {
		return doIndexOfChar(str, find, 0, equal) != -1;
	}

	public static boolean contains(CharSequence str, CharPredicate find) {
		return indexOf(str, find, 0) != -1;
	}

	public static boolean contains(CharSequence str, int find) {
		return doIndexOf(str, find, 0) != -1;
	}

	public static boolean contains(CharSequence str, int find, CodePointEqual equal) {
		return doIndexOf(str, find, 0, equal) != -1;
	}

	public static boolean contains(CharSequence str, IntPredicate find) {
		return indexOf(str, find, 0) != -1;
	}

	public static boolean contains(CharSequence str, CharSequence find) {
		return indexOf(str, find, 0) != -1;
	}

	public static boolean contains(CharSequence str, CharSequence find, CharEqual equal) {
		return indexOf(str, find, 0, equal) != -1;
	}

	public static boolean contains(CharSequence str, CharSequence find, CodePointEqual equal) {
		return indexOf(str, find, 0, equal) != -1;
	}

	// IndexOf

	// IndexOf: char

	public static int indexOf(CharSequence str, char find) {
		return doIndexOfChar(str, find, 0);
	}

	public static int indexOf(CharSequence str, char find, int start) {
		if (start < 0) {
			start = 0;
		}
		return doIndexOfChar(str, find, start);
	}

	public static int indexOf(CharSequence str, char find, int start, int end) {
		if (start < 0) {
			start = 0;
		}
		int len = str.length();
		if (end > len) {
			end = len;
		}
		return doIndexOfChar(str, find, start);
	}

	public static int indexOf(CharSequence str, char find, CharEqual equal) {
		return doIndexOfChar(str, find, 0, equal);
	}

	public static int indexOf(CharSequence str, char find, int start, CharEqual equal) {
		if (start < 0) {
			start = 0;
		}
		return doIndexOfChar(str, find, start, equal);
	}

	public static int doIndexOfChar(CharSequence str, char find, int start) {
		return doIndexOfChar(str, find, start, str.length());
	}

	public static int doIndexOfChar(CharSequence str, char find, int start, int end) {
		while (start < end) {
			if (find == str.charAt(start)) {
				return start;
			}
			start++;
		}
		return -1;
	}

	public static int doIndexOfChar(CharSequence str, char find, int start, CharEqual equal) {
		return doIndexOfChar(str, find, start, str.length(), equal);
	}

	public static int doIndexOfChar(CharSequence str, char find, int start, int end, CharEqual equal) {
		while (start < end) {
			if (equal.isEqualChar(str.charAt(start), find)) {
				return start;
			}
			start++;
		}
		return -1;
	}

	// IndexOf: char predicate

	public static int indexOf(CharSequence str, CharPredicate find) {
		return doIndexOfCharPredicate(str, find, 0);
	}

	public static int indexOf(CharSequence str, CharPredicate find, int start) {
		if (start < 0) {
			start = 0;
		}
		return doIndexOfCharPredicate(str, find, start);
	}

	public static int doIndexOfCharPredicate(CharSequence str, CharPredicate find, int start) {
		int end = str.length();
		for (int i = start; i < end; i++) {
			if (find.test(str.charAt(i))) {
				return i;
			}
		}
		return -1;
	}

	// IndexOf: code point

	public static int indexOf(CharSequence str, int find) {
		return doIndexOf(str, find, 0);
	}

	public static int indexOf(CharSequence str, int find, int start) {
		if (start < 0) {
			start = 0;
		}
		return doIndexOf(str, find, start);
	}

	public static int indexOf(CharSequence str, int find, int start, int end) {
		if (start < 0) {
			start = 0;
		}
		int len = str.length();
		if (end > len) {
			end = len;
		}
		return doIndexOf(str, find, start, end);
	}

	public static int doIndexOf(CharSequence str, int find, int start) {
		return doIndexOf(str, find, start, str.length());
	}

	public static int doIndexOf(CharSequence str, int find, int start, int end) {
		// Note that the distinction between char and code point is not only for performance reasons,
		// but also needed to have the same behavior as Java string where String.indexOf() finds an occurrence of just a single surrogate char
		if (CodePointTools.isCharCodePoint(find)) {
			return doIndexOfChar(str, (char) find, start, end);
		} else {
			return doIndexOfCodePoint(str, find, start, end);
		}
	}

	public static int indexOf(CharSequence str, int find, CodePointEqual equal) {
		return doIndexOf(str, find, 0, equal);
	}

	public static int indexOf(CharSequence str, int find, int start, CodePointEqual equal) {
		if (start < 0) {
			start = 0;
		}
		return doIndexOf(str, find, start, equal);
	}

	public static int doIndexOf(CharSequence str, int find, int start, CodePointEqual equal) {
		// Note that the distinction between char and code point is not only for performance reasons,
		// but also needed to have the same behavior as Java string where String.indexOf() finds an occurrence of just a single surrogate char
		if (CodePointTools.isCharCodePoint(find)) {
			return doIndexOfChar(str, (char) find, start, equal.asCharEqual());
		} else {
			return doIndexOfCodePoint(str, find, start, equal);
		}
	}

	public static int doIndexOfCodePoint(CharSequence str, int find, int start) {
		return doIndexOfCodePoint(str, find, start, str.length());
	}

	public static int doIndexOfCodePoint(CharSequence str, int find, int start, int end) {
		assert CodePointTools.charCount(find) == 2;

		end--; // code point has lenght 2
		if (start >= end) {
			return -1;
		}

		char c0 = Character.highSurrogate(find);
		char c1 = Character.lowSurrogate(find);
		for (int i = start; i < end; i++) {
			char high = str.charAt(i);
			if (high == c0) {
				char low = str.charAt(i + 1);
				if (low == c1) {
					return i;
				}
			}
		}
		return -1;
	}

	public static int doIndexOfCodePoint(CharSequence str, int find, int start, CodePointEqual equal) {
		assert CodePointTools.charCount(find) == 2;

		int len = str.length();
		while (start < len) {
			int codePoint = CodePointTools.codePointAt(str, start);
			if (equal.isEqualCodePoint(codePoint, find)) {
				return start;
			}
			start += CodePointTools.charCount(codePoint);
		}

		return -1;
	}

	// IndexOf: code point predicate

	public static int indexOf(CharSequence str, IntPredicate predicate) {
		return doIndexOfCodePointPredicate(str, predicate, 0);
	}

	public static int indexOf(CharSequence str, IntPredicate predicate, int start) {
		if (start < 0) {
			start = 0;
		}
		return doIndexOfCodePointPredicate(str, predicate, start);
	}

	public static int doIndexOfCodePointPredicate(CharSequence str, IntPredicate finder, int start) {
		int len = str.length();
		if (start >= len) {
			return -1;
		}

		start = CodePointTools.getValidStartIndexForCodePoint(str, start, true);
		while (start < len) {
			int codePoint = CodePointTools.codePointAt(str, start);
			if (finder.test(codePoint)) {
				return start;
			}
			start += CodePointTools.charCount(codePoint);
		}

		return -1;
	}

	// IndexOf: String

	public static int indexOfString(String str, String find, int start) {
		return doIndexOfString(str, find, start);
	}

	public static int doIndexOfString(String str, String find, int start) {
		// Correct "abc".indexOf("", 9) == 3
		int index = str.indexOf(find, start);
		return (index >= start) ? index : -1;
	}

	public static int indexOf(CharSequence str, CharSequence find) {
		return doIndexOfCharSequence(str, find, 0);
	}

	public static int indexOf(CharSequence str, CharSequence find, CharEqual equal) {
		return doIndexOfCharSequence(str, find, 0, equal);
	}

	public static int indexOf(CharSequence str, CharSequence find, CodePointEqual equal) {
		return doIndexOfCharSequence(str, find, 0, equal);
	}

	public static int indexOf(CharSequence str, CharSequence find, int start) {
		if (start < 0) {
			start = 0;
		}
		return doIndexOfCharSequence(str, find, start);
	}

	public static int indexOf(CharSequence str, CharSequence find, int start, int end) {
		if (start < 0) {
			start = 0;
		}
		int len = str.length();
		if (end > len) {
			end = len;
		}
		return doIndexOfCharSequence(str, find, start, end);
	}

	public static int indexOf(CharSequence str, CharSequence find, int start, CharEqual equal) {
		if (start < 0) {
			start = 0;
		}
		return doIndexOfCharSequence(str, find, start, equal);
	}

	public static int indexOf(CharSequence str, CharSequence find, int start, CodePointEqual equal) {
		if (start < 0) {
			start = 0;
		}
		return doIndexOfCharSequence(str, find, start, equal);
	}

	//

	public static int doIndexOfCharSequence(CharSequence str, CharSequence find, int start) {
		return doIndexOfCharSequence(str, find, start, str.length());
	}

	public static int doIndexOfCharSequence(CharSequence str, CharSequence find, int start, CharEqual equal) {
		int strLen = str.length();
		int findLen = find.length();
		while (start + findLen <= strLen) {
			if (doEquals(str, start, find, 0, findLen, equal)) {
				return start;
			}
			start++;
		}
		return -1;
	}

	public static int doIndexOfCharSequence(CharSequence str, CharSequence find, int start, CodePointEqual equal) {
		return doIndexOfCharSequence(str, find, start, str.length());
	}

	public static int doIndexOfCharSequence(CharSequence str, CharSequence find, int start, int end, CodePointEqual equal) {
		// Assert start>=0, end <=str.length() 
		int findLen = find.length();
		while (start + findLen <= end) {
			if (doEquals(str, start, find, 0, findLen, equal)) {
				return start;
			}
			start++;
		}
		return -1;
	}

	//

	/** Returns index of find[0..find.len] in str[start..end], -1 if not found */
	public static int doIndexOfCharSequence(CharSequence str, CharSequence find, int strStart, int strEnd) {
		// Assert start>=0, end <=str.length() 
		return doIndexOfCharSequence(str, find, strStart, strEnd, 0, find.length());
	}

	/** Returns index of find[findStart..findEnd] in str[strStart..strEnd], -1 if not found */
	public static int doIndexOfCharSequence(CharSequence str, CharSequence find, int strStart, int strEnd, int findStart, int findEnd) {
		// Assert strStart>=0, strStart<=strEnd, strEnd <=str.length && findStart>=0, findStart<=findEnd, findEnd <=find.length
		int findLen = findEnd - findStart;
		while (strStart + findLen <= strEnd) {
			if (doEquals(str, strStart, find, findStart, findLen)) {
				return strStart;
			}
			strStart++;
		}
		return -1;
	}

	// MatchNot

	public static boolean matchNot(Collection<? extends CharSequence> ignores, CharSequence str, int matchStart, int matchLen) {
		for (CharSequence ignore : ignores) {
			if (CharSequenceTools.matchNot(ignore, str, matchStart, matchLen)) {
				return true;
			}
		}
		return false;
	}

	/** Return true if the match index/len in str is part of string matchNot */
	public static boolean matchNot(CharSequence matchNot, CharSequence str, int matchStart, int matchLen) {
		// If the match length is longer than matchNot, matchNot cannot be part of the match
		if (matchLen > matchNot.length()) {
			return false;
		}

		int start = 0;
		while (true) {
			start = doIndexOfCharSequence(matchNot, str, start, matchNot.length(), matchStart, matchStart + matchLen);
			if (start == -1) {
				break;
			}
			if (matchStart - start >= 0 && matchStart - start + matchNot.length() <= str.length()) {
				if (CharSequenceTools.equals(matchNot, 0, str, matchStart - start, matchNot.length())) {
					return true;
				}
			}
			start++;
		}
		return false;
	}

	// LastIndexOf / ReverseIndexOf

	// ReverseIndexOf:
	// Returns end index of last occurrence of find in the string, -1 if not found.
	// Searching starts at the specified position end going backward.
	// Method reverseIndexOf() is related to lastIndexOf(), in case of a match the following is true: <br>
	// reverseIndexOf(str, 'x', str.length()) == lastIndexOf(str, 'x', str.length()-1) + 1

	// LastIndexOf: char

	/**
	 * Returns start index of last occurrence of find in the string, -1 if not found.
	 * Searching starts at the specified position start going backward. 
	 */

	public static int lastIndexOf(CharSequence str, char find) {
		return doLastIndexOf(str, find, str.length() - 1);
	}

	public static int lastIndexOf(CharSequence str, char find, CharEqual equal) {
		return doLastIndexOf(str, find, str.length() - 1, equal);
	}

	public static int lastIndexOf(CharSequence str, char find, int start) {
		if (start < 0) {
			return -1;
		}
		return doLastIndexOf(str, find, Math.min(start, str.length() - 1));
	}

	public static int lastIndexOf(CharSequence str, char find, int start, CharEqual equal) {
		if (start < 0) {
			return -1;
		}
		return doLastIndexOf(str, find, Math.min(start, str.length() - 1), equal);
	}

	public static int doLastIndexOf(CharSequence str, char find, int start) {
		while (start >= 0) {
			if (find == str.charAt(start)) {
				return start;
			}
			start--;
		}
		return -1;
	}

	public static int doLastIndexOf(CharSequence str, char find, int start, CharEqual equal) {
		while (start >= 0) {
			if (equal.isEqualChar(str.charAt(start), find)) {
				return start;
			}
			start--;
		}
		return -1;
	}

	// ReverseIndexOf: char

	public static int reverseIndexOf(CharSequence str, char find) {
		return doReverseIndexOf(str, find, str.length());
	}

	public static int reverseIndexOf(CharSequence str, char find, CharEqual equal) {
		return doReverseIndexOf(str, find, str.length(), equal);
	}

	public static int reverseIndexOf(CharSequence str, char find, int end) {
		if (end <= 0) {
			return -1;
		}
		return doReverseIndexOf(str, find, Math.min(end, str.length()));
	}

	public static int reverseIndexOf(CharSequence str, char find, int end, CharEqual equal) {
		if (end <= 0) {
			return -1;
		}
		return doReverseIndexOf(str, find, Math.min(end, str.length()), equal);
	}

	public static int doReverseIndexOf(CharSequence str, char find, int end) {
		while (end > 0) {
			end--;
			if (find == str.charAt(end)) {
				return end + 1;
			}
		}
		return -1;
	}

	public static int doReverseIndexOf(CharSequence str, char find, int end, CharEqual equal) {
		while (end > 0) {
			end--;
			if (equal.isEqualChar(str.charAt(end), find)) {
				return end + 1;
			}
		}
		return -1;
	}

	// LastIndexOf: char predicate

	public static int lastIndexOf(CharSequence str, CharPredicate find) {
		return doLastIndexOf(str, find, str.length() - 1);
	}

	public static int lastIndexOf(CharSequence str, CharPredicate find, int start) {
		if (start < 0) {
			return -1;
		}
		return doLastIndexOf(str, find, Math.min(start, str.length() - 1));
	}

	public static int doLastIndexOf(CharSequence str, CharPredicate find, int start) {
		for (int i = start; i >= 0; i--) {
			if (find.test(str.charAt(i))) {
				return i;
			}
		}
		return -1;
	}

	// ReverseIndexOf: char predicate

	public static int reverseIndexOf(CharSequence str, CharPredicate find) {
		return doReverseIndexOf(str, find, str.length());
	}

	public static int reverseIndexOf(CharSequence str, CharPredicate find, int end) {
		if (end <= 0) {
			return -1;
		}
		return doReverseIndexOf(str, find, Math.min(end, str.length()));
	}

	public static int doReverseIndexOf(CharSequence str, CharPredicate find, int end) {
		while (end > 0) {
			end--;
			if (find.test(str.charAt(end))) {
				return end + 1;
			}
		}
		return -1;
	}

	// LastIndexOf: code point

	public static int lastIndexOf(CharSequence str, int find) {
		return doLastIndexOf(str, find, str.length() - 1);
	}

	public static int lastIndexOf(CharSequence str, int find, CodePointEqual equal) {
		return doLastIndexOf(str, find, str.length() - 1, equal);
	}

	public static int lastIndexOf(CharSequence str, int find, int start) {
		if (start < 0) {
			return -1;
		}
		return doLastIndexOf(str, find, Math.min(start, str.length() - 1));
	}

	public static int lastIndexOf(CharSequence str, int find, int start, CodePointEqual equal) {
		if (start < 0) {
			return -1;
		}
		return doLastIndexOf(str, find, Math.min(start, str.length() - 1), equal);
	}

	public static int doLastIndexOf(CharSequence str, int find, int start) {
		int len = str.length();
		if (len == 0) {
			return -1;
		}

		// Handle non supplementary characters specially as surrogates are rare 
		if (CodePointTools.isCharCodePoint(find)) {
			return doLastIndexOf(str, (char) find, start);
		}

		// Handle supplementary characters, also values above Character.MAX_CODE_POINT
		start = CodePointTools.getValidStartIndexForCodePoint(str, start, true);
		while (start >= 0) {
			int codePoint = CodePointTools.codePointAt(str, start);
			if (codePoint == find) {
				return start;
			}
			start -= CodePointTools.charCount(codePoint);
		}

		return -1;
	}

	public static int doLastIndexOf(CharSequence str, int find, int start, CodePointEqual equal) {
		int len = str.length();
		if (len == 0) {
			return -1;
		}

		// Handle non supplementary characters specially as surrogates are rare 
		if (CodePointTools.isCharCodePoint(find)) {
			return doLastIndexOf(str, (char) find, start, equal.asCharEqual());
		}

		// Handle supplementary characters, also values above Character.MAX_CODE_POINT
		start = CodePointTools.getValidStartIndexForCodePoint(str, start, true);
		while (start >= 0) {
			int codePoint = CodePointTools.codePointAt(str, start);
			if (equal.isEqualCodePoint(codePoint, find)) {
				return start;
			}
			start -= CodePointTools.charCount(codePoint);
		}

		return -1;
	}

	// ReverseIndexOf: code point

	public static int reverseIndexOf(CharSequence str, int find) {
		return doReverseIndexOf(str, find, str.length());
	}

	public static int reverseIndexOf(CharSequence str, int find, CodePointEqual equal) {
		return doReverseIndexOf(str, find, str.length(), equal);
	}

	public static int reverseIndexOf(CharSequence str, int find, int end) {
		if (end <= 0) {
			return -1;
		}
		return doReverseIndexOf(str, find, Math.min(end, str.length()));
	}

	public static int reverseIndexOf(CharSequence str, int find, int end, CodePointEqual equal) {
		if (end <= 0) {
			return -1;
		}
		return doReverseIndexOf(str, find, Math.min(end, str.length()), equal);
	}

	public static int doReverseIndexOf(CharSequence str, int find, int end) {
		int len = str.length();
		if (len == 0) {
			return -1;
		}

		// Handle non supplementary characters specially as surrogates are rare 
		if (CodePointTools.isCharCodePoint(find)) {
			return doReverseIndexOf(str, (char) find, end);
		}

		// Handle supplementary characters, also values above Character.MAX_CODE_POINT
		while (end > 0) {
			int codePoint = CodePointTools.codePointBefore(str, end - 1);
			if (codePoint == find) {
				return end;
			}
			end -= CodePointTools.charCount(codePoint);
		}
		return -1;
	}

	public static int doReverseIndexOf(CharSequence str, int find, int end, CodePointEqual equal) {
		int len = str.length();
		if (len == 0) {
			return -1;
		}

		// Handle non supplementary characters specially as surrogates are rare 
		if (CodePointTools.isCharCodePoint(find)) {
			return doReverseIndexOf(str, (char) find, end, equal.asCharEqual());
		}

		// Handle supplementary characters, also values above Character.MAX_CODE_POINT
		while (end > 0) {
			int codePoint = CodePointTools.codePointBefore(str, end - 1);
			if (equal.isEqualCodePoint(codePoint, find)) {
				return end;
			}
			end -= CodePointTools.charCount(codePoint);
		}
		return -1;
	}

	// LastIndexOf: code point predicate

	public static int lastIndexOf(CharSequence str, IntPredicate find) {
		return doLastIndexOf(str, find, str.length() - 1);
	}

	public static int lastIndexOf(CharSequence str, IntPredicate find, int start) {
		if (start < 0) {
			return -1;
		}
		return doLastIndexOf(str, find, Math.min(start, str.length() - 1));
	}

	public static int doLastIndexOf(CharSequence str, IntPredicate find, int start) {
		int len = str.length();
		if (len == 0) {
			return -1;
		}

		// Handle supplementary characters, also values above Character.MAX_CODE_POINT
		start = CodePointTools.getValidStartIndexForCodePoint(str, start, true);
		while (start >= 0) {
			int codePoint = CodePointTools.codePointAt(str, start);
			if (find.test(codePoint)) {
				return start;
			}
			start -= CodePointTools.charCount(codePoint);
		}

		return -1;
	}

	// ReverseIndexOf: code point predicate

	public static int reverseIndexOf(CharSequence str, IntPredicate find) {
		return doReverseIndexOf(str, find, str.length());
	}

	public static int reverseIndexOf(CharSequence str, IntPredicate find, int end) {
		if (end <= 0) {
			return -1;
		}
		return doReverseIndexOf(str, find, Math.min(end, str.length()));
	}

	public static int doReverseIndexOf(CharSequence str, IntPredicate find, int end) {
		int len = str.length();
		if (len == 0) {
			return -1;
		}

		// Handle supplementary characters, also values above Character.MAX_CODE_POINT
		while (end > 0) {
			int codePoint = CodePointTools.codePointBefore(str, end - 1);
			if (find.test(codePoint)) {
				return end;
			}
			end -= CodePointTools.charCount(codePoint);
		}
		return -1;
	}

	// LastIndexOf: string

	public static int lastIndexOf(CharSequence str, CharSequence find) {
		return doLastIndexOf(str, find, str.length() - 1);
	}

	public static int lastIndexOf(CharSequence str, CharSequence find, CharEqual equal) {
		return doLastIndexOf(str, find, str.length() - 1, equal);
	}

	public static int lastIndexOf(CharSequence str, CharSequence find, CodePointEqual equal) {
		return doLastIndexOf(str, find, str.length() - 1, equal);
	}

	public static int lastIndexOf(CharSequence str, CharSequence find, int start) {
		if (start < 0) {
			return -1;
		}
		return doLastIndexOf(str, find, Math.min(start, str.length() - 1));
	}

	public static int lastIndexOf(CharSequence str, CharSequence find, int start, CharEqual equal) {
		if (start < 0) {
			return -1;
		}
		return doLastIndexOf(str, find, Math.min(start, str.length() - 1), equal);
	}

	public static int lastIndexOf(CharSequence str, CharSequence find, int start, CodePointEqual equal) {
		if (start < 0) {
			return -1;
		}
		return doLastIndexOf(str, find, Math.min(start, str.length() - 1), equal);
	}

	public static int doLastIndexOf(CharSequence str, CharSequence find, int start) {
		int strLen = str.length();
		int findLen = find.length();
		if (findLen > strLen) {
			return -1;
		}

		start = Math.min(start, strLen - findLen);
		while (start >= 0) {
			if (startsAt(str, find, start)) {
				return start;
			}
			start--;//TODO code point
		}
		return -1;
	}

	public static int doLastIndexOf(CharSequence str, CharSequence find, int start, CharEqual equal) {
		int strLen = str.length();
		int findLen = find.length();
		if (findLen > strLen) {
			return -1;
		}

		start = Math.min(start, strLen - findLen);
		while (start >= 0) {
			if (startsAt(str, find, start, equal)) {
				return start;
			}
			start--;
		}
		return -1;
	}

	public static int doLastIndexOf(CharSequence str, CharSequence find, int start, CodePointEqual equal) {
		int strLen = str.length();
		int findLen = find.length();
		if (findLen > strLen) {
			return -1;
		}

		start = Math.min(start, strLen - findLen);
		while (start >= 0) {
			if (startsAt(str, find, start, equal)) {
				return start;
			}
			start--;
		}
		return -1;
	}

	// ReverseIndexOf: string

	public static int reverseIndexOf(CharSequence str, CharSequence find) {
		return doReverseIndexOf(str, find, str.length());
	}

	public static int reverseIndexOf(CharSequence str, CharSequence find, CharEqual equal) {
		return doReverseIndexOf(str, find, str.length(), equal);
	}

	public static int reverseIndexOf(CharSequence str, CharSequence find, CodePointEqual equal) {
		return doReverseIndexOf(str, find, str.length(), equal);
	}

	public static int reverseIndexOf(CharSequence str, CharSequence find, int end) {
		if (end < 0) {
			return -1;
		}
		return doReverseIndexOf(str, find, Math.min(end, str.length()));
	}

	public static int reverseIndexOf(CharSequence str, CharSequence find, int end, CharEqual equal) {
		if (end < 0) {
			return -1;
		}
		return doReverseIndexOf(str, find, Math.min(end, str.length()), equal);
	}

	public static int reverseIndexOf(CharSequence str, CharSequence find, int end, CodePointEqual equal) {
		if (end < 0) {
			return -1;
		}
		return doReverseIndexOf(str, find, Math.min(end, str.length()), equal);
	}

	public static int doReverseIndexOf(CharSequence str, CharSequence find, int end) {
		int findLen = find.length();
		if (findLen == 0) {
			return end;
		}
		int strLen = str.length();
		if (findLen > strLen) {
			return -1;
		}

		while (end > 0) {
			if (endsAt(str, find, end)) {
				return end;
			}
			end--;
		}
		return -1;
	}

	public static int doReverseIndexOf(CharSequence str, CharSequence find, int end, CharEqual equal) {
		int findLen = find.length();
		if (findLen == 0) {
			return end;
		}
		int strLen = str.length();
		if (findLen > strLen) {
			return -1;
		}

		while (end > 0) {
			if (endsAt(str, find, end, equal)) {
				return end;
			}
			end--;
		}
		return -1;
	}

	public static int doReverseIndexOf(CharSequence str, CharSequence find, int end, CodePointEqual equal) {
		int findLen = find.length();
		if (findLen == 0) {
			return end;
		}
		int strLen = str.length();
		if (findLen > strLen) {
			return -1;
		}

		while (end > 0) {
			if (endsAt(str, find, end, equal)) {
				return end;
			}
			end--;
		}
		return -1;
	}

	// StartsAt

	/** Determines whether str starts at specified position with find */
	public static boolean startsAt(CharSequence str, char find, int start) {
		if (start < 0) {
			return false;
		}
		return doStartsAt(str, find, start);
	}

	// TODO add doStartsAt for all paramter types
	public static boolean doStartsAt(CharSequence str, char find, int start) {
		if (start >= str.length()) {
			return false;
		}
		return str.charAt(start) == find;
	}

	public static boolean startsAt(CharSequence str, char find, int start, CharEqual equal) {
		if (start < 0 || start >= str.length()) {
			return false;
		}
		return equal.isEqualChar(str.charAt(start), find);
	}

	public static boolean startsAt(CharSequence str, int find, int start) {
		if (start < 0 || start >= str.length()) {
			return false;
		}
		return CodePointTools.codePointAt(str, start) == find;
	}

	public static boolean startsAt(CharSequence str, int find, int start, CodePointEqual equal) {
		if (start < 0 || start >= str.length()) {
			return false;
		}
		return equal.isEqualCodePoint(CodePointTools.codePointAt(str, start), find);
	}

	public static boolean startsAt(CharSequence str, CharPredicate find, int start) {
		if (start < 0 || start >= str.length()) {
			return false;
		}
		return find.test(str.charAt(start));
	}

	public static boolean startsAt(CharSequence str, IntPredicate find, int start) {
		if (start < 0 || start >= str.length()) {
			return false;
		}
		return find.test(CodePointTools.codePointAt(str, start));
	}

	public static boolean startsAt(CharSequence str, CharSequence find, int start) {
		if (start < 0) {
			return false;
		}
		int findLen = find.length();
		if (start + findLen > str.length()) {
			return false;
		}

		return doEquals(str, start, find, 0, findLen);
	}

	public static boolean startsAt(CharSequence str, CharSequence find, int start, CharEqual equal) {
		if (start < 0) {
			return false;
		}
		int findLen = find.length();
		if (start + findLen > str.length()) {
			return false;
		}

		return doEquals(str, start, find, 0, findLen, equal);
	}

	public static boolean startsAt(CharSequence str, CharSequence find, int start, CodePointEqual equal) {
		if (start < 0) {
			return false;
		}
		int findLen = find.length();
		if (start + findLen > str.length()) {
			return false;
		}

		return doEquals(str, start, find, 0, findLen, equal);
	}

	// EndsAt

	public static boolean endsAt(CharSequence str, char find, int end) {
		if (end <= 0) {
			return false;
		}
		return doEndsAt(str, find, end);
	}

	public static boolean doEndsAt(CharSequence str, char find, int end) {
		if (end > str.length()) {
			return false;
		}
		return str.charAt(end - 1) == find;
	}

	public static boolean endsAt(CharSequence str, char find, int end, CharEqual equal) {
		if (end <= 0 || end > str.length()) {
			return false;
		}
		return equal.isEqualChar(str.charAt(end - 1), find);
	}

	public static boolean endsAt(CharSequence str, int find, int end) {
		if (end <= 0 || end > str.length()) {
			return false;
		}
		return CodePointTools.codePointBefore(str, end - 1) == find;
	}

	public static boolean endsAt(CharSequence str, int find, int end, CodePointEqual equal) {
		if (end <= 0 || end > str.length()) {
			return false;
		}
		return equal.isEqualCodePoint(CodePointTools.codePointBefore(str, end - 1), find);
	}

	public static boolean endsAt(CharSequence str, CharPredicate find, int end) {
		if (end <= 0 || end > str.length()) {
			return false;
		}
		return find.test(str.charAt(end - 1));
	}

	public static boolean endsAt(CharSequence str, IntPredicate find, int end) {
		if (end <= 0 || end > str.length()) {
			return false;
		}
		return find.test(CodePointTools.codePointBefore(str, end - 1));
	}

	public static boolean endsAt(CharSequence str, CharSequence find, int end) {
		if (end < 0 || end > str.length()) {
			return false;
		}
		int findLen = find.length();
		if (end - findLen < 0) {
			return false;
		}

		int start = end - findLen;
		for (int i = findLen - 1; i >= 0; i--) {
			if (find.charAt(i) != str.charAt(start + i)) {
				return false;
			}
		}
		return true;
	}

	public static boolean endsAt(CharSequence str, CharSequence find, int end, CharEqual equal) {
		if (end < 0 || end > str.length()) {
			return false;
		}
		int findLen = find.length();
		if (end - findLen < 0) {
			return false;
		}

		int start = end - findLen;
		for (int i = findLen - 1; i >= 0; i--) {
			if (!equal.isEqualChar(find.charAt(i), str.charAt(start + i))) {
				return false;
			}
		}
		return true;
	}

	public static boolean endsAt(CharSequence str, CharSequence find, int end, CodePointEqual equal) {
		if (end < 0 || end > str.length()) {
			return false;
		}
		int findLen = find.length();
		if (end - findLen < 0) {
			return false;
		}

		while (findLen > 0) {
			int findCodePoint = CodePointTools.codePointBefore(find, findLen - 1);
			int strCodePoint = CodePointTools.codePointBefore(str, end - 1);
			if (!equal.isEqualCodePoint(findCodePoint, strCodePoint)) {
				return false;
			}
			end -= CodePointTools.charCount(strCodePoint);
			findLen -= CodePointTools.charCount(findCodePoint);
		}
		return true;
	}

	// StartsWith

	public static boolean startsWith(CharSequence str, char find) {
		return doStartsAt(str, find, 0);
	}

	public static boolean startsWith(CharSequence str, char find, CharEqual equal) {
		return startsAt(str, find, 0, equal);
	}

	public static boolean startsWith(CharSequence str, CharPredicate find) {
		return startsAt(str, find, 0);
	}

	public static boolean startsWith(CharSequence str, int find) {
		return startsAt(str, find, 0);
	}

	public static boolean startsWith(CharSequence str, int find, CodePointEqual equal) {
		return startsAt(str, find, 0, equal);
	}

	public static boolean startsWith(CharSequence str, IntPredicate find) {
		return startsAt(str, find, 0);
	}

	public static boolean startsWith(CharSequence str, CharSequence find) {
		return startsAt(str, find, 0);
	}

	public static boolean startsWith(CharSequence str, CharSequence find, CharEqual equal) {
		return startsAt(str, find, 0, equal);
	}

	public static boolean startsWith(CharSequence str, CharSequence find, CodePointEqual equal) {
		return startsAt(str, find, 0, equal);
	}

	// EndsWith

	public static boolean endsWith(CharSequence str, char find) {
		return doEndsAt(str, find, str.length());
	}

	public static boolean endsWith(CharSequence str, char find, CharEqual equal) {
		return endsAt(str, find, str.length(), equal);
	}

	public static boolean endsWith(CharSequence str, CharPredicate find) {
		return endsAt(str, find, str.length());
	}

	public static boolean endsWith(CharSequence str, int find) {
		return endsAt(str, find, str.length());
	}

	public static boolean endsWith(CharSequence str, int find, CodePointEqual equal) {
		return endsAt(str, find, str.length(), equal);
	}

	public static boolean endsWith(CharSequence str, IntPredicate find) {
		return endsAt(str, find, str.length());
	}

	public static boolean endsWith(CharSequence str, CharSequence find) {
		return endsAt(str, find, str.length());
	}

	public static boolean endsWith(CharSequence str, CharSequence find, CharEqual equal) {
		return endsAt(str, find, str.length(), equal);
	}

	public static boolean endsWith(CharSequence str, CharSequence find, CodePointEqual equal) {
		return endsAt(str, find, str.length(), equal);
	}

	// Remove

	// removeChar

	public static CharSequence removeChar(CharSequence str, char find) {
		return doRemoveChar(str, find, 0);
	}

	public static CharSequence removeChar(CharSequence str, char find, int start) {
		if (start < 0) {
			start = 0;
		}
		return doRemoveChar(str, find, start);
	}

	public static CharSequence removeChar(CharSequence str, char find, CharEqual equal) {
		return doRemoveCharPredicate(str, CharPredicates.equals(find, equal), 0);
	}

	public static CharSequence removeChar(CharSequence str, char find, CharEqual equal, int start) {
		if (start < 0) {
			start = 0;
		}
		return doRemoveCharPredicate(str, CharPredicates.equals(find, equal), start);
	}

	// removeCodePoint

	public static CharSequence removeCodePoint(CharSequence str, char find) {
		return doRemoveCodePoint(str, find, 0);
	}

	public static CharSequence removeCodePoint(CharSequence str, char find, int start) {
		if (start < 0) {
			start = 0;
		}
		return doRemoveCodePoint(str, find, start);
	}

	public static CharSequence removeCodePoint(CharSequence str, int find, CodePointEqual equal) {
		return doRemoveCodePointPredicate(str, CodePointPredicates.equals(find, equal), 0);
	}

	public static CharSequence removeCodePoint(CharSequence str, int find, CodePointEqual equal, int start) {
		if (start < 0) {
			start = 0;
		}
		return doRemoveCodePointPredicate(str, CodePointPredicates.equals(find, equal), start);
	}

	// removeCharPredicate

	public static CharSequence removeCharPredicate(CharSequence str, CharPredicate find) {
		return doRemoveCharPredicate(str, find, 0);
	}

	public static CharSequence removeCharPredicate(CharSequence str, CharPredicate find, int start) {
		if (start < 0) {
			start = 0;
		}
		return doRemoveCharPredicate(str, find, start);
	}

	// removeCodePointPredicate

	public static CharSequence removeCodePointPredicate(CharSequence str, IntPredicate find) {
		return doRemoveCodePointPredicate(str, find, 0);
	}

	public static CharSequence removeCodePointPredicate(CharSequence str, IntPredicate find, int start) {
		if (start < 0) {
			start = 0;
		}
		return doRemoveCodePointPredicate(str, find, start);
	}

	// removeString

	public static CharSequence removeString(CharSequence str, CharSequence find) {
		if (find.length() == 0) {
			return str;
		}
		return doRemoveString(str, find, 0, IndexOfString.indexOf());
	}

	public static CharSequence removeString(CharSequence str, CharSequence find, int start) {
		if (find.length() == 0) {
			return str;
		}
		if (start < 0) {
			start = 0;
		}
		return doRemoveString(str, find, start, IndexOfString.indexOf());
	}

	public static CharSequence removeString(CharSequence str, CharSequence find, CharEqual equal) {
		if (find.length() == 0) {
			return str;
		}
		return doRemoveString(str, find, 0, IndexOfString.indexOfChar(equal));
	}

	public static CharSequence removeString(CharSequence str, CharSequence find, CharEqual equal, int start) {
		if (find.length() == 0) {
			return str;
		}
		if (start < 0) {
			start = 0;
		}
		return doRemoveString(str, find, start, IndexOfString.indexOfChar(equal));
	}

	public static CharSequence removeString(CharSequence str, CharSequence find, CodePointEqual equal) {
		if (find.length() == 0) {
			return str;
		}
		return doRemoveString(str, find, 0, IndexOfString.indexOfCodePoint(equal));
	}

	public static CharSequence removeString(CharSequence str, CharSequence find, CodePointEqual equal, int start) {
		if (find.length() == 0) {
			return str;
		}
		if (start < 0) {
			start = 0;
		}
		return doRemoveString(str, find, start, IndexOfString.indexOfCodePoint(equal));
	}

	// doRemoveString

	public static CharSequence doRemoveString(CharSequence str, CharSequence find, int start, IndexOfString indexOfString) {
		int index = indexOfString.indexOf(str, find, start);
		if (index == -1) {
			return str; // not found at all -> no change to do
		}

		int strLen = str.length();
		int findLen = find.length();

		StringBuilder buf = new StringBuilder(strLen);
		while (index != -1) {
			buf.append(str, start, index);
			start = index + findLen;
			index = indexOfString.indexOf(str, find, start);
		}
		buf.append(str, start, strLen);
		return buf.toString();
	}

	// Remove Checked

	// removeCharChecked

	public static CharSequence removeCharChecked(CharSequence str, char find, int numChanges, boolean checkChanges) {
		return doRemoveCharChecked(str, find, 0, numChanges, checkChanges);
	}

	public static CharSequence removeCharChecked(CharSequence str, char find, int start, int numChanges, boolean checkChanges) {
		if (start < 0) {
			start = 0;
		}
		return doRemoveCharChecked(str, find, start, numChanges, checkChanges);
	}

	public static CharSequence removeCharChecked(CharSequence str, char find, CharEqual equal, int numChanges, boolean checkChanges) {
		return doRemoveCharPredicateChecked(str, CharPredicates.equals(find, equal), 0, numChanges, checkChanges);
	}

	public static CharSequence removeCharChecked(CharSequence str, char find, CharEqual equal, int start, int numChanges, boolean checkChanges) {
		if (start < 0) {
			start = 0;
		}
		return doRemoveCharPredicateChecked(str, CharPredicates.equals(find, equal), start, numChanges, checkChanges);
	}

	// removeCodePointChecked

	public static CharSequence removeCodePointChecked(CharSequence str, int find, int numChanges, boolean checkChanges) {
		return doRemoveCodePointChecked(str, find, 0, numChanges, checkChanges);
	}

	public static CharSequence removeCodePointChecked(CharSequence str, int find, int start, int numChanges, boolean checkChanges) {
		if (start < 0) {
			start = 0;
		}
		return doRemoveCodePointChecked(str, find, start, numChanges, checkChanges);
	}

	public static CharSequence removeCodePointChecked(CharSequence str, int find, CodePointEqual equal, int numChanges, boolean checkChanges) {
		return doRemoveCodePointPredicateChecked(str, CodePointPredicates.equals(find, equal), 0, numChanges, checkChanges);
	}

	public static CharSequence removeCodePointChecked(CharSequence str, int find, CodePointEqual equal, int start, int numChanges, boolean checkChanges) {
		if (start < 0) {
			start = 0;
		}
		return doRemoveCodePointPredicateChecked(str, CodePointPredicates.equals(find, equal), start, numChanges, checkChanges);
	}

	// removeCharPredicateChecked

	public static CharSequence removeCharPredicateChecked(CharSequence str, CharPredicate find, int numChanges, boolean checkChanges) {
		return doRemoveCharPredicateChecked(str, find, 0, numChanges, checkChanges);
	}

	public static CharSequence removeCharPredicateChecked(CharSequence str, CharPredicate find, int start, int numChanges, boolean checkChanges) {
		if (start < 0) {
			start = 0;
		}
		return doRemoveCharPredicateChecked(str, find, start, numChanges, checkChanges);
	}

	// removeCodePointPredicateChecked

	public static CharSequence removeCodePointPredicateChecked(CharSequence str, IntPredicate find, int numChanges, boolean checkChanges) {
		return doRemoveCodePointPredicateChecked(str, find, 0, numChanges, checkChanges);
	}

	public static CharSequence removeCodePointPredicateChecked(CharSequence str, IntPredicate find, int start, int numChanges, boolean checkChanges) {
		if (start < 0) {
			start = 0;
		}
		return doRemoveCodePointPredicateChecked(str, find, start, numChanges, checkChanges);
	}

	// doRemoveChar

	public static CharSequence doRemoveChar(CharSequence str, char find, int start) {
		int pos = CharSequenceTools.doIndexOfChar(str, find, start);
		if (pos == -1) {
			return str;
		}

		final char[] chars = CharSequenceTools.toCharArray(str);
		for (int i = pos + 1; i < chars.length; i++) {
			if (chars[i] != find) {
				chars[pos++] = chars[i];
			}
		}
		return new String(chars, 0, pos);
	}

	// doRemoveCodePoint

	public static CharSequence doRemoveCodePoint(CharSequence str, int find, int start) {
		int pos = doIndexOfCodePoint(str, find, start, str.length());
		if (pos == -1) {
			return str;
		}

		// Use pos/2 as start position as pos is in chars, but cps in code points
		final int[] cps = CodePointTools.getCodePointArray(str);
		for (int i = pos / 2; i < cps.length; i++) {
			if (cps[i] != find) {
				cps[pos++] = cps[i];
			}
		}
		return new String(cps, 0, pos);
	}

	// doRemoveCharPredicate

	public static CharSequence doRemoveCharPredicate(CharSequence str, CharPredicate predicate, int start) {
		int pos = doIndexOfCharPredicate(str, predicate, start);
		if (pos == -1) {
			return str;
		}

		final char[] chars = toCharArray(str);
		for (int i = pos + 1; i < chars.length; i++) {
			if (!predicate.test(chars[i])) {
				chars[pos++] = chars[i];
			}
		}
		return new String(chars, 0, pos);
	}

	// doRemoveCodePointPredicate

	public static CharSequence doRemoveCodePointPredicate(CharSequence str, IntPredicate predicate, int start) {
		int pos = doIndexOfCodePointPredicate(str, predicate, start);
		if (pos == -1) {
			return str;
		}

		// Use pos/2 as start position as pos is in chars, but cps in code points
		final int[] cps = CodePointTools.getCodePointArray(str);
		for (int i = pos / 2; i < cps.length; i++) {
			if (!predicate.test(cps[i])) {
				cps[pos++] = cps[i];
			}
		}
		return new String(cps, 0, pos);
	}

	// doRemoveCharChecked

	public static CharSequence doRemoveCharChecked(CharSequence str, char find, int start, int numChanges, boolean checkChanges) {
		// BEGIN numReplace/checkReplace
		if (numChanges == 0 && !checkChanges) {
			return str;
		}
		// END numReplace/checkReplace

		int pos = doIndexOfChar(str, find, start);
		if (pos == -1) {
			return str;
		}

		int num = 0;
		final char[] chars = toCharArray(str);
		for (int i = pos + 1; i < chars.length; i++) {
			if (chars[i] != find) {
				// keep
				chars[pos++] = chars[i];
			} else {
				// remove
				// BEGIN numReplace/checkReplace
				num++;
				if (!checkChanges) {
					if (num == numChanges) {
						break;
					}
				} else {
					if (num > numChanges && numChanges != -1) {
						return null; // return null as the number of replacement was wrong
					}
				}
				// END numReplace/checkReplace
			}
		}

		// BEGIN numReplace/checkReplace
		if (checkChanges) {
			if (numChanges == -1) {
				if (num == 0) {
					return null; // return null as no replacement has been made
				}
			} else {
				if (num != numChanges) {
					return null; // return null as the number of replacement was wrong
				}
			}
		}
		// END numReplace/checkReplace

		return new String(chars, 0, pos);
	}

	// doRemoveCodePointChecked

	public static CharSequence doRemoveCodePointChecked(CharSequence str, int find, int start, int numChanges, boolean checkChanges) {
		// BEGIN numReplace/checkReplace
		if (numChanges == 0 && !checkChanges) {
			return str;
		}
		// END numReplace/checkReplace

		int pos = doIndexOfCodePoint(str, find, start, str.length());
		if (pos == -1) {
			return str;
		}

		// Use pos/2 as start position as pos is in chars, but cps in code points
		int num = 0;
		final int[] cps = CodePointTools.getCodePointArray(str);
		for (int i = pos / 2; i < cps.length; i++) {
			if (cps[i] != find) {
				// keep
				cps[pos++] = cps[i];
			} else {
				// remove
				// BEGIN numReplace/checkReplace
				num++;
				if (!checkChanges) {
					if (num == numChanges) {
						break;
					}
				} else {
					if (num > numChanges && numChanges != -1) {
						return null; // return null as the number of replacement was wrong
					}
				}
				// END numReplace/checkReplace

			}
		}

		// BEGIN numReplace/checkReplace
		if (checkChanges) {
			if (numChanges == -1) {
				if (num == 0) {
					return null; // return null as no replacement has been made
				}
			} else {
				if (num != numChanges) {
					return null; // return null as the number of replacement was wrong
				}
			}
		}
		// END numReplace/checkReplace

		return new String(cps, 0, pos);
	}

	// doRemoveCharPredicateChecked

	public static CharSequence doRemoveCharPredicateChecked(CharSequence str, CharPredicate predicate, int start, int numChanges, boolean checkChanges) {
		// BEGIN numReplace/checkReplace
		if (numChanges == 0 && !checkChanges) {
			return str;
		}
		// END numReplace/checkReplace

		int pos = doIndexOfCharPredicate(str, predicate, start);
		if (pos == -1) {
			return str;
		}

		int num = 0;
		final char[] chars = toCharArray(str);
		for (int i = pos + 1; i < chars.length; i++) {
			if (!predicate.test(chars[i])) {
				// keep
				chars[pos++] = chars[i];
			} else {
				// remove
				// BEGIN numReplace/checkReplace
				num++;
				if (!checkChanges) {
					if (num == numChanges) {
						break;
					}
				} else {
					if (num > numChanges && numChanges != -1) {
						return null; // return null as the number of replacement was wrong
					}
				}
				// END numReplace/checkReplace
			}
		}

		// BEGIN numReplace/checkReplace
		if (checkChanges) {
			if (numChanges == -1) {
				if (num == 0) {
					return null; // return null as no replacement has been made
				}
			} else {
				if (num != numChanges) {
					return null; // return null as the number of replacement was wrong
				}
			}
		}
		// END numReplace/checkReplace

		return new String(chars, 0, pos);
	}

	// doRemoveCodePointPredicateChecked

	public static CharSequence doRemoveCodePointPredicateChecked(CharSequence str, IntPredicate predicate, int start, int numChanges, boolean checkChanges) {
		// BEGIN numReplace/checkReplace
		if (numChanges == 0 && !checkChanges) {
			return str;
		}
		// END numReplace/checkReplace

		int pos = doIndexOfCodePointPredicate(str, predicate, start);
		if (pos == -1) {
			return str;
		}

		// Use pos/2 as start position as pos is in chars, but cps in code points
		int num = 0;
		final int[] cps = CodePointTools.getCodePointArray(str);
		for (int i = pos / 2; i < cps.length; i++) {
			if (!predicate.test(cps[i])) {
				// keep
				cps[pos++] = cps[i];
			} else {
				// remove
				// BEGIN numReplace/checkReplace
				num++;
				if (!checkChanges) {
					if (num == numChanges) {
						break;
					}
				} else {
					if (num > numChanges && numChanges != -1) {
						return null; // return null as the number of replacement was wrong
					}
				}
				// END numReplace/checkReplace
			}
		}

		// BEGIN numReplace/checkReplace
		if (checkChanges) {
			if (numChanges == -1) {
				if (num == 0) {
					return null; // return null as no replacement has been made
				}
			} else {
				if (num != numChanges) {
					return null; // return null as the number of replacement was wrong
				}
			}
		}
		// END numReplace/checkReplace
		return new String(cps, 0, pos);
	}

	// removeStringChecked

	public static CharSequence removeStringChecked(CharSequence str, CharSequence findStr,
			int numChanges, boolean checkChanges) {
		return doRemoveStringChecked(str, findStr, 0, IndexOfString.indexOf(), numChanges, checkChanges);
	}

	public static CharSequence removeStringChecked(CharSequence str, CharSequence findStr, int start,
			int numChanges, boolean checkChanges) {
		if (start < 0) {
			start = 0;
		}
		return doRemoveStringChecked(str, findStr, start, IndexOfString.indexOf(), numChanges, checkChanges);
	}

	public static CharSequence removeStringChecked(CharSequence str, CharSequence find, CharEqual equal,
			int numChanges, boolean checkChanges) {
		return doRemoveStringChecked(str, find, 0, IndexOfString.indexOfChar(equal), numChanges, checkChanges);
	}

	public static CharSequence removeStringChecked(CharSequence str, CharSequence find, CharEqual equal, int start,
			int numChanges, boolean checkChanges) {
		if (start < 0) {
			start = 0;
		}
		return doRemoveStringChecked(str, find, start, IndexOfString.indexOfChar(equal), numChanges, checkChanges);
	}

	public static CharSequence removeStringChecked(CharSequence str, CharSequence find, CodePointEqual equal,
			int numChanges, boolean checkChanges) {
		return doRemoveStringChecked(str, find, 0, IndexOfString.indexOfCodePoint(equal), numChanges, checkChanges);
	}

	public static CharSequence removeStringChecked(CharSequence str, CharSequence find, CodePointEqual equal, int start,
			int numChanges, boolean checkChanges) {
		if (start < 0) {
			start = 0;
		}
		return doRemoveStringChecked(str, find, start, IndexOfString.indexOfCodePoint(equal), numChanges, checkChanges);
	}

	public static CharSequence doRemoveStringChecked(CharSequence str, CharSequence find, int start, IndexOfString indexOfString,
			int numChanges, boolean checkChanges) {

		// BEGIN numChanges/checkChanges
		if (numChanges == 0 && !checkChanges) {
			return str;
		}
		if (find.length() == 0) {
			return (checkChanges && numChanges != 0) ? null : str;
		}
		// END numChanges/checkChanges

		int index = indexOfString.indexOf(str, find, start);
		if (index == -1) {
			return str; // not found at all -> no change to do
		}

		int num = 0; // ADD numChanges/checkChanges
		int strLen = str.length();
		int findLen = find.length();
		StringBuilder buf = new StringBuilder(strLen);
		while (index != -1) {
			buf.append(str, start, index);
			start = index + findLen;
			index = indexOfString.indexOf(str, find, start);

			// BEGIN numChanges/checkChanges
			num++;
			if (!checkChanges) {
				if (num == numChanges) {
					break;
				}
			} else {
				if (num > numChanges && numChanges != -1) {
					return null; // return null as the number of replacement was wrong
				}
			}
			// END numChanges/checkChanges
		}

		// BEGIN numChanges/checkChanges
		if (checkChanges) {
			if (numChanges == -1) {
				if (num == 0) {
					return null; // return null as no replacement has been made
				}
			} else {
				if (num != numChanges) {
					return null; // return null as the number of replacement was wrong
				}
			}
		}
		// END numChanges/checkChanges

		buf.append(str, start, strLen);
		return buf.toString();
	}

	// Replace

	// replaceChar

	/**
	 * Replace all occurrences of findChar in str with replaceChar, starting at position start. <br>
	 * Note that the returned string will also include the characters in the range from position 0 to start. <br>
	 * If no replacements are made, the input string is returned unchanged.
	 */
	public static CharSequence replaceChar(CharSequence str, char find, char replace) {
		if (find == replace) {
			return str;
		}
		return doReplaceChar(str, find, replace, 0);
	}

	public static CharSequence replaceChar(CharSequence str, char find, char replace, int start) {
		if (find == replace) {
			return str;
		}
		if (start < 0) {
			start = 0;
		}
		return doReplaceChar(str, find, replace, start);
	}

	public static CharSequence replaceChar(CharSequence str, char find, char replace, CharEqual equal) {
		if (find == replace) {
			return str;
		}
		return doReplaceCharPredicate(str, CharPredicates.equals(find, equal), c -> replace, 0);
	}

	public static CharSequence replaceChar(CharSequence str, char find, char replace, CharEqual equal, int start) {
		if (find == replace) {
			return str;
		}
		if (start < 0) {
			start = 0;
		}
		return doReplaceCharPredicate(str, CharPredicates.equals(find, equal), c -> replace, start);
	}

	// replaceCharPredicate

	public static CharSequence replaceCharPredicate(CharSequence str, CharPredicate finder, CharOperator replacer) {
		return doReplaceCharPredicate(str, finder, replacer, 0);
	}

	public static CharSequence replaceCharPredicate(CharSequence str, CharPredicate finder, CharOperator replacer, int start) {
		if (start < 0) {
			start = 0;
		}
		return doReplaceCharPredicate(str, finder, replacer, start);
	}

	// doReplaceChar

	// Result will have same length as input
	public static CharSequence doReplaceChar(CharSequence str, char find, char replace, int start) {
		int pos = doIndexOfChar(str, find, start);
		if (pos == -1) {
			return str;
		}

		final char[] chars = toCharArray(str);
		chars[pos++] = replace;
		for (int i = pos; i < chars.length; i++) {
			if (chars[i] == find) {
				// replace
				chars[i] = replace;
			}
		}
		return new String(chars);
	}

	// doReplaceCharPredicate

	public interface CharFunctionTEST {

		/**
		 * Apply conversion to char.
		 * 
		 * @param c	source character
		 * @return character after conversion
		 */
		int apply(char c);
	}

	public static CharSequence doReplaceCharPredicateTEST(CharSequence str, CharPredicate finder, CharFunctionTEST replacer, int start) {
		int pos = doIndexOfCharPredicate(str, finder, start);
		if (pos == -1) {
			return str;
		}

		int len = str.length();
		char[] chars = toCharArray(str);
		int tgt = pos;
		for (int i = pos; i < len; i++) {
			int nc = replacer.apply(str.charAt(i));
			if (nc != -1) {
				chars[tgt++] = (char) nc;
			}
		}
		return new String(chars, 0, tgt);
	}

	// Result will have same length as input
	public static CharSequence doReplaceCharPredicate(CharSequence str, CharPredicate finder, CharOperator replacer, int start) {
		int pos = doIndexOfCharPredicate(str, finder, start);
		if (pos == -1) {
			return str;
		}

		final char[] chars = toCharArray(str);
		chars[pos] = replacer.apply(chars[pos]);
		pos++;
		for (int i = pos; i < chars.length; i++) {
			if (finder.test(chars[i])) {
				// replace
				chars[i] = replacer.apply(chars[i]);
			}
		}
		return new String(chars);
	}

	// Result can have different length as input
	public static CharSequence doReplaceCharPredicate(CharSequence str, CharPredicate finder, AppendCharReplacer replacer, int start) {
		int index = doIndexOfCharPredicate(str, finder, start);
		if (index == -1) {
			return str;
		}

		int base = 0;
		int len = str.length();
		StringBuilder buf = new StringBuilder(len);
		while (index != -1) {
			// replace
			char find = str.charAt(index);
			buf.append(str, base, index);
			replacer.replaceAppend(find, buf);
			base = index + 1;
			index = doIndexOfCharPredicate(str, finder, base);
		}

		buf.append(str, base, len);
		return buf.toString();
	}

	public static CharSequence doReplaceCharPredicateTEST(CharSequence str, CharPredicate finder, AppendCodePointReplacer replacer, int start) {
		int index = doIndexOfCharPredicate(str, finder, start);
		if (index == -1) {
			return str;
		}

		int len = str.length();
		StringBuilder buf = new StringBuilder(len);

		// replace
		int base = 0;
		char find = str.charAt(index);
		buf.append(str, base, index);
		replacer.replaceAppend(find, buf);
		index++;

		while (index < len) {
			find = str.charAt(index);
			if (finder.test(find)) {
				// replace
				buf.append(str, base, index);
				replacer.replaceAppend(find, buf);
				index++;
				base = index;
			} else {
				index++;
			}
		}

		buf.append(str, base, len);
		return buf.toString();
	}

	// replaceCodePoint

	public static CharSequence replaceCodePoint(CharSequence str, int find, int replace) {
		if (find == replace) {
			return str;
		}
		return doReplaceCodePointSwitch(str, find, replace, 0);
	}

	public static CharSequence replaceCodePoint(CharSequence str, int find, int replace, int start) {
		if (find == replace) {
			return str;
		}
		if (start < 0) {
			start = 0;
		}
		return doReplaceCodePointSwitch(str, find, replace, start);
	}

	public static CharSequence replaceCodePoint(CharSequence str, int find, int replace, CodePointEqual equal) {
		return doReplaceCodePointPredicate(str, CodePointPredicates.equals(find, equal), AppendCodePointReplacer.appendCodePoint(replace), 0);
	}

	public static CharSequence replaceCodePoint(CharSequence str, int find, int replace, CodePointEqual equal, int start) {
		if (start < 0) {
			start = 0;
		}
		return doReplaceCodePointPredicate(str, CodePointPredicates.equals(find, equal), AppendCodePointReplacer.appendCodePoint(replace), start);
	}

	// replaceCodePointPredicate

	public static CharSequence replaceCodePointPredicate(CharSequence str, IntPredicate find, IntUnaryOperator replacer) {
		return doReplaceCodePointPredicate(str, find, replacer, 0);
	}

	public static CharSequence replaceCodePointPredicate(CharSequence str, IntPredicate find, IntUnaryOperator replace, int start) {
		if (start < 0) {
			start = 0;
		}
		return doReplaceCodePointPredicate(str, find, replace, start);
	}

	// doReplaceCodePointSwitch

	public static CharSequence doReplaceCodePointSwitch(CharSequence str, int find, int replace, int start) {
		boolean isFindChar = CodePointTools.isCharCodePoint(find);
		boolean isReplaceChar = CodePointTools.isCharCodePoint(replace);
		if (!isFindChar && !isReplaceChar) {
			return doReplaceCodePoint(str, find, replace, start);
		} else if (isFindChar && isReplaceChar) {
			return doReplaceChar(str, (char) find, (char) replace, start);
		} else if (isFindChar) {
			return doReplaceCharPredicate(str, CharPredicates.equals((char) find), AppendCharReplacer.appendCodePoint(replace), start);
		} else {
			return doReplaceCodePointPredicate(str, CodePointPredicates.equals(find), AppendCodePointReplacer.appendChar((char) replace), start);
		}
	}

	// doReplaceCodePoint

	// Result will have same length as input
	public static CharSequence doReplaceCodePoint(CharSequence str, int find, int replace, int start) {
		assert CodePointTools.charCount(find) == 2;
		assert CodePointTools.charCount(replace) == 2;

		int pos = doIndexOfCodePoint(str, find, start, str.length());
		if (pos == -1) {
			return str;
		}

		// Use pos/2 as start position as pos is in chars, but cps in code points
		final int[] cps = CodePointTools.getCodePointArray(str);
		for (int i = pos / 2; i < cps.length; i++) {
			if (cps[i] == find) {
				// replace
				cps[pos] = replace;
			}
		}
		return new String(cps, 0, pos);
	}

	// Result will have same length as input
	public static CharSequence doReplaceCodePointPredicate(CharSequence str, IntPredicate finder, IntUnaryOperator replacer, int start) {
		int pos = doIndexOfCodePointPredicate(str, finder, start);
		if (pos == -1) {
			return str;
		}

		// Use pos/2 as start position as pos is in chars, but cps in code points
		final int[] cps = CodePointTools.getCodePointArray(str);
		for (int i = pos / 2; i < cps.length; i++) {
			if (finder.test(cps[i])) {
				// replace
				cps[pos] = replacer.applyAsInt(cps[i]);
			}
		}
		return new String(cps, 0, pos);
	}

	// doReplaceCodePointPredicate

	// Result can different length as input
	public static CharSequence doReplaceCodePointPredicate(CharSequence str, IntPredicate predicate, AppendCodePointReplacer replacer, int start) {
		int index = doIndexOfCodePointPredicate(str, predicate, start);
		if (index == -1) {
			return str;
		}

		int len = str.length();
		StringBuilder buf = new StringBuilder(len);

		// replace
		int base = 0;
		int find = CodePointTools.codePointAt(str, index);
		buf.append(str, base, index);
		replacer.replaceAppend(find, buf);
		index += CodePointTools.charCount(find);

		while (index < len) {
			find = CodePointTools.codePointAt(str, index);
			if (predicate.test(find)) {
				// replace
				buf.append(str, base, index);
				replacer.replaceAppend(find, buf);
				index += CodePointTools.charCount(find);
				base = index;
			} else {
				index += CodePointTools.charCount(find);
			}
		}
		buf.append(str, base, len);
		return buf.toString();
	}

	// replaceString

	public static CharSequence replaceString(CharSequence str, CharSequence find, CharSequence replace) {
		if (equals(find, replace)) {
			return str;
		}
		return doReplaceString(str, find, replace, 0, IndexOfString.indexOf());
	}

	public static CharSequence replaceString(CharSequence str, CharSequence find, CharSequence replace, int start) {
		if (equals(find, replace)) {
			return str;
		}
		if (start < 0) {
			start = 0;
		}
		return doReplaceString(str, find, replace, start, IndexOfString.indexOf());
	}

	public static CharSequence replaceString(CharSequence str, CharSequence find, CharSequence replace, CharEqual equal) {
		if (find.length() == 0) {
			return str;
		}
		return doReplaceString(str, find, replace, 0, IndexOfString.indexOfChar(equal));
	}

	public static CharSequence replaceString(CharSequence str, CharSequence find, CharSequence replace, CharEqual equal, int start) {
		if (start < 0) {
			start = 0;
		}
		return doReplaceString(str, find, replace, start, IndexOfString.indexOfChar(equal));
	}

	public static CharSequence replaceString(CharSequence str, CharSequence find, CharSequence replace, CodePointEqual equal) {
		return doReplaceString(str, find, replace, 0, IndexOfString.indexOfCodePoint(equal));
	}

	public static CharSequence replaceString(CharSequence str, CharSequence find, CharSequence replace, CodePointEqual equal, int start) {
		if (start < 0) {
			start = 0;
		}
		return doReplaceString(str, find, replace, start, IndexOfString.indexOfCodePoint(equal));
	}

	// doReplaceString

	public static CharSequence doReplaceString(CharSequence str, CharSequence find, CharSequence replace, int start, IndexOfString indexOfString) {
		return doReplaceString(str, find, AppendReplacer.appendString(replace), start, indexOfString);
	}

	public static CharSequence doReplaceString(CharSequence str, CharSequence find, AppendReplacer replacer, int start, IndexOfString indexOfString) {
		int index = indexOfString.indexOf(str, find, start);
		if (index == -1) {
			return str; // not found at all -> no change to do
		}

		int strLen = str.length();
		int findLen = find.length();
		int startOffset = (findLen == 0) ? 1 : 0; // guarantee termination if findStr is empty
		StringBuilder buf = new StringBuilder(strLen);
		while (index != -1) {
			buf.append(str, start, index);
			replacer.replaceAppend(buf);
			start = index + findLen;
			index = indexOfString.indexOf(str, find, start + startOffset);
		}
		buf.append(str, start, strLen);
		return buf.toString();
	}

	// replaceCharChecked

	/**
	 * Replace all occurrences of findChar in str with replaceChar, starting at position start. <br>
	 * Note that the returned string will also include the characters in the range from position 0 to start. <br>
	 * If no replacements are made, the input string is returned unchanged.
	 */
	public static CharSequence replaceCharChecked(CharSequence str, char find, char replace, int numChanges, boolean checkChanges) {
		if (find == replace) {
			return str;
		}
		return doReplaceCharChecked(str, find, replace, 0, numChanges, checkChanges);
	}

	public static CharSequence replaceCharChecked(CharSequence str, char find, char replace, int start, int numChanges, boolean checkChanges) {
		if (find == replace) {
			return str;
		}
		if (start < 0) {
			start = 0;
		}
		return doReplaceCharChecked(str, find, replace, start, numChanges, checkChanges);
	}

	public static CharSequence replaceCharChecked(CharSequence str, char find, char replace, CharEqual equal, int numChanges, boolean checkChanges) {
		if (find == replace) {
			return str;
		}
		return doReplaceCharPredicateChecked(str, CharPredicates.equals(find, equal), c -> replace, 0, numChanges, checkChanges);
	}

	public static CharSequence replaceCharChecked(CharSequence str, char find, char replace, CharEqual equal, int start, int numChanges, boolean checkChanges) {
		if (find == replace) {
			return str;
		}
		if (start < 0) {
			start = 0;
		}
		return doReplaceCharPredicateChecked(str, CharPredicates.equals(find, equal), c -> replace, start, numChanges, checkChanges);
	}

	// replaceCharPredicate

	public static CharSequence replaceCharPredicateChecked(CharSequence str, CharPredicate find, CharOperator replace, int numChanges, boolean checkChanges) {
		return doReplaceCharPredicateChecked(str, find, replace, 0, numChanges, checkChanges);
	}

	public static CharSequence replaceCharPredicateChecked(CharSequence str, CharPredicate find, CharOperator replace, int start, int numChanges,
			boolean checkChanges) {
		if (start < 0) {
			start = 0;
		}
		return doReplaceCharPredicateChecked(str, find, replace, start, numChanges, checkChanges);
	}

	// doReplaceChar

	public static CharSequence doReplaceCharChecked(CharSequence str, char find, char replace, int start, int numChanges, boolean checkChanges) {
		// BEGIN numChanges/checkChanges
		if (numChanges == 0 && !checkChanges) {
			return str;
		}
		// END numChanges/checkChanges

		int pos = doIndexOfChar(str, find, start);
		if (pos == -1) {
			return str;
		}

		int num = 0; // ADD numChanges/checkChanges
		final char[] chars = toCharArray(str);
		chars[pos++] = replace;
		for (int i = pos; i < chars.length; i++) {
			if (chars[i] == find) {
				// replace
				chars[i] = replace;

				// BEGIN numChanges/checkChanges
				num++;
				if (!checkChanges) {
					if (num == numChanges) {
						break;
					}
				} else {
					if (num > numChanges && numChanges != -1) {
						return null; // return null as the number of replacement was wrong
					}
				}
				// END numChanges/checkChanges
			}
		}

		// BEGIN numChanges/checkChanges
		if (checkChanges) {
			if (numChanges == -1) {
				if (num == 0) {
					return null; // return null as no replacement has been made
				}
			} else {
				if (num != numChanges) {
					return null; // return null as the number of replacement was wrong
				}
			}
		}
		// END numChanges/checkChanges

		return new String(chars);
	}

	// doReplaceCharPredicate

	public static CharSequence doReplaceCharPredicateChecked(CharSequence str, CharPredicate predicate, CharOperator operator, int start,
			int numChanges, boolean checkChanges) {

		// BEGIN numChanges/checkChanges
		if (numChanges == 0 && !checkChanges) {
			return str;
		}
		// END numChanges/checkChanges

		int pos = doIndexOfCharPredicate(str, predicate, start);
		if (pos == -1) {
			return str;
		}

		int num = 0; // ADD numChanges/checkChanges
		final char[] chars = toCharArray(str);
		chars[pos] = operator.apply(chars[pos]);
		pos++;
		for (int i = pos; i < chars.length; i++) {
			if (predicate.test(chars[i])) {
				// replace
				chars[i] = operator.apply(chars[i]);

				// BEGIN numChanges/checkChanges
				num++;
				if (!checkChanges) {
					if (num == numChanges) {
						break;
					}
				} else {
					if (num > numChanges && numChanges != -1) {
						return null; // return null as the number of replacement was wrong
					}
				}
				// END numChanges/checkChanges
			}
		}

		// BEGIN numChanges/checkChanges
		if (checkChanges) {
			if (numChanges == -1) {
				if (num == 0) {
					return null; // return null as no replacement has been made
				}
			} else {
				if (num != numChanges) {
					return null; // return null as the number of replacement was wrong
				}
			}
		}
		// END numChanges/checkChanges

		return new String(chars);
	}

	// Result can have different length as input
	public static CharSequence doReplaceCharPredicateChecked(CharSequence str, CharPredicate finder, AppendCharReplacer replacer, int start,
			int numChanges, boolean checkChanges) {

		// BEGIN numChanges/checkChanges
		if (numChanges == 0 && !checkChanges) {
			return str;
		}
		// END numChanges/checkChanges

		int index = doIndexOfCharPredicate(str, finder, start);
		if (index == -1) {
			return str;
		}

		int num = 0; // ADD numChanges/checkChanges
		int base = 0;
		int len = str.length();
		StringBuilder buf = new StringBuilder(len);
		while (index != -1) {
			// replace
			char find = str.charAt(index);
			buf.append(str, base, index);
			replacer.replaceAppend(find, buf);
			base = index + 1;
			index = doIndexOfCharPredicate(str, finder, base);

			// BEGIN numChanges/checkChanges
			num++;
			if (!checkChanges) {
				if (num == numChanges) {
					break;
				}
			} else {
				if (num > numChanges && numChanges != -1) {
					return null; // return null as the number of replacement was wrong
				}
			}
			// END numChanges/checkChanges
		}

		// BEGIN numChanges/checkChanges
		if (checkChanges) {
			if (numChanges == -1) {
				if (num == 0) {
					return null; // return null as no replacement has been made
				}
			} else {
				if (num != numChanges) {
					return null; // return null as the number of replacement was wrong
				}
			}
		}
		// END numChanges/checkChanges

		buf.append(str, base, len);
		return buf.toString();
	}

	// replaceCodePoint

	public static CharSequence replaceCodePointChecked(CharSequence str, int find, int replace, int numChanges, boolean checkChanges) {
		if (find == replace) {
			return str;
		}
		return doReplaceCodePointSwitchChecked(str, find, replace, 0, numChanges, checkChanges);
	}

	public static CharSequence replaceCodePointChecked(CharSequence str, int find, int replace, int start, int numChanges, boolean checkChanges) {
		if (find == replace) {
			return str;
		}
		if (start < 0) {
			start = 0;
		}
		return doReplaceCodePointSwitchChecked(str, find, replace, start, numChanges, checkChanges);
	}

	public static CharSequence replaceCodePointChecked(CharSequence str, int find, int replace, CodePointEqual equal, int numChanges, boolean checkChanges) {
		return doReplaceCodePointPredicateChecked(str, CodePointPredicates.equals(find, equal), c -> replace, 0, numChanges, checkChanges);
	}

	public static CharSequence replaceCodePointChecked(CharSequence str, int find, int replace, CodePointEqual equal, int start, int numChanges,
			boolean checkChanges) {
		if (start < 0) {
			start = 0;
		}
		return doReplaceCodePointPredicateChecked(str, CodePointPredicates.equals(find, equal), c -> replace, start, numChanges, checkChanges);
	}

	// replaceCodePointPredicate

	public static CharSequence replaceCodePointPredicateChecked(CharSequence str, IntPredicate find, IntUnaryOperator replace, int numChanges,
			boolean checkChanges) {
		return doReplaceCodePointPredicateChecked(str, find, replace, 0, numChanges, checkChanges);
	}

	public static CharSequence replaceCodePointPredicateChecked(CharSequence str, IntPredicate find, IntUnaryOperator replace, int start, int numChanges,
			boolean checkChanges) {
		if (start < 0) {
			start = 0;
		}
		return doReplaceCodePointPredicateChecked(str, find, replace, start, numChanges, checkChanges);
	}

	// doReplaceCodePointSwitch

	public static CharSequence doReplaceCodePointSwitchChecked(CharSequence str, int find, int replace, int start, int numChanges, boolean checkChanges) {
		boolean isFindChar = CodePointTools.isCharCodePoint(find);
		boolean isReplaceChar = CodePointTools.isCharCodePoint(replace);
		if (!isFindChar && !isReplaceChar) {
			return doReplaceCodePointChecked(str, find, replace, start, numChanges, checkChanges);
		} else if (isFindChar && isReplaceChar) {
			return doReplaceCharChecked(str, (char) find, (char) replace, start, numChanges, checkChanges);
		} else {
			return doReplaceCodePointMixedChecked(str, find, replace, start, numChanges, checkChanges);
		}
	}

	// doReplaceCodePoint

	public static CharSequence doReplaceCodePointChecked(CharSequence str, int find, int replace, int start, int numChanges, boolean checkChanges) {
		assert CodePointTools.charCount(find) == 2;
		assert CodePointTools.charCount(replace) == 2;

		// BEGIN numChanges/checkChanges
		if (numChanges == 0 && !checkChanges) {
			return str;
		}
		// END numChanges/checkChanges

		int pos = doIndexOfCodePoint(str, find, start, str.length());
		if (pos == -1) {
			return str;
		}

		int num = 0; // ADD numChanges/checkChanges
		// Use pos/2 as start position as pos is in chars, but cps in code points
		final int[] cps = CodePointTools.getCodePointArray(str);
		for (int i = pos / 2; i < cps.length; i++) {
			if (cps[i] != find) {
				cps[pos] = cps[i];
			} else {
				cps[pos] = replace;

				// BEGIN numChanges/checkChanges
				num++;
				if (!checkChanges) {
					if (num == numChanges) {
						break;
					}
				} else {
					if (num > numChanges && numChanges != -1) {
						return null; // return null as the number of replacement was wrong
					}
				}
				// END numChanges/checkChanges
			}
		}

		// BEGIN numChanges/checkChanges
		if (checkChanges) {
			if (numChanges == -1) {
				if (num == 0) {
					return null; // return null as no replacement has been made
				}
			} else {
				if (num != numChanges) {
					return null; // return null as the number of replacement was wrong
				}
			}
		}
		// END numChanges/checkChanges

		return new String(cps, 0, pos);
	}

	public static CharSequence doReplaceCodePointMixedChecked(CharSequence str, int find, int replace, int start, int numChanges, boolean checkChanges) {
		assert CodePointTools.charCount(find) + CodePointTools.charCount(replace) == 3;

		// BEGIN numChanges/checkChanges
		if (numChanges == 0 && !checkChanges) {
			return str;
		}
		// END numChanges/checkChanges

		int index = doIndexOf(str, find, start);
		if (index == -1) {
			return str;
		}

		int num = 0; // ADD numChanges/checkChanges
		int len = str.length();
		int base = 0;
		StringBuilder buf = new StringBuilder(len);
		while (index != -1) {
			buf.append(str, base, index);
			buf.appendCodePoint(replace);
			base = index + CodePointTools.charCount(find);
			index = doIndexOf(str, find, base);

			// BEGIN numChanges/checkChanges
			num++;
			if (!checkChanges) {
				if (num == numChanges) {
					break;
				}
			} else {
				if (num > numChanges && numChanges != -1) {
					return null; // return null as the number of replacement was wrong
				}
			}
			// END numChanges/checkChanges
		}

		// BEGIN numChanges/checkChanges
		if (checkChanges) {
			if (numChanges == -1) {
				if (num == 0) {
					return null; // return null as no replacement has been made
				}
			} else {
				if (num != numChanges) {
					return null; // return null as the number of replacement was wrong
				}
			}
		}
		// END numChanges/checkChanges

		buf.append(str, base, len);
		return buf.toString();
	}

	// doReplaceCodePointPredicateChecked

	public static CharSequence doReplaceCodePointPredicateChecked(CharSequence str, IntPredicate predicate, IntUnaryOperator operator, int start,
			int numChanges, boolean checkChanges) {

		// BEGIN numChanges/checkChanges
		if (numChanges == 0 && !checkChanges) {
			return str;
		}
		// END numChanges/checkChanges

		int index = doIndexOfCodePointPredicate(str, predicate, start);
		if (index == -1) {
			return str;
		}

		int num = 0; // ADD numChanges/checkChanges
		int len = str.length();
		int base = 0;
		StringBuilder buf = new StringBuilder(len);
		while (index != -1) {
			int find = CodePointTools.codePointAt(str, index);
			int replace = operator.applyAsInt(find);
			buf.append(str, base, index);
			buf.appendCodePoint(replace);
			base = index + CodePointTools.charCount(find);
			index = doIndexOfCodePointPredicate(str, predicate, base);

			// BEGIN numChanges/checkChanges
			num++;
			if (!checkChanges) {
				if (num == numChanges) {
					break;
				}
			} else {
				if (num > numChanges && numChanges != -1) {
					return null; // return null as the number of replacement was wrong
				}
			}
			// END numChanges/checkChanges
		}

		// BEGIN numChanges/checkChanges
		if (checkChanges) {
			if (numChanges == -1) {
				if (num == 0) {
					return null; // return null as no replacement has been made
				}
			} else {
				if (num != numChanges) {
					return null; // return null as the number of replacement was wrong
				}
			}
		}
		// END numChanges/checkChanges

		buf.append(str, base, len);
		return buf.toString();
	}

	// Result can different length as input
	public static CharSequence doReplaceCodePointPredicateChecked(CharSequence str, IntPredicate predicate, AppendCodePointReplacer replacer, int start,
			int numChanges, boolean checkChanges) {

		// BEGIN numChanges/checkChanges
		if (numChanges == 0 && !checkChanges) {
			return str;
		}
		// END numChanges/checkChanges

		int index = doIndexOfCodePointPredicate(str, predicate, start);
		if (index == -1) {
			return str;
		}

		int num = 0; // ADD numChanges/checkChanges
		int len = str.length();
		StringBuilder buf = new StringBuilder(len);

		// replace
		int base = 0;
		int find = CodePointTools.codePointAt(str, index);
		buf.append(str, base, index);
		replacer.replaceAppend(find, buf);
		index += CodePointTools.charCount(find);

		while (index < len) {
			find = CodePointTools.codePointAt(str, index);
			if (predicate.test(find)) {
				// replace
				buf.append(str, base, index);
				replacer.replaceAppend(find, buf);
				index += CodePointTools.charCount(find);
				base = index;

				// BEGIN numChanges/checkChanges
				num++;
				if (!checkChanges) {
					if (num == numChanges) {
						break;
					}
				} else {
					if (num > numChanges && numChanges != -1) {
						return null; // return null as the number of replacement was wrong
					}
				}
				// END numChanges/checkChanges
			} else {
				index += CodePointTools.charCount(find);
			}
		}

		// BEGIN numChanges/checkChanges
		if (checkChanges) {
			if (numChanges == -1) {
				if (num == 0) {
					return null; // return null as no replacement has been made
				}
			} else {
				if (num != numChanges) {
					return null; // return null as the number of replacement was wrong
				}
			}
		}

		// END numChanges/checkChanges
		buf.append(str, base, len);
		return buf.toString();
	}

	// replaceString

	public static CharSequence replaceStringChecked(CharSequence str, CharSequence find, CharSequence replace, int numChanges, boolean checkChanges) {
		if (equals(find, replace)) {
			return str;
		}
		return doReplaceStringChecked(str, find, replace, 0, IndexOfString.indexOf(), numChanges, checkChanges);
	}

	public static CharSequence replaceStringChecked(CharSequence str, CharSequence find, CharSequence replace, int start, int numChanges,
			boolean checkChanges) {
		if (equals(find, replace)) {
			return str;
		}
		if (start < 0) {
			start = 0;
		}
		return doReplaceStringChecked(str, find, replace, start, IndexOfString.indexOf(), numChanges, checkChanges);
	}

	public static CharSequence replaceStringChecked(CharSequence str, CharSequence find, CharSequence replace, CharEqual equal, int numChanges,
			boolean checkChanges) {
		if (find.length() == 0) {
			return str;
		}
		return doReplaceStringChecked(str, find, replace, 0, IndexOfString.indexOfChar(equal), numChanges, checkChanges);
	}

	public static CharSequence replaceStringChecked(CharSequence str, CharSequence find, CharSequence replace, CharEqual equal, int start, int numChanges,
			boolean checkChanges) {
		if (start < 0) {
			start = 0;
		}
		return doReplaceStringChecked(str, find, replace, start, IndexOfString.indexOfChar(equal), numChanges, checkChanges);
	}

	public static CharSequence replaceStringChecked(CharSequence str, CharSequence find, CharSequence replace, CodePointEqual equal, int numChanges,
			boolean checkChanges) {
		return doReplaceStringChecked(str, find, replace, 0, IndexOfString.indexOfCodePoint(equal), numChanges, checkChanges);
	}

	public static CharSequence replaceStringChecked(CharSequence str, CharSequence find, CharSequence replace, CodePointEqual equal, int start, int numChanges,
			boolean checkChanges) {
		if (start < 0) {
			start = 0;
		}
		return doReplaceStringChecked(str, find, replace, start, IndexOfString.indexOfCodePoint(equal), numChanges, checkChanges);
	}

	// doReplaceStringChecked

	public static CharSequence doReplaceStringChecked(CharSequence str, CharSequence find, CharSequence replace, int start, IndexOfString indexOfString,
			int numChanges, boolean checkChanges) {

		// BEGIN numChanges/checkChanges
		if (numChanges == 0 && !checkChanges) {
			return str;
		}
		// END numChanges/checkChanges

		int index = indexOfString.indexOf(str, find, start);
		if (index == -1) {
			return str; // not found at all -> no change to do
		}

		int num = 0; // ADD numChanges/checkChanges
		int strLen = str.length();
		int findLen = find.length();
		int replaceLen = replace.length();
		int startOffset = (findLen == 0) ? 1 : 0; // guarantee termination if findStr is empty
		int newLen = strLen - findLen + replaceLen;
		StringBuilder buf = new StringBuilder(newLen);
		while (index != -1) {
			buf.append(str, start, index);
			buf.append(replace);
			start = index + findLen;
			index = indexOfString.indexOf(str, find, start + startOffset);

			// BEGIN numChanges/checkChanges
			num++;
			if (!checkChanges) {
				if (num == numChanges) {
					break;
				}
			} else {
				if (num > numChanges && numChanges != -1) {
					return null; // return null as the number of replacement was wrong
				}
			}
			// END numChanges/checkChanges
		}

		// BEGIN numChanges/checkChanges
		if (checkChanges) {
			if (numChanges == -1) {
				if (num == 0) {
					return null; // return null as no replacement has been made
				}
			} else {
				if (num != numChanges) {
					return null; // return null as the number of replacement was wrong
				}
			}
		}
		// END numChanges/checkChanges

		buf.append(str, start, strLen);
		return buf.toString();
	}

	//-----------------

	/**
	 * Note that the predicate selects the part to be removed.
	 */
	public static CharSequence retainCharPredicate(CharSequence str, int start, CharPredicate predicate) {
		// FIXME refactor, test for match first
		StringBuilder buf = null;
		int len = str.length();
		int base = -1;
		for (int i = start; i < len; i++) {
			char c = str.charAt(i);
			if (predicate.test(c)) {
				// remove
				if (base == -1) {
					base = i;
				}
			} else {
				// retain
				if (base != -1) {
					if (buf == null) {
						buf = new StringBuilder(len);
					}
					buf.append(str, base, i);
				}
				base = -1;
			}
		}

		if (base == 0) {
			return str;
		} else if (base != -1) {
			if (buf == null) {
				buf = new StringBuilder(len);
			}
			buf.append(str, base, len);
			return buf.toString();
		} else {
			return (buf != null) ? buf.toString() : "";
		}
	}

	/**
	 * Note that the predicate selects the part to be removed.
	 */
	public static CharSequence retainCodePointPredicate(CharSequence str, int start, IntPredicate predicate) {
		// FIXME refactor, test for match first
		StringBuilder buf = null;
		int len = str.length();
		int base = -1;
		for (int i = start; i < len;) {
			int cp = CodePointTools.codePointAt(str, i);
			if (predicate.test(cp)) {
				// remove
				if (base == -1) {
					base = i;
				}
			} else {
				// retain
				if (base != -1) {
					if (buf == null) {
						buf = new StringBuilder(len);
					}
					buf.append(str, base, i);
				}
				base = -1;
			}
			i += CodePointTools.charCount(cp);
		}

		if (base == 0) {
			return str;
		} else if (base != -1) {
			if (buf == null) {
				buf = new StringBuilder(len);
			}
			buf.append(str, base, len);
			return buf.toString();
		} else {
			return (buf != null) ? buf.toString() : "";
		}
	}

	//

	public static CharSequence replaceAnyChar(CharSequence str, int start, String findAnyChar, String replaceAnyChar, CharIndexEqual indexer) {
		return doReplaceAnyChar(str, start, findAnyChar, replaceAnyChar, indexer);
	}

	/**
	 * If replace is true, replaceAnyChar is used for replacement.
	 * If replace is false, occurrences of findAnyChar are removed (replaceAnyChar is not used)
	 */
	public static CharSequence doReplaceAnyChar(CharSequence str, int start, String findAnyChar, String replaceAnyChar,
			CharIndexEqual indexer) {

		// Performance:
		// The use of char[] instead of StringBuilder can lead to slightly higher memory consumption if
		// the input contains only ASCII characters and StringBuilder can profit of compact string representation.
		// However the performance of char[] is superior.

		int pos;
		int len = str.length();
		int findCharIndex = 0;
		for (pos = start; pos < len; pos++) {
			findCharIndex = indexer.indexOf(findAnyChar, str.charAt(pos));
			if (findCharIndex >= 0) {
				break;
			}
		}
		if (pos >= len) {
			return str;
		}

		final char[] chars = toCharArray(str);
		int replaceCharsLen = replaceAnyChar.length();
		if (findCharIndex < replaceCharsLen) {
			chars[pos++] = replaceAnyChar.charAt(findCharIndex);
		}

		for (int i = pos; i < len; i++) {
			char c = str.charAt(i);
			findCharIndex = indexer.indexOf(findAnyChar, c);
			if (findCharIndex == -1) {
				chars[pos++] = c;
			} else {
				if (findCharIndex < replaceCharsLen) {
					chars[pos++] = replaceAnyChar.charAt(findCharIndex);
				}
			}
		}
		return new String(chars, 0, pos);
	}

	public static CharSequence doReplaceAnyCharTEST(CharSequence str, int start, CharFunctionTEST finder, IntUnaryOperator replacer) {

		// Performance:
		// The use of char[] instead of StringBuilder can lead to slightly higher memory consumption if
		// the input contains only ASCII characters and StringBuilder can profit of compact string representation.
		// However the performance of char[] is superior.

		int pos;
		int len = str.length();
		int findCharIndex = 0;
		for (pos = start; pos < len; pos++) {
			findCharIndex = finder.apply(str.charAt(pos));
			if (findCharIndex >= 0) {
				break;
			}
		}
		if (pos >= len) {
			return str;
		}

		final char[] chars = toCharArray(str, pos);
		int cc = replacer.applyAsInt(findCharIndex);
		if (cc != -1) {
			chars[pos++] = (char) cc;
		}

		for (int i = pos; i < len; i++) {
			char c = str.charAt(i);
			findCharIndex = finder.apply(c);
			if (findCharIndex == -1) {
				chars[pos++] = c;
			} else {
				int ccc = replacer.applyAsInt(findCharIndex);
				if (ccc != -1) {
					chars[pos++] = (char) cc;
				}
			}
		}
		return new String(chars, 0, pos);
	}

	public static char[] toCharArray(CharSequence str, int len) {
		//if (str instanceof String) {
		//return ((String) str).toCharArray();
		//}

		char[] cs = new char[str.length()];
		for (int i = 0; i < len; i++) {
			cs[i] = str.charAt(i);
		}
		return cs;
	}

	//

	public static CharSequence replaceString_OLD(CharSequence str, int start, int num, CharSequence target, CharSequence replacement) {
		// TODO from String.replace, do performance comparison
		String tgtStr = target.toString();
		String replStr = replacement.toString();
		int j = indexOf(str, tgtStr, start);
		if (j < 0) {
			return str;
		}
		int tgtLen = tgtStr.length();
		int tgtLen1 = Math.max(tgtLen, 1);
		int thisLen = str.length();

		int newLenHint = thisLen - tgtLen + replStr.length();
		if (newLenHint < 0) {
			throw new OutOfMemoryError();
		}
		StringBuilder sb = new StringBuilder(newLenHint);
		int i = 0;
		int n = 0;
		do {
			sb.append(str, i, j).append(replStr);
			i = j + tgtLen;

			n++;
			if (n == num) {
				break;
			}
		} while (j < thisLen && (j = indexOf(str, tgtStr, j + tgtLen1)) > 0);
		return sb.append(str, i, thisLen).toString();
	}

	//

	public static int commonLength(CharSequence str0, CharSequence str1) {
		int maxLen = Math.min(str0.length(), str1.length());
		if (maxLen == 0) {
			return 0;
		}

		int len = 0;
		while (len < maxLen && str0.charAt(len) == str1.charAt(len)) {
			len++;
		}
		return len;
	}

	public static int commonLength(CharSequence str0, int start0, CharSequence str1, int start1) {
		int maxLen = Math.min(str0.length() - start0, str1.length() - start1);
		if (maxLen == 0) {
			return 0;
		}

		int len = 0;
		while (len < maxLen && str0.charAt(start0 + len) == str1.charAt(start1 + len)) {
			len++;
		}
		return len;
	}

}