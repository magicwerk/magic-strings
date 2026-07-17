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
 * Interface {@link IStringExistsMapper} can be used together with a {@link IStringMapper}.
 * Whereas the IStringMapper returns the value for a key, the IStringExistsMapper just checks whether the specified key is valid.
 * This can be used to check the format string in advance for correctness.
 */
public interface IStringExistsMapper {
	/**
	 * Checks whether specified key is valid.
	 * 
	 * @param key	key to look up
	 * @return		true if key is valid, otherwise false
	 */
	public boolean exists(Object key);
}
