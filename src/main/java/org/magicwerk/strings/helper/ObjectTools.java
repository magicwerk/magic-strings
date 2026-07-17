/*
 * Copyright 2015 by Thomas Mauch
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

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.magicwerk.brownies.collections.helper.Option;

/**
 * Class {@link ObjectTools} contains tools for working with Objects.
 */
public class ObjectTools {

	/**
	 * Casts object to specified class (null is accepted).
	 * If cast is not possible, a ClassCastException is thrown.
	 */
	public static <T> T cast(Object obj, Class<T> clazz) {
		return clazz.cast(obj);
	}

	/**
	 * Casts object to specified class (null is accepted).
	 * If cast is not possible, null is returned.
	 */
	public static <T> T castOr(Object obj, Class<T> clazz) {
		return castOr(obj, clazz, null);
	}

	/**
	 * Casts object to specified class (null is accepted).
	 * If cast is not possible, the specified default value is returned.
	 */
	public static <T> T castOr(Object obj, Class<T> clazz, T defObj) {
		if (obj != null && clazz.isInstance(obj)) {
			return clazz.cast(obj);
		} else {
			return defObj;
		}
	}

	/**
	 * If obj is of type clazz (not null), apply the accessor and return the result.
	 * Otherwise thrown an exception.
	 */
	public static <T, V> V get(Object obj, Class<T> clazz, Function<T, V> accessor) {
		if (clazz.isInstance(obj)) {
			T obj2 = clazz.cast(obj);
			return accessor.apply(obj2);
		}
		throw CheckTools.error("Invalid object {}", obj);
	}

	/**
	 * If obj is of type clazz (not null), apply the accessor and return the result.
	 * Otherwise return null.
	 */
	public static <T, V> V getOr(Object obj, Class<T> clazz, Function<T, V> accessor) {
		return getOr(obj, clazz, accessor, null);
	}

	/**
	 * If obj is of type clazz (not null), apply the accessor and return the result.
	 * Otherwise return the specified default value.
	 */
	public static <T, V> V getOr(Object obj, Class<T> clazz, Function<T, V> accessor, V defObj) {
		if (clazz.isInstance(obj)) {
			T obj2 = clazz.cast(obj);
			return accessor.apply(obj2);
		}
		return defObj;
	}

	/**
	 * Returns the index of the specified search string in the list of strings.
	 * If null is passed as search string and null is also contained in
	 * the list of strings, the test is also successful.
	 *
	 * @param search	search string
	 * @param values	strings
	 * @return			index of search string in the list of strings
	 */
	@SafeVarargs
	public static <T> int indexOneOf(T search, T... values) {
		for (int i = 0; i < values.length; i++) {
			if (ObjectTools.equals(search, values[i])) {
				return i;
			}
		}
		return -1;
	}

	public static <T> int indexOneOf(T search, List<T> values) {
		for (int i = 0; i < values.size(); i++) {
			if (ObjectTools.equals(search, values.get(i))) {
				return i;
			}
		}
		return -1;
	}

	@SafeVarargs
	public static <T> int indexOneOf(T[] search, T... values) {
		for (int i = 0; i < values.length; i++) {
			for (T s : search) {
				if (ObjectTools.equals(s, values[i])) {
					return i;
				}
			}
		}
		return -1;
	}

	@SafeVarargs
	public static <T> boolean isOneOf(T[] search, T... values) {
		return indexOneOf(search, values) != -1;
	}

	/**
	 * Returns true if the specified search string is contained in the
	 * list of strings.
	 * If null is passed as search string and null is also contained in
	 * the list of strings, the test is also successful.
	 *
	 * @param search	search string
	 * @param values	strings
	 * @return			true if search string is contained in the list of strings
	 */
	@SafeVarargs
	public static <T> boolean isOneOf(T search, T... values) {
		return indexOneOf(search, values) != -1;
	}

	public static <T> boolean isOneOf(T search, List<T> values) {
		return indexOneOf(search, values) != -1;
	}

