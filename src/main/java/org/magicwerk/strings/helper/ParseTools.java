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

import org.magicwerk.strings.format.StringFormatter;

/**
 * Class {@link ParseTools} contains tools for parsing, formatting and checking types.
 */
public class ParseTools {

	// Digit

	public static boolean isDigit(char c) {
		return '0' <= c && c <= '9';
	}

	public static int getDigitValue(char c) {
		if ('0' <= c && c <= '9') {
			return c - '0';
		} else {
			return -1;
		}
	}

	// Int

	/**
	 * Determines whether given String denotes an integer.
	 *
	 * @param str String to check
	 * @return true if string denotes an integer, false otherwise
	 */
	public static Integer parseIntIf(String str) {
		if (str != null && str.length() > 0) {
			int len = str.length();
			int i = 0;
			int sign = 1;
			if (i < len) {
				if (str.charAt(i) == '-') {
					sign = -1;
					i++;
				} else if (str.charAt(i) == '+') {
					i++;
				}
			}

			int val = 0;
			if (i < len) {
				int newDigit = getDigitValue(str.charAt(i));
				if (newDigit != -1) {
					val = sign * newDigit;
					i++;
					while (i < len) {
						newDigit = getDigitValue(str.charAt(i));
						if (newDigit == -1) {
							break;
						}
						int newVal = 10 * val + sign * newDigit;
						if (val != 0 && MathTools.signum(newVal) != MathTools.signum(val)) {
							// Overflow -> break to throw exception
							break;
						}
						val = newVal;
						i++;
					}
				}
			}
			if (i == len) {
				return val;
			}
		}
		return null;
	}

	/**
	 * Parse integer from string.
	 * The string must match the following regex "[+-]\\d+".
	 * Note that therefore not leading or trailing spaces are allowed.
	 *
	 * @param str  string to parse
	 * @return     parsed integer
	 * @throws ParseException if the string is not a valid integer
	 */
	public static int parseInt(String str) {
		Integer val = parseIntIf(str);
		if (val != null) {
			return val;
		}
		throw getErrorInvalidValue(str, int.class);
	}

	/**
	 * Determines whether given String denotes an integer.
	 *
	 * @param str String to check
	 * @return true if string denotes an integer, false otherwise
	 */
	public static boolean isInt(String str) {
		Integer val = parseIntIf(str);
		return (val != null);
	}

	/**
	 * Determines whether given String denotes an integer.
	 *
	 * @param str String to check
	 * @return true if string denotes an integer, false otherwise
	 */
	public static int parseInt(String str, int defVal) {
		Integer val = parseIntIf(str);
		return (val != null) ? val : defVal;
	}

	/**
	 * Determines whether given String denotes an integer.
	 *
	 * @param str String to check
	 * @return true if string denotes an integer, false otherwise
	 */
	public static int parseInt(String str, int defVal, boolean preferDefVal) {
		Integer val = parseIntIf(str);
		if (val != null) {
			return val;
		} else if (preferDefVal || str == null) {
			return defVal;
		} else {
			throw getErrorInvalidValue(str, int.class);
		}
	}

	public static ParseException getErrorInvalidValue(String value, Class<?> clazz) {
		return getErrorInvalidValue(value, clazz.getSimpleName());
	}

	public static ParseException getErrorInvalidValue(String value, String type) {
		return new ParseException(StringFormatter.format("Invalid value for {}: {}", type, value));
	}

}
