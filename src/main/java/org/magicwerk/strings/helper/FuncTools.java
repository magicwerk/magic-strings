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

import java.util.Comparator;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Class {@link FuncTools} contains helper functions.
 */
public class FuncTools {

	/**
	 * The first object which can be casted to the specified type is returned, otherwise null.
	 */
	@SafeVarargs
	public static <T, U extends T> U getFirstCastIf(Class<U> type, T... objs) {
		for (T obj : objs) {
			U cast = castIf(obj, type);
			if (cast != null) {
				return cast;
			}
		}
		return null;
	}

	/**
	 * The first object where the applied filter evaluates to true is returned, otherwise null.
	 */
	@SafeVarargs
	public static <T> T getFirstIf(Predicate<T> filter, T... objs) {
		for (T obj : objs) {
			if (filter.test(obj)) {
				return obj;
			}
		}
		return null;
	}

	/**
	 * If the passed object is of specified type, it is casted and return, otherwise null is returned.
	 */
	public static <T, U extends T> U castIf(T obj, Class<U> type) {
		if (type.isInstance(obj)) {
			@SuppressWarnings("unchecked")
			U cast = (U) obj;
			return cast;
		} else {
			return null;
		}
	}

	/**
	 * If the passed options has a value which is of specified type, it is casted and return, otherwise null is returned.
	 */
	public static <T, U extends T> U getCastIf(Optional<T> obj, Class<U> type) {
		return castIf(obj.orElse(null), type);
	}

	//

	/**
	 * Convert {@link Consumer} to {@link Function} returning null.
	 */
	public static <T> Function<T, Void> toFunction(Consumer<T> consumer) {
		Function<T, Void> f = t -> {
			consumer.accept(t);
			return null;
		};
		return f;
	}

	/**
	 * Convert {@link BiConsumer} to {@link BiFunction} returning null.
	 */
	public static <T, U> BiFunction<T, U, Void> toBiFunction(BiConsumer<T, U> consumer) {
		BiFunction<T, U, Void> f = (t, u) -> {
			consumer.accept(t, u);
			return null;
		};
		return f;
	}

	//

	/**
	 * Implement mapping functionality: The conditions are checked in order. If the first condition matches, the associated value is returned.
	 * If no condition matches, the last default value is returned. If there is no default value, an exception is thrown.
	 *
	 * @param cond1		first condition
	 * @param val1		first value to return if condition is true
	 * @param condVals	pairs of condition (type boolean) and values (type V)
	 * @return			determined return value
	 */
	@SuppressWarnings("unchecked")
	public static <V> V map(boolean cond1, V val1, Object... condVals) {
		if (cond1) {
			return val1;
		}
		int numArgs = condVals.length / 2;
		for (int i = 0; i < numArgs; i++) {
			boolean cond = (Boolean) condVals[2 * i];
			if (cond) {
				return (V) condVals[2 * i + 1];
			}
		}
		if (condVals.length % 2 == 1) {
			return (V) condVals[condVals.length - 1];
		}
		throw CheckTools.error("No default value");
	}

	public enum MapMode {
		/** If no match for the input value is found, an error is raised */
		ERROR,
		/** If no match for the input value is found, the input value is returned */
		INPUT,
		/** If no match for the input value is found, null is returned */
		NULL,
		/** If no match for the input value is found, the provided default value is returned */
		DEFAULT
	}

	/**
	 * Implement mapping functionality: An input value is checked for equality with the first key. If they are equal, the first value is returned.
	 * Otherwise the next keys are checked until an equal one is found. Then the associated value is returned.
	 * If no key is found, behavior is determined by the passed mode.
	 * <p>
	 * To determine the type of the return value, the first value to check for equality and the first return value are typed explicitly. 
	 * To allow an arbitrary number of arguments, the following arguments are just declared as Object. It can therefore be that the method throws a
	 * ClassCastException if a value of wrong type is passed.
	 *
	 * @param input		object to check
	 * @param mode		see {@link MapMode}
	 * @param key1		first value to check for equality
	 * @param val1		first return value if check for equality is true
	 * @param keyVals	pairs of values for equality check and return
	 * @return			determined return value
	 */
	public static <K, V> V map(K input, MapMode mode, K key1, V val1, Object... keyVals) {
		return doMap(false, input, mode, key1, val1, keyVals);
	}

