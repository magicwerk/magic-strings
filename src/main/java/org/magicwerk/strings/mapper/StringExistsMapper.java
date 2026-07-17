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

/**
 * Checks whether a key is valid.
 */
public class StringExistsMapper implements IStringExistsMapper {
	private String[] keys;

	public StringExistsMapper(String[] keys) {
		this.keys = keys;
	}

	/**
	 * Checks whether key is valid.
	 *
	 * @param key	key to check
	 * @return		true if key exists, false otherwise
	 */
	@Override
	public boolean exists(Object key) {
		for (String k : keys) {
			if (k.equals(key)) {
				return true;
			}
		}
		return false;
	}
}
