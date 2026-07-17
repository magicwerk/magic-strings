/*
 * Copyright 2013 by Thomas Mauch
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

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.magicwerk.brownies.collections.GapList;
import org.magicwerk.brownies.collections.primitive.IntGapList;

/**
 * Class {@link CharsetTools} contains operations related to {@link Charset}s. 
 * <p>
 * It follows the naming convention of {@link CharsetEncoder}/{@link CharsetDecoder}: <br>
 * - encode: convert characters to bytes <br>
 * - decode: convert bytes to characters <br>
 */
public class CharsetTools {

	/**
	 * Class {@link CharsetCodingException} contains information about a failed character conversion.
	 */
	public static class CharsetCodingException extends RuntimeException {

		byte[] bytes;
		Charset charset;
		int position;

		public CharsetCodingException(byte[] bytes, Charset charset, int position, Throwable t) {
			super("Conversion to " + charset + " failed at position " + position, t);

			this.bytes = bytes;
			this.charset = charset;
			this.position = position;
		}

		public byte[] getBytes() {
			return bytes;
		}

		public Charset getCharset() {
			return charset;
		}

		public int getPosition() {
			return position;
		}
	}

	public static final String UTF8_NAME = "UTF-8";
	public static final String ISO88591_NAME = "ISO-8859-1";
	public static final String ASCII_NAME = "US-ASCII";

	public static final Charset UTF8 = Charset.forName("UTF-8");
	public static final Charset ISO88591 = Charset.forName("ISO-8859-1");
	public static final Charset ASCII = Charset.forName("US-ASCII");
	public static final Charset UTF16BE = Charset.forName("UTF-16BE");
	public static final Charset UTF16LE = Charset.forName("UTF-16LE");
	public static final Charset UTF32BE = Charset.forName("UTF-32BE");
	public static final Charset UTF32LE = Charset.forName("UTF-32LE");

	/** The default Charset used is UTF-8 */
	public static final Charset DEFAULT_CHARSET = UTF8;

	// BOMs
	public static final int MAX_BOM_LENGTH = 4;
	public static final byte[] BOM_UTF8 = new byte[] { (byte) 0xef, (byte) 0xbb, (byte) 0xbf };
	public static final byte[] BOM_UTF16BE = new byte[] { (byte) 0xfe, (byte) 0xff };
	public static final byte[] BOM_UTF16LE = new byte[] { (byte) 0xff, (byte) 0xfe };
	public static final byte[] BOM_UTF32BE = new byte[] { (byte) 0x00, (byte) 0x00, (byte) 0xfe, (byte) 0xff };
	public static final byte[] BOM_UTF32LE = new byte[] { (byte) 0xff, (byte) 0xfe, (byte) 0x00, (byte) 0x00 };

	public static final String UTF8_BOM = "\uFEFF";

	public static final List<Charset> CHARSET_ISO_UTF8_UTF16 = GapList.create(UTF16BE, UTF16LE, UTF8, ISO88591).unmodifiableList();

	/**
	 * Determines whether the number of bytes used to represent a char is fixed.
	 * 
	 * @param cs	charset
	 * @return		true for a fixed, false for a dynamic representation
	 */
	public static boolean isBytesPerCharFixed(Charset cs) {
		return getBytesPerChar(cs) != -1;
	}

	/**
	 * Returns length of character starting at start in data using the specified charset.
	 * 
	 * @param cs		charset
	 * @param data		bytes
	 * @param start		start index
	 * @return			len of character at position, -1 if an invalid UTF-8 character is found
	 */
	public static int getCharLen(Charset cs, byte[] data, int start) {
		int len = getBytesPerChar(cs);
		if (len != -1) {
			return len;
		} else {
			// UTF-8 is the only charset supported with variable encoding
			return CharTools.getUtf8Len(data, start);
		}
	}

	/**
	 * Returns the number of bytes used to represent a char.
	 * 
	 * @param cs	charset
	 * @return		number of bytes used to represent a char, -1 for a dynamic representation
	 */
	public static int getBytesPerChar(Charset cs) {
		if (cs.equals(UTF8)) {
			return -1;
		} else if (cs.equals(ISO88591)) {
			return 1;
		} else if (cs.equals(ASCII)) {
			return 1;
		}

		String name = cs.name();
		if (name.startsWith("ISO-8859-")) {
			return 1;
		} else if (name.startsWith("UTF-16")) {
			return 2;
		}
		throw new IllegalArgumentException("Unsupported charset: " + name);
	}