	public static <K, V> V map(K input, MapMode mode, Predicate<K> keyPred1, V val1, Object... keyPredVals) {
		return doMap(true, input, mode, keyPred1, val1, keyPredVals);
	}

	@SuppressWarnings("unchecked")
	static <K, V> V doMap(boolean predicate, K input, MapMode mode, Object key1, V val1, Object... keyVals) {
		CheckTools.checkNonNull(mode, "mode");
		boolean even = (keyVals.length % 2 == 0);
		if (mode == MapMode.DEFAULT) {
			CheckTools.check(!even, "Number of arguments must be odd for mode {}", mode);
		} else {
			CheckTools.check(even, "Number of arguments must be even for mode {}", mode);
		}

		if (matchKey(predicate, input, key1)) {
			return val1;
		}

		int numArgs = keyVals.length / 2;
		for (int i = 0; i < numArgs; i++) {
			if (matchKey(predicate, input, keyVals[2 * i])) {
				return (V) keyVals[2 * i + 1];
			}
		}
		if (mode == MapMode.ERROR) {
			throw CheckTools.error("No match found for value: {}", input);
		} else if (mode == MapMode.INPUT) {
			return (V) input;
		} else if (mode == MapMode.NULL) {
			return null;
		} else {
			return (V) keyVals[2 * numArgs];
		}
	}

