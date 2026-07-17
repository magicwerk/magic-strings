/*
 * Copyright 2010 by Thomas Mauch
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
package org.magicwerk.strings;

import java.util.Collection;

import org.magicwerk.brownies.collections.IList;
import org.magicwerk.strings.chars.CharPredicate;
import org.magicwerk.strings.chars.CodePointTools;
import org.magicwerk.strings.helper.FuncTools;

/**
 * Class {@link StringTools} contains tools for working with {@link String}s.
 */
public class StringTools {

	/** Character used as substitution for missing null in primitive data type char. */
	public static final char NOT_A_CHAR = 0xffff;

	/** Character used as substitution for missing null in primitive data type int representing a code point.  */
	public static final int NOT_A_CODEPOINT = -1;

	/**
	 * Replacement character (U+FFFD) 65533 used in Unicode.
	 * A replacement character can be introduced if a string is constructed using "String(bytes[], charset)"
	 * and some bytes cannot be represented in the charset.
	 */
	public static final char REPLACEMENT_CHAR = 0xFFFD;

	//

	// Substring before / after

	public static String substringBefore(CharSequence str, String sep) {
		// StringUtils.substringBefore(str, sep) // TODO
		return StringSplitter.builder().setFindString(sep).build().getFirst(str);
	}

	public static String substringAfter(CharSequence str, String sep) {
		// StringUtils.substringAfter(str, sep) // TODO
		return StringSplitter.builder().setFindString(sep).setPartNullIfNoMatch(false).build().getAfterFirst(str);
	}

	// Split

	public static IList<String> split(String str, char sep) {
		return StringSplitter.builder().setFindChar(sep).build().split(str); // TODO replace call sites
	}

	public static IList<String> split(String str, String sep) {
		return StringSplitter.builder().setFindString(sep).build().split(str); // TODO replace call sites
	}

	//

	/** Returns true if string is null or empty */
	public static boolean isEmpty(CharSequence str) {
		return str == null || str.length() == 0;
	}