	/**
	 * Returns the maximum number of bytes used to represent a char.
	 * 
	 * @param cs	charset
	 * @return		maximum number of bytes used to represent a char
	 */
	public static int getMaxBytesPerChar(Charset cs) {
		if (cs.equals(UTF8)) {
			return 4;
		} else if (cs.equals(ISO88591)) {
			return 1;
		} else if (cs.equals(ASCII)) {
			return 1;
		}

		String name = cs.name();
		if (name.startsWith("ISO-8859-")) {
			return 1;
		} else if (name.startsWith("UTF-16")) {
			return 2;
		}
		throw new IllegalArgumentException("Unsupported charset: " + name);
	}

	public static Charset getCharset(String csn) throws UnsupportedEncodingException {
		// behaves as String.lookupCharset()
		Objects.requireNonNull(csn);
		try {
			return Charset.forName(csn);
		} catch (UnsupportedCharsetException | IllegalCharsetNameException x) {
			throw new UnsupportedEncodingException(csn);
		}
	}

	/**
	 * @param data	first bytes read from file
	 * @return		charset determined from BOM, null if no BOM was detected
	 */
	public static Charset getCharsetFromBom(byte[] data) {
		if (data.length >= 4) {
			if (data[0] == BOM_UTF32BE[0] && data[1] == BOM_UTF32BE[0] && data[2] == BOM_UTF32BE[2] && data[3] == BOM_UTF32BE[3]) {
				return UTF32BE;
			}
			if (data[0] == BOM_UTF32LE[0] && data[1] == BOM_UTF32LE[0] && data[2] == BOM_UTF32LE[2] && data[3] == BOM_UTF32LE[3]) {
				return UTF32LE;
			}
		}
		if (data.length >= 3) {
			if (data[0] == BOM_UTF8[0] && data[1] == BOM_UTF8[1] && data[2] == BOM_UTF8[2]) {
				return UTF8;
			}
		}
		if (data.length >= 2) {
			if (data[0] == BOM_UTF16BE[0] && data[1] == BOM_UTF16BE[1]) {
				return UTF16BE;
			}
			if (data[0] == BOM_UTF16LE[0] && data[1] == BOM_UTF16LE[1]) {
				return UTF16LE;
			}
		}
		return null;
	}

	/**
	 * @param cs	charset
	 * @return	BOM to use for charset, null if there is none
	 */
	public static byte[] getBomFromCharset(Charset cs) {
		if (cs == UTF32BE) {
			return BOM_UTF32BE;
		} else if (cs == UTF32LE) {
			return BOM_UTF32LE;
		} else if (cs == UTF8) {
			return BOM_UTF8;
		} else if (cs == UTF16BE) {
			return BOM_UTF16BE;
		} else if (cs == UTF16LE) {
			return BOM_UTF16LE;
		}
		return null;
	}

	public static int getBomLengthFromCharset(Charset cs) {
		if (cs == UTF8) {
			return 3;
		} else if (cs == UTF16BE || cs == UTF16LE) {
			return 2;
		} else if (cs == UTF32BE || cs == UTF32LE) {
			return 4;
		}
		return 0;
	}

	/**
	 * Returns char as bytes in UTF-8.
	 * This method replaces malformed-input and unmappable-character sequences with the charset's default replacement string.
	 */
	public static byte[] getBytes(char c) {
		return getBytes(String.valueOf(c));
	}

	/**
	 * Returns string as bytes in UTF-8.
	 * This method replaces malformed-input and unmappable-character sequences with the charset's default replacement string.
	 */
	public static byte[] getBytes(String str) {
		return getBytes(str, DEFAULT_CHARSET);
	}

	/**
	 * Returns bytes in UTF-8 as string.
	 * This method replaces malformed-input and unmappable-character sequences with the charset's default replacement string.
	 */
	public static String getString(byte[] bytes) {
		return getString(bytes, DEFAULT_CHARSET);
	}

	/**
	 * Returns char as bytes in specified encoding.
	 * This method replaces malformed-input and unmappable-character sequences with the charset's default replacement string.
	 */
	public static byte[] getBytes(char c, Charset cs) {
		return getBytes(String.valueOf(c), cs);
	}

	/**
	 * Returns string as bytes in specified encoding.
	 */
	public static byte[] getBytes(String str, Charset cs) {
		return str.getBytes(cs);
	}

	/**
	 * Returns bytes in specified encoding as string.
	 * This method replaces malformed-input and unmappable-character sequences with the charset's default replacement string.
	 */
	public static String getString(byte[] bytes, Charset cs) {
		return new String(bytes, cs);
	}