	/**
	 * If obj is null, the specified class may not be a primitive.
	 */
	public static boolean isInstanceOrNull(Object obj, Class<?> clazz) {
		if (obj == null) {
			return !clazz.isPrimitive();
		} else {
			return clazz.isAssignableFrom(obj.getClass());
		}
	}

	/**
	 * Compares two objects for equality. <br>
	 * - if object do not have the same type, they are not equal <br>
	 * - two null pointers are considered equal. <br>
	 * - array objects are considered equal if the corresponding java.util.Arrays.equals() method returns true
	 *   (see the discussion there about the difference between equals and == for types float and double)
	 * - multi-dimensional arrays are considered equal if all their dimensions and elements are equals
	 * - BigDecimals are considered equal if compareTo return 0 (because equals also checks for the same scale)
	 * - all other objects are considered equal if calling equals()
	 *   on them returns true (which e.g. compares Collections value by value)
	 *
	 * @param o1 first object to compare for equality
	 * @param o2 second object to compare for equality
	 * @return a boolean indicating whether the objects are equal
	 */
	public static boolean equals(Object o1, Object o2) {
		if (o1 == null || o2 == null) {
			return o1 == o2;
		}
		if (o1.getClass().isArray() && o2.getClass().isArray()) {
			return equalsArray(o1, o2);
		} else if (o1 instanceof BigDecimal && o2 instanceof BigDecimal) {
			BigDecimal bd1 = (BigDecimal) o1;
			BigDecimal bd2 = (BigDecimal) o2;
			return bd1.compareTo(bd2) == 0;
		} else {
			return o1.equals(o2);
		}
	}

	static boolean equalsArray(Object o1, Object o2) {
		Class<?> c = o1.getClass();
		assert (c.isArray() && c == o2.getClass());

		Class<?> cc = c.getComponentType();
		if (cc.isPrimitive()) {
			if (cc == int.class) {
				return Arrays.equals((int[]) o1, (int[]) o2);
			} else if (cc == double.class) {
				return Arrays.equals((double[]) o1, (double[]) o2);
			} else if (cc == boolean.class) {
				return Arrays.equals((boolean[]) o1, (boolean[]) o2);
			} else if (cc == byte.class) {
				return Arrays.equals((byte[]) o1, (byte[]) o2);
			} else if (cc == long.class) {
				return Arrays.equals((long[]) o1, (long[]) o2);
			} else if (cc == float.class) {
				return Arrays.equals((float[]) o1, (float[]) o2);
			} else if (cc == char.class) {
				return Arrays.equals((char[]) o1, (char[]) o2);
			} else if (cc == short.class) {
				return Arrays.equals((short[]) o1, (short[]) o2);
			} else {
				throw new AssertionError();
			}
		} else {
			return Arrays.deepEquals((Object[]) o1, (Object[]) o2);
		}
	}

	static int hashCodeArray(Object o1) {
		Class<?> c = o1.getClass();
		assert (c.isArray());

		Class<?> cc = c.getComponentType();
		if (cc.isPrimitive()) {
			if (cc == int.class) {
				return Arrays.hashCode((int[]) o1);
			} else if (cc == long.class) {
				return Arrays.hashCode((long[]) o1);
			} else if (cc == double.class) {
				return Arrays.hashCode((double[]) o1);
			} else if (cc == boolean.class) {
				return Arrays.hashCode((boolean[]) o1);
			} else if (cc == byte.class) {
				return Arrays.hashCode((byte[]) o1);
			} else if (cc == float.class) {
				return Arrays.hashCode((float[]) o1);
			} else if (cc == char.class) {
				return Arrays.hashCode((char[]) o1);
			} else if (cc == short.class) {
				return Arrays.hashCode((short[]) o1);
			} else {
				throw new AssertionError();
			}
		} else {
			return Arrays.hashCode((Object[]) o1);
		}
	}

	/**
	 * Helper method for use in the implementation of the hashCode() method.
	 * A typical implementation will look like this:
	 *
	 * <pre>
	 * public int hashCode() {
	 *    return ObjectTools.hashCode(field1, field2);
	 * }
	 * </pre>
	 *
	 * @param fields	values of object to calculate hash code
	 * @return			calculated hash code
	 */
	public static int hashCode(Object... fields) {
		int hashCode = 1;
		for (Object field : fields) {
			hashCode = 31 * hashCode + hashCode(field);
		}
		return hashCode;
	}

