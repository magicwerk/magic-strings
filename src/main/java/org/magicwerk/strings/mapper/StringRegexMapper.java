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
 * $Id: StringListMapper.java 1147 2012-07-11 15:51:03Z origo $
 */
package org.magicwerk.strings.mapper;

import java.util.regex.Matcher;

import org.magicwerk.strings.helper.ParseTools;

/**
 * A StringRegexMapper extracts matched part of a regular expression.
 */
public class StringRegexMapper implements IStringMapper {
	private Matcher matcher;
	private int currIndex;

	public StringRegexMapper(Matcher matcher) {
		this.matcher = matcher;
	}

	@Override
	public String getString(Object key) {
		if (key != null) {
			if (key instanceof Integer) {
				currIndex = (Integer) key;
			} else {
				currIndex = ParseTools.parseInt((String) key);
			}
		}
		String str = matcher.group(currIndex);
		currIndex++;
		return str;
	}
}
