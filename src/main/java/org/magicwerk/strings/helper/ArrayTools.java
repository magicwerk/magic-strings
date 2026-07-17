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

import java.lang.reflect.Array;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Function;

import org.magicwerk.brownies.collections.GapList;
import org.magicwerk.brownies.collections.IList;

/**
 * Class {@link ArrayTools} contains helper tools for working with arrays.
 */
public class ArrayTools {

	/**
	 * Class {@link ArrayIterator} implements a {@link Iterator} for an array.
	 */
	public static class ArrayIterator<E> implements Iterator<E> {

		E[] array;
		int index;

		public ArrayIterator(E[] array) {
			this.array = array;
		}

		@Override
		public boolean hasNext() {
			return index < array.length;
		}

		@Override
		public E next() {
			if (index >= array.length) {
				throw new NoSuchElementException();
			}
			return array[index++];
		}
	}

	/**
	 * Convert array into an array with the same content but different type.
	 * If a conversion is not possible, an {@link ArrayStoreException} is thrown.
	 * Example: You can always convert an Integer[] into an Object[], but the reverse conversion is only possible if the object array contains 
	 * only integer (or null) values. 
	 */
	public static <T, R> R[] convert(T[] array, Class<R> newClass) {
		if (array == null) {
			return null;
		}
		R[] newArray = create(newClass, array.length);
		System.arraycopy(array, 0, newArray, 0, array.length);
		return newArray;
	}

	/**
	 * Convert array into an array with the same content but different type.
	 * If a conversion is not possible, null is returned.
	 * Example: You can always convert an Integer[] into an Object[], but the reverse conversion is only possible if the object array contains 
	 * only integer (or null) values.
	 */
	public static <T, R> R[] convertIf(T[] array, Class<R> newClass) {
		try {
			return convert(array, newClass);
		} catch (ArrayStoreException e) {
			return null;
		}
	}

	/**
	 * Convert array into an array with different using a function to convert the elements. 
	 */
	public static <T, R> R[] convert(T[] array, Class<R> newClass, Function<T, R> converter) {
		R[] newArray = create(newClass, array.length);
		for (int i = 0; i < array.length; i++) {
			newArray[i] = converter.apply(array[i]);
		}
		return newArray;
	}

	/**
	 * Convert an array represented as object into a list. 
	 * The array can contain primitives or objects.
	 * This method can be used if the array is represented by an object, i.e. its type is not known. 
	 * In this case, Arrays.asList() does not work properly.
	 *
	 * @param array	an array as object
	 * @return	created list
	 */
	public static <T> IList<T> toList(Object array) {
		return toList(array, false);
	}

	@SafeVarargs
	public static <T> List<T> toList(T... array) {
		return toList(array, false);
	}

	@SuppressWarnings("unchecked")
	public static <T> IList<T> toList(Object array, boolean recursive) {
		checkArray(array);

		int size = getLength(array);
		IList<T> list = new GapList<>(size);
		for (int i = 0; i < size; i++) {
			Object obj = get(array, i);
			if (recursive && isArray(obj)) {
				obj = toList(obj, recursive);
			}
			list.add((T) obj);
		}
		return list;
	}

	static void checkArray(Object array) {
		CheckTools.check(isArray(array), "Not an array: {}", array);
	}

	public static <T> T[] create(Class<T> type, int length) {
		@SuppressWarnings("unchecked")
		T[] array = (T[]) Array.newInstance(type, length);
		return array;
	}

	public static Object create(Class<?> type, int... lengths) {
		return Array.newInstance(type, lengths);
	}

	public static Object get(Object array, int index) {
		return Array.get(array, index);
	}

	public static void set(Object array, int index, Object obj) {
		Array.set(array, index, obj);
	}

	public static Object getIf(Object array, int index) {
		if (array == null) {
			return null;
		}
		return get(array, index);
	}

	public static <T> T getIf(T[] array, int index) {
		if (array == null) {
			return null;
		}
		return array[index];
	}

	public static <T> boolean isArrayClass(Class<T> clazz) {
		return clazz.isArray();
	}

	public static boolean isArray(Object array) {
		return (array != null) ? array.getClass().isArray() : false;
	}

	public static Class<?> getComponentType(Object array) {
		if (array == null) {
			return null;
		} else {
			return getComponentType(array.getClass());
		}
	}

	/** Returns component type of array, null if this not an array class */
	public static Class<?> getComponentType(Class<?> arrayClass) {
		return arrayClass.getComponentType();
	}

	public static int getNumDimensions(Object array) {
		if (array == null) {
			return -1;
		} else {
			return getNumDimensions(array.getClass());
		}
	}

	public static int getNumDimensions(Class<?> arrayClass) {
		int num = 0;
		while (arrayClass.isArray()) {
			num++;
			arrayClass = arrayClass.getComponentType();
		}
		return num;
	}

	public static Class<?> getBaseComponentType(Object array) {
		if (array == null) {
			return null;
		} else {
			return getBaseComponentType(array.getClass());
		}
	}

	public static Class<?> getBaseComponentType(Class<?> arrayClass) {
		while (arrayClass.isArray()) {
			arrayClass = arrayClass.getComponentType();
		}
		return arrayClass;
	}

	public static int getLength(Object array) {
		return Array.getLength(array);
	}

	public static void reverse(Object[] array) {
		reverse(array, 0, array.length);
	}

	/**
	 * Reverses the order of the specified elements in the list.
	 *
	 * @param index	index of first element to reverse
	 * @param len	number of elements to reverse
	 */
	public static void reverse(Object[] array, int index, int len) {
		int pos1 = index;
		int pos2 = index + len - 1;
		int mid = len / 2;
		for (int i = 0; i < mid; i++) {
			Object swap = array[pos1];
			array[pos1] = array[pos2];
			array[pos2] = swap;
			pos1++;
			pos2--;
		}
	}

}
