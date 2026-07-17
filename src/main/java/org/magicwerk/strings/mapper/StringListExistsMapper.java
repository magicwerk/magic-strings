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

import java.util.List;

/**
 * Class {@link StringListExistsMapper} checks whether key is valid for a StringListMapper, 
 * i.e. it is checked to be a valid positive integer.
 */
public class StringListExistsMapper implements IStringExistsMapper {
	private int numStrings = -1;

	/**
	 * Constructor.
	 * Note that the key will only be checked to be a valid positive integer.
	 * and not whether it is within the valid range.
	 */
	public StringListExistsMapper() {
	}

	/**
	 * Constructor.
	 *
	 * @param strings		list of strings (only size is used)
	 */
	public StringListExistsMapper(List<Object> strings) {
		this.numStrings = strings.size();
	}

	/**
	 * Constructor.
	 *
	 * @param numStrings	number of strings
	 */
	public StringListExistsMapper(int numStrings) {
		this.numStrings = numStrings;
	}

	/**
	 * {@inheritDoc}
	 * Note that the key is only checked to be within the valid range
	 * if this information has been provided during construction.
	 * Otherwise the key is just checked to be a valid positive integer.
	 */
	@Override
	public boolean exists(Object key) {
		if (key == null) {
			return true;
		}

		Integer index;
		if (key instanceof Integer) {
			index = (Integer) key;
		} else {
			index = null;
		}

		if (index == null || index < 0) {
			return false;
		}
		if (numStrings != -1 && index >= numStrings) {
			return false;
		}
		return true;
	}
}
