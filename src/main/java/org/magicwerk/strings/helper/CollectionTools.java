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

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;

/**
 * Class {@link CollectionTools} contains helper tools for working with collections.
 */
public class CollectionTools {

	public enum OrderMode {
		ORDERD_ASC,
		ORDERD_DESC,
		ALL_EQUAL,
		UNORDERED
	}

	public static <T> boolean isOrdered(Collection<T> coll, Comparator<T> comp) {
		OrderMode mode = getOrderMode(coll, comp); // TODO imporve performance
		return mode == OrderMode.ORDERD_ASC || mode == OrderMode.ALL_EQUAL;
	}

	public static <T> OrderMode getOrderMode(Collection<T> coll, Comparator<T> comp) {
		int index = -1;
		int lastCmp = 0;
		T prev = null;
		for (T elem : coll) {
			if (index >= 0) {
				int cmp = comp.compare(prev, elem);
				if (index == 0) {
					lastCmp = cmp;
				} else {
					if (cmp != 0) {
						if ((cmp > 0 && lastCmp < 0) || (cmp < 0 && lastCmp > 0)) {
							return OrderMode.UNORDERED;
						}
						lastCmp = cmp;
					}
				}
			}
			prev = elem;
			index++;
		}
		if (lastCmp < 0) {
			return OrderMode.ORDERD_ASC;
		} else if (lastCmp > 0) {
			return OrderMode.ORDERD_DESC;
		} else {
			return OrderMode.ALL_EQUAL;
		}
	}

	/**
	 * Create a {@link HashMap} and fill it with the provided key/value pairs.
	 * The first key/value pair defines the expected class types to which the following data must adhere.
	 *
	 * @param key		first key
	 * @param value		first value
	 * @param keyValues	more key/value pairs
	 * @return			created HashMap
	 */
	public static <K, V> HashMap<K, V> createHashMap(K key, V value, Object... keyValues) {
		HashMap<K, V> map = new HashMap<K, V>();
		doCreateMap(map, key, value, true, keyValues);
		return map;
	}

	/**
	 * Create a {@link LinkedHashMap} and fill it with the provided key/value pairs.
	 * The first key/value pair defines the expected class types to which the following data must adhere.
	 *
	 * @param key		first key
	 * @param value		first value
	 * @param keyValues	more key/value pairs
	 * @return			created HashMap
	 */
	public static <K, V> LinkedHashMap<K, V> createLinkedHashMap(K key, V value, Object... keyValues) {
		LinkedHashMap<K, V> map = new LinkedHashMap<K, V>();
		doCreateMap(map, key, value, true, keyValues);
		return map;
	}

	@SuppressWarnings("unchecked")
	static <K, V> void doCreateMap(Map<K, V> map, K key, V value, boolean checkTypes, Object... keyValues) {
		if (checkTypes) {
			VarargTools.checkVarargTuples(key, value, keyValues);
		}
		map.put(key, value);
		for (int i = 0; i < keyValues.length / 2; i++) {
			K k = (K) keyValues[2 * i];
			V v = (V) keyValues[2 * i + 1];
			map.put(k, v);
		}
	}

	/**
	 * Create a {@link TreeMap} and fill it with the provided key/value pairs.
	 * The first key/value pair defines the expected class types to which the following data must adhere.
	 *
	 * @param key		first key
	 * @param value		first value
	 * @param keyValues	more key/value pairs
	 * @return			created HashMap
	 */
	public static <K, V> Map<K, V> createTreeMap(K key, V value, Object... keyValues) {
		TreeMap<K, V> map = new TreeMap<K, V>();
		doCreateMap(map, key, value, true, keyValues);
		return map;
	}

	public static <M extends Map<?, ?>> Map<?, ?> createMap(Supplier<M> mapCreator, Object key, Object value, Object... keyValues) {
		@SuppressWarnings("unchecked")
		Map<Object, Object> map = (Map<Object, Object>) mapCreator.get();
		doCreateMap(map, key, value, false, keyValues);
		return map;
	}

	/** Returns the size of the collection, 0 if coll is null */
	public static int size(Collection<?> coll) {
		return (coll != null) ? coll.size() : 0;
	}

	/**
	 * Returns first entry of null if there is none.
	 */
	public static <T> T getFirst(Iterable<T> coll) {
		Iterator<T> iter = coll.iterator();
		return (iter.hasNext()) ? iter.next() : null;
	}

	/**
	 * Return the maximum value if the passed function is applied to all elements of the collection.
	 * If the collection is empty, 0 is returned. 
	 */
	public static <E> int maxInt(Collection<E> coll, ToIntFunction<E> func) {
		int max = 0;
		boolean first = true;
		for (E elem : coll) {
			int val = func.applyAsInt(elem);
			if (first) {
				max = val;
				first = false;
			} else {
				if (val > max) {
					max = val;
				}
			}
		}
		return max;
	}

	/**
	 * Return the minimum value if the passed function is applied to all elements of the collection.
	 * If the collection is empty, 0 is returned. 
	 */
	public static <E> int minInt(Collection<E> coll, ToIntFunction<E> fnc) {
		int min = 0;
		boolean first = true;
		for (E elem : coll) {
			int val = fnc.applyAsInt(elem);
			if (first) {
				min = val;
				first = false;
			} else {
				if (val < min) {
					min = val;
				}
			}
		}
		return min;
	}

}