	/**
	 * Encodes a string into a byte array.
	 * Unlike String.getBytes(), which uses replacement characters for conversion problems,
	 * this method throws an exception if the conversion encounters a problem.
	 *
	 * @param str	string to encode
	 * @param cs	charset to use
	 * @return		encoded byte array
	 * @throws		IllegalArgumentException if conversion fails
	 */
	public static byte[] encode(String str, Charset cs) {
		CharsetEncoder encoder = getEncoder(cs);
		return encode(str, encoder);
	}

	/**
	 * Encodes a string into a byte array.
	 * Unlike String.getBytes(), which uses replacement characters for conversion problems,
	 * this method throws an exception if the conversion encounters a problem.
	 *
	 * @param str		string to encode
	 * @param encoder	charset encoder to use
	 * @return			encoded byte array
	 * @throws			IllegalArgumentException if conversion fails
	 */
	public static byte[] encode(CharSequence str, CharsetEncoder encoder) {
		CharBuffer cbuf = CharBuffer.wrap(str);
		return encode(cbuf, encoder);
	}

	public static byte[] encode(char[] carr, CharsetEncoder encoder) {
		CharBuffer cbuf = CharBuffer.wrap(carr);
		return encode(cbuf, encoder);
	}

	public static byte[] encode(CharBuffer cbuf, CharsetEncoder encoder) {
		try {
			// Conversion:  CharBuffer -> ByteBuffer -> byte[]
			ByteBuffer bbuf = encoder.encode(cbuf);
			byte[] data = new byte[bbuf.remaining()];
			bbuf.get(data);
			return data;
		} catch (CharacterCodingException e) {
			throw new IllegalArgumentException("Conversion from " + encoder.charset() + " failed at position " + cbuf.position() + " for " + cbuf, e);
		}
	}

	/**
	 * Create a reporting encoder.
	 * See {@link CodingErrorAction#REPORT} for details.
	 * 
	 * @param cs charset
	 * @return	created encoder
	 */
	public static CharsetEncoder getEncoder(Charset cs) {
		return getEncoder(cs, CodingErrorAction.REPORT);
	}

	/**
	 * Create a reporting decoder.
	 * See {@link CodingErrorAction#REPORT} for details.
	 * 
	 * @param cs charset
	 * @return	created decoder
	 */
	public static CharsetDecoder getDecoder(Charset cs) {
		return getDecoder(cs, CodingErrorAction.REPORT);
	}

	/**
	 * Create a decoder.
	 * 
	 * @param cs charset
	 * @param error error action
	 * @return	created encoder
	 */
	public static CharsetDecoder getDecoder(Charset cs, CodingErrorAction error) {
		CharsetDecoder decoder = cs.newDecoder().onMalformedInput(error).onUnmappableCharacter(error);
		return decoder;
	}

	/**
	 * Create an encoder.
	 * 
	 * @param cs charset
	 * @param error error action
	 * @return	created encoder
	 */
	public static CharsetEncoder getEncoder(Charset cs, CodingErrorAction error) {
		return cs.newEncoder().onMalformedInput(error).onUnmappableCharacter(error);
	}

	public static Charset getCharsetFromBytes(byte[] bytes, List<Charset> css) {
		for (Charset cs : css) {
			try {
				decode(bytes, 0, bytes.length, cs);
				return cs;
			} catch (Exception e) {
				// ignore
			}
		}
		return null;
	}

	/**
	 * Decodes a byte array into a string by trying out all passed {@link Charset}s.
	 * The first working charset is used to create the string. Returns null if no conversion is possible.
	 *
	 * @param bytes	bytes to decode
	 * @param css	charsets to use
	 * @return		decoded string
	 */
	public static String decode(byte[] bytes, List<Charset> css) {
		for (Charset cs : css) {
			try {
				return decode(bytes, 0, bytes.length, cs);
			} catch (Exception e) {
				// ignore
			}
		}
		return null;
	}

	/**
	 * Decodes a byte array into a string.
	 * Note that {@code new String(bytes, cs)} is slightly faster, but silently replaces invalid characters. 
	 *
	 * @param bytes	bytes to decode
	 * @param cs	charset to use
	 * @return		decoded string
	 * @throws		IllegalArgumentException if conversion fails
	 */
	public static String decode(byte[] bytes, Charset cs) {
		return decode(bytes, 0, bytes.length, cs);
	}

