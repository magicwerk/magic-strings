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

import org.magicwerk.brownies.collections.primitive.CharGapList;
import org.magicwerk.strings.helper.ByteTools;
import org.magicwerk.strings.helper.CheckTools;

/**
 * Class {@link CharTools} contains utilities for working with characters.
 */
public class CharTools {

	/**
	 * Class {@link UniqueChars} helps to create a string which contains only unique characters, i.e. there are no duplicates.
	 */
	public static class UniqueChars {
		CharGapList chars = new CharGapList();

		public void add(char c) {
			if (!chars.contains(c)) {
				chars.add(c);
			}
		}

		public void add(String str) {
			for (int i = 0; i < str.length(); i++) {
				add(str.charAt(i));
			}
		}

		@Override
		public String toString() {
			return chars.toString();
		}
	}

	public static final char REPLACEMENT_CHAR = '\ufffd';

	public static boolean hasSurrogate(String str) {
		for (int i = 0; i < str.length(); i++) {
			if (Character.isSurrogate(str.charAt(i))) {
				return true;
			}
		}
		return false;
	}

	public static String replaceSurrogate(String str) {
		StringBuilder buf = null;
		int start = 0;
		int i;
		for (i = start; i < str.length(); i++) {
			int surrogate = 0;
			if (Character.isHighSurrogate(str.charAt(i))) {
				surrogate = 1;
				if (i + 1 < str.length()) {
					if (Character.isLowSurrogate(str.charAt(i + 1))) {
						surrogate = 2;
					}
				}
			} else if (Character.isLowSurrogate(str.charAt(i))) {
				// Invalid, high surrogate should come first
				surrogate = 1;
			}

			if (surrogate > 0) {
				if (buf == null) {
					buf = new StringBuilder();
				}
				buf.append(str, start, i);
				buf.append(REPLACEMENT_CHAR);
				start = i + surrogate;
				i = start - 1;
			}
		}
		if (buf == null) {
			return str;
		}
		buf.append(str, start, i);
		return buf.toString();
	}

	/**
	 * Returns length of character in bytes if encoded in UTF-8.
	 *
	 * @param c	character
	 * @return	length of character in bytes if encoded in UTF-8
	 */
	public static int getUtf8Len(char c) {
		if (c <= 0x7f) {
			return 1;
		} else if (c <= 0x7ff) {
			return 2;
		} else {
			return 3;
		}
	}

	/**
	 * @return true if character is an ASCII character (0 .. 127)
	 */
	public static boolean isAscii(char c) {
		return c <= 0x7f;
	}

	/**
	 * @return true if character is an ASCII digit ('0' .. '9')
	 */
	public static boolean isAsciiDigit(char c) {
		return '0' <= c && c <= '9';
	}

	/**
	 * @return true if character is an ASCII lower case ('a' .. 'z')
	 */
	public static boolean isAsciiLower(char c) {
		return 'a' <= c && c <= 'z';
	}

	/**
	 * @return true if character is an ASCII upper case ('A' .. 'Z')
	 */
	public static boolean isAsciiUpper(char c) {
		return 'A' <= c && c <= 'Z';
	}

	/**
	 * @return true if character is an ASCII alphabetic character ([A-Za-z])
	 */
	public static boolean isAsciiAlpha(char c) {
		return isAsciiLower(c) || isAsciiUpper(c);
	}

	/**
	 * @return true if character is an ASCII alphanumeric character ([0-9A-Za-z])
	 */
	public static boolean isAsciiAlnum(char c) {
		return isAsciiDigit(c) || isAsciiAlpha(c);
	}