	/**
	 * Returns hash code of object, 0 for null.
	 * 
	 * @param obj	object
	 * @return		hash code of object 
	 */
	public static int hashCode(Object obj) {
		if (obj == null) {
			return 0;
		}
		if (obj.getClass().isArray()) {
			return hashCodeArray(obj);
		} else {
			return obj.hashCode();
		}
	}

	/*
	 * Implementing singletons in Java SE is not as easy as it seems:
	 * The trivial approach to provide only a private constructor and a public
	 * static instance variable fails with serialization where suddenly
	 * several objects of this type can exist. If the construction of the objects
	 * is cheap, you can live with several objects and use the methods below
	 * to treat them like a single instance.
	 */

	public static boolean equalsSingleton(Object o1, Object o2) {
		if (o1 == null || o2 == null) {
			return o1 == o2;
		}
		return o1.getClass().getName().equals(o2.getClass().getName());
	}

	/**
	 * The hash code of a singleton is the hash code of the singleton's class name.
	 */
	public static int hashCodeSingleton(Object obj) {
		return obj.getClass().getName().hashCode();
	}

	/**
	 * @param objs  objects to compare
	 * @return		true if all objects are equals
	 */
	public static boolean allEquals(Object... objs) {
		for (int i = 1; i < objs.length; i++) {
			if (!equals(objs[i - 1], objs[i])) {
				return false;
			}
		}
		return true;
	}

	public static <T> boolean isOptionEqual(Option<T> obj0, Option<T> obj1, BiFunction<T, T, Boolean> equalFnc) {
		if (obj0 == null || obj1 == null) {
			return org.magicwerk.strings.helper.ObjectTools.equals(obj0, obj1);
		}

		if (obj0.hasValue() != obj1.hasValue()) {
			return false;
		} else {
			return equalFnc.apply(obj0.getValue(), obj1.getValue());
		}
	}