	public static String decode(byte[] bytes, CharsetDecoder decoder) {
		return decode(bytes, 0, bytes.length, decoder);
	}

	/**
	 * Decodes a byte array into a string.
	 * Note that {@code new String(bytes, offset, len, cs)} is slightly faster, but silently replaces invalid characters. 
	 *
	 * @param bytes	bytes to decode
	 * @param offset	offset of first byte to decode
	 * @param len	length of bytes to decode
	 * @param cs	charset to use
	 * @return		decoded string
	 * @throws		IllegalArgumentException if conversion fails
	 */
	public static String decode(byte[] bytes, int offset, int len, Charset cs) {
		CharsetDecoder decoder = getDecoder(cs);
		return decode(bytes, offset, len, decoder);
	}

	public static String decode(byte[] bytes, int offset, int len, CharsetDecoder decoder) {
		CharBuffer cbuf = decodeCharSequence(bytes, offset, len, decoder);
		return cbuf.toString();
	}

	public static CharBuffer decodeCharSequence(byte[] bytes, int offset, int len, CharsetDecoder decoder) {
		ByteBuffer bbuf = null;
		try {
			// Conversion: byte[] -> ByteBuffer -> CharBuffer -> String
			bbuf = ByteBuffer.wrap(bytes, offset, len);
			CharBuffer cbuf = decoder.decode(bbuf);
			return cbuf;
		} catch (CharacterCodingException e) {
			throw new CharsetCodingException(bytes, decoder.charset(), bbuf.position(), e);
		}
	}

	/**
	 * Converts a string, which has been encoded using a wrong charset, into a different charset.
	 *
	 * @param str		input string
	 * @param csSrc		charset used by input string
	 * @param csDst		charset to use for output string
	 * @return			string converted into destination charset
	 * @throws			IllegalArgumentException if conversion fails
	 */
	public static String convert(String str, Charset csSrc, Charset csDst) {
		CharsetEncoder encoder = getEncoder(csSrc);
		CharsetDecoder decoder = getDecoder(csDst);

		try {
			// Convert a string into a CharBuffer and then in a ByteBuffer
			ByteBuffer bbuf = encoder.encode(CharBuffer.wrap(str));

			// Convert ByteBuffer into CharBuffer and then in a String
			CharBuffer cbuf = decoder.decode(bbuf);
			return cbuf.toString();
		} catch (CharacterCodingException e) {
			throw new IllegalArgumentException("Conversion failed for " + str, e);
		}
	}

	/**
	 * Decodes a byte array into a string.
	 *
	 * @param bytes		bytes to decode
	 * @param charsets	charsets to try for decoding
	 * @return			map with charset and exception raised during conversion or null for success
	 */
	public static Map<Charset, Exception> tryDecode(byte[] bytes, Collection<Charset> charsets) {
		Map<Charset, Exception> result = new LinkedHashMap<>();
		for (Charset cs : charsets) {
			Exception error = null;
			try {
				CharsetTools.decode(bytes, cs);
			} catch (Exception e) {
				error = e;
			}
			result.put(cs, error);
		}
		return result;
	}

	/**
	 * Replacement character (U+FFFD) 65533 used in Unicode.
	 * A replacement character can be introduced if a string is constructed using "String(bytes[], charset)"
	 * and some bytes cannot be represented in the charset.
	 */
	public static final char REPLACEMENT_CHAR = 0xFFFD;

	/**
	 * Check whether the string contains replacement characters.
	 * A replacement character (U+FFFD) 65533 can be introduced if a string is constructed using "String(bytes[], charset)"
	 * and some bytes cannot be represented in the charset.
	 *
	 * @param str   string to check
	 * @return      true if string contains replacement characters
	 */
	public static boolean hasReplacementChar(String str) {
		return getNextReplacementChar(str, 0) != -1;
	}

	/**
	 * Returns index of next replacement character or -1 if none found.
	 */
	public static int getNextReplacementChar(String str, int start) {
		return str.indexOf(REPLACEMENT_CHAR, start);
	}

	public static IntGapList getAllReplacementChars(String str) {
		IntGapList pos = new IntGapList();
		int i = -1;
		while (true) {
			i = getNextReplacementChar(str, i + 1);
			if (i == -1) {
				break;
			}
			pos.add(i);
		}
		return pos;
	}

	public static int getNumReplacementChars(String str) {
		int num = 0;
		int i = -1;
		while (true) {
			i = getNextReplacementChar(str, i + 1);
			if (i == -1) {
				break;
			}
			num++;
		}
		return num;
	}

}
