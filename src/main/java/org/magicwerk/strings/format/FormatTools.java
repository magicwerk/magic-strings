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
 * $Id: StringFormatter.java 1687 2013-06-21 22:26:23Z origo $
 */
package org.magicwerk.strings.format;

/**
 * Class {@link FormatTools} builds formatted string using a format string and a string mapper.
 */
public class FormatTools {

	public static final String STRING_true = "true";
	public static final String STRING_false = "false";

	public static char[] formatCharAsChars(char c) {
		return new char[] { c };
	}

	public static char[] formatBooleanAsChars(boolean val) {
		String str = (val) ? "true" : "false";
		return str.toCharArray();
	}

	public static char[] formatIntAsChars(int val) {
		int len = getIntStringSize(val);
		char[] chars = new char[len];
		writeInt(val, chars, 0, len);
		return chars;
	}

	public static char[] formatLongAsChars(long val) {
		int len = getLongStringSize(val);
		char[] chars = new char[len];
		writeLong(val, chars, 0, len);
		return chars;
	}

	public static int writeInt(int val, char[] chars, int offset, int len) {
		boolean negative = (val < 0);
		int num = len;
		if (!negative) {
			val = -val;
		} else {
			num--;
		}

		int pos = offset + len - 1;
		for (int i = 0; i < num; i++) {
			int r = val % 10;
			val = val / 10;
			chars[pos--] = (char) ('0' - r);
		}
		if (negative) {
			chars[pos] = '-';
		}

		return offset + len;
	}

	public static int writeLong(long val, char[] chars, int offset, int len) {
		boolean negative = (val < 0);
		int num = len;
		if (!negative) {
			val = -val;
		} else {
			num--;
		}

		int pos = offset + len - 1;
		for (int i = 0; i < num; i++) {
			int r = (int) (val % 10);
			val = val / 10;
			chars[pos--] = (char) ('0' - r);
		}
		if (negative) {
			chars[pos] = '-';
		}

		return offset + len;
	}

	public static int getIntStringSize(int x) {
		// Code as in Integer.stringSize()
		int d = 1;
		if (x >= 0) {
			d = 0;
			x = -x;
		}
		int p = -10;
		for (int i = 1; i < 10; i++) {
			if (x > p)
				return i + d;
			p = 10 * p;
		}
		return 10 + d;
	}

	static int getLongStringSize(long x) {
		// Code as in Long.stringSize()
		int d = 1;
		if (x >= 0) {
			d = 0;
			x = -x;
		}
		long p = -10;
		for (int i = 1; i < 19; i++) {
			if (x > p)
				return i + d;
			p = 10 * p;
		}
		return 19 + d;
	}

}