	static boolean matchKey(boolean predicate, Object input, Object key) {
		if (predicate) {
			@SuppressWarnings("unchecked")
			Predicate<Object> pred = (Predicate<Object>) key;
			if (pred.test(input)) {
				return true;
			}
		} else {
			if (ObjectTools.equals(input, key)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Implement mapping functionality: An input value is checked for equality with the first key. If they are equal, the first value is returned.
	 * Otherwise the next keys are checked until an equal one is found. Then the associated value is returned.
	 * If no key is found, behavior is determined by the passed mode.
	 * <p>
	 * To determine the type of the return value, the first value to check for equality and the first return value are typed explicitly. 
	 * To allow an arbitrary number of arguments, the following arguments are just declared as Object. It can therefore be that the method throws a
	 * ClassCastException if a value of wrong type is passed.
	 *
	 * @param input		object to check
	 * @param mode		see {@link MapMode}
	 * @param keys		first value to check for equality
	 * @param vals		first return value if check for equality is true
	 * @return			determined return value
	 */
	public static <K, V> V map(K input, MapMode mode, K[] keys, V[] vals) {
		CheckTools.checkNonNull(mode, "mode");
		if (mode != MapMode.DEFAULT) {
			CheckTools.check(keys.length == vals.length, "Both array must contain the same number of values for mode {}", mode);
		} else {
			CheckTools.check(keys.length + 1 == vals.length, "Output array must contain an additional default value for mode {}", mode);
		}

		for (int i = 0; i < keys.length; i++) {
			if (ObjectTools.equals(input, keys[i])) {
				return vals[i];
			}
		}
		if (mode == MapMode.ERROR) {
			throw CheckTools.error("No match found for value: {}", input);
		} else if (mode == MapMode.INPUT) {
			@SuppressWarnings("unchecked")
			V val = (V) input;
			return val;
		} else if (mode == MapMode.NULL) {
			return null;
		} else {
			return vals[vals.length - 1];
		}
	}

	/**
	 * Return the element at the specified index in the array of arguments.
	 * It can be used as replacement for manually creating a temporary array, e.g.
	 * <pre>
	 * s = mapIndex(1, "a", "b", "c")
	 * s = new String[] { "a", "b", "c" }[1]
	 * </pre>
	 */
	@SafeVarargs
	public static <V> V mapIndex(int index, V... vals) {
		return vals[index];
	}

	/**
	 * Returns val if it is not null, else nvl.
	 *
	 * @param val  value to check
	 * @param nvl  value to return if value to check is null
	 * @return     val if it is not null, else nvl
	 */
	public static <T> T nvl(T val, T nvl) {
		return (val != null) ? val : nvl;
	}

	public static <T> T nvl(T val, Supplier<T> nvl) {
		return (val != null) ? val : nvl.get();
	}

	/**
	 * Returns the first argument which is not null.
	 * If all arguments are null, null is returned.
	 *
	 * @param vals values to check
	 * @return     first argument which is not null (if all arguments are null, null is returned)
	 */
	@SuppressWarnings("unchecked")
	public static <T> T nvl(T... vals) {
		for (T val : vals) {
			if (val != null) {
				return val;
			}
		}
		return null;
	}

	/**
	 * Returns the object with maximum value.
	 * The values must implement Comparable.
	 * Note that null values are ignored.
	 *
	 * @param values   objects to compare
	 * @return         object with maximum value
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Comparable<T>> T max(T... values) {
		T max = null;
		for (T val : values) {
			if (val != null) {
				if (max == null || val.compareTo(max) > 0) {
					max = val;
				}
			}
		}
		return max;
	}

	/**
	 * Returns the object with minimum value.
	 * The values must implement Comparable.
	 * Note that null values are ignored.
	 *
	 * @param values  objects to compare
	 * @return        object with minimum value
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Comparable<T>> T min(T... values) {
		T min = null;
		for (T val : values) {
			if (val != null) {
				if (min == null || val.compareTo(min) < 0) {
					min = val;
				}
			}
		}
		return min;
	}

	@SuppressWarnings("unchecked")
	public static <T> T max(Comparator<T> comparator, T... values) {
		T max = null;
		for (T val : values) {
			if (val != null) {
				if (max == null || comparator.compare(val, max) > 0) {
					max = val;
				}
			}
		}
		return max;
	}

	@SuppressWarnings("unchecked")
	public static <T> T min(Comparator<T> comparator, T... values) {
		T min = null;
		for (T val : values) {
			if (val != null) {
				if (min == null || comparator.compare(val, min) < 0) {
					min = val;
				}
			}
		}
		return min;
	}

	/** Returns result of applying the specified function, null if the function raises an execption */
	public static <T1, T2> T2 getIf(T1 obj, Function<T1, T2> f0) {
		try {
			return get(obj, f0);
		} catch (Exception e) {
			return null;
		}
	}

	public static <T1, T2> T2 getIf(T1 obj, Function<T1, T2> f0, T2 val) {
		try {
			T2 r = get(obj, f0);
			return (r != null) ? r : val;
		} catch (Exception e) {
			return val;
		}
	}

	public static <T1, T2, T3> T3 getIf(T1 obj, Function<T1, T2> f0, Function<T2, T3> f1) {
		try {
			return get(obj, f0, f1);
		} catch (Exception e) {
			return null;
		}
	}

	public static <T1, T2, T3, T4> T4 getIf(T1 obj, Function<T1, T2> f0, Function<T2, T3> f1, Function<T3, T4> f2) {
		try {
			return get(obj, f0, f1, f2);
		} catch (Exception e) {
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	public static <T1, T2> T2 get(T1 obj, Function<T1, T2> f0) {
		Object o = obj;
		if (o != null) {
			o = f0.apply((T1) o);
		}
		return (T2) o;
	}

	@SuppressWarnings("unchecked")
	public static <T1, T2, T3> T3 get(T1 obj, Function<T1, T2> f0, Function<T2, T3> f1) {
		Object o = obj;
		if (o != null) {
			o = f0.apply((T1) o);
		}
		if (o != null) {
			o = f1.apply((T2) o);
		}
		return (T3) o;
	}

	@SuppressWarnings("unchecked")
	public static <T1, T2, T3, T4> T4 get(T1 obj, Function<T1, T2> f0, Function<T2, T3> f1, Function<T3, T4> f2) {
		Object o = obj;
		if (o != null) {
			o = f0.apply((T1) o);
		}
		if (o != null) {
			o = f1.apply((T2) o);
		}
		if (o != null) {
			o = f2.apply((T3) o);
		}
		return (T4) o;
	}

	@SuppressWarnings("unchecked")
	public static <T1> void call(T1 obj, Consumer<T1> m0) {
		Object o = obj;
		if (o != null) {
			m0.accept((T1) o);
		}
	}

	@SuppressWarnings("unchecked")
	public static <T1, T2> void call(T1 obj, Function<T1, T2> f0, Consumer<T2> m1) {
		Object o = obj;
		if (o != null) {
			o = f0.apply((T1) o);
		}
		if (o != null) {
			m1.accept((T2) o);
		}
	}

}
