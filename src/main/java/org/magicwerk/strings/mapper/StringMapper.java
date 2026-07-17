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
package org.magicwerk.strings.mapper;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.magicwerk.strings.helper.CheckTools;

/**
 * Class {@link StringMapper} implements {@link IStringMapper} using a map.
 */
public class StringMapper implements IStringMapper {

	boolean failIfNotFound = false;
	HashMap<String, Object> map = new HashMap<String, Object>();

	/**
	 * Constructor.
	 *
	 * @param strings 	a string array which contains both key and value string
	 * 					(keys are at even, values at odd positions)
	 */
	public StringMapper(Object... strings) {
		this(Arrays.asList(strings));
	}

	/**
	 * Constructor.
	 *
	 * @param strings 	list of strings which contains both key and value string
	 * 					(keys are at even, values at odd positions)
	 */
	public StringMapper(List<Object> strings) {
		if (strings.size() % 2 != 0) {
			throw new IllegalArgumentException("The number of arguments must be odd (key/values pairs)");
		}
		for (int i = 0; i < strings.size() / 2; i++) {
			map.put((String) strings.get(2 * i), strings.get(2 * i + 1));
		}
	}

	/**
	 * Constructor.
	 *
	 * @param map		a map containing keys and values
	 */
	// Note that the map must be defined as Map<String,?>.
	// If Map<String,Object> would be used, Map<String,String> would not match.
	public StringMapper(Map<String, ?> map) {
		this.map.putAll(map);
	}

	/** Setter for {@link #failIfNotFound} */
	public StringMapper setFailIfNotFound(boolean failIfNotFound) {
		this.failIfNotFound = failIfNotFound;
		return this;
	}

	@Override
	public String getString(Object key) {
		Object obj = map.get(key);
		if (obj == null) {
			if (failIfNotFound) {
				CheckTools.check(map.containsKey(key), "Key {} not found", key);
			}
			return null;
		}
		return obj.toString();
	}
}
