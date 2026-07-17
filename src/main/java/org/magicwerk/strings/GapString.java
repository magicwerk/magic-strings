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
package org.magicwerk.strings;

import java.io.UnsupportedEncodingException;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CodingErrorAction;
import java.util.Arrays;
import java.util.Locale;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.magicwerk.collections.primitive.CharGapList;
import org.magicwerk.collections.primitive.ICharListable;
import org.magicwerk.strings.chars.CharsetTools;
import org.magicwerk.strings.chars.CodePointTools;
import org.magicwerk.strings.chars.CharCaseTools.CharMode;
import org.magicwerk.strings.format.FormatTools;
import org.magicwerk.strings.helper.ObjectTools;
import org.magicwerk.strings.text.TextTools.LineIterator;

/**
 * Class {@link GapString} is the default implementation of a mutable string as defined by {@link IString}.
 * It extends {@link CharGapList} and adds additional methods to support code points, {@link CharSequence}, and {@link String}.
 */
public class GapString extends CharGapList implements IString {

	/**
	 * Wrapper to treat a {@link CharSequence} as {@link ICharListable}.
	 */
	protected static class ICharListableFromCharSequence implements ICharListable {

		CharSequence str;
		int off;
		int len;

		ICharListableFromCharSequence(CharSequence str) {
			this.str = str;
			this.off = 0;
			this.len = str.length();
		}

		ICharListableFromCharSequence(CharSequence str, int off, int len) {
			this.str = str;
			this.off = off;
			this.len = len;
		}

		@Override
		public int size() {
			return len;
		}

		@Override
		public char get(int index) {
			return str.charAt(off + index);
		}
	}

	/**
	 * Wrapper to treat a {@link CharSequence} as {@link ICharListable}.
	 */
	protected static class ICharListableFromCodePoints implements ICharListable {

		char[] data;

		ICharListableFromCodePoints(int[] codePoints) {
			data = CodePointTools.getCharArray(codePoints);
		}

		ICharListableFromCodePoints(int[] codePoints, int off, int len) {
			data = CodePointTools.getCharArray(codePoints, off, off + len);
		}

		@Override
		public int size() {
			return data.length;
		}

		@Override
		public char get(int index) {
			return data[index];
		}
	}

	// valueOf (static)

	public static GapString valueOf(boolean val) {
		char[] chars = FormatTools.formatBooleanAsChars(val);
		GapString gs = new GapString(true);
		gs.init(chars, chars.length);
		return gs;
	}

	public static GapString valueOf(char c) {
		char[] chars = FormatTools.formatCharAsChars(c);
		GapString gs = new GapString(true);
		gs.init(chars, chars.length);
		return gs;
	}

	public static GapString valueOf(int val) {
		char[] chars = FormatTools.formatIntAsChars(val);
		GapString gs = new GapString(true);
		gs.init(chars, chars.length);
		return gs;
	}

	public static GapString valueOf(long val) {
		char[] chars = FormatTools.formatLongAsChars(val);
		GapString gs = new GapString(true);
		gs.init(chars, chars.length);
		return gs;
	}

	public static GapString valueOf(char[] chars) {
		return new GapString(chars);
	}

	public static GapString valueOf(char[] chars, int offset, int len) {
		return new GapString(chars, offset, len);
	}

	public static GapString valueOf(float val) {
		String str = Float.toString(val); // TODO optimize
		return new GapString(str);
	}

	public static GapString valueOf(double val) {
		String str = Double.toString(val); // TODO optimize
		return new GapString(str);
	}

	public static GapString valueOf(Object obj) {
		String str = ObjectTools.toString(obj);
		return new GapString(str);
	}

	// copyValueOf (static)

	public static GapString copyValueOf(char[] chars) {
		return new GapString(chars);
	}

	public static GapString copyValueOf(char[] chars, int offset, int len) {
		return new GapString(chars, offset, len);
	}

	// Constructor

	public GapString() {
	}

	public GapString(int capacity) {
		super(capacity);
	}

	public GapString(String str) {
		this(false);
		init(str);
	}

	public GapString(CharSequence str) {
		// CharSequence supports StringBuilder / StringBuffer
		this(false);
		init(str);
	}

	public GapString(CharSequence str, int offset, int len) {
		this(false);
		init(str, offset, len);
	}

	/** Protected no-op constructor (no fields are initialized), boolean dummy arguments is only used for a unique signature */
	protected GapString(boolean dummy) {
		super(false, null);
	}