	/**
	 * Checks whether all bytes represent ASCII characters.
	 *
	 * @param data	bytes to check
	 * @return		true if all bytes represent ASCII characters
	 */
	public static boolean isAscii(byte[] data) {
		for (int i = 0; i < data.length; i++) {
			if ((data[i] & 0x80) == 0x80) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Checks whether all bytes can represent UTF-8 encoded characters.
	 *
	 * @param data	bytes to check
	 * @return		true if all bytes can represent UTF-8 encoded characters
	 */
	public static boolean isUtf8(byte[] data) {
		int i = 0;
		while (i < data.length) {
			int c = getUtf8Char(data, i);
			if (c == -1) {
				return false;
			}
			i += getUtf8Len((char) c);
		}
		return i == data.length;
	}

	/**
	 * Find next start position of a UTF-8 character in a byte array.
	 *
	 * @param data	byte array
	 * @param start	start position
	 * @return		start of UTF-8 character or -1 if no start position	for a UTF-8 character could be found
	 */
	public static int getUtf8Start(byte[] data, int start) {
		while (start < data.length) {
			if (!isUtf8ContinuationByte(data[start])) {
				return start;
			}
			start++;
		}
		return -1;
	}

	public static boolean isUtf8ValidByte(byte b) {
		char c = (char) b;
		return !(c <= 127 || (c >= 194 && c <= 244));
	}

	public static boolean isUtf8ContinuationByte(byte b) {
		char c = (char) b;
		return (c & 0xc0) == 0x80;
	}

	/**
	 * Find previous start position of a UTF-8 character in a byte array.
	 *
	 * @param data	byte array
	 * @param start	start position
	 * @return		start of UTF-8 character or -1 if no start position	for a UTF-8 character could be found
	 */
	public static int getUtf8StartReverse(byte[] data, int start) {
		while (start >= 0) {
			if (!isUtf8ContinuationByte(data[start])) {
				return start;
			}
			start--;
		}
		return -1;
	}

	/**
	 * Returns length of the UTF-8 character represented by the specified bytes.
	 * If the bytes can not represent an UTF-8 encoded character, -1 is returned.
	 *
	 * @param data	byte array
	 * @param start	start index in byte array
	 * @return		length of UTF-8 character represented by the data, or -1 if it is not a valid UTF-8 character
	 */
	public static int getUtf8Len(byte[] data, int start) {
		int c = getUtf8Char(data, start);
		if (c == -1) {
			return -1;
		} else {
			return getUtf8Len((char) c);
		}
	}

	/**
	 * Returns the UTF-8 character represented by the specified bytes.
	 * If the bytes can not represent an UTF-8 encoded character, -1 is returned.
	 *
	 * @param data	byte array
	 * @param start	start index in byte array
	 * @return		UTF-8 character represented by the data, or -1 if it is not a valid UTF-8 character
	 */
	public static int getUtf8Char(byte[] data, int start) {
		int b0 = ByteTools.byteToUbyte(data[start]);
		if (b0 <= 127) {
			// 1 byte character: 0xxxxxxx
			return (char) b0;

		} else if (b0 >= 194 && b0 <= 244) {
			if (b0 <= 223) {
				// 2 byte character: 110xxxxx 10xxxxxx
				if (start + 1 < data.length) {
					int b1 = ByteTools.byteToUbyte(data[start + 1]);
					if (b1 >= 128 && b1 <= 191) {
						int c = ((b0 & 0x1f) << 6) | ((b1 & 0x3f));
						return (char) c;
					}
				}
			} else if (b0 <= 239) {
				// 3 byte character: 1110xxxx 10xxxxxx 10xxxxxx
				if (start + 2 < data.length) {
					int b1 = ByteTools.byteToUbyte(data[start + 1]);
					int b2 = ByteTools.byteToUbyte(data[start + 2]);
					if (b1 >= 128 && b1 <= 191 &&
							b2 >= 128 && b2 <= 191) {
						int c = ((b0 & 0x0f) << 12) | ((b1 & 0x3f) << 6) | (b2 & 0x3f);
						return (char) c;
					}
				}
			} else {
				// 4 byte character: 11110xxx 10xxxxxx 10xxxxxx 10xxxxxx
				if (start + 3 < data.length) {
					int b1 = ByteTools.byteToUbyte(data[start + 1]);
					int b2 = ByteTools.byteToUbyte(data[start + 2]);
					int b3 = ByteTools.byteToUbyte(data[start + 3]);
					if (b1 >= 128 && b1 <= 191 &&
							b2 >= 128 && b2 <= 191 &&
							b3 >= 128 && b3 <= 191) {
						int c = ((b0 & 0x07) << 18) | ((b1 & 0x3f) << 12) | ((b2 & 0x3f) << 6) | (b3 & 0x3f);
						return (char) c;
					}
				}
			}
		}
		return -1;
	}

	/**
	 * Note that this is only a guess.
	 *
	 * @param data	binary data
	 * @return		true if data could represent binary data
	 */
	public static boolean isBinary(byte[] data) {
		if (CharTools.isAscii(data)) {
			return false;
		}
		if (CharTools.isUtf8(data)) {
			return hasBinaryCharsUtf8(data);
		} else {
			return hasBinaryChars(data);
		}
	}

	static boolean hasBinaryCharsUtf8(byte[] data) {
		int i = 0;
		while (i < data.length) {
			int c = CharTools.getUtf8Char(data, i);
			CheckTools.check(c != -1, "Invalid UTF-8 data");
			if (isBinaryChar((char) c)) {
				return true;
			}
			i += CharTools.getUtf8Len((char) c);
		}
		return false;
	}

	static boolean hasBinaryChars(byte[] data) {
		for (int i = 0; i < data.length; i++) {
			char c = (char) data[i];
			if (isBinaryChar(c)) {
				return true;
			}
		}
		return false;
	}

	public static boolean isBinaryChar(int c) {
		return Character.isISOControl(c) && !Character.isWhitespace(c);
	}

	public static boolean isPrint(int c) {
		return !Character.isISOControl(c) && !Character.isWhitespace(c);
	}

	public static boolean isPunct(int c) {
		int type = Character.getType(c);
		return type == Character.DASH_PUNCTUATION || type == Character.START_PUNCTUATION ||
				type == Character.END_PUNCTUATION || type == Character.INITIAL_QUOTE_PUNCTUATION ||
				type == Character.FINAL_QUOTE_PUNCTUATION || type == Character.CONNECTOR_PUNCTUATION ||
				type == Character.OTHER_PUNCTUATION;
	}

	//

	/**
	 * Convert a hexadecimal character into a byte value.
	 *
	 * @param c	hexadecimal character
	 * @return	byte value as integer
	 * @throws IllegalArgumentException if the character is not a valid hexadecimal character
	 */
	public static int fromHex(char c) {
		if (c >= '0' && c <= '9') {
			return c - '0';
		} else if (c >= 'a' && c <= 'f') {
			return c - 'a' + 10;
		} else if (c >= 'A' && c <= 'F') {
			return c - 'A' + 10;
		} else {
			return -1;
		}
	}

	public static int fromDec(char c) {
		if (c >= '0' && c <= '9') {
			return c - '0';
		} else {
			return -1;
		}
	}

	/**
	 * Convert an octal character into a byte value.
	 *
	 * @param c	octal character
	 * @return	byte value as integer
	 * @throws IllegalArgumentException if the character is not a valid octal character
	 */
	public static int fromOctal(char c) {
		if (c >= '0' && c <= '7') {
			return c - '0';
		} else {
			throw new IllegalArgumentException("Invalid octal char: " + c);
		}
	}

	/**
	 * Returns octal character from a string with format "\\NNN".
	 * Throws IllegalArgumentException if the string is not valid.
	 */
	public static char fromOctalChar(String str) {
		if (str.length() == 4 && str.startsWith("\\")) {
			int code = fromOctal(str.charAt(1));
			if (code <= 3) {
				code = code << 3 | fromOctal(str.charAt(2));
				code = code << 3 | fromOctal(str.charAt(3));
				return (char) code;
			}
		}
		throw new IllegalArgumentException("Invalid octal char: " + str);
	}

	/**
	 * Returns unicode character from a string with format "\\uNNNN".
	 * Throws IllegalArgumentException if the string is not valid.
	 */
	public static char fromUnicodeChar(String str) {
		if (str.length() == 6 && str.startsWith("\\u")) {
			int c0 = fromHex(str.charAt(2));
			int c1 = fromHex(str.charAt(3));
			int c2 = fromHex(str.charAt(4));
			int c3 = fromHex(str.charAt(5));
			if (c0 != -1 && c1 != -1 && c2 != -1 && c3 != -1) {
				return (char) (c0 << 12 | c1 << 8 | c2 << 4 | c3);
			}
		}
		throw new IllegalArgumentException("Invalid unicode char: " + str);
	}

	/**
	 * Returns string in format {@code "\}{@code u12ab"} (always 6 digits)"
	 */
	public static String toUnicodeChar(char c) {
		if (c < 0x10) {
			return "\\u000" + Integer.toHexString(c);
		} else if (c < 0x100) {
			return "\\u00" + Integer.toHexString(c);
		} else if (c < 0x1000) {
			return "\\u0" + Integer.toHexString(c);
		} else {
			return "\\u" + Integer.toHexString(c);
		}
	}

	public static String toUnicodeChars(int cp) {
		if (CodePointTools.isCharCodePoint(cp)) {
			return toUnicodeChar((char) cp);
		} else {
			char hi = Character.highSurrogate(cp);
			char lo = Character.lowSurrogate(cp);
			return toUnicodeChar(hi) + toUnicodeChar(lo);
		}
	}

	/**
	 * Return string in format {@code U+12ab34}.
	 */
	public static String toUnicodeNumber(int c) {
		if (c < 0x10) {
			return "U+000" + Integer.toHexString(c);
		} else if (c < 0x100) {
			return "U+00" + Integer.toHexString(c);
		} else if (c < 0x1000) {
			return "U+0" + Integer.toHexString(c);
		} else {
			return "U+" + Integer.toHexString(c);
		}
	}

	/**
	 * Returns string in format {@code "12ab"} (always 4 digits)"
	 */
	public static String toUnicode(char c) {
		if (c < 0x10) {
			return "000" + Integer.toHexString(c);
		} else if (c < 0x100) {
			return "00" + Integer.toHexString(c);
		} else if (c < 0x1000) {
			return "0" + Integer.toHexString(c);
		} else {
			return Integer.toHexString(c);
		}
	}

}