	/** Returns true if string is null or blank */
	public static boolean isBlank(CharSequence str) {
		if (str != null) {
			int len = str.length();
			for (int i = 0; i < len; i++) {
				if (Character.isWhitespace(str.charAt(i))) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Return length of string, 0 if string is null
	 */
	public static int length(CharSequence str) {
		return (str != null) ? str.length() : 0;
	}

	/**
	 * Returns character at specified position.
	 * If position is out of valid range or string is null, {@link #NOT_A_CHAR} is returned.
	 *
	 * @param str	string
	 * @param pos	character position
	 * @return		character at specified position or {@link #NOT_A_CHAR}
	 */
	public static char charAt(CharSequence str, int pos) {
		if (str != null && pos >= 0 && pos < str.length()) {
			return str.charAt(pos);
		}
		return NOT_A_CHAR;
	}

	/**
	 * Returns character at specified position starting at the end.
	 * If position is out of valid range or string is null, {@link #NOT_A_CHAR} is returned.
	 *
	 * @param str	string
	 * @param pos	character position starting at the end (0 means last position)
	 * @return		character at specified position or {@link #NOT_A_CHAR}
	 */
	// TODO use negative positions for atEnd?
	public static char charAtEnd(CharSequence str, int pos) {
		if (str != null) {
			int len = str.length();
			if (pos >= 0 && pos < len) {
				return str.charAt(len - pos - 1);
			}
		}
		return NOT_A_CHAR;
	}

	/**
	 * Returns first character of string. If string is null or empty, {@link #NOT_A_CHAR} is returned.
	 * 
	 * @param str	string
	 * @return		first character of string or {@link #NOT_A_CHAR}
	 */
	public static char firstChar(CharSequence str) {
		if (str == null || str.length() == 0) {
			return NOT_A_CHAR;
		} else {
			return str.charAt(0);
		}
	}

	/**
	 * Returns last character of string. If string is null or empty, {@link #NOT_A_CHAR} is returned.
	 * 
	 * @param str	string
	 * @return		last character of string or {@link #NOT_A_CHAR}
	 */
	public static char lastChar(CharSequence str) {
		if (str == null || str.length() == 0) {
			return NOT_A_CHAR;
		} else {
			return str.charAt(str.length() - 1);
		}
	}

	/** 
	 * Returns the single char representing the string.
	 * If the string cannot be represented by a single char, NOT_A_CHAR is returned.
	 */
	public static char singleChar(CharSequence str) {
		if (str == null || str.length() != 1) {
			return NOT_A_CHAR;
		} else {
			return str.charAt(0);
		}
	}

	/** 
	 * Returns the single code point representing the string.
	 * If the string cannot be represented by a single code point, NOT_A_CODEPOINT is returned.
	 */
	public static int singleCodePoint(CharSequence str) {
		if (str == null || (str.length() != 1 && str.length() != 2)) {
			return NOT_A_CODEPOINT;
		}
		int cp = CodePointTools.codePointAt(str, 0);
		if (CodePointTools.charCount(cp) != str.length()) {
			return NOT_A_CODEPOINT;
		}
		return cp;
	}

	//

	/** Call contains(). If str is null, false is returned */
	public static boolean contains(String str, String find) {
		return (str != null) ? str.contains(find) : false;
	}

	/** Call indexOf(). If str is null, -1 is returned */
	public static int indexOf(String str, String find) {
		return (str != null) ? str.indexOf(find) : -1;
	}

	/** Call indexOf(). If str is null, -1 is returned */
	public static int indexOf(String str, char find) {
		return (str != null) ? str.indexOf(find) : -1;
	}

	/** Call startsWith(). If str is null, false is returned */
	public static boolean startsWith(String str, String prefix) {
		return (str != null) ? str.startsWith(prefix) : false;
	}

	/** Call endsWith(). If str is null, false is returned */
	public static boolean endsWith(String str, String suffix) {
		return (str != null) ? str.endsWith(suffix) : false;
	}

	public static boolean startsWith(CharSequence str, char c) {
		return (!isEmpty(str)) ? (str.charAt(0) == c) : false;
	}

	public static boolean endsWith(CharSequence str, char c) {
		return (!isEmpty(str)) ? (str.charAt(str.length() - 1) == c) : false;
	}

	// Reverse

	// new StringBuilder(hi).reverse().toString()
	// FIXME support surrogates

	public static String reverse(String str) {
		int len = length(str);
		if (str == null && len <= 1) {
			return str;
		}
		return doReverse(str, len);
	}

	public static CharSequence reverse(CharSequence str) {
		int len = length(str);
		if (str == null && len <= 1) {
			return str;
		}
		return doReverse(str, len);
	}

	static String doReverse(CharSequence str, int len) {
		char[] cs = new char[len];
		int src = 0;
		int tgt = len - 1;
		while (src < len) {
			char c = str.charAt(src);
			boolean surrogate = false;
			if (Character.isHighSurrogate(c)) {
				char c2 = str.charAt(src + 1);
				if (Character.isLowSurrogate(c2)) {
					cs[tgt - 1] = c;
					cs[tgt] = c2;
					src += 2;
					tgt -= 2;
					surrogate = true;
				}
			}
			if (!surrogate) {
				cs[tgt] = c;
				src++;
				tgt--;
			}
		}
		return new String(cs);
	}

	// Common head / tail

	// Guava: commonPrefix / commonSuffix 
	// FIXME handle surrogates

	// TODO like trim, but return instead of remove
	public static String getHead(String str, CharPredicate cp) {
		if (str == null) {
			return null;
		}

		int i;
		for (i = 0; i < str.length(); i++) {
			if (!cp.test(str.charAt(i))) {
				break;
			}
		}

		if (i == 0) {
			return "";
		} else {
			return str.substring(0, i);
		}
	}

	// Repeat

	public static String repeat(char c, int count) {
		return repeat(String.valueOf(c), count, "");
	}

	public static String repeat(String str, int count) {
		return repeat(str, count, "");
	}

	/**
	 * Creates a new string by appending the argument string several times. Each after appending the string (except at the end) a separator is added.
	 *
	 * @param str string to multiply
	 * @param count times to multiply
	 * @param separator separator string
	 * @return created string
	 */
	public static String repeat(String str, int count, String separator) {
		// Argument checking like StringUtils.repeat
		if (str == null) {
			return null;
		}
		if (count <= 0) {
			return "";
		}
		StringBuilder buf = new StringBuilder(count * str.length() + (count - 1) * separator.length());
		for (int i = 0; i < count; i++) {
			if (i > 0) {
				buf.append(separator);
			}
			buf.append(str);
		}
		return buf.toString();
	}

	// Join

	/** Join all strings without adding a separator */
	public static String join(Collection<? extends CharSequence> strings) {
		return join(strings, "");
	}

	public static String join(Collection<? extends CharSequence> strings, String separator) {
		if (separator == null) {
			separator = "";
		}
		StringBuilder buf = new StringBuilder();
		boolean add = false;
		for (CharSequence string : strings) {
			if (add) {
				buf.append(separator);
			} else {
				add = true;
			}
			buf.append(string);
		}
		return buf.toString();
	}

	public static String join(String separator, String... strings) {
		if (separator == null) {
			separator = "";
		}
		StringBuilder buf = new StringBuilder();
		for (String string : strings) {
			if (string != null && string.length() > 0) {
				if (buf.length() > 0) {
					buf.append(separator);
				}
				buf.append(string);
			}
		}
		return buf.toString();
	}

	// Concat

	public static CharSequence concat(CharSequence str0, CharSequence str1) {
		int len1 = CharSequenceTools.length(str1);
		if (len1 == 0) {
			return str0;
		}
		int len0 = CharSequenceTools.length(str0);
		if (len0 == 0) {
			return str1;
		}
		StringBuilder buf = new StringBuilder(len0 + len1);
		buf.append(str0).append(str1);
		return buf.toString();
	}

	public static String concat(CharSequence str0, CharSequence str1, CharSequence str2) {
		int len = str0.length() + str1.length() + str2.length();
		StringBuilder buf = new StringBuilder(len);
		buf.append(str0).append(str1).append(str2);
		return buf.toString();
	}

	public static String concat(CharSequence... strs) {
		int len = 0;
		for (CharSequence str : strs) {
			len += str.length();
		}
		StringBuilder buf = new StringBuilder(len);
		for (CharSequence str : strs) {
			buf.append(str);
		}
		return buf.toString();
	}

	// Left / Right

	public static String left(String str, int len) {
		return (String) left((CharSequence) str, len);
	}

	public static CharSequence left(CharSequence str, int len) {
		if (str == null) {
			return null;
		}
		int maxLen = str.length();
		int end = (len >= 0) ? len : maxLen + len;
		if (end > maxLen) {
			end = maxLen;
		} else if (end < 0) {
			end = 0;
		}
		return str.subSequence(0, end);
	}

	public static String right(String str, int len) {
		return (String) right((CharSequence) str, len);
	}

	public static CharSequence right(CharSequence str, int len) {
		if (str == null) {
			return null;
		}
		int maxLen = str.length();
		int start = (len >= 0) ? maxLen - len : -len;
		if (start > maxLen) {
			start = maxLen;
		} else if (start < 0) {
			start = 0;
		}
		return str.subSequence(start, maxLen);
	}

	public static String removeLeft(String str, int len) {
		return (String) removeLeft((CharSequence) str, len);
	}

	public static String removeRight(String str, int len) {
		return (String) removeRight((CharSequence) str, len);
	}

	public static CharSequence removeLeft(CharSequence str, int len) {
		if (str == null) {
			return null;
		}
		int maxLen = str.length();
		int remove = (len > maxLen) ? maxLen : len;
		return str.subSequence(remove, maxLen);
	}

	public static CharSequence removeRight(CharSequence str, int len) {
		if (str == null) {
			return null;
		}
		int maxLen = str.length();
		int remove = (len > maxLen) ? maxLen : len;
		return str.subSequence(0, maxLen - remove);
	}

	// Substring / mid

	public static String mid(String str, int pos, int len) {
		return (String) mid((CharSequence) str, pos, len);
	}

	/** Method mid() allows str to be empty and pos and len to be out of bounds */
	public static CharSequence mid(CharSequence str, int pos, int len) {
		if (str == null) {
			return null;
		}
		int start, end;
		if (len >= 0) {
			start = pos;
			end = pos + len;
			if (end < 0) {
				end = Integer.MAX_VALUE;
			}
		} else {
			start = pos + len;
			end = pos;
		}
		int maxLen = str.length();
		if (start > maxLen) {
			start = maxLen;
		} else if (start < 0) {
			start = 0;
		}
		if (end > maxLen) {
			end = maxLen;
		} else if (end < 0) {
			end = 0;
		}
		return str.subSequence(start, end);
	}

	// Remove

	/**
	 * Remove all occurrences of specified character from string.
	 * If the character is not found, the original string is returned and no allocation is done.
	 */
	public static String removeChar(String str, char c) {
		StringRemover remover = StringRemover.build(b -> b.setFindChar(c));
		return remover.remove(str);

		//		if (str == null) {
		//			return null;
		//		}
		//
		//		StringBuilder buf = null;
		//		int len = str.length();
		//		int start = 0;
		//		for (int i = 0; i < len; i++) {
		//			if (str.charAt(i) == c) {
		//				if (buf == null) {
		//					buf = new StringBuilder();
		//				}
		//				buf.append(str, start, i);
		//				start = i + 1;
		//			}
		//		}
		//		if (buf == null) {
		//			return str;
		//		}
		//		buf.append(str, start, len);
		//		return buf.toString();
	}

	public static String removeChar(String str, CharPredicate predicate) {
		StringRemover remover = StringRemover.build(b -> b.setFindCharPredicate(predicate));
		return remover.remove(str);

		//		if (str == null) {
		//			return null;
		//		}
		//
		//		StringBuilder buf = null;
		//		int len = str.length();
		//		int start = 0;
		//		for (int i = 0; i < len; i++) {
		//			if (predicate.test(str.charAt(i))) {
		//				if (buf == null) {
		//					buf = new StringBuilder();
		//				}
		//				buf.append(str, start, i);
		//				start = i + 1;
		//			}
		//		}
		//		if (buf == null) {
		//			return str;
		//		}
		//		buf.append(str, start, len);
		//		return buf.toString();
	}

	/**
	 * Return string without all occurrences of part.
	 * If the string does not contain part, null is returned.
	 *
	 * @param str	input string
	 * @param part	string to remove in input string
	 * @return		input string with part removed or null
	 */
	public static String remove(String str, String part) {
		StringRemover remover = StringRemover.build(b -> b.setFindString(part));
		return remover.remove(str);

		//		int pos = str.indexOf(part);
		//		if (pos == -1) {
		//			return str;
		//		}
		//		StringBuilder buf = new StringBuilder();
		//		int start = 0;
		//		while (pos != -1) {
		//			buf.append(str.substring(start, pos));
		//			start = pos + part.length();
		//			pos = str.indexOf(part, start);
		//		}
		//		buf.append(str.substring(start));
		//		return buf.toString();
	}

	// Replace

	public static String replaceChars(String str, String searchChars, String replaceChars) {
		StringReplacer replaceAnyChar = StringReplacer.build(b -> b.replaceAnyChar(searchChars, replaceChars));
		return replaceAnyChar.replace(str); // TODO
	}

	/** 
	 * Replaces each occurrence of search with replace. 
	 * If any of the strings is null, the input string is returned unchanged.
	 */
	public static String replace(String str, CharSequence search, CharSequence replace) {
		if (str == null || search == null || replace == null) {
			return str;
		}
		// TODO toString() is called on search/replace
		return str.replace(search, replace);
	}

	/** 
	 * Replaces each occurrence of search with replace. 
	 * If the input string is null, it is returned unchanged.
	 */
	public static String replace(String str, char search, char replace) {
		if (str == null) {
			return str;
		}
		return str.replace(search, replace);
	}

	// Head / Tail

	/**
	 * Add head if string does not start with it, otherwise return string unchanged.
	 */
	public static String addHead(String str, String head) {
		return (str.startsWith(head)) ? str : head + str;
	}

	/**
	 * Add tail if string does not end with it, otherwise return string unchanged.
	 */
	public static String addTail(String str, String tail) {
		return (str.endsWith(tail)) ? str : str + tail;
	}

	/** Remove head and tail if both are present, otherwise null is returned */
	public static String removeHeadTail(String str, String head, String tail) {
		if (str.length() >= head.length() + tail.length() && str.startsWith(head) && str.endsWith(tail)) {
			return str.substring(head.length(), str.length() - tail.length());
		}
		return null;
	}

	/** Remove head and tail if both are present, otherwise the string is returned unchanged */
	public static String removeHeadTailIf(String str, String head, String tail) {
		String result = removeHeadTail(str, head, tail);
		return FuncTools.nvl(result, str);
	}

	/**
	 * Return string without leading head.
	 * If the string does not start with head, null is returned.
	 *
	 * @param str	input string
	 * @param head	string to remove at head of input string
	 * @return		input string with head removed or null
	 */
	public static String removeHead(String str, String head) {
		if (head == null) {
			return str;
		}
		if (str != null && str.startsWith(head)) {
			return str.substring(head.length());
		} else {
			return null;
		}
	}

	/**
	 * Return string without leading head.
	 * If the string does not start with head, the string is returned unchanged.
	 *
	 * @param str	input string
	 * @param head	string to remove at head of input string
	 * @return		input string with head removed or input string unchanged
	 */
	public static String removeHeadIf(String str, String head) {
		String result = removeHead(str, head);
		return FuncTools.nvl(result, str);
	}

	/**
	 * If string ends with tail, tail is removed and the remaining string
	 * is returned. Otherwise null is returned.
	 *
	 * @param str	string
	 * @param tail	tail of string 
	 * @return		string without tail or null
	 */
	public static String removeTail(String str, String tail) {
		if (tail == null) {
			return str;
		}
		if (str != null && str.endsWith(tail)) {
			return str.substring(0, str.length() - tail.length());
		} else {
			return null;
		}
	}

	/**
	 * If string ends with tail, tail is removed and the remaining string
	 * is returned. Otherwise the string is returned unchanged.
	 *
	 * @param str   string
	 * @param tail  tail of string
	 * @return      string without tail or unchanged string
	 */
	public static String removeTailIf(String str, String tail) {
		String result = removeTail(str, tail);
		return FuncTools.nvl(result, str);
	}

	// Head / Tail
	// TODO replace with StringSplitter?

	//public static String getHeadBy(String str, String split)
	//public static String getHeadByIf(String str, String split) {
	//public static String getTailByIf(String str, String split) {

	public static String getTailBy(String str, String split) {
		int index = str.lastIndexOf(split);
		if (index != -1) {
			return str.substring(index + split.length());
		} else {
			return null;
		}
	}

	// Ignore case

	public static boolean startsWithIgnoreCase(String str, String prefix) {
		if (str == null || prefix == null) {
			return false;
		}
		return str.regionMatches(true, 0, prefix, 0, prefix.length());
	}

	public static boolean endsWithIgnoreCase(String str, String suffix) {
		if (str == null || suffix == null) {
			return false;
		}
		int len = suffix.length();
		return str.regionMatches(true, str.length() - len, suffix, 0, len);
	}

	public static boolean equalsIgnoreCase(String s1, String s2) {
		if (s1 == null || s2 == null) {
			return s1 == s2;
		} else {
			return s1.equalsIgnoreCase(s2);
		}
	}

}