	// compare / compareToIgnoreCase

	@Override
	public int compareTo(IString str) {
		return CharSequenceTools.compare(this, str);
	}

	public int compareToIgnoreCase(IString str) {
		return CharSequenceTools.compare(this, str, CharMode.CI_CODEPOINT.getCodePointEqual());
	}

	// equalsIgnoreCase

	public boolean equalsIgnoreCase(CharSequence str) {
		return (this == str) ? true
				: (str != null) && (str.length() == length()) && regionMatches(true, 0, str, 0, length());
	}

	// regionMatches

	public boolean regionMatches(boolean ignoreCase, int toffset, CharSequence other, int ooffset, int len) {
		if (!ignoreCase) {
			return regionMatches(toffset, other, ooffset, len);
		}

		// Note: toffset, ooffset, or len might be near -1>>>1.
		if ((ooffset < 0) || (toffset < 0) ||
				(toffset > (long) length() - len) ||
				(ooffset > (long) other.length() - len)) {
			return false;// TODO move check into doEquals
		}
		return CharSequenceTools.doEquals(this, toffset, other, ooffset, len, CharMode.CI_CODEPOINT.getCodePointEqual());
	}

	public boolean regionMatches(int toffset, CharSequence other, int ooffset, int len) {
		// Note: toffset, ooffset, or len might be near -1>>>1.
		if ((ooffset < 0) || (toffset < 0) ||
				(toffset > (long) length() - len) ||
				(ooffset > (long) other.length() - len)) {
			return false; // TODO move check into doEquals
		}
		return CharSequenceTools.doEquals(this, toffset, other, ooffset, len);
	}

	// contentEquals

	public boolean contentEquals(StringBuffer buf) {
		return contentEquals((CharSequence) buf);
	}

	public boolean contentEquals(CharSequence str) {
		if (str instanceof StringBuffer) {
			synchronized (str) { // as in String.contentEquals
				return doContentEquals(str);
			}
		} else {
			return doContentEquals(str);
		}
	}

	boolean doContentEquals(CharSequence str) {
		int len = length();
		if (len != str.length()) {
			return false;
		}
		for (int i = 0; i < len; i++) {
			if (charAt(i) != str.charAt(i)) {
				return false;
			}
		}
		return true;
	}

	// Replace regex

	public GapString replaceAllApply(String regex, String replacement) {
		return initGapString(replaceAll(regex, replacement));
	}

	public String replaceAll(String regex, String replacement) {
		// Pattern.compile() and Matcher.replaceAll() accepts a String
		return Pattern.compile(regex).matcher(this).replaceAll(replacement);
	}

	public GapString replaceFirstApply(String regex, String replacement) {
		return initGapString(replaceFirst(regex, replacement));
	}

	public String replaceFirst(String regex, String replacement) {
		// Pattern.compile() and Matcher.replaceFirst() accepts a String
		return Pattern.compile(regex).matcher(this).replaceFirst(replacement);
	}

	// byte[]

	public GapString(byte[] bytes, int offset, int length, String charsetName) throws UnsupportedEncodingException {
		this(bytes, offset, length, CharsetTools.getCharset(charsetName));
	}

	public GapString(byte[] bytes, String charsetName) throws UnsupportedEncodingException {
		this(bytes, CharsetTools.getCharset(charsetName));
	}

	public GapString(byte[] bytes) {
		this(bytes, Charset.defaultCharset());
	}

	public GapString(byte[] bytes, int offset, int length) {
		this(bytes, offset, length, Charset.defaultCharset());
	}

	public GapString(byte[] bytes, Charset charset) {
		this(bytes, 0, bytes.length, charset);
	}

	public GapString(byte[] bytes, int offset, int length, Charset charset) {
		super();

		CharsetDecoder decoder = CharsetTools.getDecoder(charset, CodingErrorAction.REPLACE);
		CharBuffer cs = CharsetTools.decodeCharSequence(bytes, offset, length, decoder);
		init(cs);
	}

	// char[]

	public GapString(char[] chars) {
		// String(char[])
		char[] arr = chars.clone();
		init(arr, arr.length);
	}

	public GapString(char[] chars, int offset, int len) {
		// String(char[],int,int)
		char[] arr = Arrays.copyOfRange(chars, offset, len);
		init(arr, arr.length);
	}

	// int[]

	public GapString(int[] codePoints) {
		// No equivalent in String
		char[] arr = new ICharListableFromCodePoints(codePoints).data;
		init(arr, arr.length);
	}

