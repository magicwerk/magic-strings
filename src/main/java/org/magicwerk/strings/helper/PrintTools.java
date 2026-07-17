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

import java.util.function.Supplier;

/**
 * Class {@link PrintTools} allows printing the content of objects.
 * Features: <br>
 * - indentation <br>
 * - guessing of parameterized collection and map types <br>
 */
public class PrintTools {

	/**
	 * Class {@link ToStringSupplier} creates a temporary wrapper return the supplied string if {@link #toString} is called.
	 */
	public static class ToStringSupplier {
		public static ToStringSupplier of(Supplier<String> s) {
			return new ToStringSupplier() {
				@Override
				public String toString() {
					return s.get();
				}
			};
		}
	}

	public static final String NULL_STRING = "null";

	public static String toString(String str) {
		return toString(str, NULL_STRING);
	}

	public static String toString(String str, String nullString) {
		return (str != null) ? str : nullString;
	}

	/**
	 * Returns value returned by the toString() method of the specified object or the string "null" if the object is null.
	 * It supports printing the content of arrays and reports thrown exception during execution of toString()
	 * (returned string will have format {@literal "<ERROR: toString() of type class java.util.ArrayList throws StackOverflowError>"}).
	 *
	 * @param obj	object to print
	 * @return		string representation of object
	 */
	public static String toString(Object obj) {
		return toString(obj, NULL_STRING);
	}

	/**
	 * Returns value returned by the toString() method of the specified object or the specified null string if the object is null.
	 * It supports printing the content of arrays and reports thrown exception during execution of toString() 
	 * (returned string will have format {@literal "<ERROR: toString() of type class java.util.ArrayList throws StackOverflowError>"}).
	 *
	 * @param obj	object to print
	 * @return		string representation of object
	 */
	public static String toString(Object obj, String nullString) {
		try {
			if (obj == null) {
				return nullString;
			} else if (ArrayTools.isArray(obj)) {
				return toStringArray(obj, nullString);
			} else {
				return obj.toString();
			}
		} catch (Throwable t) {
			return "<ERROR: toString() of type " + obj.getClass() + " throws " + t.getClass().getSimpleName() + ">";
		}
	}

	static String toStringArray(Object obj, String nullString) {
		int size = ArrayTools.getLength(obj);
		if (size == 0) {
			return "[]";
		}

		StringBuilder buf = new StringBuilder();
		buf.append("[");
		for (int i = 0; i < size; i++) {
			Object val = ArrayTools.get(obj, i);
			if (i > 0) {
				buf.append(", ");
			}
			buf.append(toString(val, nullString));
		}
		buf.append("]");
		return buf.toString();
	}

}
