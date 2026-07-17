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
 * Class {@link MathTools} provides additional math tools.
 */
public class MathTools {

	/**
	 * Returns the sign of the input value, i.e. <br>
	 * - 1 if the value is greater than 0
	 * - 0 if the value is 0
	 * - -1 if the value is less than 0
	 *
	 * @param n		value
	 * @return		sign of value
	 */
	public static int signum(int n) {
		if (n > 0) {
			return 1;
		} else if (n < 0) {
			return -1;
		} else {
			return 0;
		}
	}

	/**
	 * Returns the sign of the input value, i.e. <br>
	 * - 1 if the value is greater than 0
	 * - 0 if the value is 0
	 * - -1 if the value is less than 0
	 *
	 * @param n		value
	 * @return		sign of value
	 */
	public static long signum(long n) {
		if (n > 0) {
			return 1;
		} else if (n < 0) {
			return -1;
		} else {
			return 0;
		}
	}

}