	public GapString(int[] codePoints, int offset, int len) {
		// String(int[],int,int)
		char[] arr = new ICharListableFromCodePoints(codePoints, offset, len).data;
		init(arr, arr.length);
	}

	//

	public void init(String str) {
		char[] array = str.toCharArray();
		init(array, array.length);
	}

	public void init(CharSequence str) {
		int len = str.length();
		char[] array = new char[len];
		for (int i = 0; i < len; i++) {
			array[i] = str.charAt(i);
		}
		init(array, array.length);
	}

	public void init(CharSequence str, int offset, int len) {
		char[] array = new char[len];
		for (int i = 0; i < len; i++) {
			array[i] = str.charAt(offset + i);
		}
		init(array, array.length);
	}

	@Override
	public CharGapList doCreate(int capacity) {
		if (capacity == -1) {
			capacity = DEFAULT_CAPACITY;
		}
		return new GapString(capacity);
	}

	// Implementation of CharSequence

	@Override
	public int length() {
		return size();
	}

	@Override
	public char charAt(int index) {
		return get(index);
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This method does not modify this instance, but returns an independent {@link GapString}.
	 */
	@Override
	public GapString subSequence(int start, int end) {
		CharSequenceTools.checkStringRange(this, start, end);
		return new GapString(this, start, end - start);
	}

	@Override
	public GapString getAll(int index, int len) {
		return (GapString) super.getAll(index, len);
	}

	@Override
	public String toString() {
		if (isNormalizedArray()) {
			return new String(doGetArray());
		} else {
			return new String(toArray());
		}
	}

	// String.indexOf(String)

	public int indexOf(CharSequence str) {
		return CharSequenceTools.indexOf(this, str);
	}

	public int indexOf(CharSequence str, int start) {
		return CharSequenceTools.indexOf(this, str, start);
	}

	public int indexOf(CharSequence str, int start, int end) {
		CharSequenceTools.checkStringRange(this, start, end); // as done by String.indexOf
		return CharSequenceTools.indexOf(this, str, start, end);
	}

	// String.indexOf(int)

	public int indexOf(int cp) {
		return CharSequenceTools.indexOf(this, cp);
	}

	public int indexOf(int cp, int start) {
		return CharSequenceTools.indexOf(this, cp, start);
	}

	public int indexOf(int cp, int start, int end) {
		CharSequenceTools.checkStringRange(this, start, end); // as done by String.indexOf
		return CharSequenceTools.indexOf(this, cp, start, end);
	}

	// String.lastIndexOf

	public int lastIndexOf(CharSequence str) {
		return CharSequenceTools.lastIndexOf(this, str);
	}

	public int lastIndexOf(CharSequence str, int fromIndex) {
		return CharSequenceTools.lastIndexOf(this, str, fromIndex);
	}

	public int lastIndexOf(int cp) {
		return CharSequenceTools.lastIndexOf(this, cp);
	}

	public int lastIndexOf(CharSequence str, int cp, int fromIndex) {
		return CharSequenceTools.lastIndexOf(this, cp, fromIndex);
	}

	// String code point

	public int codePointAt(int pos) {
		return CodePointTools.codePointAt(this, pos);
	}

	public int codePointBefore(int pos) {
		return CodePointTools.codePointBefore(this, pos);
	}

	public int codePointCount(int start, int end) {
		return CodePointTools.codePointCount(this, start, end);
	}

	// char[]

	public char[] toCharArray() {
		return this.toArray();
	}

	// byte[]

	public byte[] getBytes() {
		return getBytes(Charset.defaultCharset());
	}

	public byte[] getBytes(String charsetName) throws UnsupportedEncodingException {
		return getBytes(CharsetTools.getCharset(charsetName));
	}

	public byte[] getBytes(Charset charset) {
		CharsetEncoder encoder = CharsetTools.getEncoder(charset, CodingErrorAction.REPLACE);
		return CharsetTools.encode(this, encoder);
	}

	// String.startsWith()

	public boolean startsWith(CharSequence str) {
		return CharSequenceTools.startsWith(this, str);
	}

	public boolean startsWith(CharSequence str, int pos) {
		return CharSequenceTools.startsAt(this, str, pos);
	}

	// String.endsWith()

	public boolean endsWith(CharSequence str) {
		return CharSequenceTools.endsWith(this, str);
	}

	// String.transform()

	public <R> R transform(Function<? super GapString, ? extends R> f) {
		// since Java 17
		return f.apply(this);
	}

	// String.toLowerCase()/toUpperCase()

	public String toLowerCase() {
		return toLowerCase(Locale.getDefault());
	}

	public String toLowerCase(Locale locale) {
		return toString().toLowerCase(locale);
	}

	public GapString toLowerCaseApply() {
		return toLowerCaseApply(Locale.getDefault());
	}

	public GapString toLowerCaseApply(Locale locale) {
		return initGapString(toLowerCase(locale));
	}

	public String toUpperCase() {
		return toUpperCase(Locale.getDefault());
	}

	public String toUpperCase(Locale locale) {
		return toString().toUpperCase(locale);
	}

	public GapString toUpperCaseApply() {
		return toUpperCaseApply(Locale.getDefault());
	}

	public GapString toUpperCaseApply(Locale locale) {
		return initGapString(toUpperCase(locale));
	}

	GapString initGapString(String str) {
		init(str);
		return this;
	}

	// String.indent / String.indent

	//public GapString indent() {
	// since Java 17 TODO
	//}

	//public GapString stripIndent() {
	// since Java 17 TODO
	//}

	// String repeat

	public GapString repeatApply(int count) {
		return initGapString(repeat(count));
	}

	public String repeat(int count) {
		return toString().repeat(count);
	}

	// String.format

	//public GapString formatted(Object... args) {
	// since Java 17
	//}

	// String.isBlank()

	public boolean isBlank() {
		return indexOfNonWhitespace(this) == length();
	}

	static int indexOfNonWhitespace(CharSequence value) {
		int length = value.length();
		int left = 0;
		while (left < length) {
			if (!isWhitespace(value.charAt(left))) {
				break;
			}
			left++;
		}
		return left;
	}

	static boolean isWhitespace(char c) {
		// as in String.indexOfNonWhitespace/lastIndexOfNonWhitespace TODO is this faster
		return c == ' ' || c == '\t' || Character.isWhitespace(c);
	}

	static int lastIndexOfNonWhitespace(CharSequence value) {
		int length = value.length();
		int right = length;
		while (right > 0) {
			if (!isWhitespace(value.charAt(right - 1))) {
				break;
			}
			right--;
		}
		return right;
	}

	public GapString trim() {
		int len = length();
		int left = 0;
		while (left < len && charAt(left) <= ' ') {
			left++;
		}

		if (left == len) {
			clear();
			return this;
		}
		int right = len;
		while (right > 0 && charAt(right - 1) <= ' ') {
			right--;
		}

		if (right < len) {
			remove(right, len - right);
		}
		if (left > 0) {
			remove(0, left);
		}
		return this;
	}

	public GapString strip() {
		int len = length();
		int left = indexOfNonWhitespace(this);
		if (left == len) {
			clear();
			return this;
		}
		int right = lastIndexOfNonWhitespace(this);
		if (right < len) {
			remove(right, len - right);
		}
		if (left > 0) {
			remove(0, left);
		}
		return this;
	}

	public GapString stripLeading() {
		int len = length();
		int left = indexOfNonWhitespace(this);
		if (left == len) {
			clear();
			return this;
		}
		if (left > 0) {
			remove(0, left);
		}
		return this;
	}

	public GapString stripTrailing() {
		int len = length();
		int right = lastIndexOfNonWhitespace(this);
		if (right == 0) {
			clear();
			return this;
		}
		if (right < len) {
			remove(right, len - right);
		}
		return this;
	}

	// String.lines()

	public Stream<String> lines() {
		return new LineIterator(this).setIncludeEol(false).stream();
	}

	// String.replace()

	public String replace(char findChar, char replaceChar) {
		return toString().replace(findChar, replaceChar);
	}

	public GapString replaceApply(char findChar, char replaceChar) {
		CharSequenceInlineTools.doReplaceInlineChar(this, 0, findChar, replaceChar);
		return this;
	}

	public String replace(CharSequence findStr, CharSequence replaceStr) {
		return toString().replace(findStr, replaceStr);
	}

	public GapString replaceApply(CharSequence findStr, CharSequence replaceStr) {
		CharSequenceInlineTools.doReplaceInlineString(this, 0, findStr, replaceStr);
		return this;
	}

	// String.concat()

	public String concat(CharSequence str) {
		StringBuilder buf = new StringBuilder();
		buf.append(this);
		buf.append(str);
		return buf.toString();
	}

	/**
	 * See {@link String#concat}.
	 * <p>
	 * This method modifies this instance and returns it.
	 */
	public GapString concatApply(CharSequence str) {
		addString(str);
		return this;
	}

	// String.split()

	String[] split(String regex) {
		return split(regex, 0);
	}

	String[] split(String regex, int limit) {
		return Pattern.compile(regex).split(this, limit);
	}

	//String[] splitWithDelimiters(String regex, int limit) {
	// Since Java 21: String.splitWithDelimiters() / Pattern.splitWithDelimiters()
	//return Pattern.compile(regex).splitWithDelimiters(regex, limit)ö 
	//}

	// String.subString()

	public String substring(int start) {
		return substring(start, length());
	}

	public String substring(int start, int end) {
		return toString().substring(start, end);
	}

	/**
	 * See {@link String#substring(int)}.
	 * <p>
	 * This method modifies this instance and returns it.
	 */
	public GapString substringApply(int start) {
		return substringApply(start, length());
	}

	/**
	 * See {@link String#substring(int, int)}.
	 * <p>
	 * This method modifies this instance and returns it.
	 */
	public GapString substringApply(int start, int end) {
		CharSequenceTools.checkStringRange(this, start, end);
		int len = length();
		if (start == 0 && end == len) {
			return this;
		}

		if (end == len) {
			remove(0, start);
		} else if (start == 0) {
			remove(end, len - end);
		} else {
			remove(end, len - end);
			remove(0, start);
		}
		return this;
	}

	// Non String

	@Override
	public int indexOfCodePoint(int codepoint) {
		return CharSequenceTools.indexOf(this, codepoint);
	}

	@Override
	public int getCodePoint(int index) {
		return CodePointTools.codePointAt(this, index);
	}

	@Override
	public void addCodePoint(int index, int codepoint) {
		if (CodePointTools.isCharCodePoint(codepoint)) {
			super.add(index, (char) codepoint);
		} else {
			super.add(index, Character.highSurrogate(codepoint));
			super.add(index + 1, Character.lowSurrogate(codepoint));
		}
	}

	@Override
	public void removeCodePoint(int index) {
		if (Character.isHighSurrogate(get(index))) {
			if (index + 1 < size()) {
				if (Character.isLowSurrogate(get(index + 1))) {
					super.remove(index, 2);
					return;
				}
			}
		}
		super.remove(index);
	}

	@Override
	public String getCodePointsAsString(int index, int len) {
		char[] chars = getCodePointsAsChars(index, len);
		return new String(chars);
	}

	@Override
	public char[] getCodePointsAsChars(int index, int len) {
		int end = getCodePointsEnd(index, len);
		int len2 = end - index;
		return getAsChars(index, len2);
	}

	/** Get the specified number of codepoints as string */
	@Override
	public String getAsString(int index, int len) {
		char[] chars = getAsChars(index, len);
		return new String(chars);
	}

	/** Get the specified number of codepoints as char array */
	@Override
	public char[] getAsChars(int index, int len) {
		char[] chars = new char[len];
		super.doGetAll(chars, index, len);
		return chars;
	}

	@Override
	public void removeCodePoints(int index, int len) {
		int end = getCodePointsEnd(index, len);
		super.remove(index, end - index);
	}

	int getCodePointsEnd(int index, int len) {
		int end = index;
		while (len > 0) {
			if (Character.isHighSurrogate(get(end))) {
				if (end + 1 < size()) {
					if (Character.isLowSurrogate(get(index + 1))) {
						end++;
					}
				}
			}
			end++;
		}
		return len;
	}

	@Override
	public boolean addString(CharSequence str) {
		return addString(length(), str);
	}

	@Override
	public boolean addString(int index, CharSequence str) {
		checkIndexAdd(index);
		return doAddAll(index, new ICharListableFromCharSequence(str));
	}

	@Override
	public boolean addString(CharSequence str, int off, int len) {
		return doAddAll(-1, new ICharListableFromCharSequence(str, off, len));
	}

	@Override
	public boolean addString(int index, CharSequence str, int off, int len) {
		checkIndexAdd(index);
		return doAddAll(index, new ICharListableFromCharSequence(str, off, len));
	}

	@Override
	public void putString(int index, CharSequence str) {
		doPutAll(index, new ICharListableFromCharSequence(str));
	}

	@Override
	public void putString(int index, CharSequence str, int off, int len) {
		doPutAll(index, new ICharListableFromCharSequence(str, off, len));
	}

	public void replace(int index, int len, CharSequence str) {
		replace(index, len, new ICharListableFromCharSequence(str));
	}

}
