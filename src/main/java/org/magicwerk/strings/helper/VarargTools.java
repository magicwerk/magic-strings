/*
 * Copyright 2012 by Thomas Mauch
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

import java.util.function.BiConsumer;

import org.magicwerk.collections.GapList;
import org.magicwerk.collections.IList;

/**
 * Class {@link VarargTools} contains functionality to work with varargs.
 */
public class VarargTools {

	/** Check that the variable arguments provided in keyValues have the same class as key / value (or are null) */
	public static <K, V> void checkVarargTuples(K key, V value, Object... keyValues) {
		processVarargTuples((k, v) -> {
			if (!(k == null || key == null || key.getClass().isAssignableFrom(k.getClass()))) {
				CheckTools.error("Key must be of type " + key.getClass() + " or null: " + k);
			}
			if (!(v == null || value == null || value.getClass().isAssignableFrom(v.getClass()))) {
				CheckTools.error("Value must be of type " + value.getClass() + " or null: " + v);
			}
		}, key, value, keyValues);
	}

	/** Call the provided consumer for the parameters key and value and for each key/value pair in keyValues */
	public static <K, V> void processVarargTuples(BiConsumer<K, V> consumer, K key, V value, Object... keyValues) {
		CheckTools.check(keyValues.length % 2 == 0, "Number of arguments must be even");
		consumer.accept(key, value);

		for (int i = 0; i < keyValues.length / 2; i++) {
			@SuppressWarnings("unchecked")
			K k = (K) keyValues[2 * i];
			@SuppressWarnings("unchecked")
			V v = (V) keyValues[2 * i + 1];

			consumer.accept(k, v);
		}
	}

	public static <K> IList<K> getVarargTupleKeys(K key, Object... keyValues) {
		return doGetVarargTupleArgs(0, key, keyValues);
	}

	public static <V> IList<V> getVarargTupleValues(V value, Object... keyValues) {
		return doGetVarargTupleArgs(1, value, keyValues);
	}

	static <T> IList<T> doGetVarargTupleArgs(int offset, T arg, Object... keyValues) {
		int len = keyValues.length / 2 + 1;
		IList<T> args = new GapList<T>(len);
		args.add(arg);
		for (int i = 0; i < len; i++) {
			@SuppressWarnings("unchecked")
			T k = (T) keyValues[2 * i + offset];
			args.add(k);
		}
		return args;
	}

}