	/**
	 * @param obj	object to compare
	 * @param objs	objects to compare against
	 * @return		true if obj1 is equal to one of objs
	 */
	public static boolean equalsOneOf(Object obj, Object... objs) {
		if (objs == null) {
			return false;
		}
		for (Object o : objs) {
			if (equals(obj, o)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Helper method for use in the implementation of the equals() method.
	 * A typical implementation will look like this:
	 *
	 * <pre>
	 * public boolean equals(Object obj) {
	 *    if (obj instanceof MyClass) {
	 *       MyClass that = (MyClass) obj;
	 *       return ObjectTools.equalsObjects(this, that, this.field1, that.field1, this.field2, that.field2);
	 *    } else {
	 *       return false;
	 *    }
	 * }
	 * </pre>
	 *
	 * @param obj1		first object to compare
	 * @param obj2		second object to compare
	 * @param fields	array of field values to compare
	 * @return			true if the object are equal, false otherwise
	 */
	public static boolean equalsObjects(Object obj1, Object obj2, Object... fields) {
		if (obj1 == obj2) {
			return true;
		}
		for (int i = 0; i < fields.length; i += 2) {
			if (!equals(fields[i], fields[i + 1])) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Helper method for use in the implementation of the compareTo() method.
	 * A typical implementation will look like this:
	 *
	 * <pre>
	 * public boolean compareTo(MyObj that) {
	 *    return ObjectTools.compareFields(this.field1, that.field1, this.field2, that.field2);
	 * }
	 * </pre>
	 *
	 * @param fields	values of fields to compare
	 * @return			true if values are equal, false otherwise
	 */
	@SuppressWarnings("rawtypes")
	public static int compareFields(Object... fields) {
		CheckTools.check(fields.length % 2 == 0, "Number of arguments must be even");

		for (int i = 0; i < fields.length; i += 2) {
			Comparable c0 = (Comparable) fields[i];
			Comparable c1 = (Comparable) fields[i + 1];

			@SuppressWarnings("unchecked")
			int cmp = compare(c0, c1);
			if (cmp != 0) {
				return cmp;
			}
		}
		return 0;
	}

	/**
	 * Compares the objects by just analyzing whether they are null or not.
	 * So if two objects are both non-null, 0 is returned and probably a
	 * further comparison must be done.
	 *
	 * @param o1	first object
	 * @param o2	second object
	 * @return		0, -1, or 1
	 */
	public static <T> int compareNull(T o1, T o2) {
		if (o1 == null) {
			if (o2 == null) {
				return 0;
			} else {
				return 1;
			}
		} else {
			if (o2 == null) {
				return -1;
			} else {
				return 0;
			}
		}
	}

	/**
	 * Compares two objects including null values (nulls first).
	 */
	public static <T extends Comparable<? super T>> int compare(T o1, T o2) {
		if (o1 == null) {
			if (o2 == null) {
				return 0;
			} else {
				return 1;
			}
		} else {
			if (o2 == null) {
				return -1;
			} else {
				return o1.compareTo(o2);
			}
		}
	}

	public static <T> int compareByIdentity(T o1, T o2) {
		int id1 = System.identityHashCode(o1);
		int id2 = System.identityHashCode(o2);
		return Integer.compare(id1, id2);
	}

	public static boolean equalsFields(Object... fields) {
		for (int i = 0; i < fields.length; i += 2) {
			if (!equals(fields[i], fields[i + 1])) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Returns value returned by the toString() method of the specified
	 * object or the string "null" if the object is null.
	 *
	 * @param obj	object to print
	 * @return		string representation of object
	 */
	public static String toString(Object obj) {
		return PrintTools.toString(obj);
	}

	/**
	 * Returns value returned by the toString() method of the specified
	 * object or the specified null string if the object is null.
	 *
	 * @param obj			object to print
	 * @param nullString	string to return if object is null
	 * @return				string representation of object
	 */
	public static String toString(Object obj, String nullString) {
		return PrintTools.toString(obj, nullString);
	}

	/**
	 * This function returns the same output as toString() would if would not have been overwritten. 
	 * <p>
	 * The toString method for class Object returns a string consisting of the name of the class of which the object is an instance, 
	 * the at-sign character `@', and the unsigned hexadecimal representation of the hash code of the object. 
	 * In other words, this method returns a string equal to the value of: 
	 * <pre> getClass().getName() + '@' + Integer.toHexString(hashCode()) </pre>
	 *
	 * @see <a href="http://java.sun.com/javase/6/docs/api/java/lang/Object.html#toString()">Object.toString()</a>
	 *
	 * @param obj 	object to generate identity hash code
	 * @return 		generated identity hash code (null for null input)
	 */
	public static String toObjectString(Object obj) {
		if (obj == null) {
			return null;
		}
		String id = obj.getClass().getName() + "@" + Integer.toHexString(System.identityHashCode(obj));
		return id;
	}

	/**
	 * This function returns the unsigned hexadecimal representation of the identity hash code of the object. In other words, this method returns a string equal
	 * to the value of: <pre> Integer.toHexString(System.identityHashCode(obj) </pre>
	 *
	 * @see <a href="http://java.sun.com/javase/6/docs/api/java/lang/Object.html#toString()">Object.toString()</a>
	 *
	 * @param obj 	object to generate identity hash code
	 * @return 		generated identity hash code (null for null input)
	 */
	public static String getIdentityHashCode(Object obj) {
		if (obj == null) {
			return null;
		}
		String id = Integer.toHexString(System.identityHashCode(obj));
		return id;
	}

	/**
	 * @return class of input object, null if object is null
	 */
	public static Class<?> getClass(Object obj) {
		return (obj != null) ? obj.getClass() : null;
	}

	public static Class<?>[] getClasses(Object[] objs) {
		if (objs == null) {
			return null;
		}
		Class<?>[] classes = new Class<?>[objs.length];
		for (int i = 0; i < objs.length; i++) {
			classes[i] = getClass(objs[i]);
		}
		return classes;
	}

}
