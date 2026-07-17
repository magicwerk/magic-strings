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

import java.util.List;
import java.util.Map;

import org.magicwerk.strings.format.StringFormatBase.StringListMapper;
import org.magicwerk.strings.helper.CollectionTools;
import org.magicwerk.strings.mapper.IStringExistsMapper;
import org.magicwerk.strings.mapper.IStringMapper;
import org.magicwerk.strings.mapper.StringMapExistsMapper;
import org.magicwerk.strings.mapper.StringMapper;
import org.magicwerk.strings.text.TextTools;

/**
 * Class {@link StringFormatter} builds formatted string using a format string and a string mapper.
 */
public class StringFormatter {

	/**
	 * Print formatted string into string builder.
	 */
	public static void print(StringBuilder buf, String format, Object... args) {
		buf.append(format(format, args));
	}

	public static void printIndent(StringBuilder buf, String indent, String str) {
		str = TextTools.indentLines(str, indent);
		buf.append(str);
	}

	public static void printIndent(StringBuilder buf, String indent, String format, Object... args) {
		String str = format(format, args);
		str = TextTools.indentLines(str, indent);
		buf.append(str);
	}

	/**
	 * Format given string.
	 * The arguments contained in the format string are replaced by string
	 * provided by the StringProvider.
	 *
	 * @param format	format string
	 * @param mapper	string mapper
	 * @return			formatted string
	 */
	public static String formatMapper(String format, IStringMapper mapper) {
		StringFormatterImpl formatter = new StringFormatterImpl(format);
		return formatter.doFormat(mapper);
	}

	/**
	 * Format given string with the key/value pairs specified in the map.
	 */
	public static String formatMap(String format, Map<String, ?> strings) {
		StringFormatterImpl formatter = new StringFormatterImpl(format);
		StringMapper mapper = new StringMapper(strings);
		return formatter.doFormat(mapper);
	}

	public static String formatMap(boolean check, String format, Map<String, ?> strings) {
		StringFormatterImpl formatter = new StringFormatterImpl(format);
		StringMapper mapper = new StringMapper(strings).setFailIfNotFound(check);
		return formatter.doFormat(mapper);
	}

	/**
	 * Format given string with the specified key/value pairs.
	 */
	public static String formatMap(String format, String key, Object value, Object... keyValues) {
		Map<String, Object> map = CollectionTools.createHashMap(key, value, keyValues);
		return formatMap(format, map);
	}

	public static String formatMap(boolean check, String format, String key, Object value, Object... keyValues) {
		Map<String, Object> map = CollectionTools.createHashMap(key, value, keyValues);
		return formatMap(check, format, map);
	}

	/**
	 * Format given string.
	 *
	 * @param format	format string
	 * @param strings	format arguments
	 * @return			formatted string
	 */
	public static String formatList(String format, List<?> strings) {
		StringFormatterImpl formatter = new StringFormatterImpl(format);
		IStringMapper mapper = new StringListMapper(strings);
		return formatter.doFormat(mapper);
	}

	/**
	 * Format specified string.
	 *
	 * @param format	format string
	 * @param objs		arguments
	 * @return			formatted string
	 */
	public static String format(String format, Object... objs) {
		StringFormat formatter = new StringFormat(format);
		return formatter.format(objs);
	}

	/**
	 * Check format of specified string.
	 *
	 * @param format		format string
	 * @param mapStrings	list of string with argument names
	 * @throws 				RuntimeException if the format string is invalid
	 */
	public static void checkFormat(String format, List<String> mapStrings) {
		IStringExistsMapper existsMapper = new StringMapExistsMapper(mapStrings);
		@SuppressWarnings("unused")
		StringFormat formatter = new StringFormat(format, existsMapper);
	}

}
