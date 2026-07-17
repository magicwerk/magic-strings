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
package org.magicwerk.strings.helper;

import java.util.Collection;
import java.util.List;

import org.magicwerk.brownies.core.strings.escape.StringBuilders.FixedStringBuilder;
import org.magicwerk.brownies.core.strings.stream.StringStreamer;
import org.magicwerk.strings.CharSequenceTools;
import org.magicwerk.strings.chars.CharOperator;
import org.magicwerk.strings.chars.CharPredicate;
import org.magicwerk.strings.helper.FuncTools;

/**
 * Class {@link StringTools_NEW_commonHeadArray} contains tools for working with {@link String}s.
 */
public class StringTools_NEW_commonHeadArray {

	/** Optimum buffer size to use for IO operations. */
	// We use the same value as in java.io.BufferedReader.defaultCharBufferSize.
	// Is there a system independent way to determine the optimal buffer size?
	public final static int BUFFER_SIZE = 8192;

	/** Character used as substitution for missing null in primitive data type char. */
	public static final char NOT_A_CHAR = 0xffff;

	//

	public static String replaceChars(String str, String searchChars, String replaceChars) {
		if (StringTools_NEW_commonHeadArray.isEmpty(str) || StringTools_NEW_commonHeadArray.isEmpty(searchChars)) {
			return str;
		}
		if (replaceChars == null) {
			replaceChars = "";
		}

		int replaceCharsLen = replaceChars.length();
		int strLen = str.length();
		FixedStringBuilder buf = null;
		for (int i = 0; i < strLen; i++) {
			char c = str.charAt(i);
			int pos = searchChars.indexOf(c);
			if (pos >= 0) {
				if (buf == null) {
					buf = new FixedStringBuilder(strLen);
					if (i > 0) {
						buf.append(str, 0, i);
					}
				}
				if (pos < replaceCharsLen) {
					buf.append(replaceChars.charAt(pos));
				}
			} else {
				if (buf != null) {
					buf.append(c);
				}
			}
		}

		if (buf != null) {
			return buf.toString();
		}
		return str;
	}

	/**
	 * Remove all occurrences of specified character from string.
	 * If the character is not found, the original string is returned and no allocation is done.
	 */
	public static String removeChar(String str, char c) {
		if (str == null) {
			return null;
		}

		StringBuilder buf = null;
		int len = str.length();
		int start = 0;
		for (int i = 0; i < len; i++) {
			if (str.charAt(i) == c) {
				if (buf == null) {
					buf = new StringBuilder();
				}
				buf.append(str, start, i);
				start = i + 1;
			}
		}
		if (buf == null) {
			return str;
		}
		buf.append(str, start, len);
		return buf.toString();
	}

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

	public static String removeChar(String str, CharPredicate predicate) {
		if (str == null) {
			return null;
		}

		StringBuilder buf = null;
		int len = str.length();
		int start = 0;
		for (int i = 0; i < len; i++) {
			if (predicate.test(str.charAt(i))) {
				if (buf == null) {
					buf = new StringBuilder();
				}
				buf.append(str, start, i);
				start = i + 1;
			}
		}
		if (buf == null) {
			return str;
		}
		buf.append(str, start, len);
		return buf.toString();
	}

	public static String replaceChar(String str, CharOperator operator) {
		if (str == null) {
			return null;
		}

		StringBuilder buf = null;
		int len = str.length();
		int start = 0;
		for (int i = 0; i < len; i++) {
			char c = str.charAt(i);
			char c2 = operator.apply(c);
			if (c2 != c) {
				if (buf == null) {
					buf = new StringBuilder();
				}
				buf.append(str, start, i);
				buf.append(c2);
				start = i + 1;
			}
		}
		if (buf == null) {
			return str;
		}
		buf.append(str, start, len);
		return buf.toString();
	}

