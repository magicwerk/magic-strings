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

/**
 * Class {@link StringMarkers} supports working with markers {@link String}s.
 * A marker is defined as a character or string which does not occur in the string and can therefore be safely added and removed without
 * changing the original content of the string.
 * <p>
 * Example: for string "abc012" the character '-' would be valid marker
 */
public class StringMarkers {

	// Marker char / strings

	public static boolean isMarkerChar(String str, char c) {
		return str.indexOf(c) == -1;
	}

	public static char getMarkerChar(String str) {
		for (char c = 0; true; c++) {
			if (isMarkerChar(str, c)) {
				return c;
			}
			if (c == Character.MAX_VALUE) {
				throw new IllegalArgumentException("No marker char found");
			}
		}
	}

	public static String getMarkerString(String str) {
		return String.valueOf(getMarkerChar(str));
	}

	/**
	 * Checks whether the marker occurs in the string.
	 * If it occurs, an exception is thrown, otherwise the marker string is returned unchanged.
	 *
	 * @param str		string
	 * @param marker	marker string
	 * @return			marker string if does not occur in string
	 * @throws			IllegalArgumentException if marker string occurs in string
	 */
	public static boolean isMarkerString(String str, String marker) {
		return str.indexOf(marker) == -1;
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
	public static String getMarkerString(String str, String marker, String suffix) {
		while (true) {
			if (str.indexOf(marker) == -1) {
				return marker;
			}
			marker = marker + suffix;
		}
	}

	public static String getMarkerString(String str, String marker, int suffix) {
		while (true) {
			if (str.indexOf(marker) == -1) {
				return marker;
			}
			marker = marker + suffix;
		}
	}

}
