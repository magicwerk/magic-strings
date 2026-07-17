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

import java.util.Map;

/**
 * Class {@link StringMapFormatter} builds formatted string using a format string and a string mapper.
 */
public class StringMapFormatter {

	Map<String, Object> argsMap;

	public StringMapFormatter(Map<String, Object> argsMap) {
		this.argsMap = argsMap;
	}

	public String format(String format) {
		return StringFormatter.formatMap(format, argsMap);
	}

}
