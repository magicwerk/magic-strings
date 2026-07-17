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
package org.magicwerk.strings.format;

import java.util.List;
import java.util.Map;

import org.magicwerk.strings.helper.CheckTools;
import org.magicwerk.strings.helper.PrintTools;
import org.magicwerk.strings.mapper.IStringExistsMapper;
import org.magicwerk.strings.mapper.IStringMapper;
import org.magicwerk.strings.mapper.StringMapper;

/**
 * Class {@link StringFormatBase} build a formatted string using a format string and a string mapper.
 */
public abstract class StringFormatBase {

	static abstract class IndexedStringMapper implements IStringMapper {
		int currIndex;

		abstract Object doGet(int index);

		@Override
		public String getString(Object key) {
			if (key != null) {
				currIndex = (Integer) key;
			}
			String str = PrintTools.toString(doGet(currIndex));
			currIndex++;
			return str;
		}
	}

	/**
	 * Class {@link StringListMapper} provides string values through access by position or by use.
	 * As it is stateful, for each run a new instance must be created.
	 */
	static class StringListMapper extends IndexedStringMapper {
		List<Object> strings;

		/**
		 * Constructor.
		 *
		 * @param strings	string available for mapper
		 */
		@SuppressWarnings("unchecked")
		public StringListMapper(List<?> strings) {
			this.strings = (List<Object>) strings;
		}

		@Override
		Object doGet(int index) {
			return strings.get(index);
		}
	}

	/**
	 * Class {@link StringArrayMapper} provides string values through access by position or by use.
	 * As it is stateful, for each run a new instance must be created.
	 */
	static class StringArrayMapper extends IndexedStringMapper {
		Object[] strings;

		/**
		 * Constructor.
		 *
		 * @param strings	string available for mapper
		 */
		public StringArrayMapper(Object... strings) {
			this.strings = strings;
		}

		@Override
		Object doGet(int index) {
			return strings[index];
		}
	}

	/** The format string used */
	String format;
	/** Mapper to check for existence of key (or null if not used) */
	IStringExistsMapper existsMapper;
	boolean throwOnMissingArguments;
	boolean throwOnExtraArguments;

	//

	/**
	 * Add constant string to format.
	 */
	public abstract void addConst(String str);

	/**
	 * Add parameter string to format (type integer or string)
	 */
	public abstract void addParam(Object key);

	/**
	 * @return the format string
	 */
	public String getFormat() {
		return format;
	}

	/**
	 * Format string.
	 *
	 * @param strings	format arguments
	 * @return			formatted string
	 */
	public String format(Object... strings) {
		StringArrayMapper mapper = new StringArrayMapper(strings);
		String str = doFormat(mapper);
		if (throwOnExtraArguments) {
			CheckTools.check(mapper.currIndex <= strings.length, "Too many arguments");
		}
		return str;
	}

	/**
	 * Format string.
	 *
	 * @param strings	format arguments
	 * @return			formatted string
	 */
	public String formatList(List<?> strings) {
		StringListMapper mapper = new StringListMapper(strings);
		String str = doFormat(mapper);
		if (throwOnExtraArguments) {
			CheckTools.check(mapper.currIndex <= strings.size(), "Too many arguments");
		}
		return str;
	}

	public String formatMap(Map<String, ?> strings) {
		IStringMapper mapper = new StringMapper(strings);
		return doFormat(mapper);
	}

	public String formatMapper(IStringMapper mapper) {
		return doFormat(mapper);
	}

	/**
	 * Format string.
	 *
	 * @param mapper	mapper to use
	 * @return			formatted string
	 */
	protected abstract String doFormat(IStringMapper mapper);

}