	public static String trim(String str, CharPredicate cp) {
		if (str == null) {
			return null;
		}

		int start;
		int len = str.length();
		for (start = 0; start < len; start++) {
			if (!cp.test(str.charAt(start))) {
				break;
			}
		}
		if (start == len) {
			return "";
		}
		int end;
		for (end = len - 1;; end--) {
			if (!cp.test(str.charAt(end))) {
				break;
			}
		}
		return str.substring(start, end + 1);
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
	public static char charAtEnd(CharSequence str, int pos) {
		if (str != null) {
			int len = str.length();
			if (pos >= 0 && pos < len) {
				return str.charAt(len - pos - 1);
			}
		}
		return NOT_A_CHAR;
	}

	public static boolean equalsIgnoreCase(String s1, String s2) {
		if (s1 == null || s2 == null) {
			return s1 == s2;
		} else {
			return s1.equalsIgnoreCase(s2);
		}
	}

	public static boolean equals(String str1, int pos1, String str2, int pos2, int len) {
		if (pos1 < 0 || pos2 < 0) {
			if (pos1 != pos2) {
				return false;
			}
			len += pos1;
			pos1 = 0;
			pos2 = 0;
		}
		int len1 = Math.min(str1.length() - pos1, len);
		int len2 = Math.min(str2.length() - pos2, len);
		if (len1 != len2) {
			return false;
		}
		for (int i = 0; i < len1; i++) {
			if (str1.charAt(pos1 + i) != str2.charAt(pos2 + i)) {
				return false;
			}
		}
		return true;
	}

	/** Returns true if string is null or empty */
	public static boolean isEmpty(CharSequence str) {
		return str == null || str.length() == 0;
	}

	// --- Adding functionality to StringUtils

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

	/** 
	 * Returns common start string of the two specified strings.
	 * The empty string is returned if the do not share a common start, except if one of the strings is null, then null is returned. 
	 */
	public static String getCommonStart(String str1, String str2) {
		if (str1 == null || str2 == null) {
			return null;
		}

		int len1 = str1.length();
		int len2 = str2.length();
		int len = Math.min(len1, len2);
		int i;
		for (i = 0; i < len; i++) {
			if (str1.charAt(i) != str2.charAt(i)) {
				break;
			}
		}
		if (i == len1) {
			return str1;
		} else if (i == len2) {
			return str2;
		} else {
			return str1.substring(0, i);
		}
	}

	public static String getCommonEnd(String str1, String str2) {
		if (str1 == null || str2 == null) {
			return null;
		}

		int len1 = str1.length();
		int len2 = str2.length();
		int len = Math.min(len1, len2);
		int i;
		for (i = 0; i < len; i++) {
			if (str1.charAt(len1 - i - 1) != str2.charAt(len2 - i - 1)) {
				break;
			}
		}
		if (i == str1.length()) {
			return str1;
		} else if (i == str2.length()) {
			return str2;
		} else {
			return str1.substring(len1 - i, len1);
		}
	}

	@SafeVarargs
	public static String commonHead(String... lists) {
		return doCommonHeadTail(true, lists);
	}

	@SafeVarargs
	public static String commonTail(String... lists) {
		return doCommonHeadTail(false, lists);
	}

	@SafeVarargs
	static String doCommonHeadTail(boolean head, String... lists) {
		int i = doCommonElem(head, lists);
		if (i == 0) {
			return "";
		} else {
			String list = lists[0];
			if (head) {
				return list.substring(0, i);
			} else {
				int end = list.length();
				return list.substring(end - i, end);
			}
		}
	}

	@SafeVarargs
	static int doCommonElem(boolean head, String... lists) {
		int size = minSize(lists);
		int i;
		for (i = 0; i < size; i++) {
			char elem = 0;
			boolean add = true;
			for (int c = 0; c < lists.length; c++) {
				String list = lists[c];
				int index = (head) ? i : list.length() - i - 1;
				char e = list.charAt(index);
				if (c == 0) {
					elem = e;
				} else if (e != elem) {
					add = false;
					break;
				}
			}
			if (!add) {
				break;
			}
		}
		return i;
	}

	@SafeVarargs
	public static int minSize(String... colls) {
		if (colls.length == 0) {
			return 0;
		}
		int size = Integer.MAX_VALUE;
		for (String coll : colls) {
			size = Math.min(size, coll.length());
		}
		return size;
	}

	@SafeVarargs
	public static int maxSize(String... colls) {
		if (colls.length == 0) {
			return 0;
		}
		int size = 0;
		for (String coll : colls) {
			size = Math.max(size, coll.length());
		}
		return size;
	}

	//

	/** Join all strings without adding a separator */
	public static String join(List<? extends CharSequence> strings) {
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

	//

	/**
	 * Checks whether the marker occurs in the string.
	 * If it occurs, an exception is thrown, otherwise the marker string is returned unchanged.
	 *
	 * @param str		string
	 * @param marker	marker string
	 * @return			marker string if does not occur in string
	 * @throws			IllegalArgumentException if marker string occurs in string
	 */
	public static String getMarker(String str, String marker) {
		if (str.indexOf(marker) != -1) {
			throw new IllegalArgumentException("Marker " + marker + " occurs in string " + str);
		}
		return marker;
	}

	/**
	 * Checks whether the marker occurs in the string.
	 * If it occurs, the suffix is appended to the marker until the resulting string does not occur in the string.
	 *
	 * @param str		string
	 * @param marker	marker string
	 * @param suffix	suffix to append to marker string
	 * @return			marker string which does not occur in string
	 */
	public static String getMarker(String str, String marker, String suffix) {
		while (true) {
			if (str.indexOf(marker) == -1) {
				return marker;
			}
			marker = marker + suffix;
		}
	}

	public static String left(String str, int len) {
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
		return str.substring(0, end);
	}

	public static String right(String str, int len) {
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
		return str.substring(start, maxLen);
	}

	public static String removeLeft(String str, int len) {
		if (str == null) {
			return null;
		}
		int maxLen = str.length();
		int remove = (len > maxLen) ? maxLen : len;
		return str.substring(remove);
	}

	public static String removeRight(String str, int len) {
		if (str == null) {
			return null;
		}
		int maxLen = str.length();
		int remove = (len > maxLen) ? maxLen : len;
		return str.substring(0, maxLen - remove);
	}

	/** Variant of String.substring() which allows str to be empty and start and end to be out of bounds */
	public static String substring(String str, int start, int end) {
		return mid(str, start, end - start);
	}

	/** Method mid() allows str to be empty and pos and len to be out of bounds */
	public static String mid(String str, int pos, int len) {
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
		return str.substring(start, end);
	}

	public static String replace(String str, CharSequence search, CharSequence replace) {
		if (str == null || search == null || replace == null) {
			return str;
		}
		return str.replace(search, replace);
	}

	public static String replace(String str, char search, char replace) {
		if (str == null) {
			return str;
		}
		return str.replace(search, replace);
	}

	public static String reverse(String str) {
		if (str == null) {
			return str;
		}
		return new StringBuilder(str).reverse().toString();
	}

	/**
	 * Return length of string, 0 if string is null
	 */
	public static int length(String str) {
		return CharSequenceTools.length(str);
	}

	public static String value(String str) {
		return (str != null) ? str : "";
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

	public static char singleChar(CharSequence str) {
		if (str == null || str.length() != 1) {
			return NOT_A_CHAR;
		} else {
			return str.charAt(0);
		}
	}

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

	/**
	 * Convert a name to format suited for display.
	 * Examples: <br>
	 * - "ok"        : "ok"
	 * - "okButton"  : "Ok Button"
	 * - "OK_BUTTON" : "Ok Button"
	 *
	 * @param str	string to convert
	 * @return		string for display
	 */
	public static String getDisplayName(String str) {
		if (str == null || str.isEmpty()) {
			return str;
		}
		StringStreamer buf = new StringStreamer(str).useWriteBuffer().setAutoFlush(false);
		int c = buf.readChar();
		if (Character.isLowerCase(c)) {
			// Format: okButton (camel case)
			buf.setCharAt(-1, Character.toUpperCase((char) c));
			while (true) {
				c = buf.readChar();
				if (c == -1) {
					break;
				}
				if (Character.isUpperCase((char) c)) {
					buf.insert(-1, ' ');
				}
			}
		} else {
			// Format: OK_BUTTON
			boolean toLower = true;
			while (true) {
				c = buf.readChar();
				if (c == -1) {
					break;
				}
				if (c == '_') {
					buf.setCharAt(-1, ' ');
					toLower = false;
				} else {
					if (toLower) {
						buf.setCharAt(-1, Character.toLowerCase((char) c));
					} else {
						toLower = true;
					}
				}
			}
		}
		return buf.buffer();
	}

	/**
	 * Replacement character (U+FFFD) 65533 used in Unicode.
	 * A replacement character can be introduced
	 * if a string is constructed using "String(bytes[], charset)"
	 * and some bytes cannot be represented in the charset.
	 */
	public static final char REPLACEMENT_CHAR = 0xFFFD;

	/**
	 * Check whether the string contains replacement characters.
	 * A replacement character (U+FFFD) 65533 can be introduced
	 * if a string is constructed using "String(bytes[], charset)"
	 * and some bytes cannot be represented in the charset.
	 *
	 * @param str   string to check
	 * @return      true if string contains replacement characters
	 */
	public static boolean hasReplacementChar(String str) {
		for (int i = 0; i < str.length(); i++) {
			if (str.charAt(i) == REPLACEMENT_CHAR) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Return string without all occurrences of part.
	 * If the string does not contain part, the string is returned unchanged.
	 *
	 * @param str	input string
	 * @param part	string to remove in input string
	 * @return		input string with part removed or input string
	 */
	public static String removePartIf(String str, String part) {
		String s = removePart(str, part);
		if (s == null) {
			return str;
		}
		return s;
	}

	public static String remove(String str, String remove) {
		String s = removePart(str, remove);
		return (s != null) ? s : str;
	}

	/**
	 * Return string without all occurrences of part.
	 * If the string does not contain part, null is returned.
	 *
	 * @param str	input string
	 * @param part	string to remove in input string
	 * @return		input string with part removed or null
	 */
	public static String removePart(String str, String part) {
		int pos = str.indexOf(part);
		if (pos == -1) {
			return null;
		}
		StringBuilder buf = new StringBuilder();
		int start = 0;
		while (pos != -1) {
			buf.append(str.substring(start, pos));
			start = pos + part.length();
			pos = str.indexOf(part, start);
		}
		buf.append(str.substring(start));
		return buf.toString();
	}

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

	public static String removeHeadTail(String str, String head, String tail) {
		if (str.length() >= head.length() + tail.length() && str.startsWith(head) && str.endsWith(tail)) {
			return str.substring(head.length(), str.length() - tail.length());
		}
		return null;
	}

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

	//

	public static String getHeadBy(String str, String split) {
		int index = str.indexOf(split);
		if (index != -1) {
			return str.substring(0, index);
		} else {
			return null;
		}
	}

	public static String getHeadByIf(String str, String split) {
		String result = getHeadBy(str, split);
		if (result == null) {
			return str;
		} else {
			return result;
		}
	}

	public static String getAfterHeadBy(String str, String split) {
		int index = str.indexOf(split);
		if (index != -1) {
			return str.substring(index + split.length());
		} else {
			return null;
		}
	}

	public static String getAfterHeadByIf(String str, String split) {
		String result = getAfterHeadBy(str, split);
		if (result == null) {
			return str;
		} else {
			return result;
		}
	}

	public static String getTailBy(String str, String split) {
		int index = str.lastIndexOf(split);
		if (index != -1) {
			return str.substring(index + split.length());
		} else {
			return null;
		}
	}

	public static String getTailByIf(String str, String split) {
		String result = getTailBy(str, split);
		if (result == null) {
			return str;
		} else {
			return result;
		}
	}

	public static String getBeforeTailBy(String str, String split) {
		int index = str.lastIndexOf(split);
		if (index != -1) {
			return str.substring(0, index);
		} else {
			return null;
		}
	}

	public static String getBeforeTailByIf(String str, String split) {
		String result = getBeforeTailBy(str, split);
		if (result == null) {
			return str;
		} else {
			return result;
		}
	}

}
